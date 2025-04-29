package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;
import static com.alectriciti.gdx.UIManager.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;

/**
 * Widgets are the building blocks of Alectric UI which can be updated/rendered manually, or using WidgetManager
 * @author alectriciti
 */
public class Widget {
	
	
	transient protected UIManager manager;
	

	public String type = "widget";
	
	public boolean render_text = true;
	
	
	public String name;
	
	protected transient Widget parent;
	
	
	//for serializations
	public String id;
	
	public String getId() {
		return id;
	}
	


	public transient LinkedList<Widget> widgets = new LinkedList<Widget>();

	
	//A constantly refreshing cache for quickly determining grandchildren
	transient List<Widget> widgets_heirarchy_cache = new ArrayList<Widget>();
	
	
	protected Rectangle shape;
	protected Rectangle shape_base = new Rectangle(); // for UI offset
	protected Rectangle shape_global = new Rectangle();
	public Point font_offset = new Point(0, 0);
	
	public Direction alignment = Direction.NONE;
	
	public boolean editable = true;

	
	public transient int z; //for z ordering
	public int z_layer_offset = 0; //a more static z offset. This gets added to the standard z value when applying new z positions
	
	transient boolean hovering = false;
	transient private boolean currently_clicked = false;
	
	transient boolean animating; // this will run on update() if relevant

	transient protected Color color_texture_alpha = new Color(1,1,1,1);
	
	transient protected Texture texture;
	protected FileHandle texture_file;
	
	
	public void setTexture(FileHandle fileHandle) {
		this.texture_file = fileHandle;
		this.texture = new Texture(texture_file);
	}
	
	
	/**
	 * Loading from serialization
	 * @return
	 */
	public void reloadAllData() {
		if(texture_file!=null && texture_file.exists())
			texture = new Texture(texture_file);
		updateGlobalPosition();
	}
	
	/**
	 * With WidgetManager, Widgets are required to be visible in order to render or be interacted with
	 */
	public boolean visible = true;
	
	
	/*
	 * ignores grabbing the object, can be used for intermediate actions
	 * such as DropdownMenuButton when a widget retracts. It's still visible, but not touchabale.
	 */
	public boolean touchable = true;
	
	
	public Color color_default = new Color(0, 0, 0, 1);
	public Color color_edit = new Color(1, 0.25f, 0.25f, 1);
	public Color color_trim = new Color(0.125f, 0.125f, 0.125f, 1);
	public Color color_trim_highlight = new Color(0.8f, 0.8f, 0.8f, 1);
	public Color font_color = Color.WHITE.cpy();
	
	/**
	 * The color which is drawn to be active at all times
	 */
	public Color color = new Color(0, 0, 0, 1);
	private float opacity = 1;

	
	/**
	 * Reserved for Canvas
	 */
	protected Widget() {
		//Must create some kind of registry!
		}
	
