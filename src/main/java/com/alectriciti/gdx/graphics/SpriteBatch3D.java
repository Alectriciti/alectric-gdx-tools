package com.alectriciti.gdx.graphics;

/*******************************************************************************
 * Adapted from com.badlogic.gdx.graphics.g2d.SpriteBatch (Copyright 2011 See AUTHORS file)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

// Package guessed to mirror your com.alectriciti.gdx namespace / libGDX's own g3d split.
// Move it wherever it actually belongs in your project - just update this line.

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Affine2;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Disposable;

import java.nio.Buffer;

/** A z-aware analog of {@link com.badlogic.gdx.graphics.g2d.SpriteBatch}. It batches textured quads exactly like SpriteBatch
 * does, but every vertex carries a real z coordinate, and rendering uses actual depth testing/writing instead of relying on
 * draw order. That's the whole point of this class: submit sprites in <i>any</i> order and let the GPU sort them by z, instead
 * of maintaining a manually z-sorted draw list.
 * <p>
 * Usage looks like this:
 *
 * <pre>
 * camera.update();
 * spriteBatch3D.begin(camera);
 * spriteBatch3D.draw(texture, x, y, z, width, height);
 * spriteBatch3D.end();
 * </pre>
 *
 * <h3>Why plain SpriteBatch can't just be given a z</h3>
 * If you tried bolting a z onto SpriteBatch's vertex data and turning on GL_DEPTH_TEST, you likely ran into this: SpriteBatch's
 * fragment shader writes every fragment it touches, including fully transparent ones. With depth <i>writing</i> also enabled,
 * the transparent padding around a sprite in its texture region still writes to the depth buffer as an opaque rectangle - so a
 * sprite's bounding box occludes whatever is behind it even where the sprite itself is invisible. That's almost certainly the
 * "can't get the same results" you ran into.
 * <p>
 * The fix here is a fragment shader that {@code discard}s any fragment at or below an alpha threshold (default 0, i.e. fully
 * transparent texels only) before it can write color <i>or</i> depth. See {@link #setAlphaTestThreshold(float)}.
 * <p>
 * <b>What this does and doesn't solve:</b> discard-before-depth-write gives you correct, GPU-sorted occlusion for opaque and
 * alpha-cutout sprites (pixel art with hard transparent/opaque edges - the common case), submitted in any order, no manual
 * sorting required. It does <i>not</i> give you correct blending between two overlapping <i>semi</i>-transparent pixels (e.g. a
 * soft drop shadow overlapping a translucent glow) - that's an inherent limitation of combining alpha blending with a depth
 * buffer (order-independent transparency is a much bigger topic), and would need per-frame back-to-front sorting same as
 * regular alpha blending always has.
 * <p>
 * <b>Camera setup:</b> higher z is closer to the camera (drawn on top) when using a standard LibGDX camera looking down -z.
 * Make sure your camera's near/far planes actually bound the z range you draw at, or sprites will be clipped.
 * @see com.badlogic.gdx.graphics.g2d.SpriteBatch */
public class SpriteBatch3D implements Disposable, Batch {
	/** Number of floats per vertex: x, y, z, packedColor, u, v. */
	static public final int VERTEX_SIZE = 6;
	/** Number of floats per sprite (4 vertices). */
	static public final int SPRITE_SIZE = 4 * VERTEX_SIZE;

	// Per-vertex float offsets, handy if you ever want to poke at raw vertex data directly.
	static public final int X1 = 0, Y1 = 1, Z1 = 2, C1 = 3, U1 = 4, V1 = 5;
	static public final int X2 = 6, Y2 = 7, Z2 = 8, C2 = 9, U2 = 10, V2 = 11;
	static public final int X3 = 12, Y3 = 13, Z3 = 14, C3 = 15, U3 = 16, V3 = 17;
	static public final int X4 = 18, Y4 = 19, Z4 = 20, C4 = 21, U4 = 22, V4 = 23;

	private final VertexDataType currentDataType;

	private final Mesh mesh;

	final float[] vertices;
	int idx = 0;
	Texture lastTexture = null;
	float invTexWidth = 0, invTexHeight = 0;

