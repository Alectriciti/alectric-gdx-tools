package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;
import static com.alectriciti.gdx.WidgetManager.*;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

/**
 * Widgets are the building blocks of Alectric UI which can be updated/rendered manually, or using WidgetManager
 */
public class Widget {
	


	public List<Widget> widgets = new ArrayList<Widget>();
	public List<Button> buttons = new ArrayList<Button>();
	
	protected WidgetManager manager;
	
	public String name;
	protected Rectangle shape;
	protected Rectangle shape_global = new Rectangle();
	public int z; //for z ordering
	
	boolean hovering = false;
	protected Widget parent;

	
	
	protected Texture texture;
	private FileHandle texture_file;

	public void setTexture(FileHandle fileHandle) {
		this.texture_file = fileHandle;
		this.texture = new Texture(texture_file);
	}
	
	public Texture getAndLoadTexture() {
		if(texture==null) {
			if(texture_file.exists())
			texture = new Texture(texture_file);
		}
		return texture;
	}
	
	/**
	 * With WidgetManager, Widgets are required to be visible in order to render or be interacted with
	 */
	public boolean visible = true;

	public Color color_default = new Color(0, 0, 0, 1);
	public Color color_edit = new Color(1, 0.25f, 0.25f, 1);
	public Color color_trim = new Color(0.25f, 0.25f, 0.25f, 1);
	public Color color_trim_highlight = new Color(0.5f, 1f, 0.5f, 1);
	
	/**
	 * The color which is drawn to be active at all times
	 */
	public Color color = new Color(0, 0, 0, 1);

	
	/**
	 * Reserved for Canvas
	 */
	protected Widget() {
		}
	
	/**
	 * Used for other widgets
	 * @param name name of the widget
	 * @param canvas name of the container to apply it to
	 */
	public Widget(String name, Canvas canvas) {
		this.name = name;
		this.shape = new Rectangle();
		if(canvas != null) {
			this.manager = canvas.manager;
			canvas.registerWidget(this);
		}else {
			printError("Error instantiating "+name+" ... Canvas is NULL. Register with a WidgetManager instead");
		}
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	public Widget(String name, int pos_x, int pos_y, int width, int height, Canvas canvas) {
		this(name, canvas);
		this.shape = new Rectangle(pos_x, pos_y, width, height);
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	public Widget(String name, WidgetManager manager) {
		this.name = name;
		this.manager = manager;
		this.shape = new Rectangle();
		this.manager.registerWidget(this);
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	
	
	
	
	
	
	
	
	public void setSize(float width, float height) {
		this.shape.setWidth(width);
		this.shape.setHeight(height);
		this.shape_global.setWidth(width);
		this.shape_global.setHeight(height);
	}
	
	public Widget getParent() {
		return parent;
	}

	public Widget setRelativePosition(float x, float y) {
		this.shape.x = x;
		this.shape.y = y;
		updateGlobalPosition();
		return this;
	}
	
	public void updateGlobalPosition() {
		if(parent!=null) {
			shape_global.x = parent.getGlobalX() + shape.x;
			shape_global.y = parent.getGlobalY() + shape.y;
		}else {
			shape_global.x = shape.x;
			shape_global.y = shape.y;
		}
		for(Widget w : widgets) {
			w.updateGlobalPosition();
		}
	}
	
	
	public void setGlobalPosition(float x, float y) {
		if(parent != null) {
			this.shape.x = x - parent.getGlobalX();
			this.shape.y = y - parent.getGlobalY();
		}else {
			this.shape.x = x;
			this.shape.y = y;
		}
		updateGlobalPosition();
	}




	public boolean containsGlobal(int mouse_x, int mouse_y) {
		return shape_global.contains(mouse_x, mouse_y);
	}
	
	
	public float getGlobalX() {
		return shape_global.x;
	}
	
	public float getGlobalY() {
		return shape_global.y;
	}
	
	public void setColorDefault(Color c) {
		this.color_default = new Color(c.r, c.g, c.b, c.a);
	}
	
	public void setColorTrim(Color c) {
		this.color_trim = new Color(c.r, c.g, c.b, c.a);
	}

	public void setColor(Color c) {
		this.color = c;
	}
	
	/**
	 * This
	 * @param renderer The ShapeRenderer which has already utilized .begin()
	 */
	public void drawShape(ShapeRenderer renderer) {
		if(!visible){
			return;
		}
		renderer.setColor(color);
		renderer.set(ShapeType.Line);
		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
		if(manager.edit_mode) {
			
			//renderer.rect(shape_edit_handle.x, shape_edit_handle.y, shape_edit_handle.width, shape_edit_handle.height);
		}
		//renderer.rect(shape.x, shape.y, shape.width, shape.height);
		drawChildren(renderer);
	}
	
	public void drawEditMode(ShapeRenderer renderer) {
		renderer.setColor(color_edit);
		renderer.set(ShapeRenderer.ShapeType.Filled);

	    // Top and bottom edges
		int tick = manager.ui_tick/2;
		int dotSpacing = 4;
		int offset = tick % dotSpacing;
		int dotRadius = 1;
	    for (float i = getGlobalX() + offset; i <= getGlobalX() + shape.width; i += dotSpacing) {
	    	renderer.circle(i, getGlobalY() + shape.height, dotRadius); // Top
	    }
	    for (float i = getGlobalX() + 2 - offset; i <= getGlobalX() + shape.width; i += dotSpacing) {
	    	renderer.circle(i, getGlobalY(), dotRadius); // Bottom
	    }

	    // Left and right edges
	    for (float j = getGlobalY() + offset; j <= getGlobalY() + shape.height; j += dotSpacing) {
	    	renderer.circle(getGlobalX(), j, dotRadius); // Left
	    }
	    for (float j = getGlobalY() + 2 - offset; j <= getGlobalY() + shape.height; j += dotSpacing) {
	    	renderer.circle(getGlobalX() + shape.width, j, dotRadius); // Right
	    }
	}
	
	protected void drawChildren(ShapeRenderer renderer) {
		for(Widget w : widgets) {
			w.drawShape(renderer);
		}
	}

	public boolean isHoverable() {
		return false;
	}
	
	public boolean isVisible() {
		return visible;
	}

	public void drawFont(SpriteBatch sprite_batch, BitmapFont font) {
		
		if(!visible){
			return;
		}
		font.draw(sprite_batch, name, getGlobalX(), getGlobalY());
	}
	
	public void drawTexture(SpriteBatch batch) {
		batch.setColor(Color.WHITE);
		batch.draw(texture, getGlobalX(), getGlobalY(), shape.width-1, shape.height-1);
	}
	
	
	public int getZIndex() {
		return z;
	}
	
}
