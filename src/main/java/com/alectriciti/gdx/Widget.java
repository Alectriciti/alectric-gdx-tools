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
 * @author alectriciti
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
	private boolean currently_clicked = false;
	
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
			this.manager.registerWidget(this);
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
		this.manager.registerWidget(this);
		this.shape = new Rectangle();
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

	public boolean drawFont(SpriteBatch sprite_batch, BitmapFont font) {
		
		if(!visible){
			return false;
		}
		font.draw(sprite_batch, name, getGlobalX(), getGlobalY());
		return true;
	}
	
	/**
	 * 
	 * @param sprite_batch A clean {@link SpriteBatch} which has already started .begin()
	 * @return whether or not the texture was able to draw {@code
	 * 
	 * }
	 */
	public boolean drawTexture(SpriteBatch sprite_batch) {
		if(texture==null) {
			return false;
		}
		sprite_batch.setColor(Color.WHITE);
		sprite_batch.draw(texture, getGlobalX(), getGlobalY(), shape.width-1, shape.height-1);
		return true;
	}
	
	
	public int getZIndex() {
		return z;
	}

	final void callOnClicked() {
		currently_clicked = true;
		OnMouseClicked();
	}

	final void callOnReleased() {
		currently_clicked = false;
		OnMouseReleased();
	}

	protected void OnMouseClicked() {
		// TODO Auto-generated method stub
	}

	protected void OnMouseReleased() {
		// TODO Auto-generated method stub
	}
	
	public boolean getCurrentlyClicked() {
		return currently_clicked;
	}
	
	


	/**
	 * Registers the widget to this canvas.
	 * The idea is that this is called at two places:
	 * 1. When the widget is initalized
	 * 2. When a widget switches canvases
	 */
	void registerWidget(Widget widget) {
		
		//Check if the widget already has a parent
		if(widget.parent != null) {
			widget.parent.unregisterWidget(widget);
		}
		widgets.add(widget);
		widget.parent = this;
		
		if(widget instanceof Button) {
			Button b = (Button)widget;
			buttons.add(b);
			manager.buttons.add(b);
			manager.buttons_by_name.put(b.name, b);
			manager.buttons_by_key.put(b.key, b);
		}
	}

	public void unregisterWidget(Widget widget_to_remove) {
		widgets.remove(widget_to_remove);
		if(widget_to_remove instanceof Button) {
			Button b = (Button) widget_to_remove;
			buttons.remove(b);
		}
	}
	
}
