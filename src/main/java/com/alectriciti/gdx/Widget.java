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

	protected static int DEFAULT_WIDGET_SIZE = 32;
	protected static int DEFAULT_SLIDER_SIZE = 64;
	
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
		if(id==null)return getClass().getSimpleName();
		return id;
	}
	
	public String name_for_display; //The display name
	public boolean show_text = false;

	public transient LinkedList<Widget> widgets_children = new LinkedList<Widget>();
	
	//A constantly refreshing cache for quickly determining grandchildren
	transient List<Widget> widgets_heirarchy_cache = new ArrayList<Widget>();
	
	protected Rectangle shape;
	protected Rectangle shape_base = new Rectangle(); // source of truth
	protected Rectangle shape_global = new Rectangle(); // literal position
	public transient float clamp_offset_x = 0;
	public transient float clamp_offset_y = 0;
	
	public Point font_offset = new Point(0, 0);
	
	public Direction alignment = Direction.NONE;
	
	public boolean editable = true;
	
	public transient int z; //for z ordering
	public int z_layer_offset = 0; //a more static z offset. This gets added to the standard z value when applying new z positions
	
	public transient boolean hovering = false;
	public transient boolean pressing = false;
	public transient boolean locked = false;
	
	transient private boolean currently_clicked = false;
	
	transient boolean animating; // this will run on update() if relevant
	
	transient protected Color color_texture_alpha = new Color(1,1,1,1);
	
	transient protected Texture texture;
	protected FileHandle texture_file;	

	transient List<Runnable> run_on_click = new ArrayList<Runnable>();
	
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
	 * such as DropdownMenuButton when a widget retracts. It's still visible, but not touchable.
	 */
//	public boolean touchable = true;
	
	
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
		//Must create some kind of registry
		initializeParameters();
	}
	
	/**
	 * Used for other widgets
	 * @param id name of the widget
	 * @param canvas name of the container to apply it to
	 */
	public Widget(String id, Widget parent) {
		if(parent==null) printError("AHHH!");
		initializeParameters();
		this.id = id;
		this.name_for_display = id;
		this.shape = new Rectangle();
		if(parent != null) {
			this.manager = parent.manager;
			this.attachToWidget(parent); //attach to parent first!
			this.manager.registerWidget(this); // do this last, it might make an orphan
		}else {
			printError("Error instantiating "+id+" ... Canvas is NULL. Register with a WidgetManager instead");
		}
		setSize(DEFAULT_WIDGET_SIZE, DEFAULT_WIDGET_SIZE);
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
		setSize(DEFAULT_WIDGET_SIZE, DEFAULT_WIDGET_SIZE);
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
		parameters = new EnumMap<>(Parameter.class);
		for(Parameter p : Parameter.values()) {
			parameters.put(p, Value.UNASSIGNED);
		}
	}

	public String getName() {
		if(name_for_display==null) {
			if(id==null) {
				return getClass().getSimpleName();
			}
			return id;
		}
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
			if(this.parent.doesSendInheritanceUponAttach()) {
				for(Parameter p : Parameter.values()) {
					setValue(p, this.parent.getValue(p));
				}
			}
			refreshHeirarchyCache();
			return true;
		}else {
			//making this independant
			manager.widget_independants.add(this);
			return true;
		}
	}
	
	protected boolean doesSendInheritanceUponAttach() {
		return true;
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
		widgets_children.add(widget_to_attach);
		
		refreshHeirarchyUpward();
	}
	
	/**
	 * Detaches
	 * @param widget_to_remove
	 */
	public void detachWidget(Widget widget_to_remove, boolean update_cache) {
		widgets_children.remove(widget_to_remove);
		if(update_cache) {
			refreshHeirarchyUpward();
		}
	}
	
	private void refreshHeirarchyCache() {
		widgets_heirarchy_cache.clear();
		for(Widget child : widgets_children) {
			widgets_heirarchy_cache.add(child); // add the child directly
			child.refreshHeirarchyCache(); // update child's cache
			widgets_heirarchy_cache.addAll(child.getDescendants()); //add all descendants
		}
	}
	
	private void refreshHeirarchyUpward() {
		refreshHeirarchyCache();
		if(parent!=null) {
			parent.refreshHeirarchyUpward();
		}
	}

	public List<Widget> getChildren(){
		return widgets_children;
	}
	
	public List<Widget> getDescendants() {
		return widgets_heirarchy_cache;
	}
	
