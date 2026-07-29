package com.alectriciti.gdx.font;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.PixmapIO;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFont.BitmapFontData;
import com.badlogic.gdx.graphics.g2d.BitmapFont.Glyph;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Disposable;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds a LibGDX {@link BitmapFont} at runtime directly from a plain image, with no {@code .fnt} file,
 * no Hiero, and no BMFont involved. Glyph artists box each character in pure red ({@code #FF0000}) on
 * their sprite sheet; this class detects every red box, figures out reading order the way a human would
 * (row by row, top to bottom, then left to right within a row), assigns them to the characters of a
 * charset string in exact order, strips the red guide color out of the final texture, and hands back a
 * ready-to-use {@link BitmapFont}.
 *
 * <h2>The red guide convention</h2>
 * <ul>
 * <li>Every glyph is enclosed in a fully closed rectangle drawn in pure, opaque red
 * ({@code #FF0000}, i.e. {@code R=255 G=0 B=0 A=255}). The rectangle does not need to be filled - only
 * the border needs to be red. Border thickness can be anything (1px is fine) and does not need to be the
 * same on all four sides.</li>
 * <li>Leave at least a 1px transparent gap between separate boxes. Boxes that touch or overlap will be
 * merged into a single detected region and cause a count-mismatch error.</li>
 * <li>Do not use pure red anywhere else in the image, including inside a glyph's own artwork - it will be
 * treated as part of the guide and stripped out.</li>
 * <li>Turn off anti-aliasing when drawing the red guide lines. An anti-aliased edge blends red with the
 * background and will not match the guide color, which can make a box look "open" and fail validation.
 * If your art tool cannot avoid this, raise {@link #setRedTolerance(int)} slightly.</li>
 * </ul>
 *
 * <h2>Exact ordering contract (read this if a character comes out wrong)</h2>
 * <ol>
 * <li>The charset string is walked with {@link String#codePoints()}, not {@code charAt}, so it is real
 * Unicode code points in the order they appear in the string, left to right as written in your source
 * code. (This also means multi-{@code char} UTF-16 surrogate pairs would be read as one unit - though see
 * the BMP note below.)</li>
 * <li>Detected red boxes are sorted into rows by vertical overlap: two boxes belong to the same row if
 * their vertical extents overlap by at least {@link #setRowOverlapThreshold(int)}% (default 40%) of the
 * shorter box's height. This tolerates hand-drawn rows that are not pixel-perfectly aligned. Rows are
 * then ordered top to bottom by their topmost pixel.</li>
 * <li>Within a row, boxes are ordered left to right by their leftmost pixel.</li>
 * <li>The Nth code point of the charset is assigned to the Nth box in that reading order. The number of
 * code points and the number of detected boxes must match exactly, or generation fails with a message
 * telling you both counts.</li>
 * </ol>
 *
 * <h2>Unicode / Basic Multilingual Plane limitation</h2>
 * LibGDX's {@code BitmapFontData} stores glyphs in a 128-page table indexed by a bare {@code char}
 * (16-bit), so it can only address code points {@code U+0000}-{@code U+FFFF} (the Basic Multilingual
 * Plane). Anything outside that range (emoji, rare CJK extension characters, etc.) cannot be stored by
 * {@code BitmapFont} at all, in the generator or otherwise, so the constructor rejects such characters
 * immediately with a clear error rather than letting them fail deep inside LibGDX later.
 *
 * <h2>Baseline and descenders</h2>
 * Every glyph is bottom-aligned within a shared line height (the tallest glyph box in the charset, unless
 * you override it with {@link #setLineHeight(int)}). By default there is no reserved descender area, which
 * matches how most simple pixel fonts are drawn (g/y/p/q tucked into the normal row instead of hanging
 * below the baseline). If you want true hanging descenders, reserve room for them uniformly across every
 * box with {@link #setDescenderReservation(int)} and draw your descending glyphs taller, using the extra
 * pixels at the bottom of their box. For one-off nudges to a single character after generation, the result
 * exposes the real {@code BitmapFontData}, e.g.:
 * <pre>{@code font.getFont().getData().getGlyph('g').yoffset -= 2;}</pre>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz" +
 *                   "1234567890!@#$%^&*()-=_+[]{};':\",.<>/?\\|~`";
 *
 * GeneratedPixelFontMapResult result = new GeneratePixelFontMap(charset)
 *         .setSource(Gdx.files.internal("fonts/my_font.png"))
 *         .setLetterSpacing(1)
 *         .generate();
 *
 * BitmapFont font = result.getFont();
 * // ... use font normally, e.g. with a SpriteBatch or Scene2D Label ...
 *
 * // later, when you are completely done with it:
 * result.dispose();
 * }</pre>
 *
 * Because everything is derived live from the source image every time {@link #generate()} runs, an artist
 * can move, resize or redraw any glyph box and simply restart the game (or re-run generation) to see the
 * change - there is no intermediate baked file to fall out of sync.
 *
 * @author Claude, for Alec
 */
public class GeneratePixelFontMap {

	/** Pure, fully opaque red - the one and only guide color, packed as RGBA8888 ({@code R=255 G=0 B=0 A=255}). */
	public static final int GUIDE_COLOR_RGBA8888 = 0xFF0000FF;

	/** Fraction of a guide box's border edge that must actually be red for the box to be considered closed. */
	private static final float MIN_EDGE_CLOSURE_FRACTION = 0.9f;

	private final int[] codepoints;

	private Pixmap sourcePixmap;
	private boolean disposeSourceOnGenerate;

	private int redTolerance = 0;
	private int letterSpacing = 1;
	private int descenderReservationPx = 0;
	private int rowOverlapPercent = 40;
	private Integer lineHeightOverride = null;
	private Integer spaceWidthOverride = null;
	private Character missingGlyphChar = '?';
	private boolean pixelPerfect = true;
	private FileHandle debugOutput = null;

	/**
	 * @param charset The characters to assign to detected boxes, in exact reading order. Every code point
	 *                must be in the Basic Multilingual Plane ({@code U+0000}-{@code U+FFFF}).
	 */
	public GeneratePixelFontMap (String charset) {
		if (charset == null || charset.isEmpty())
			throw new PixelFontException("charset must not be null or empty.");

		this.codepoints = charset.codePoints().toArray();
		for (int cp : codepoints) {
			if (cp > 0xFFFF) {
				throw new PixelFontException("Character '" + new String(Character.toChars(cp)) + "' (U+"
					+ Integer.toHexString(cp).toUpperCase() + ") is outside the Basic Multilingual Plane. "
					+ "LibGDX's BitmapFont can only store code points from U+0000 to U+FFFF, so this "
					+ "character cannot be used no matter how the font is generated.");
			}
		}
	}

	/** Loads the source sprite sheet from disk. The generator reads it once during {@link #generate()} and
	 * disposes its own copy of the pixel data afterwards; you do not need to dispose anything yourself. */
	public GeneratePixelFontMap setSource (FileHandle imageFile) {
		if (imageFile == null) throw new PixelFontException("imageFile must not be null.");
		this.sourcePixmap = new Pixmap(imageFile);
		this.disposeSourceOnGenerate = true;
		return this;
	}

	/** Uses an already-loaded (or in-memory-edited) Pixmap as the source sprite sheet. The generator works
	 * on its own internal copy and never modifies or disposes the Pixmap you pass in - you keep ownership. */
	public GeneratePixelFontMap setSource (Pixmap pixmap) {
		if (pixmap == null) throw new PixelFontException("pixmap must not be null.");
		this.sourcePixmap = pixmap;
		this.disposeSourceOnGenerate = false;
		return this;
	}

	/** How far (0-255) a pixel's channels may drift from pure red and still count as a guide pixel.
	 * Default is 0 (exact match only). Only raise this if your art tool cannot produce hard, non-anti-aliased
	 * red edges - a large tolerance risks catching reddish pixels that are actually part of the glyph art. */
	public GeneratePixelFontMap setRedTolerance (int tolerance0to255) {
		if (tolerance0to255 < 0 || tolerance0to255 > 255)
			throw new PixelFontException("redTolerance must be between 0 and 255.");
		this.redTolerance = tolerance0to255;
		return this;
	}

	/** Extra pixels added on top of each glyph's box width when advancing the cursor for the next
	 * character. Default is 1. Set to 0 for glyphs that should sit flush against each other. */
	public GeneratePixelFontMap setLetterSpacing (int extraPixelsBetweenGlyphs) {
		this.letterSpacing = extraPixelsBetweenGlyphs;
		return this;
	}

	/** Reserves this many pixels below the baseline, uniformly, on every glyph. Default is 0 (no reserved
	 * descender area - glyphs are simply bottom-aligned to the shared line height). If you want true
	 * hanging descenders, set this to the depth of your deepest descender and draw those glyphs taller,
	 * using the reserved strip at the bottom of their box; non-descending glyphs just leave it empty. This
	 * only changes the font's reported {@code descent}/{@code ascent} metrics, not glyph positions - see
	 * the class docs for why. */
	public GeneratePixelFontMap setDescenderReservation (int pixelsBelowBaseline) {
		if (pixelsBelowBaseline < 0) throw new PixelFontException("descenderReservation cannot be negative.");
		this.descenderReservationPx = pixelsBelowBaseline;
		return this;
	}

	/** Overrides the line height (default: the tallest detected glyph box). Must be at least as tall as the
	 * tallest glyph box or glyphs on adjacent lines would overlap. */
	public GeneratePixelFontMap setLineHeight (int pixels) {
		if (pixels <= 0) throw new PixelFontException("lineHeight must be positive.");
		this.lineHeightOverride = pixels;
		return this;
	}

	/** Overrides the advance width of the space character. Only used when the charset does not already
	 * include an explicit {@code ' '} with its own guide box. Default is derived automatically from the
	 * average glyph width. */
	public GeneratePixelFontMap setSpaceWidth (int pixels) {
		if (pixels <= 0) throw new PixelFontException("spaceWidth must be positive.");
		this.spaceWidthOverride = pixels;
		return this;
	}

	/** Minimum vertical overlap, as a percentage of the shorter box's height, for two boxes to be
	 * considered part of the same row. Default is 40. Lower this if your rows are drawn very unevenly;
	 * raise it if unrelated rows are incorrectly getting merged together. */
	public GeneratePixelFontMap setRowOverlapThreshold (int percent) {
		if (percent < 1 || percent > 100) throw new PixelFontException("rowOverlapThreshold must be between 1 and 100.");
		this.rowOverlapPercent = percent;
		return this;
	}

	/** If true (the default), rendering positions are rounded to integers and the texture uses nearest-
	 * neighbor filtering, which is almost always what you want for a hand-drawn pixel font: crisp edges,
	 * no blur, no shimmering on sub-pixel movement. Turn off only if you deliberately want smooth-scaled
	 * text. */
	public GeneratePixelFontMap setPixelPerfect (boolean enabled) {
		this.pixelPerfect = enabled;
		return this;
	}

	/** The character used as the fallback glyph for codepoints not present in the charset (so unmapped
	 * characters render as this instead of silently vanishing). Defaults to {@code '?'} if present in the
	 * charset. Pass {@code null} to disable the fallback entirely. */
	public GeneratePixelFontMap setMissingGlyphCharacter (Character character) {
		this.missingGlyphChar = character;
		return this;
	}

	/** If set, {@link #generate()} writes the cleaned (red-stripped) texture out as a PNG to this location
	 * before disposing its working pixel data. Handy during development to visually confirm exactly what
	 * the generator saw and produced. */
	public GeneratePixelFontMap setDebugOutput (FileHandle pngTarget) {
		this.debugOutput = pngTarget;
		return this;
	}

	/** Runs detection, ordering, mapping, and red-stripping, and builds the {@link BitmapFont}. Throws
	 * {@link PixelFontException} with a specific, actionable message if anything about the source image
	 * does not match the charset or the red-box convention. */
	public GeneratedPixelFontMapResult generate () {
		if (sourcePixmap == null)
			throw new PixelFontException("No source image set - call setSource(...) before generate().");

		int width = sourcePixmap.getWidth();
		int height = sourcePixmap.getHeight();

		// Work on our own RGBA8888 copy so the caller's Pixmap (if they supplied one directly, e.g. for a
		// live in-game glyph editor) is never modified, and so source formats without an alpha channel
		// can't interfere with the guide-color stripping step below.
		Pixmap working = new Pixmap(width, height, Pixmap.Format.RGBA8888);
		working.setBlending(Pixmap.Blending.None);
		working.drawPixmap(sourcePixmap, 0, 0);

		if (disposeSourceOnGenerate) {
			sourcePixmap.dispose();
		}
		sourcePixmap = null;

		int[] pixels = snapshotRGBA8888(working);
		boolean[] guideMask = new boolean[pixels.length];
		for (int i = 0; i < pixels.length; i++)
			guideMask[i] = isGuideColor(pixels[i], redTolerance);

		List<GlyphBox> boxes = floodFillBoxes(guideMask, width, height);
		if (boxes.isEmpty()) {
			working.dispose();
			throw new PixelFontException("No red (#FF0000) guide boxes were found in the source image. "
				+ "Every glyph must be enclosed in a fully closed rectangle drawn in pure red.");
		}

		List<GlyphBox> ordered = orderReadingDirection(boxes);

		if (ordered.size() != codepoints.length) {
			working.dispose();
			throw new PixelFontException(String.format(
				"Charset / box count mismatch: the charset has %d character%s but %d red guide box%s were "
					+ "detected in the image. These two counts must be exactly equal - either fix the charset "
					+ "string or add/remove/merge boxes in the source image.",
				codepoints.length, codepoints.length == 1 ? "" : "s",
				ordered.size(), ordered.size() == 1 ? "" : "es"));
		}
		for (int i = 0; i < ordered.size(); i++)
			ordered.get(i).assign(codepoints[i]);

		stripGuidePixels(working, guideMask, width, height);

		Texture texture = new Texture(working);
		if (pixelPerfect) texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
		TextureRegion page = new TextureRegion(texture);

		if (debugOutput != null) PixmapIO.writePNG(debugOutput, working);
		working.dispose();

		int maxHeight = 0;
		for (GlyphBox b : ordered) maxHeight = Math.max(maxHeight, b.innerHeight);
		int lineHeight = lineHeightOverride != null ? lineHeightOverride : maxHeight;
		if (lineHeight < maxHeight) {
			texture.dispose();
			throw new PixelFontException("lineHeight override (" + lineHeight + "px) is smaller than the "
				+ "tallest glyph box (" + maxHeight + "px); glyphs on adjacent lines would overlap.");
		}

		BitmapFontData data = new BitmapFontData();
		data.flipped = false;
		data.padTop = data.padRight = data.padBottom = data.padLeft = 0;
		data.setLineHeight(lineHeight);

		for (GlyphBox b : ordered) {
			Glyph glyph = new Glyph();
			glyph.id = b.codepoint;
			glyph.srcX = b.innerX;
			glyph.srcY = b.innerY;
			glyph.width = b.innerWidth;
			glyph.height = b.innerHeight;
			glyph.xoffset = 0;
			// Every glyph is bottom-aligned within the shared line height: top-of-line to top-of-glyph is
			// (lineHeight - glyphHeight), which BitmapFontData's non-flipped convention folds into a
			// constant yoffset of -lineHeight for every glyph. See the class docs for the full reasoning.
			int topOffsetFromLineTop = lineHeight - b.innerHeight;
			glyph.yoffset = -(b.innerHeight + topOffsetFromLineTop);
			glyph.xadvance = b.innerWidth + letterSpacing;
			data.setGlyphRegion(glyph, page);
			data.setGlyph(b.codepoint, glyph);
			b.resolvedGlyph = glyph;
		}

		Glyph spaceGlyph = data.getGlyph(' ');
		if (spaceGlyph == null) {
			spaceGlyph = new Glyph();
			spaceGlyph.id = ' ';
			int fallbackWidth;
			if (spaceWidthOverride != null) {
				fallbackWidth = spaceWidthOverride;
			} else {
				long totalWidth = 0;
				for (GlyphBox b : ordered) totalWidth += b.innerWidth;
				fallbackWidth = Math.max(1, Math.round((totalWidth / (float) ordered.size()) * 0.6f));
			}
			spaceGlyph.xadvance = fallbackWidth;
			data.setGlyph(' ', spaceGlyph);
		}
		data.spaceXadvance = spaceGlyph.xadvance;

		Glyph capReference = data.getGlyph('M');
		if (capReference == null) capReference = data.getFirstGlyph();
		data.capHeight = capReference.height;

		Glyph xReference = data.getGlyph('x');
		if (xReference == null) xReference = capReference;
		data.xHeight = xReference.height;

		data.descent = -descenderReservationPx;
		data.ascent = lineHeight - data.capHeight - descenderReservationPx;

		if (missingGlyphChar != null) {
			Glyph mg = data.getGlyph(missingGlyphChar);
			if (mg != null) data.missingGlyph = mg;
		}

		BitmapFont font = new BitmapFont(data, page, pixelPerfect);
		return new GeneratedPixelFontMapResult(font, texture, ordered);
	}

	// ---------------------------------------------------------------------------------------------
	// Detection internals
	// ---------------------------------------------------------------------------------------------

	/** Reads every pixel once via the native buffer instead of the (much slower, JNI-per-call) getPixel(),
	 * since this scan touches every pixel of the sheet and getPixel() overhead adds up fast on large sheets. */
	private static int[] snapshotRGBA8888 (Pixmap pm) {
		int w = pm.getWidth(), h = pm.getHeight();
		ByteBuffer buf = pm.getPixels();
		buf.rewind();
		int[] out = new int[w * h];
		for (int i = 0; i < out.length; i++) {
			int r = buf.get() & 0xFF;
			int g = buf.get() & 0xFF;
			int b = buf.get() & 0xFF;
			int a = buf.get() & 0xFF;
			out[i] = (r << 24) | (g << 16) | (b << 8) | a;
		}
		return out;
	}

	private static boolean isGuideColor (int rgba8888, int tolerance) {
		int r = (rgba8888 >>> 24) & 0xFF;
		int g = (rgba8888 >>> 16) & 0xFF;
		int b = (rgba8888 >>> 8) & 0xFF;
		int a = rgba8888 & 0xFF;
		if (a < 255 - tolerance) return false;
		return r >= 255 - tolerance && g <= tolerance && b <= tolerance;
	}

	/** 4-connected flood fill over the guide mask. 4-connectivity (not 8) is deliberate: a hollow rectangle
	 * outline is always a single 4-connected component on its own, while using 8-connectivity would risk
	 * merging two boxes whose corners happen to touch diagonally. */
	private List<GlyphBox> floodFillBoxes (boolean[] guideMask, int w, int h) {
		boolean[] visited = new boolean[guideMask.length];
		List<GlyphBox> boxes = new ArrayList<>();
		ArrayDeque<Integer> queue = new ArrayDeque<>();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int idx = y * w + x;
				if (visited[idx]) continue;
				visited[idx] = true;
				if (!guideMask[idx]) continue;

				int minX = x, maxX = x, minY = y, maxY = y;
				queue.clear();
				queue.add(idx);

				while (!queue.isEmpty()) {
					int pidx = queue.poll();
					int px = pidx % w;
					int py = pidx / w;
					if (px < minX) minX = px;
					if (px > maxX) maxX = px;
					if (py < minY) minY = py;
					if (py > maxY) maxY = py;

					offer(queue, visited, guideMask, w, h, px - 1, py);
					offer(queue, visited, guideMask, w, h, px + 1, py);
					offer(queue, visited, guideMask, w, h, px, py - 1);
					offer(queue, visited, guideMask, w, h, px, py + 1);
				}

				boxes.add(buildGlyphBox(guideMask, w, minX, minY, maxX, maxY));
			}
		}
		return boxes;
	}

	private static void offer (ArrayDeque<Integer> queue, boolean[] visited, boolean[] guideMask, int w, int h,
		int x, int y) {
		if (x < 0 || y < 0 || x >= w || y >= h) return;
		int idx = y * w + x;
		if (visited[idx]) return;
		visited[idx] = true;
		if (guideMask[idx]) queue.add(idx);
	}

	private GlyphBox buildGlyphBox (boolean[] guideMask, int w, int minX, int minY, int maxX, int maxY) {
		checkHorizontalEdge(guideMask, w, minY, minX, maxX); // top
		checkHorizontalEdge(guideMask, w, maxY, minX, maxX); // bottom
		checkVerticalEdge(guideMask, w, minX, minY, maxY); // left
		checkVerticalEdge(guideMask, w, maxX, minY, maxY); // right

		// Probe inward from the box's center along both axes to find where the border ends and the
		// artwork begins. This works regardless of border thickness, including uneven thickness on
		// different sides, as long as the center row/column is solidly red across the full border.
		int centerX = (minX + maxX) / 2;
		int centerY = (minY + maxY) / 2;

		int innerMinY = minY;
		while (innerMinY <= maxY && guideMask[innerMinY * w + centerX]) innerMinY++;
		int innerMaxY = maxY;
		while (innerMaxY >= minY && guideMask[innerMaxY * w + centerX]) innerMaxY--;
		int innerMinX = minX;
		while (innerMinX <= maxX && guideMask[centerY * w + innerMinX]) innerMinX++;
		int innerMaxX = maxX;
		while (innerMaxX >= minX && guideMask[centerY * w + innerMaxX]) innerMaxX--;

		if (innerMinX > innerMaxX || innerMinY > innerMaxY) {
			throw new PixelFontException(String.format(
				"The red guide box at (%d,%d) sized %dx%d has no room left for glyph artwork once its "
					+ "border is excluded. Make the box larger or the border thinner.",
				minX, minY, maxX - minX + 1, maxY - minY + 1));
		}

		return new GlyphBox(minX, minY, maxX - minX + 1, maxY - minY + 1,
			innerMinX, innerMinY, innerMaxX - innerMinX + 1, innerMaxY - innerMinY + 1);
	}

	private static void checkHorizontalEdge (boolean[] guideMask, int w, int y, int xStart, int xEnd) {
		int total = xEnd - xStart + 1, red = 0;
		for (int x = xStart; x <= xEnd; x++)
			if (guideMask[y * w + x]) red++;
		if (red < total * MIN_EDGE_CLOSURE_FRACTION) {
			throw new PixelFontException("A red guide box has a gap in its border near (" + xStart + "," + y
				+ "). Every glyph box must be a complete, unbroken red rectangle.");
		}
	}

	private static void checkVerticalEdge (boolean[] guideMask, int w, int x, int yStart, int yEnd) {
		int total = yEnd - yStart + 1, red = 0;
		for (int y = yStart; y <= yEnd; y++)
			if (guideMask[y * w + x]) red++;
		if (red < total * MIN_EDGE_CLOSURE_FRACTION) {
			throw new PixelFontException("A red guide box has a gap in its border near (" + x + "," + yStart
				+ "). Every glyph box must be a complete, unbroken red rectangle.");
		}
	}

	private static void stripGuidePixels (Pixmap working, boolean[] guideMask, int w, int h) {
		for (int y = 0; y < h; y++)
			for (int x = 0; x < w; x++)
				if (guideMask[y * w + x]) working.drawPixel(x, y, 0x00000000);
	}

	/** Row-by-row, left-to-right reading order. See the "Exact ordering contract" section of the class docs. */
	private List<GlyphBox> orderReadingDirection (List<GlyphBox> boxes) {
	    List<GlyphBox> sorted = new ArrayList<>(boxes);
	    // Sort top-to-bottom
	    sorted.sort((a, b) -> Integer.compare(a.outerY, b.outerY));

	    List<List<GlyphBox>> rows = new ArrayList<>();
	    List<GlyphBox> currentRow = new ArrayList<>();
	    
	    // Track the row's baseline/anchor based on the FIRST character of the row
	    int anchorTop = 0, anchorBottom = 0;

	    for (GlyphBox box : sorted) {
	        int boxTop = box.outerY;
	        int boxBottom = box.outerY + box.outerHeight - 1;

	        if (currentRow.isEmpty()) {
	            currentRow.add(box);
	            anchorTop = boxTop;
	            anchorBottom = boxBottom;
	            continue;
	        }

	        // Compare overlap strictly against the anchor box, NOT an infinitely expanding boundary
	        int overlap = Math.min(anchorBottom, boxBottom) - Math.max(anchorTop, boxTop) + 1;
	        int shorterHeight = Math.min(anchorBottom - anchorTop + 1, box.outerHeight);
	        boolean sameRow = overlap > 0 && overlap >= shorterHeight * (rowOverlapPercent / 100f);

	        if (sameRow) {
	            currentRow.add(box);
	            // DO NOT continually expand the row bounds here!
	        } else {
	            rows.add(currentRow);
	            currentRow = new ArrayList<>();
	            currentRow.add(box);
	            
	            // This new box becomes the anchor for the next row
	            anchorTop = boxTop;
	            anchorBottom = boxBottom;
	        }
	    }
	    if (!currentRow.isEmpty()) rows.add(currentRow);

	    // Finally, sort each detected row horizontally (left-to-right)
	    List<GlyphBox> result = new ArrayList<>(boxes.size());
	    for (List<GlyphBox> row : rows) {
	        row.sort((a, b) -> Integer.compare(a.outerX, b.outerX));
	        result.addAll(row);
	    }
	    return result;
	}

	// ---------------------------------------------------------------------------------------------
	// Result / data / exception types
	// ---------------------------------------------------------------------------------------------

	/** The output of {@link GeneratePixelFontMap#generate()}: a ready-to-use font plus the resources you
	 * are responsible for disposing when you're done with it, and the box mapping for debugging. */
	public static class GeneratedPixelFontMapResult implements Disposable {
		private final BitmapFont font;
		private final Texture texture;
		private final List<GlyphBox> glyphBoxes;

		GeneratedPixelFontMapResult (BitmapFont font, Texture texture, List<GlyphBox> glyphBoxes) {
			this.font = font;
			this.texture = texture;
			this.glyphBoxes = glyphBoxes;
		}

		public BitmapFont getFont () {
			return font;
		}

		public Texture getTexture () {
			return texture;
		}

		/** The exact box each character was mapped to, in final reading order. Log this if a glyph looks
		 * wrong to see precisely which pixels of your sheet were assigned to it. */
		public List<GlyphBox> getGlyphBoxes () {
			return glyphBoxes;
		}

		/** Disposes the generated {@link BitmapFont} and its backing {@link Texture}. Call this the same
		 * way you'd dispose any other font/texture pair - once, when you are completely done with it. */
		@Override
		public void dispose () {
			font.dispose();
			texture.dispose();
		}
	}

	/** One detected red guide box, with both its outer extent (including the red border, in source-image
	 * pixel coordinates) and its inner extent (the actual glyph artwork, border excluded). */
	public static class GlyphBox {
		public final int outerX, outerY, outerWidth, outerHeight;
		public final int innerX, innerY, innerWidth, innerHeight;
		private int codepoint = -1;
		Glyph resolvedGlyph;

		GlyphBox (int outerX, int outerY, int outerWidth, int outerHeight, int innerX, int innerY, int innerWidth,
			int innerHeight) {
			this.outerX = outerX;
			this.outerY = outerY;
			this.outerWidth = outerWidth;
			this.outerHeight = outerHeight;
			this.innerX = innerX;
			this.innerY = innerY;
			this.innerWidth = innerWidth;
			this.innerHeight = innerHeight;
		}

		void assign (int codepoint) {
			this.codepoint = codepoint;
		}

		/** The Unicode code point assigned to this box. */
		public int getCodepoint () {
			return codepoint;
		}

		/** Human-readable form of the assigned character, for debug logging. */
		public String getCharacterDisplay () {
			return codepoint < 0 ? "?" : new String(Character.toChars(codepoint));
		}

		@Override
		public String toString () {
			return String.format("'%s' outer[%d,%d %dx%d] inner[%d,%d %dx%d]", getCharacterDisplay(), outerX, outerY,
				outerWidth, outerHeight, innerX, innerY, innerWidth, innerHeight);
		}
	}

	/** Thrown for any problem with the source image or charset that would otherwise produce a broken or
	 * mis-mapped font. Every message is written to point at the specific box, character, or count involved. */
	public static class PixelFontException extends RuntimeException {
		public PixelFontException (String message) {
			super(message);
		}
	}
}