	boolean drawing = false;

	private final Matrix4 transformMatrix = new Matrix4();
	private final Matrix4 projectionMatrix = new Matrix4();
	private final Matrix4 combinedMatrix = new Matrix4();

	private boolean blendingDisabled = false;
	private int blendSrcFunc = GL20.GL_SRC_ALPHA;
	private int blendDstFunc = GL20.GL_ONE_MINUS_SRC_ALPHA;
	private int blendSrcFuncAlpha = GL20.GL_SRC_ALPHA;
	private int blendDstFuncAlpha = GL20.GL_ONE_MINUS_SRC_ALPHA;

	private final ShaderProgram shader;
	private ShaderProgram customShader = null;
	private boolean ownsShader;

	private final Color color = new Color(1, 1, 1, 1);
	float colorPacked = Color.WHITE_FLOAT_BITS;

	/** Fragment alpha at or below this value is discarded before it can write color or depth. Default 0 (fully transparent
	 * texels only). See the class javadoc for why this matters. */
	private float alphaTestThreshold = 0f;

	/** Number of render calls since the last {@link #begin()}. **/
	public int renderCalls = 0;

	/** Number of rendering calls, ever. Will not be reset unless set manually. **/
	public int totalRenderCalls = 0;

	/** The maximum number of sprites rendered in one batch so far. **/
	public int maxSpritesInBatch = 0;

	/** Constructs a new SpriteBatch3D with a size of 1000 and the default shader.
	 * @see SpriteBatch3D#SpriteBatch3D(int, ShaderProgram) */
	public SpriteBatch3D () {
		this(1000, null);
	}
	
	public float y_to_z_scale_multiplier = 1.0f;

	/** Constructs a SpriteBatch3D with the default shader.
	 * @see SpriteBatch3D#SpriteBatch3D(int, ShaderProgram) */
	public SpriteBatch3D (int size) {
		this(size, null);
	}

	/** Constructs a new SpriteBatch3D.
	 * @param size The max number of sprites in a single batch. Max of 8191.
	 * @param defaultShader The default shader to use. This is not owned by the SpriteBatch3D and must be disposed separately. */
	public SpriteBatch3D (int size, ShaderProgram defaultShader) {
		// 32767 is max vertex index (short), so 32767 / 4 vertices per sprite = 8191 sprites max. Same limit as SpriteBatch.
		if (size > 8191) throw new IllegalArgumentException("Can't have more than 8191 sprites per batch: " + size);

		VertexDataType vertexDataType = (Gdx.gl30 != null) ? VertexDataType.VertexBufferObjectWithVAO
			: VertexDataType.VertexBufferObject;
		currentDataType = vertexDataType;

		mesh = new Mesh(currentDataType, false, size * 4, size * 6,
			new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE),
			new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE),
			new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

		vertices = new float[size * SPRITE_SIZE];

		int len = size * 6;
		short[] indices = new short[len];
		short j = 0;
		for (int i = 0; i < len; i += 6, j += 4) {
			indices[i] = j;
			indices[i + 1] = (short)(j + 1);
			indices[i + 2] = (short)(j + 2);
			indices[i + 3] = (short)(j + 2);
			indices[i + 4] = (short)(j + 3);
			indices[i + 5] = j;
		}
		mesh.setIndices(indices);

		if (defaultShader == null) {
			shader = createDefaultShader();
			ownsShader = true;
		} else {
			shader = defaultShader;
		}