	/**
	 * Used for other widgets
	 * @param name name of the widget
	 * @param canvas name of the container to apply it to
	 */
	public Widget(String name, Widget parent) {
		this.name = name;
		this.shape = new Rectangle();
		if(parent != null) {
			this.manager = parent.manager;
			this.attachToWidget(parent); //parent first!
			this.manager.registerWidget(this); // do this last, it might make an orphan
		}else {
			printError("Error instantiating "+name+" ... Canvas is NULL. Register with a WidgetManager instead");
		}
		setSize(32, 32);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	public Widget(String name, UIManager manager) {
		this.name = name;
		this.manager = manager;
		this.manager.registerWidget(this);
		this.shape = new Rectangle();
		setSize(32, 32);
		updateGlobalPosition();
		pushNewZPosition(false);
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
		this.shape_base.setWidth(width);
		this.shape_base.setHeight(height);
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
		this.shape_base.x = x;
		this.shape_base.y = y;
		this.shape.x = x;
		this.shape.y = y;
		updateAlignment();
		updateGlobalPosition();
		return this;
	}
	
	/**
	 * Run this ONLY after a position change
	 */
	public void updateAlignment() {
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();

		shape.x = shape_base.x;
		shape.y = shape_base.y;

		boolean align_top = false,  align_right = false;
		switch(alignment) {
		case DOWN_RIGHT:
			align_right = true;
			break;
		case RIGHT:
			align_right = true;
			break;
		case UP:
			align_top = true;
			break;
		case UP_LEFT:
			align_top = true;
			break;
		case UP_RIGHT:
			align_right = true;
			align_top = true;
			break;
		default:
			break;
		}
		if(align_top) {
			shape.y = height + shape_base.y - shape.height;
		}
		if(align_right) {
			shape.x = width + shape_base.x - shape.width;
		}
	}


	public void setRelativeX(float x) {
		// TODO Auto-generated method stub
		this.shape_base.x = x;
		updateAlignment();
		updateGlobalPosition();
	}
	
	public void setRelativeY(float y) {
		// TODO Auto-generated method stub
		this.shape_base.y = y;
		updateAlignment();
		updateGlobalPosition();
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
		int width = Gdx.graphics.getWidth();
		int height = Gdx.graphics.getHeight();
		boolean align_top = false,  align_right = false;
		switch(alignment) {
		case DOWN_RIGHT:
			align_right = true;
			break;
		case RIGHT:
			align_right = true;
			break;
		case UP:
			align_top = true;
			break;
		case UP_LEFT:
			align_top = true;
			break;
		case UP_RIGHT:
			align_right = true;
			align_top = true;
			break;
		default:
			break;
		}
		
		
	    if (parent != null) {
	        shape_base.x = x - parent.shape_global.x;
	        shape_base.y = y - parent.shape_global.y;
	        
	        if(align_top) {
	            shape_base.y = (y - parent.shape_global.y) - (height - shape.height);
	        }else {
	            shape_base.y = y - parent.shape_global.y;
	        }
	        if(align_right) {
	            shape_base.x = (x - parent.shape_global.x) - (width - shape.width);
	        }else {
	            shape_base.x = x - parent.shape_global.x;
	        }
	    } else {
	        shape_base.x = x;
	        shape_base.y = y;

	        if(align_top) {
	            shape_base.y = y - (height - shape.height);
	        } else {
	            shape_base.y = y;
	        }

	        if(align_right) {
	            shape_base.x = x - (width - shape.width);
	        } else {
	            shape_base.x = x;
	        }
	    }
	    
	    updateAlignment();
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
		this.color_default = new Color(c.r, c.g, c.b, opacity);
	}
	
	public void setColorTrim(Color c) {
		this.color_trim = new Color(c.r, c.g, c.b, opacity);
	}

	public void setColor(Color c) {
		this.color = c;
		c.a = opacity;
	}
	
	/**
	 * Logic, only to be ran once per frame
	 */
	protected void update() {
		// TODO Auto-generated method stub
		
		if(animating) {
			color.a = opacity;
			color_default.a = opacity;
			font_color.a = opacity;
		}
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
		if(hovering) {
			if(manager.edit_mode) {
				drawEditMode(renderer, recursive);
			}else {
				renderer.setColor(color);
				renderer.set(ShapeType.Line);
				renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
			}
		}
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
		
		if(render_text) {
		//print(getGlobalX()+" "+getGlobalY());
			font.setColor(font_color);
			font.draw(sprite_batch, name, getGlobalX()+font_offset.x, getGlobalY()+font.getCapHeight()+font_offset.y);
		}
		if(recursive) {
			drawFontChildren(sprite_batch, font, recursive);
		}
		return true;
	}

	/**
	 * 
	 * @param sprite_batch A clean {@link SpriteBatch} which has already started .begin()
	 * @param b 
	 * @return whether or not the texture was able to draw
	 */
	public boolean drawTexture(SpriteBatch sprite_batch, boolean recursive) {
		boolean valid = texture!=null;
		if(visible) {
			if(valid) {
				sprite_batch.setColor(color_texture_alpha);
				sprite_batch.draw(texture, getGlobalX(), getGlobalY(), shape.width-1, shape.height-1);
			}
		}
		if(recursive) {
			drawTextureChildren(sprite_batch, recursive);
		}
		return valid;
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
	
	protected void drawTextureChildren(SpriteBatch sprite_batch, boolean recursive) {
		for(Widget w : widgets) {
			w.drawTexture(sprite_batch, recursive);
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
	
	public boolean isTouchable() {
		return touchable;
	}
	
	
	public int getZIndex() {
		return z;
	}
	
	
	
	public void pushNewZPosition(boolean recursive) {
		z = manager.global_canvas_z;
		manager.global_canvas_z++;
		if(recursive) {
			for(Widget w : widgets) {
				w.pushNewZPosition(recursive);
			}
		}
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
	
	public void setOpacity(float alpha) {
		this.opacity  = alpha;
		this.color.a = alpha;
		this.color.a = alpha;
	}

	

	public void dispose() {
		if(texture!=null) {
			texture.dispose();
		}
	}

	public void setVisible(boolean b, boolean recursive) {
		visible = b;
		if(recursive) {
			for(Widget w : widgets) {
				w.setVisible(b, recursive);
			}
		}
	}

	public void setTouchable(boolean b, boolean recursive) {
		touchable = b;
		if(recursive) {
			for(Widget w : widgets) {
				w.setTouchable(b, recursive);
			}
		}
	}

	public boolean isEditable() {
		// TODO Auto-generated method stub
		return editable;
	}
	
	public String toString() {
		return name;
	}


	public void autoAssignId() {
		
		
		//TODO
		//make it us a set or list, and use "cotains(proposed_id)"
		if(id == null) {
			//ensure the new id hasn't been used
			if(name!=null) {
				boolean free_to_use = true;
				for(Widget w : manager.widgets) {
					if(w == this) {
						continue;
					}
					if(id!=null && w.id.equalsIgnoreCase(this.name)) {
						free_to_use = false;
						return;
					}
				}
				if(free_to_use) {
					id = name;
					
				}
			}
		}
	}
	
	
	public JsonValue saveToJson() {
	    JsonValue out = new JsonValue(JsonValue.ValueType.object);
	    out.addChild("type", new JsonValue("widget"));
	    out.addChild("name", new JsonValue(name));
	    out.addChild("id", new JsonValue(id));
	    if(texture_file!=null)
	    out.addChild("texture", new JsonValue(texture_file.path()));
	    if (shape != null) {
	        JsonValue shapeObj = new JsonValue(JsonValue.ValueType.object);
	        shapeObj.addChild("x", new JsonValue(shape_base.x));
	        shapeObj.addChild("y", new JsonValue(shape_base.y));
	        shapeObj.addChild("width", new JsonValue(shape.width));
	        shapeObj.addChild("height", new JsonValue(shape.height));
	        out.addChild("shape", shapeObj);
	    }
	    return out;
	}

	public void loadFromJson(JsonValue data) {
	    this.name = data.getString("name", name);
	    this.id = data.getString("id", id);
	    
		if (data.has("texture")) {
			this.texture_file = new FileHandle(data.getString("texture"));
			print("widget texture loaded: " + texture_file);
	    }
	    
	    JsonValue shapeObj = data.get("shape");
	    if (shapeObj != null) {
	        setRelativeX(shapeObj.getFloat("x", shape.x));
	        setRelativeY(shapeObj.getFloat("y", shape.y));
	        this.shape.width = shapeObj.getFloat("width", shape.width);
	        this.shape.height = shapeObj.getFloat("height", shape.height);
	    }
	}


	public boolean isAlwaysEditable() {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void destroy() {
		manager.markForDestruction(this);
	}
	
	
}