//	private Widget[] getDescendants() {
//		// TODO Auto-generated method stub
//		return null;
//	}
	
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

	public float getParentWidth() {
		if(hasParent()) {
			return getParent().getWidth();
		}
		return Gdx.graphics.getWidth();
	}
	
	public float getParentHeight() {
		if(hasParent()) {
			return getParent().getHeight();
		}
		return Gdx.graphics.getHeight();
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
	 * Recalculates local shape positions based on alignment and base offsets.
	 * Enforces visual clamping so widgets cannot escape their parent's bounds.
	 */
	public void updateAlignment() {
		float parentW = getParentWidth();
		float parentH = getParentHeight();

		boolean alignTop = false, alignRight = false, alignCenter = false;

		switch(alignment) {
			case DOWN_RIGHT: alignRight = true; break;
			case RIGHT: alignRight = true; break;
			case UP: alignTop = true; break;
			case UP_LEFT: alignTop = true; break;
			case UP_RIGHT: alignRight = true; alignTop = true; break;
			case CENTER: alignCenter = true; break;
			default: break;
		}

		if (alignCenter) {
			shape.x = (parentW / 2f) - (shape.width / 2f) + shape_base.x;
			shape.y = (parentH / 2f) - (shape.height / 2f) + shape_base.y;
		} else {
			shape.x = alignRight ? (parentW - shape.width + shape_base.x) : shape_base.x;
			shape.y = alignTop ? (parentH - shape.height + shape_base.y) : shape_base.y;
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
		if(parent != null && follow_parent) {
			shape_global.x = parent.getGlobalX() + shape.x;
			shape_global.y = parent.getGlobalY() + shape.y;
		} else {
			shape_global.x = shape.x;
			shape_global.y = shape.y;
		}
		
		// Apply dynamic screen boundaries WITHOUT mutating the widget's intended layout intent
		shape_global.x += clamp_offset_x;
		shape_global.y += clamp_offset_y;

		for(Widget w : widgets_children) {
			w.updateGlobalPosition();
		}
		onPositionUpdate();
	}
	
	/**
	 * Calculates a temporary pushback offset to prevent this widget from rendering off the physical screen.
	 * Run this ONLY on top-level root widgets or popups before calling updateGlobalPosition().
	 */
	public void calculateScreenClamp() {
		clamp_offset_x = 0;
		clamp_offset_y = 0;

		float screenW = Gdx.graphics.getWidth();
		float screenH = Gdx.graphics.getHeight();

		// Calculate where the widget WOULD be, ignoring current offsets
		float rawX = (parent != null && follow_parent) ? parent.getGlobalX() + shape.x : shape.x;
		float rawY = (parent != null && follow_parent) ? parent.getGlobalY() + shape.y : shape.y;

		float rightEdge = rawX + shape.width;
		float topEdge = rawY + shape.height;

		// Calculate horizontal pushback
		if (rawX < 0) {
			clamp_offset_x = -rawX;
		} else if (rightEdge > screenW) {
			clamp_offset_x = screenW - rightEdge;
		}

		// Calculate vertical pushback
		if (rawY < 0) {
			clamp_offset_y = -rawY;
		} else if (topEdge > screenH) {
			clamp_offset_y = screenH - topEdge;
		}
	}
	
	
	/**
	 * This is for implementation to update various properties when this widget moves
	 */
	protected void onPositionUpdate() {
		
	}
	
	/**
	 * Sets the absolute screen position of the widget.
	 * Automatically reverse-engineers the correct relative shape_base offset.
	 */
	public void setGlobalPosition(float globalX, float globalY) {
		float parentWidth = getParentWidth();
		float parentHeight = getParentHeight();
		
		// Convert absolute global coordinates into local coordinates
		float localX = (parent != null) ? globalX - parent.getGlobalX() : globalX;
		float localY = (parent != null) ? globalY - parent.getGlobalY() : globalY;
		
		boolean alignTop = false, alignRight = false, alignCenter = false;
		switch(alignment) {
			case DOWN_RIGHT: alignRight = true; break;
			case RIGHT: alignRight = true; break;
			case UP: alignTop = true; break;
			case UP_LEFT: alignTop = true; break;
			case UP_RIGHT: alignRight = true; alignTop = true; break;
			case CENTER: alignCenter = true; break;
			default: break;
		}
		
		// Apply reverse alignment math to calculate the new anchor offset (shape_base)
		if (alignCenter) {
			shape_base.x = localX - (parentWidth / 2f) + (shape.width / 2f);
			shape_base.y = localY - (parentHeight / 2f) + (shape.height / 2f);
		} else {
			shape_base.x = alignRight ? (localX - parentWidth + shape.width) : localX;
			shape_base.y = alignTop ? (localY - parentHeight + shape.height) : localY;
		}
		
		// Process the new anchor through the alignment and bounding system
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
		
		if(show_text && name_for_display != null) {
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
		for(Widget w : widgets_children) {
			w.drawEditMode(renderer, recursive);
		}
	}

	public void drawBorder(ShapeRenderer shape_renderer) {
		shape_renderer.set(ShapeType.Line);
		shape_renderer.setColor(color_outline);
		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
//		drawRectRound(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height, style.corner_radius);
	}

	@Override
	public void drawShape(ShapeRenderer renderer) {
		if(isVisible()) {
			if(hovering) {
				color_outline = LerpColor(color_outline, isLocked()?style.color_hover_locked:style.color_hover, style.color_fade_in);
			}else {
				color_outline = LerpColor(color_outline, style.color_outline, style.color_fade_out);
			}
		}
	}
	
	protected void drawShapeChildren(ShapeRenderer renderer, boolean recursive) {
		for(Widget w : widgets_children) {
			w.drawShape(renderer);
		}
	}
	
	protected void drawTextureChildren(SpriteBatch sprite_batch, boolean recursive) {
		for(Widget w : widgets_children) {
			w.drawTexture(sprite_batch, recursive);
		}
	}
	
	protected void drawFontChildren(SpriteBatch sprite_batch, boolean recursive) {
		for(Widget w : widgets_children) {
			w.drawFont(sprite_batch, recursive);
		}
	}
	
	
	/**
	 * If the object shows a highlight around it when hovereds
	 * @return
	 */
	public boolean isHoverable() {
		return true;
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
		return getValue(Parameter.TOUCHABLE).get();
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
		if(isAncestorOf(relative)) {
			return true;
		}
		if(isDescendantOf(relative)) {
			return true;
		}
		return false;
	}
	
	public boolean isAncestorOf(Widget descendant) {
		if(widgets_heirarchy_cache.contains(descendant)) {
			return true;
		}
		return false;
//		Widget w = descendant;
//		while(w != null) {
//			if(w.getParent()==this) {
//				return true;
//			}
//			w = w.getParent();
//		}
//		return false;
	}
	
	public boolean isDescendantOf(Widget ancestor) {
		if(ancestor.widgets_heirarchy_cache.contains(this)) {
			return true;
		}
		return false;
//		Widget w = this;
//		while(w != null) {
//			if(w.getParent()==ancestor) {
//				return true;
//			}
//			w = w.getParent();
//		}
//		return false;
	}
	
	protected void pushNewZPosition(boolean recursive) {
		z = manager.global_canvas_z;
		manager.global_canvas_z++;
		if(recursive) {
			for(Widget w : widgets_children) {
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
		for(Runnable r : run_on_click) {
			r.run();
		}
	}
	
	public void addOnClick(Runnable r) {
		this.run_on_click.add(r);
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
		return !widgets_children.isEmpty();
	}
	
	
	/**
	 * Pointer input APIs — default implementations do nothing.
	 * Return true if the widget handled/consumed the event (capture it), false otherwise.
	 * UIManager will call these with global mouse coordinates.
	 */
	public boolean onPointerDown(int globalX, int globalY, int button) {
	    return false;
	}

	public boolean onPointerDragged(int globalX, int globalY) {
	    return false;
	}

	public boolean onPointerUp(int globalX, int globalY, int button) {
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
	
	public void setTouchable(boolean new_touchable, InheritanceRule rule_override) {
		setValue(Parameter.TOUCHABLE, new_touchable, rule_override);
	}
	
	
	
	public void setStyle(Style style, boolean recursive) {
		this.style = style;
		if(recursive) {
			setStyleChildren(style, recursive);
		}
	}
	
	
	private void setStyleChildren(Style style2, boolean recursive) {
		for(Widget w : widgets_children) {
			w.setStyle(style2, recursive);
		}
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
			for(Widget w : widgets_children) {
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
	 * Prevents this widget from activating
	 */
	public void setLocked(boolean locked) {
		this.locked = locked;
	}
	
	/**
	 * @return If locked, the widget will fail to activate, such as buttons/sliders
	 */
	public boolean isLocked() {
		return this.locked;
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
	/**
	 * This will keep this widget perpetually in edit mode
	 */
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
			return false;
		}
		if(group.equals(w.group)) return true;
		return false;
		
	}
	
	
}