		// Pre bind the mesh to force the upload of indices data.
		if (vertexDataType != VertexDataType.VertexArray) {
			mesh.getIndexData().bind();
			mesh.getIndexData().unbind();
		}
	}

	/** Returns a new instance of the default shader used by SpriteBatch3D when no shader is specified. Same attribute/uniform
	 * naming as {@link com.badlogic.gdx.graphics.g2d.SpriteBatch#createDefaultShader()} plus a z-aware position attribute and a
	 * {@code u_alphaTest} uniform (see {@link #setAlphaTestThreshold(float)}). */
	static public ShaderProgram createDefaultShader () {
		String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "attribute vec2 " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "uniform mat4 u_projTrans;\n" //
			+ "varying vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
			+ "   v_color.a = v_color.a * (255.0/254.0);\n" //
			+ "   v_texCoords = " + ShaderProgram.TEXCOORD_ATTRIBUTE + "0;\n" //
			+ "   gl_Position =  u_projTrans * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
			+ "}\n";
		String fragmentShader = "#ifdef GL_ES\n" //
			+ "#define LOWP lowp\n" //
			+ "precision mediump float;\n" //
			+ "#else\n" //
			+ "#define LOWP \n" //
			+ "#endif\n" //
			+ "varying LOWP vec4 v_color;\n" //
			+ "varying vec2 v_texCoords;\n" //
			+ "uniform sampler2D u_texture;\n" //
			+ "uniform float u_alphaTest;\n" //
			+ "void main()\n" //
			+ "{\n" //
			+ "  vec4 texColor = texture2D(u_texture, v_texCoords);\n" //
			+ "  vec4 finalColor = v_color * texColor;\n" //
			+ "  if (finalColor.a <= u_alphaTest) discard;\n" //
			+ "  gl_FragColor = finalColor;\n" //
			+ "}";

		ShaderProgram shader = new ShaderProgram(vertexShader, fragmentShader);
		if (!shader.isCompiled()) throw new IllegalArgumentException("Error compiling shader: " + shader.getLog());
		return shader;
	}

	/** Convenience for {@link #begin()} that also sets the projection matrix from a camera's combined (projection * view)
	 * matrix. Call {@code camera.update()} before this if the camera moved. */
	public void begin (Camera camera) {
		projectionMatrix.set(camera.combined);
		begin();
	}

	/** Sets up the SpriteBatch3D for drawing. Enables depth testing and depth writing (GL_LEQUAL) so sprites are ordered by
	 * their z rather than draw order, and enables blending. Uses whatever projection/transform matrices are currently set - use
	 * {@link #begin(Camera)} to set them from a camera in one call. */
	public void begin () {
		if (drawing) throw new IllegalStateException("SpriteBatch3D.end must be called before begin.");
		renderCalls = 0;

		Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
		Gdx.gl.glDepthFunc(GL20.GL_LEQUAL);
		Gdx.gl.glDepthMask(true);

		if (customShader != null)
			customShader.bind();
		else
			shader.bind();
		setupMatrices();

		drawing = true;
	}

	/** Finishes off rendering. Depth testing is intentionally left enabled after end() (unlike SpriteBatch, which restores
	 * depth writes to true because 2D rendering runs with them off) - this class assumes it's part of a 3D-ish scene where
	 * depth testing staying on between batches is the expected state. Disable it yourself if that's not what you want. */
	public void end () {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before end.");
		if (idx > 0) flush();
		lastTexture = null;
		drawing = false;

		if (isBlendingEnabled()) Gdx.gl.glDisable(GL20.GL_BLEND);
	}

	/** Sets the color used to tint images when they are added to the batch. Default is {@link Color#WHITE}. */
	public void setColor (Color tint) {
		color.set(tint);
		colorPacked = tint.toFloatBits();
	}

	/** @see #setColor(Color) */
	public void setColor (float r, float g, float b, float a) {
		color.set(r, g, b, a);
		colorPacked = color.toFloatBits();
	}

	/** @return the rendering color of this batch. If the returned instance is manipulated, {@link #setColor(Color)} must be
	 *         called afterward. */
	public Color getColor () {
		return color;
	}

	/** Sets the rendering color of this batch, expanding the alpha from 0-254 to 0-255.
	 * @see #setColor(Color)
	 * @see Color#toFloatBits() */
	public void setPackedColor (float packedColor) {
		Color.abgr8888ToColor(color, packedColor);
		this.colorPacked = packedColor;
	}

	/** @return the rendering color of this batch in vertex format (alpha compressed to 0-254)
	 * @see Color#toFloatBits() */
	public float getPackedColor () {
		return colorPacked;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the given width and height in pixels. The rectangle is
	 * offset by originX, originY relative to the origin, scaled around that origin, and rotated (counter clockwise, in
	 * degrees) around that origin. Every vertex shares the same z, so the quad lies flat in a plane parallel to the XY plane
	 * at depth z. The portion of the texture given by srcX/srcY/srcWidth/srcHeight is used (in texels). */
	public void draw (Texture texture, float x, float y, float z, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
		boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1, y1, x2, y2, x3, y3, x4, y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}

		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the given width and height in pixels, unrotated and
	 * unscaled. The portion of the texture given by srcX/srcY/srcWidth/srcHeight is used (in texels). */
	public void draw (Texture texture, float x, float y, float z, float width, float height, int srcX, int srcY, int srcWidth,
		int srcHeight, boolean flipX, boolean flipY) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		float u = srcX * invTexWidth;
		float v = (srcY + srcHeight) * invTexHeight;
		float u2 = (srcX + srcWidth) * invTexWidth;
		float v2 = srcY * invTexHeight;
		final float fx2 = x + width;
		final float fy2 = y + height;

		if (flipX) {
			float tmp = u;
			u = u2;
			u2 = tmp;
		}

		if (flipY) {
			float tmp = v;
			v = v2;
			v2 = tmp;
		}

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the width and height of srcWidth/srcHeight in pixels. The
	 * portion of the texture given by srcX,srcY,srcWidth,srcHeight is used (in texels). */
	public void draw (Texture texture, float x, float y, float z, int srcX, int srcY, int srcWidth, int srcHeight) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float u = srcX * invTexWidth;
		final float v = (srcY + srcHeight) * invTexHeight;
		final float u2 = (srcX + srcWidth) * invTexWidth;
		final float v2 = srcY * invTexHeight;
		final float fx2 = x + srcWidth;
		final float fy2 = y + srcHeight;

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the given width and height in pixels, using explicit UV
	 * coordinates (u,v,u2,v2 as texture size percentage). */
	public void draw (Texture texture, float x, float y, float z, float width, float height, float u, float v, float u2,
		float v2) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the width and height of the texture. */
	public void draw (Texture texture, float x, float y, float z) {
		draw(texture, x, y, z, texture.getWidth(), texture.getHeight());
	}

	/** Draws a rectangle with the bottom left corner at x,y,z, stretching the whole texture to cover the given width and
	 * height. This is the core method requested: {@code draw(texture, x, y, z, width, height)}. */
	public void draw (Texture texture, float x, float y, float z, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		if (texture != lastTexture)
			switchTexture(texture);
		else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = 0;
		final float v = 1;
		final float u2 = 1;
		final float v2 = 0;

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle using the given vertices. There must be 4 vertices, each made up of {@link #VERTEX_SIZE} elements in
	 * this order: x, y, z, color, u, v. The {@link #getColor()} of this batch is not applied. */
	public void draw (Texture texture, float[] spriteVertices, int offset, int count) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		int verticesLength = vertices.length;
		int remainingVertices = verticesLength;
		if (texture != lastTexture)
			switchTexture(texture);
		else {
			remainingVertices -= idx;
			if (remainingVertices == 0) {
				flush();
				remainingVertices = verticesLength;
			}
		}
		int copyCount = Math.min(remainingVertices, count);

		System.arraycopy(spriteVertices, offset, vertices, idx, copyCount);
		idx += copyCount;
		count -= copyCount;
		while (count > 0) {
			offset += copyCount;
			flush();
			copyCount = Math.min(verticesLength, count);
			System.arraycopy(spriteVertices, offset, vertices, 0, copyCount);
			idx += copyCount;
			count -= copyCount;
		}
	}

	/** Draws a rectangle with the bottom left corner at x,y,z having the width and height of the region. */
	public void draw (TextureRegion region, float x, float y, float z) {
		draw(region, x, y, z, region.getRegionWidth(), region.getRegionHeight());
	}
	
	
	//TODO fine tune default implementation

	public void draw(Texture texture, float x, float y) {
		draw(texture, x, y, toZDepth(y));
	}

	public void drawOffset(Texture texture, float x, float y, float offset) {
		draw(texture, x, y, toZDepth(y)+offset);
	}

	public void draw(TextureRegion region, float x, float y) {
		draw(region, x, y, toZDepth(y));
	}

	public void drawOffset(TextureRegion region, float x, float y, float offset) {
		draw(region, x, y, toZDepth(y)+offset);
	}

	public void draw(TextureRegion region, float x, float y, float width, float height) {
		draw(region, x, y, toZDepth(y), width, height);
	}

	public void drawOffset(TextureRegion region, float x, float y, float offset, float width, float height) {
		draw(region, x, y, toZDepth(y)+offset, width, height);
	}

	
	public float toZDepth(float y, float offset) {
		return (-y/y_to_z_scale_multiplier)+offset;
	}
	
	public float toZDepth(float y) {
		return -y/y_to_z_scale_multiplier;
	}
	

	/** Draws a rectangle with the bottom left corner at x,y,z, stretching the region to cover the given width and height. */
	public void draw (TextureRegion region, float x, float y, float z, float width, float height) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.getTexture();
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			flush();

		final float fx2 = x + width;
		final float fy2 = y + height;
		final float u = region.getU();
		final float v = region.getV2();
		final float u2 = region.getU2();
		final float v2 = region.getV();

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = fy2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = fx2;
		vertices[idx++] = y;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Draws a rectangle with the bottom left corner at x,y,z, stretching the region to cover the given width and height. The
	 * rectangle is offset by originX, originY relative to the origin, scaled around that origin, and rotated (counter
	 * clockwise, in degrees) around that origin. */
	public void draw (TextureRegion region, float x, float y, float z, float originX, float originY, float width, float height,
		float scaleX, float scaleY, float rotation) {
		if (!drawing) throw new IllegalStateException("SpriteBatch3D.begin must be called before draw.");

		float[] vertices = this.vertices;

		Texture texture = region.getTexture();
		if (texture != lastTexture) {
			switchTexture(texture);
		} else if (idx == vertices.length) //
			flush();

		// bottom left and top right corner points relative to origin
		final float worldOriginX = x + originX;
		final float worldOriginY = y + originY;
		float fx = -originX;
		float fy = -originY;
		float fx2 = width - originX;
		float fy2 = height - originY;

		// scale
		if (scaleX != 1 || scaleY != 1) {
			fx *= scaleX;
			fy *= scaleY;
			fx2 *= scaleX;
			fy2 *= scaleY;
		}

		// construct corner points, start from top left and go counter clockwise
		final float p1x = fx;
		final float p1y = fy;
		final float p2x = fx;
		final float p2y = fy2;
		final float p3x = fx2;
		final float p3y = fy2;
		final float p4x = fx2;
		final float p4y = fy;

		float x1, y1, x2, y2, x3, y3, x4, y4;

		// rotate
		if (rotation != 0) {
			final float cos = MathUtils.cosDeg(rotation);
			final float sin = MathUtils.sinDeg(rotation);

			x1 = cos * p1x - sin * p1y;
			y1 = sin * p1x + cos * p1y;

			x2 = cos * p2x - sin * p2y;
			y2 = sin * p2x + cos * p2y;

			x3 = cos * p3x - sin * p3y;
			y3 = sin * p3x + cos * p3y;

			x4 = x1 + (x3 - x2);
			y4 = y3 - (y2 - y1);
		} else {
			x1 = p1x;
			y1 = p1y;

			x2 = p2x;
			y2 = p2y;

			x3 = p3x;
			y3 = p3y;

			x4 = p4x;
			y4 = p4y;
		}

		x1 += worldOriginX;
		y1 += worldOriginY;
		x2 += worldOriginX;
		y2 += worldOriginY;
		x3 += worldOriginX;
		y3 += worldOriginY;
		x4 += worldOriginX;
		y4 += worldOriginY;

		final float u = region.getU();
		final float v = region.getV2();
		final float u2 = region.getU2();
		final float v2 = region.getV();

		float color = this.colorPacked;
		int idx = this.idx;

		vertices[idx++] = x1;
		vertices[idx++] = y1;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v;

		vertices[idx++] = x2;
		vertices[idx++] = y2;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u;
		vertices[idx++] = v2;

		vertices[idx++] = x3;
		vertices[idx++] = y3;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v2;

		vertices[idx++] = x4;
		vertices[idx++] = y4;
		vertices[idx++] = z;
		vertices[idx++] = color;
		vertices[idx++] = u2;
		vertices[idx++] = v;

		this.idx = idx;
	}

	/** Causes any pending sprites to be rendered, without ending the batch. */
	public void flush () {
		if (idx == 0) return;

		renderCalls++;
		totalRenderCalls++;
		int spritesInBatch = idx / SPRITE_SIZE;
		if (spritesInBatch > maxSpritesInBatch) maxSpritesInBatch = spritesInBatch;
		int count = spritesInBatch * 6;

		lastTexture.bind();
		Mesh mesh = this.mesh;
		mesh.setVertices(vertices, 0, idx);

		// Only upload indices for the vertex array type
		if (currentDataType == VertexDataType.VertexArray) {
			Buffer indicesBuffer = (Buffer)mesh.getIndicesBuffer(true);
			indicesBuffer.position(0);
			indicesBuffer.limit(count);
		}

		if (blendingDisabled) {
			Gdx.gl.glDisable(GL20.GL_BLEND);
		} else {
			Gdx.gl.glEnable(GL20.GL_BLEND);
			if (blendSrcFunc != -1) Gdx.gl.glBlendFuncSeparate(blendSrcFunc, blendDstFunc, blendSrcFuncAlpha, blendDstFuncAlpha);
		}

		mesh.render(customShader != null ? customShader : shader, GL20.GL_TRIANGLES, 0, count);

		idx = 0;
	}

	/** Disables blending for drawing sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void disableBlending () {
		if (blendingDisabled) return;
		flush();
		blendingDisabled = true;
	}

	/** Enables blending for drawing sprites. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void enableBlending () {
		if (!blendingDisabled) return;
		flush();
		blendingDisabled = false;
	}

	/** Sets the blending function to be used when rendering sprites.
	 * @param srcFunc the source function, e.g. GL20.GL_SRC_ALPHA. If set to -1, the blend function won't be changed.
	 * @param dstFunc the destination function, e.g. GL20.GL_ONE_MINUS_SRC_ALPHA */
	public void setBlendFunction (int srcFunc, int dstFunc) {
		setBlendFunctionSeparate(srcFunc, dstFunc, srcFunc, dstFunc);
	}

	/** Sets separate (color/alpha) blending function to be used when rendering sprites. */
	public void setBlendFunctionSeparate (int srcFuncColor, int dstFuncColor, int srcFuncAlpha, int dstFuncAlpha) {
		if (blendSrcFunc == srcFuncColor && blendDstFunc == dstFuncColor && blendSrcFuncAlpha == srcFuncAlpha
			&& blendDstFuncAlpha == dstFuncAlpha) return;
		flush();
		blendSrcFunc = srcFuncColor;
		blendDstFunc = dstFuncColor;
		blendSrcFuncAlpha = srcFuncAlpha;
		blendDstFuncAlpha = dstFuncAlpha;
	}

	public int getBlendSrcFunc () {
		return blendSrcFunc;
	}

	public int getBlendDstFunc () {
		return blendDstFunc;
	}

	public int getBlendSrcFuncAlpha () {
		return blendSrcFuncAlpha;
	}

	public int getBlendDstFuncAlpha () {
		return blendDstFuncAlpha;
	}

	@Override
	public void dispose () {
		mesh.dispose();
		if (ownsShader && shader != null) shader.dispose();
	}

	/** Returns the current projection matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined
	 * behaviour. */
	public Matrix4 getProjectionMatrix () {
		return projectionMatrix;
	}

	/** Returns the current transform matrix. Changing this within {@link #begin()}/{@link #end()} results in undefined
	 * behaviour. */
	public Matrix4 getTransformMatrix () {
		return transformMatrix;
	}

	/** Sets the projection matrix to be used by this batch directly. If this is called inside a {@link #begin()}/
	 * {@link #end()} block, the current batch is flushed first. Prefer {@link #begin(Camera)} for the common case. */
	public void setProjectionMatrix (Matrix4 projection) {
		if (drawing) flush();
		projectionMatrix.set(projection);
		if (drawing) setupMatrices();
	}

	/** Convenience for {@link #setProjectionMatrix(Matrix4)} using a camera's combined matrix. */
	public void setProjectionMatrix (Camera camera) {
		setProjectionMatrix(camera.combined);
	}

	/** Sets an additional transform matrix applied on top of the projection matrix (e.g. a parent transform). Identity by
	 * default. */
	public void setTransformMatrix (Matrix4 transform) {
		if (drawing) flush();
		transformMatrix.set(transform);
		if (drawing) setupMatrices();
	}

	/** Fragment alpha at or below this value is discarded before it can write color or depth, which keeps fully-transparent
	 * texels from writing bogus depth values that would otherwise occlude sprites behind them. Default 0 (only exactly
	 * transparent texels are discarded). Raise this if your art has semi-transparent edge antialiasing you'd rather clip than
	 * let write depth. Calling this within {@link #begin()}/{@link #end()} will flush the batch. */
	public void setAlphaTestThreshold (float threshold) {
		if (drawing) flush();
		alphaTestThreshold = threshold;
		if (drawing) setupMatrices();
	}

	public float getAlphaTestThreshold () {
		return alphaTestThreshold;
	}

	protected void setupMatrices () {
		combinedMatrix.set(projectionMatrix).mul(transformMatrix);
		ShaderProgram activeShader = customShader != null ? customShader : shader;
		activeShader.setUniformMatrix("u_projTrans", combinedMatrix);
		activeShader.setUniformi("u_texture", 0);
		activeShader.setUniformf("u_alphaTest", alphaTestThreshold);
	}

	protected void switchTexture (Texture texture) {
		flush();
		lastTexture = texture;
		invTexWidth = 1.0f / texture.getWidth();
		invTexHeight = 1.0f / texture.getHeight();
	}

	/** Sets the shader to be used in a GLES 2.0 environment. Vertex position attribute is "a_position" (vec4, 3 components
	 * supplied), texture coordinates attribute is "a_texCoord0", color attribute is "a_color". The combined transform and
	 * projection matrix is uploaded via a mat4 uniform called "u_projTrans", the texture sampler via "u_texture", and the
	 * alpha-cutoff via a float uniform called "u_alphaTest".
	 * <p>
	 * Call this method with a null argument to use the default shader.
	 * <p>
	 * This method will flush the batch before setting the new shader, you can call it in between {@link #begin()} and
	 * {@link #end()}. */
	public void setShader (ShaderProgram shader) {
		if (shader == customShader) // avoid unnecessary flushing in case we are drawing
			return;
		if (drawing) {
			flush();
		}
		customShader = shader;
		if (drawing) {
			if (customShader != null)
				customShader.bind();
			else
				this.shader.bind();
			setupMatrices();
		}
	}

	/** @return the current {@link ShaderProgram} set by {@link #setShader(ShaderProgram)} or the default shader */
	public ShaderProgram getShader () {
		if (customShader == null) {
			return shader;
		}
		return customShader;
	}

	/** @return true if blending for sprites is enabled */
	public boolean isBlendingEnabled () {
		return !blendingDisabled;
	}

	/** @return true if currently between begin and end. */
	public boolean isDrawing () {
		return drawing;
	}

	@Override
	public void draw(Texture texture, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation, int srcX, int srcY, int srcWidth, int srcHeight, boolean flipX,
			boolean flipY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height, int srcX, int srcY, int srcWidth,
			int srcHeight, boolean flipX, boolean flipY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Texture texture, float x, float y, int srcX, int srcY, int srcWidth, int srcHeight) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height, float u, float v, float u2,
			float v2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(Texture texture, float x, float y, float width, float height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(TextureRegion region, float x, float y, float originX, float originY, float width, float height,
			float scaleX, float scaleY, float rotation, boolean clockwise) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void draw(TextureRegion region, float width, float height, Affine2 transform) {
		// TODO Auto-generated method stub
		
	}
}
