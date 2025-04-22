package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;
import static com.alectriciti.gdx.UIManager.*;

import java.util.ArrayList;
import java.util.HashSet;
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
	


	public HashSet<Widget> widgets = new HashSet<Widget>();
	public HashSet<Button> buttons = new HashSet<Button>();

	List<Widget> widgets_heirarchy_cache = new ArrayList<Widget>();
	
	protected UIManager manager;
	
	public String name;
	protected Rectangle shape;
	protected Rectangle shape_global = new Rectangle();
	
	public int z; //for z ordering
	
	public boolean editable = true;
	
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
	public Widget(String name, Widget w) {
		this.name = name;
		this.shape = new Rectangle();
		if(w != null) {
			this.manager = w.manager;
			this.attachToWidget(w); //parent first!
			this.manager.registerWidget(this); // do this last, it might make an orphan
		}else {
			printError("Error instantiating "+name+" ... Canvas is NULL. Register with a WidgetManager instead");
		}
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	public Widget(String name, int pos_x, int pos_y, int width, int height, Widget w) {
		this(name, w);
		this.shape = new Rectangle(pos_x, pos_y, width, height);
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	public Widget(String name, UIManager manager) {
		this.name = name;
		this.manager = manager;
		this.manager.registerWidget(this);
		this.shape = new Rectangle();
		setSize(32, 32);
		updateGlobalPosition();
	}
	
	
	public String getName() {
		return name;
	}
	
	
	/*
	 * 
	 * Parents and Children 
	 * 
	 * 
	 */
	
	public Widget getParent() {
		return parent;
	}
	
	public boolean hasAncestor(Widget maybeAncestor) {
	    Widget scanning_parent = this.getParent();
	    while (scanning_parent != null) {
	        if (scanning_parent == maybeAncestor) {
	        	return true;
	        }
	        scanning_parent = scanning_parent.getParent();
	    }
	    return false;
	}
	
	public boolean attachToWidget(Widget new_widget_parent) {
		if(new_widget_parent != null) {
			if(new_widget_parent.hasAncestor(this)) {
				printWarning("Tried adding widget ["+getName()+"] to ["+new_widget_parent.getName()+"], but it was an ancestor!");
				return false;
			}
			this.parent = new_widget_parent;
			new_widget_parent.attachChildWidget(this);
			refreshHeirarchyCache();
			return true;
		}else {
			//making this an orphan
			manager.widget_orphans.add(this);
			return true;
		}
	}

	/**
	 * Attaches and registers the widget to this widget
	 * The idea is that this is called at two places:
	 * 1. When the widget is initalized
	 * 2. When a widget switches canvases
	 */
	protected void attachChildWidget(Widget widget_to_attach) {
		
		//Check if the widget already has a parent
		if(widget_to_attach.parent != null) {
			widget_to_attach.parent.detachWidget(widget_to_attach, false);
		}
		widget_to_attach.parent = this;
		widgets.add(widget_to_attach);

		refreshHeirarchyUpward();
		
		if(widget_to_attach instanceof Button) {
			Button b = (Button)widget_to_attach;
			buttons.add(b);
		}
	}
	
	/**
	 * Detaches
	 * @param widget_to_remove
	 */
	public void detachWidget(Widget widget_to_remove, boolean update_cache) {
		widgets.remove(widget_to_remove);
		if(update_cache) {
			refreshHeirarchyUpward();
		}
		if(widget_to_remove instanceof Button) {
			Button b = (Button) widget_to_remove;
			buttons.remove(b);
		}
	}
	
	private void refreshHeirarchyCache() {
		widgets_heirarchy_cache.clear();
		for(Widget child : widgets) {
			widgets_heirarchy_cache.add(child); // add the child directly
			child.refreshHeirarchyCache(); // update child's cache
			widgets_heirarchy_cache.addAll(child.getAllChildren()); //add all descendants
		}
	}
	
	private void refreshHeirarchyUpward() {
		refreshHeirarchyCache();
		if(parent!=null) {
			parent.refreshHeirarchyUpward();
		}
	}

	public List<Widget> getAllChildren() {
		return widgets_heirarchy_cache;
	}
	
	
	
	
	
	
	
	
	
	
	
	public void setSize(float width, float height) {
		this.shape.setWidth(width);
		this.shape.setHeight(height);
		this.shape_global.setWidth(width);
		this.shape_global.setHeight(height);
	}

	public float getWidth() {
		return this.shape.getWidth();
	}
	
	public float getHeight() {
		return this.shape.getHeight();
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
	 * Logic, only to be ran once per frame
	 */
	protected void update() {
		// TODO Auto-generated method stub
		
	}
	
	/**
	 * This
	 * @param renderer The ShapeRenderer which has already utilized .begin()
	 * @param recursive 
	 */
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		if(!visible){
			return;
		}
		renderer.setColor(color);
		renderer.set(ShapeType.Line);
		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
		//renderer.rect(shape.x, shape.y, shape.width, shape.height);
		if(recursive) {
			drawShapeChildren(renderer, recursive);
		}
	}
	
	public void drawEditMode(ShapeRenderer renderer, boolean recursive) {
		if(editable) {
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
	    if(recursive) {
	    	drawEditModeChildren(renderer, recursive);
	    }
	}
	
	
	public boolean drawFont(SpriteBatch sprite_batch, BitmapFont font, boolean recursive) {
		
		if(!visible){
			return false;
		}
		
		
		font.setColor(Color.WHITE);
		font.draw(sprite_batch, name, getGlobalX(), getGlobalY());
		if(recursive) {
			drawFontChildren(sprite_batch, font, recursive);
		}
		return true;
	}

	/**
	 * 
	 * @param sprite_batch A clean {@link SpriteBatch} which has already started .begin()
	 * @return whether or not the texture was able to draw
	 */
	public boolean drawTexture(SpriteBatch sprite_batch) {
		if(texture==null) {
			return false;
		}
		sprite_batch.setColor(Color.WHITE);
		sprite_batch.draw(texture, getGlobalX(), getGlobalY(), shape.width-1, shape.height-1);
		return true;
	}
	
	protected void drawEditModeChildren(ShapeRenderer renderer, boolean recursive) {
		for(Widget w : widgets) {
			w.drawEditMode(renderer, recursive);
		}
	}
	
	protected void drawShapeChildren(ShapeRenderer renderer, boolean recursive) {
		for(Widget w : widgets) {
			w.drawShape(renderer, recursive);
		}
	}
	
	protected void drawFontChildren(SpriteBatch sprite_batch, BitmapFont font, boolean recursive) {
		for(Widget w : widgets) {
			w.drawFont(sprite_batch, font, recursive);
		}
	}

	public boolean isHoverable() {
		return false;
	}
	
	public boolean isVisible() {
		return visible;
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

	

	public void dispose() {
		if(texture!=null) {
			texture.dispose();
		}
	}

	public void setVisible(boolean b) {
		visible = b;
	}

	public boolean isEditable() {
		// TODO Auto-generated method stub
		return editable;
	}
	
	public String toString() {
		return name;
	}
	
}
