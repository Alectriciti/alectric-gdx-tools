package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;
import static com.alectriciti.gdx.UIManager.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 * 
 */
public class Widget implements Contextable, Drawable{
	
	//Class stuff for Serialization
	public String type = "widget";
	
	transient protected UIManager manager;
	
	protected transient Widget parent;
	public boolean follow_parent = true;
	
	//for serializations
	public String id;
	public String group;
	protected boolean serializable = true;
	
	public Style style = UIManager.getDefaultStyle();
	
	public String getId() {
		return id;
	}
	
	public String name_for_display; //The display name
	public boolean render_text = false;

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
	
	public transient boolean hovering = false;
	public transient boolean pressing = false;
	
	transient private boolean currently_clicked = false;
	
	transient boolean animating; // this will run on update() if relevant
	
	transient protected Color color_texture_alpha = new Color(1,1,1,1);
	
	transient protected Texture texture;
	protected FileHandle texture_file;	
	
	public void setTexture(FileHandle fileHandle) {
		this.texture_file = fileHandle;
		this.texture = new Texture(texture_file);
	}
	
	public void setText(String text_for_display) {
		this.name_for_display = text_for_display;
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
	 * 
	 */
	
	/**
	 * With WidgetManager, Widgets are required to be visible in order to render or be interacted with
	public boolean visible = true;
	
	we're going to change that
	 *
	 *
	 */
	
	/**
	 * Holds
	 */
	EnumMap<Parameter, Value> parameters;
	public InheritanceRule visibility_inheritance;
	
	
	/*
	 * ignores grabbing the object, can be used for intermediate actions
	 * such as DropdownMenuButton when a widget retracts. It's still visible, but not touchabale.
	 */
	public boolean touchable = true;
	
	
	/**
	 * The color which is drawn to be active at all times
	 */
	public Color color = style.color_base;
	public Color font_color = style.color_text;
	public Color color_outline = style.color_outline;
	private float opacity = 1;
	
	public boolean focused;
	
	public boolean isFocused() {
		return focused;
	}
	
	/**
	 * Reserved for Canvas
	 */
	protected Widget() {
		//Must create some kind of registry!
		initializeParameters();
		}
	
	/**
	 * Used for other widgets
	 * @param id name of the widget
	 * @param canvas name of the container to apply it to
	 */
	public Widget(String id, Widget parent) {
		initializeParameters();
		this.id = id;
		this.name_for_display = id;
		this.shape = new Rectangle();
		if(parent != null) {
			this.manager = parent.manager;
			this.attachToWidget(parent); //parent first!
			this.manager.registerWidget(this); // do this last, it might make an orphan
		}else {
			printError("Error instantiating "+id+" ... Canvas is NULL. Register with a WidgetManager instead");
		}
		setSize(32, 32);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	public Widget(String id, UIManager manager) {
		initializeParameters();
		this.id = id;
		this.name_for_display = id;
		this.manager = manager;
		this.manager.registerWidget(this);
		this.shape = new Rectangle();
		setSize(32, 32);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	/**
	 * A barebones constructor which registers
	 * @param manager
	 */
	public Widget(UIManager manager) {
		initializeParameters();
		this.manager = manager;
		this.manager.registerWidget(this);
		this.shape = new Rectangle();
		//pushNewZPosition(false);
	}
	
	private void initializeParameters() {
		// TODO Auto-generated method stub
		parameters = new EnumMap<>(Parameter.class);
		for(Parameter p : Parameter.values()) {
			parameters.put(p, Value.UNASSIGNED);
		}
	}

	public String getName() {
		return name_for_display;
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
			//making this independant
			manager.widget_independants.add(this);
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
	
	public void setSizeToFont() {
		
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
		if(parent!=null && follow_parent) {
			shape_global.x = parent.getGlobalX() + shape.x;
			shape_global.y = parent.getGlobalY() + shape.y;
		}else {
			
			shape_global.x = shape.x;
			shape_global.y = shape.y;
		}
		for(Widget w : widgets) {
			w.updateGlobalPosition();
		}
		onPositionUpdate();
	}
	
	
	/**
	 * This is for implementation to update various properties when this widget moves
	 */
	protected void onPositionUpdate() {
		
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
		
	protected void setFollowParent(boolean b) {
		this.follow_parent = b;
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
			font_color.a = opacity;
		}
	}
	
	
	public void drawEditMode(ShapeRenderer renderer, boolean recursive) {
		if(editable) {
			renderer.setColor(style.color_edit);
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
	
	
	public boolean drawFont(SpriteBatch sprite_batch, boolean recursive) {
		
		if(!isVisible()){
			return false;
		}
		
		if(render_text && name_for_display != null) {
		//print(getGlobalX()+" "+getGlobalY());
			style.font.setColor(font_color);
			style.font.draw(sprite_batch, getTextToRender(), getGlobalX()+font_offset.x, getGlobalY()+style.font.getCapHeight()+font_offset.y);
		}
		if(recursive) {
			drawFontChildren(sprite_batch, recursive);
		}
		return true;
	}
	
	
	protected String getTextToRender() {
		// TODO Auto-generated method stub
		return name_for_display;
	}
	
	/**
	 * 
	 * @param sprite_batch A clean {@link SpriteBatch} which has already started .begin()
	 * @param b 
	 * @return whether or not the texture was able to draw
	 */
	public boolean drawTexture(SpriteBatch sprite_batch, boolean recursive) {
		boolean valid = texture!=null;
		if(isVisible()) {
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

	public void drawBorder(ShapeRenderer shape_renderer) {
		shape_renderer.set(ShapeType.Line);
		shape_renderer.setColor(color_outline);
		drawRectRound(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height, style.corner_radius);
	}

	@Override
	public void drawShape(ShapeRenderer renderer) {
		if(isVisible()) {
			if(hovering) {
				color_outline = LerpColor(color_outline, style.color_hover, style.color_fade_in);
			}else {
				color_outline = LerpColor(color_outline, style.color_outline, style.color_fade_out);
			}
		}
	}
	
	protected void drawShapeChildren(ShapeRenderer renderer, boolean recursive) {
		for(Widget w : widgets) {
			w.drawShape(renderer);
		}
	}
	
	protected void drawTextureChildren(SpriteBatch sprite_batch, boolean recursive) {
		for(Widget w : widgets) {
			w.drawTexture(sprite_batch, recursive);
		}
	}
	
	protected void drawFontChildren(SpriteBatch sprite_batch, boolean recursive) {
		for(Widget w : widgets) {
			w.drawFont(sprite_batch, recursive);
		}
	}
	
	
	/**
	 * If the object shows a highlight around it when hovereds
	 * @return
	 */
	public boolean isHoverable() {
		return false;
	}
	
	public boolean isVisible() {
		return getValue(Parameter.VISIBLE).get();
	}
	
	/**
	 * If the object is currently interactable
	 * @return
	 */
	public boolean isTouchable() {
		if(shape==null)return false;
		return touchable;
	}
	
	
	public int getZIndex() {
		return z;
	}
	
	public void focus() {
		manager.focus(this, true);
	}
	
	public void unfocus(Widget new_focus) {
		manager.unfocus(this);
	}
	
	public boolean isRelated(Widget relative) {
		if(relative == null) return false;
		if(relative == this) return true;
		if(this.getParent() == relative) {
			return true;
		}
		if(relative.getParent() == this) {
			return true;
		}
		return false;
	}
	
	public boolean isDescendantOf(Widget ancestor) {
//		if(ancestor==null)return false;
//		if(getParent()==null)return false;
		Widget w = this;
		while(w != null) {
			if(w.getParent()==ancestor) {
				return true;
			}
			w = w.getParent();
		}
		return false;
	}
	
	protected void pushNewZPosition(boolean recursive) {
		z = manager.global_canvas_z;
		manager.global_canvas_z++;
		if(recursive) {
			for(Widget w : widgets) {
				w.pushNewZPosition(recursive);
			}
		}
	}
	
	
	/**
	 * Calls when this widget is focused
	 */
	final void callOnFocus() {
		OnFocus();
	}
	
	/**
	 * Calls when this widget is clicked on, then dispatches
	 */
	final void callOnClicked() {
		currently_clicked = true;
		OnMouseClicked();
	}
	
	/*
	 * Calls when this widget is released with mouse, then dispatches
	 */
	final void callOnReleased() {
		currently_clicked = false;
		OnMouseReleased();
	}
	
	final void callOnCreate() {
		OnCreate();
	}

	protected void OnCreate() {
		// TODO Auto-generated method stub
		
	}
	
	protected void OnMouseClicked() {
		// TODO Auto-generated method stub
	}
	
	protected void OnMouseReleased() {
		// TODO Auto-generated method stub
	}
	
	protected void OnFocus() {
		// TODO Auto-generated method stub
	}
	
	public boolean hasParent() {
		return parent!=null;
	}
	
	public boolean isParent() {
		return !widgets.isEmpty();
	}
	
	
	/**
	 * Pointer input APIs — default implementations do nothing.
	 * Return true if the widget handled/consumed the event (capture it), false otherwise.
	 * UIManager will call these with global mouse coordinates.
	 */
	public boolean onPointerDown(int globalX, int globalY, int pointer, int button) {
	    return false;
	}

	public boolean onPointerDragged(int globalX, int globalY, int pointer) {
	    return false;
	}

	public boolean onPointerUp(int globalX, int globalY, int pointer, int button) {
	    return false;
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
	
	public void setVisible(boolean new_visible) {
		setValue(Parameter.VISIBLE, new_visible, InheritanceRule.STANDARD);
	}
	
	public void setVisible(boolean new_visible, InheritanceRule rule_override) {
		setValue(Parameter.VISIBLE, new_visible, rule_override);
	}
	
	public void setTouchable(boolean new_touchable) {
		setValue(Parameter.TOUCHABLE, new_touchable, InheritanceRule.STANDARD);
	}
	
	/**
	 * Sets a parameterized value for this widget.
	 * 
	 * All widgets values are "UNASSIGNED" by default unless otherwise specified. This effectively makes them capable of working with the widget inheritance system
	 * 
	 * This can be stated with:
	 * @param rule_override which mode to apply this change to this widget.
	 * @param parameter the parameter which we will change
	 * @param value the value to set it to 
	 */
	public void setValue(Parameter parameter, Value value, InheritanceRule rule_override) {
		switch(rule_override) {
		case RECURSIVE:
			// 📝 Applies the Value to itself (1/2) ✏️
			applyValue(parameter, value);
			for(Widget w : widgets) {
				// 📝 Applies the Value to it's child (2/2) ✏️
				w.setValue(parameter, value, rule_override);
			}
			break;
		case LOYAL: // this will only set the value IF the parent also has it's value set.
			if(parent!=null) {
				Value parents_value = parent.getValue(parameter);
				if(parents_value!=Value.UNASSIGNED) {
					// 📝 Applies the Value to itself (1/1) ✏️
					applyValue(parameter, parents_value);
				}
			} else {
//			visible = new_visible;
			}
//			break;
		case STANDARD:
			// 📝 Applies the Value to itself (1/1) ✏️
			applyValue(parameter, value);
			break;
		default:
			break;
		
		}
	}
	
	/**
	 * Variation methods...
	 */
	
	public void setValue(Parameter parameter, boolean value, InheritanceRule rule_override) {
		this.setValue(parameter, Value.of(value), rule_override);
	}

	public void setValue(Parameter parameter, boolean value) {
		this.setValue(parameter, Value.of(value), InheritanceRule.STANDARD);
	}

	public void setValue(Parameter parameter, Value value) {
		this.setValue(parameter, value, InheritanceRule.STANDARD);
	}
	
	
	/**
	 * Actually applies the value for a parameter. Internal use only (see setValue)
	 */
	private void applyValue(Parameter parameter, Value value) {
		parameters.put(parameter, value);
	}
	
	
	
	public Value getValue(Parameter parameter) {
		return parameters.get(parameter);
	}

	public boolean isEditable() {
		// TODO Auto-generated method stub
		return editable;
	}
	
	public String toString() {
		return name_for_display;
	}


	public void autoAssignId() {
		
		
		//TODO
		//make it us a set or list, and use "cotains(proposed_id)"
		if(id == null) {
			//ensure the new id hasn't been used
			if(name_for_display!=null) {
				boolean free_to_use = true;
				for(Widget w : manager.widgets) {
					if(w == this) {
						continue;
					}
					if(id!=null && w.id.equalsIgnoreCase(this.name_for_display)) {
						free_to_use = false;
						return;
					}
				}
				if(free_to_use) {
					id = name_for_display;
					
				}
			}
		}
	}
	
	
	public JsonValue saveToJson() {
	    JsonValue out = new JsonValue(JsonValue.ValueType.object);
	    out.addChild("type", new JsonValue("widget"));
	    out.addChild("name", new JsonValue(name_for_display));
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
	    this.name_for_display = data.getString("name", name_for_display);
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
	
	public Rectangle getShape() {
		return shape;
	}

	
	/**
	 * An implementable scroll function for when this widget is focused
	 */
	public void scroll(float amountX, float amountY) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Default selection region for a widget.
	 *
	 * By default this returns the widget's visible bounds (shape).
	 * Override this in widgets that need custom hit regions (Slider knob, color pickers, etc).
	 */
	public Rectangle getSelectionRegion() {
	    // If your Widget class already has a Rectangle field called `shape`, prefer that:
	    try {
	        if (this.shape != null) {
	            // shape stores local x,y (maybe) — we want global coordinates
	            return new Rectangle(getGlobalX(), getGlobalY(), shape.width, shape.height);
	        }
	    } catch (Throwable t) {
	        // ignore if `shape` doesn't exist in your actual Widget class (fallback below)
	    }

	    // Fallback: use getGlobalX/Y with width/height accessors (modify if your API differs)
	    float gx = getGlobalX();
	    float gy = getGlobalY();
	    float w = getWidth();   // replace with your width accessor if different
	    float h = getHeight();  // replace with your height accessor if different
	    return new Rectangle(gx, gy, w, h);
	}

	
	/**
	 * Displays the context Widget associated with this widget
	 * @return
	 */
	public ContextWidget spawnContextWidget() {
		// TODO Auto-generated method stub
		return null;
	}


	public boolean doesSerialize() {
		return serializable;
	}

	@Override
	public boolean isHovered() {
		return hovering;
	}

	@Override
	public boolean isPressed() {
		return pressing;
	}

	public boolean isInSameGroup(Widget w) {
		if(w == null) return false;
		if(group==null || w.group==null) {
			print("f yeah b");
			return false;
		}
		if(group.equals(w.group)) return true;
		return false;
		
	}
	
	
}
