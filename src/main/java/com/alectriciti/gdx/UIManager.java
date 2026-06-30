package com.alectriciti.gdx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import com.alectriciti.gdx.Button.ButtonType;
import com.alectriciti.gdx.events.WidgetRemoveEvent;
import com.alectriciti.gdx.events.EventManager;
import com.alectriciti.gdx.events.WidgetAddEvent;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectSet;

import static com.alectriciti.gdx.Toolkit.*;

/**
 * A UI Manager can be instantiated to automatically run logic and or render
 */
public class UIManager implements InputProcessor {

	public Color COLOR_BUTTON_ACTIVATED = Color.GREEN;
	public Color COLOR_BUTTON_TEXT_ACTIVATED = new Color(0.1f, 0.1f, 0.1f, 1);
	public Color COLOR_BUTTON_PRESSING = Color.GRAY;
	public Color COLOR_BUTTON_DEFAULT = new Color(0.1f, 0.1f, 0.1f, 1);
	

	public static final float EDIT_HANDLE_HEIGHT = 8;

	boolean left_mouse_is_pressed;
	boolean right_mouse_is_pressed;
	int key_repeat_delay_normal = 8; // ticks before key repeat starts
	int key_repeat_delay_fast = 60; // ticks before key repeat starts
	boolean debug_mode = true;
	
	int left_pressed_ticks, right_pressed_ticks, up_pressed_ticks, down_pressed_ticks;

	int repeat_start_ticks = 8;        // when repeating begins
	int repeat_full_speed_ticks = 60;  // when it becomes true every frame

	int repeat_max_interval = 8;       // slowest repeat rate while ramping
	int repeat_min_interval = 1;       // fastest repeat rate (1 = every frame)

	private int clampInt(int value, int min, int max) {
	    if (value < min) return min;
	    if (value > max) return max;
	    return value;
	}

	public boolean allowKeyRepeat(int base_tick, int key_pressed_ticks) {
	    if (key_pressed_ticks <= 0) {
	        return false;
	    }

	    // first press always counts
	    if (key_pressed_ticks == 1) {
	        return true;
	    }

	    // no repeat yet
	    if (key_pressed_ticks < repeat_start_ticks) {
	        return false;
	    }

	    // once fully held, return true every frame
	    if (key_pressed_ticks >= repeat_full_speed_ticks) {
	        return true;
	    }

	    // 0.0 -> 1.0 progress through the ramp
	    int ramp_ticks = repeat_full_speed_ticks - repeat_start_ticks;
	    int elapsed = key_pressed_ticks - repeat_start_ticks;

	    // gradually shrink the interval from repeat_max_interval to repeat_min_interval
	    int interval = repeat_max_interval
	            - ((repeat_max_interval - repeat_min_interval) * elapsed / ramp_ticks);

	    interval = clampInt(interval, repeat_min_interval, repeat_max_interval);

	    return (base_tick % interval == 0);
	}

	/**
	 * This is the widget that is currently being adjusted and dragged around (the
	 * red outline)
	 */
	Widget widget_currently_adjusting = null;
	Canvas canvas_proposed_to_attach = null;

	/**
	 * This is the button that is currently being clicked
	 */
	public Widget mouse_clicked_widget = null;

	
	/**
	 * These are used as ways to get the mouse offset when a widget is clicked
	 */
	float mouse_click_offset_x;
	float mouse_click_offset_y;
	
	public Widget pointerCapturedWidget = null; // widget that captured pointer (for drag)
//	private int pointerCapturedId = -1; // pointer id (if you support multi-touch) - we use 0 for mouse

	static BitmapFont primary_font;
	boolean font_activated = false;

	// List<Canvas> canvases = new ArrayList<Canvas>();
	List<Widget> widget_independants = new ArrayList<Widget>();
	List<Canvas> canvases_active = new ArrayList<Canvas>();
	


	public Widget widget_hovering;
	public Widget widget_focused;
	public Widget context_widget_candidate;
	public ContextWidget context_widget;
	int global_canvas_z = 0;

	public int ui_tick = 0;

	int mouseScrollOffset = 0; // add this to your WidgetManager or UI state
	private float scrollSelectionOffset;

	int WIDTH, HEIGHT;

	int mouse_x, mouse_y;

	/**
	 * These references exist globally to allow for extra functionality
	 */
	public ObjectSet<Widget> widgets = new ObjectSet<Widget>();
	// public HashSet<Widget> widget_orphans = new HashSet<Widget>();

	private ObjectSet<Widget> widgets_to_add = new ObjectSet<Widget>();
	private ObjectSet<Widget> widgets_to_destroy = new ObjectSet<Widget>();
	
	Set<Widget> transient_widgets = new HashSet<Widget>(); // Widgets which close automatically, relating to focus 

	public List<Button> buttons = new ArrayList<Button>();
	public List<Button> buttons_rapidfiring = new ArrayList<Button>();
	public ObjectMap<String, Button> buttons_by_name = new ObjectMap<String, Button>();
	public ObjectMap<Integer, Button> buttons_by_key = new ObjectMap<Integer, Button>();
	
	public InputMultiplexer input_multiplexer;
	
	private EventManager event_manager;
	
	public EventManager getEventManager() {
		return event_manager;
	}
	
	
	public UIManager(InputMultiplexer input, BitmapFont font) {
		input_multiplexer = input;
		input_multiplexer.addProcessor(this);
		primary_font = font;
		event_manager = new EventManager();
	}

	/**
	 * Calling this after initializing all of your buttons can automatically mark
	 * them for serialization based on their name value
	 */
	public void automaticallyAssignIDsToWidgets() {
		for (Widget w : widgets) {
			w.autoAssignId();
		}
	}
	
	/**
	 * Internal Note:
	 * Call this when a widget is to be selected
	 * @param new_widget
	 * @param move_to_front
	 */
	public void focus(Widget new_widget, boolean move_to_front) {
		if (new_widget == null) {
			
			//If focus exists, then unfocus
			if(widget_focused!=null) {
				widget_focused.unfocus(null); // Unfocus the existing focus widget
				widget_focused = null;
			}
			
		} else {
			// Focusing a new target...
			
			if (widget_focused != null) {
				if (widget_focused == new_widget) {
					return; // do nothing, focused widget is the same
				}
				widget_focused.unfocus(new_widget); //Unfocus the existing focus widget and pass the new candidate for context
			}
			widget_focused = new_widget;
			if(move_to_front) {
				global_canvas_z++;
				// TODO Do extra checks such as unclicking buttons etc.
				// mouse_adjusting_widfget = null;
				widget_focused.pushNewZPosition(true);
				widget_independants.sort(Comparator.comparingInt(Widget::getZIndex));
			}
			widget_focused.focused = true;
			if(widget_focused instanceof InputProcessor) {
				input_multiplexer.addProcessor(0, (InputProcessor) widget_focused);
			}
			widget_focused.callOnFocus();
//			print(ANSI_BLUE+"Widget Focused: " +ANSI_RESET+ new_widget.name_for_display);
		}


		//dropdown menu bug!!! TODO 
		for(Widget w : transient_widgets) {
			if(w instanceof Activatable) {
				if(new_widget == null) {
					((Activatable)w).deactivate();
					continue;
				}
				if(new_widget.isRelated(w)|| new_widget.isInSameGroup(w)) {
					continue;
				}
				((Activatable)w).deactivate();
			}
		}
//		transient_widgets.clear();
		//Regardless, we need to check that existing temporary widgets are deactivated
//		for(Widget w : transient_widgets) {
//			if(!new_widget.isRelated(w)) {
//				
//			}
//		}
		
	}
	
	public void unfocus(Widget unfocus) {
		if(unfocus instanceof InputProcessor) {
			input_multiplexer.removeProcessor(((InputProcessor) widget_focused));
		}
		unfocus.focused = false;
//		print(ANSI_BLUE+"Widget Unfocused: " +ANSI_RESET+unfocus.id);
	}

	boolean edit_mode;
	private boolean edit_mode_pressed; // a lock mechanism
	boolean constraint_mode;
	int constraint_amount = 4;

	public boolean isEdittingMode() {
		return edit_mode;
	}

	private void setWidgetSelectionCandidate(Widget widget_to_assign) {
		if (widget_hovering != null) {
			widget_hovering.hovering = false;
		}
		if (widget_to_assign != null) {
			widget_hovering = widget_to_assign;
			widget_hovering.hovering = true;
//			 print("New Candidate: "+widget_hovering.getName());
		} else {
			widget_hovering = null;
		}
	}

	/**
	 * Handles input for Buttons and Mouse
	 */
	public void update() {
		// Mouse handling
		ui_tick++;
		
		
		//Set Arrow Key data
		
		if(Gdx.input.isKeyPressed(Keys.LEFT)) {
			left_pressed_ticks++;
		}else {
			left_pressed_ticks = 0;
		}
		if(Gdx.input.isKeyPressed(Keys.RIGHT)) {
			right_pressed_ticks++;
		}else {
			right_pressed_ticks = 0;
		}
		if(Gdx.input.isKeyPressed(Keys.UP)) {
			up_pressed_ticks++;
		}else {
			up_pressed_ticks = 0;
		}
		if(Gdx.input.isKeyPressed(Keys.DOWN)) {
			down_pressed_ticks++;
		}else {
			down_pressed_ticks = 0;
		}

//		left_is_pressed = (Gdx.input.isKeyPressed(Keys.LEFT));
//		right_is_pressed = (Gdx.input.isKeyPressed(Keys.RIGHT));
//		up_is_pressed = (Gdx.input.isKeyPressed(Keys.UP));
//		down_is_pressed = (Gdx.input.isKeyPressed(Keys.DOWN));

		// mouseScrollOffset += (scroll_y/4);s

		edit_mode = Gdx.input.isKeyPressed(Keys.ALT_LEFT) || Gdx.input.isKeyPressed(Keys.ALT_RIGHT);
		constraint_mode = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);

		if (edit_mode) {
			edit_mode_pressed = true;
		} else {
			if (edit_mode_pressed) {
				edit_mode_pressed = false;
				scrollSelectionOffset = 0;
			}
		}
		if (!widgets_to_add.isEmpty()) {
			widgets.addAll(widgets_to_add);
			for (Widget widget : widgets_to_add) {
				if (widget.getParent() == null) {
					widget_independants.add(widget);
				}
			}
			ObjectSet<Widget> widgz = new ObjectSet<Widget>(widgets_to_add);
			//call widget listeners
			for(Widget widget : widgz) {
				widget.callOnCreate();
			}
			//call ui manager listeners for API support
			for(Widget w : widgz) {
				event_manager.fireEvent(new WidgetAddEvent(w)); //run.handle(yh8);//;pll8tf6ygyigtvytfsexftbhh
			}
			widgets_to_add.clear();
		}

		if (!widgets_to_destroy.isEmpty()) {

			//call ui manager listeners for API support
			for(Widget w : widgets_to_destroy) {
				event_manager.fireEvent(new WidgetRemoveEvent(w));
			}
			
			for(Widget wd : widgets_to_destroy) {
				widgets.remove(wd);
				widget_independants.remove(wd);
				buttons.remove(wd);
			}
			
//			widgets.removeAll(widgets_to_destroy);
//			widget_independants.removeAll(widgets_to_destroy);
//			buttons.removeAll(widgets_to_destroy);

			for (Widget wtd : widgets_to_destroy) {
				if (wtd instanceof Button) {
					Button b = (Button) wtd;
					if(b.button_codes!=null)
						for(int code : b.button_codes) {
							buttons_by_key.remove(code);
						}
					buttons_by_name.remove(b.name_for_display);
				}
			}
			for (Widget w : widgets) {
				for(Widget wd : widgets_to_destroy) {
					w.widgets_children.remove(wd);
				}
			}

			widgets_to_destroy.clear();
		}

		for (Button b : buttons_rapidfiring) {
			if(b.pressed_ticks>=b.rapidfire_start_delay) {
				if (ui_tick % b.rapidfire_frequency == 0) {
						b.activate();
					}
				}
			}
		for (Widget w : widgets) {
			w.update();
		}

		/*
		 * if(canvas_focused == null) { canvas_focused = canvases.get(0);
		 * if(canvas_focused == null) {
		 * printLibError("canvas_focused == null! use WidgetManager.focus(canvas)");
		 * return; } }
		 */

//		int prev_x = mouse_x;
//		int prev_y = mouse_y;

		mouse_x = getMouseX();
		mouse_y = getMouseY();
		
		if (left_mouse_is_pressed && pointerCapturedWidget != null) {
		    // For mouse we use pointer id 0
//			if(prev_x!=mouse_x && prev_y!=mouse_y)
		    pointerCapturedWidget.onPointerDragged(mouse_x, mouse_y);
		}


		//Set Mouse Data
		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
			left_click_down();
		} else if (left_mouse_is_pressed) {
			left_click_release();
		}
		//Set Mouse Data
		if (Gdx.input.isButtonPressed(Buttons.RIGHT)) {
			right_click_down();
		} else if (right_mouse_is_pressed) {
			right_click_release();
		}

		HoverMouseLogic();

	}

	private void left_click_down() {
	    left_mouse_is_pressed = true;
	    
	    
	    
	    // Update mouse_x/mouse_y earlier in update() already; still get local copy
	    int mx = mouse_x;
	    int my = mouse_y;

	    // If there is currently no captured pointer widget, try to capture
	    if (pointerCapturedWidget == null) {
	        // If a widget_hovering exists, let it handle pointerDown first (gives it priority)
	        if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
	    		left_click_just_pressed(mx, my);
	        }
	    } else {
	        // A widget already captured pointer previously. In most mouse cases this won't happen
	        // because we capture at press; but if it did, we could route pressed-to-it.
	        // (no action required here)
	    }

	    // while mouse is held and adjusting widget
	    if (widget_currently_adjusting != null) {
	        AdjustWidgetPosition();
	    }

	    // Existing behavior if a button is already selected:
	    if (mouse_clicked_widget != null) {
            if (!mouse_clicked_widget.containsGlobal(mx, my)) {
            	if(mouse_clicked_widget == pointerCapturedWidget) return;
            	mouse_clicked_widget.pressing = false;
    	        if (mouse_clicked_widget instanceof Button) {
    	            Button button_clicked = (Button) mouse_clicked_widget;
	                if (button_clicked.button_type == ButtonType.RAPIDFIRE) {
	                    buttons_rapidfiring.remove(button_clicked);
	                    button_clicked.deactivate();
	                }
	                mouse_clicked_widget = null;
	            }
	        }
	    }
	}

	private void left_click_just_pressed(int mx, int my) {
		if(context_widget!=null) {
			if(widget_hovering!=null && (widget_hovering.equals(context_widget) || context_widget.equals(widget_hovering.getParent()))) {
				//related action to context window
			} else {
				context_widget.deactivate();
				context_widget = null;
			}
		}
		if (widget_hovering != null) {
			
		    // If we are in edit mode (or widget forces editability), prefer the "move widget" behavior.
		    // This prevents interactive widgets (sliders, color pickers) from capturing pointer while editing.
		    if (edit_mode || widget_hovering.isAlwaysEditable()) {
		        if (widget_hovering.isEditable()) {
		            // Start moving/adjusting the widget (existing behavior)
		            widget_currently_adjusting = widget_hovering;
		            mouse_click_offset_x = widget_hovering.getGlobalX() - mx;
		            mouse_click_offset_y = widget_hovering.getGlobalY() - my;
		            // Note: we DO NOT call onPointerDown in edit mode; movement has priority.
		        } else {
		            // Not editable: fall back to focusing / clicking behavior
		        	
		        	WidgetClickEvent widgetClickEvent = new WidgetClickEvent(mouse_clicked_widget, mouse_x, mouse_y, true);
		            event_manager.fireEvent(widgetClickEvent);
		            if(!widgetClickEvent.isCancelled()) {
			            widget_hovering.focus();
			            mouse_clicked_widget = widget_hovering;
			            widget_hovering.callOnClicked();
		            }
		        }
		    } else {
		        // Normal (non-edit) mode: give the widget a chance to capture pointer (e.g. Slider).

		    	WidgetClickEvent widgetClickEvent = new WidgetClickEvent(widget_hovering, mouse_x, mouse_y, false);
		        event_manager.fireEvent(widgetClickEvent);
		        if(!widgetClickEvent.isCancelled() && !widget_hovering.isLocked()) {
		            mouse_click_offset_x = widget_hovering.getGlobalX() - mx;
		            mouse_click_offset_y = widget_hovering.getGlobalY() - my;
			        boolean captured = widget_hovering.onPointerDown(mx, my, Buttons.LEFT);
			        if (captured) {
			            pointerCapturedWidget = widget_hovering;
			            pointerCapturedWidget.pressing = true;
			            // also set as clicked widget for legacy logic
			        }

			        // If not captured, fall back to default focus/click behavior (buttons, etc.)
			        widget_hovering.focus();
			        mouse_clicked_widget = widget_hovering;
			        widget_hovering.callOnClicked(); // existing API call
		            return; // captured — consume event
		        }
		    }

		} else {
		    focus(null, false);
		}
	}

	public void debug(String msg) {
		if(debug_mode)
		System.out.println(ANSI_BLUE+"[DEBUG] "+ANSI_RESET+msg);
	}

	private void left_click_release() {
	    left_mouse_is_pressed = false;

//	    int mx = mouse_x;
//	    int my = mouse_y;

	    // If a widget captured the pointer, 
	    if (widget_hovering != null) {
	        widget_hovering.hovering = false;
	        widget_hovering = null;
	    }
	    //give it a chance to handle pointerUp
	    if (pointerCapturedWidget != null) {
//	        boolean consumed = pointerCapturedWidget.onPointerUp(mx, my, pointerCapturedId, Buttons.LEFT);
	        // release capture regardless (single-pointer model)
	        pointerCapturedWidget = null;
//	        pointerCapturedId = -1;

	        // We still want to call callOnReleased if we had a currently clicked widget
	        if (mouse_clicked_widget != null) {
	            mouse_clicked_widget.callOnReleased();
	            mouse_clicked_widget.pressing = false;
	            mouse_clicked_widget = null;
	        }
	        scrollSelectionOffset = 0;
	        // release buttons
	        for (Button b : buttons) {
	            b.pressing = false;
	        }
	        return; // consumed
	    }

	    // If no pointer-capture, proceed with existing release logic (unchanged)
	    if (widget_currently_adjusting != null) {
	        widget_currently_adjusting = null;
	    }
	    if (mouse_clicked_widget == null) {
	        // focus(null);
	    } else {
	        if (mouse_clicked_widget instanceof Button) {
	            Button button_clicked = (Button) mouse_clicked_widget;
	            if (button_clicked.is_key_down) {
	                button_clicked.pressing = false;
	                button_clicked.cancelled = true;
	            } else if (!button_clicked.isLocked()){
	                switch (button_clicked.button_type) {
	                    case PRESS:
	                        button_clicked.activate();
	                        break;
	                    case RAPIDFIRE:
	                        buttons_rapidfiring.remove(button_clicked);
	                        button_clicked.deactivate();
	                        break;
	                    case PRESS_AND_RELEASE:
	                        button_clicked.deactivate();
	                        break;
	                    case TOGGLE:
	                        if (!button_clicked.activated) {
	                            button_clicked.activate();
	                        } else {
	                            button_clicked.deactivate();
	                        }
	                        break;
	                }
	            }
	            button_clicked.pressing = false;
	        }
	        mouse_clicked_widget.callOnReleased();
	        mouse_clicked_widget = null;
	    }
	    scrollSelectionOffset = 0;
	    for (Button b : buttons) {
	        b.pressing = false;
	    }
	}
	


	private void right_click_down() {
		right_mouse_is_pressed = true;
    	if (widget_hovering != null) {
        	context_widget_candidate = widget_hovering;
    	}
	}

	private void right_click_release() {
	    right_mouse_is_pressed = false;
	    
	    /**
	     * Check if a context menu already exists before spawning a new oone
	     */
		boolean context_menu_exists = context_widget != null;
    	if(context_menu_exists) {
    		context_widget.deactivate(); // Deactives (closes) the existing context widget
    		if(widget_hovering == context_widget.getParent()) {
    			context_widget = null; // Prevents re-opening a context window on the same widget
    			return;
    		}
    	}
	    
	    //If the widget the pointer is over is STILL the proposed context_widget, select it.
	    if(context_widget_candidate != null && widget_hovering == context_widget_candidate) {
	    	context_widget_candidate.focus(); //focus the widget candidate
	    	context_widget = context_widget_candidate.spawnContextWidget(); //spawn a new widget
	    	context_widget_candidate = null;
	    }
		
	}


	private void AdjustWidgetPosition() {
		if (widget_currently_adjusting instanceof WindowMoverWidget) {
			// move window logic
			WindowMoverWidget window = (WindowMoverWidget) widget_currently_adjusting;
			window.moveWindow((int) mouse_click_offset_x, (int) mouse_click_offset_y);

		} else {
			if (constraint_mode) {
				widget_currently_adjusting.setGlobalPosition(
						((mouse_x + (int) (mouse_click_offset_x)) / constraint_amount) * constraint_amount,
						((mouse_y + (int) (mouse_click_offset_y)) / constraint_amount) * constraint_amount);
			} else {
				widget_currently_adjusting.setGlobalPosition((int) (mouse_x + mouse_click_offset_x),
						(int) (mouse_y + mouse_click_offset_y));
			}
		}
	}

	/**
	 * Runs finally after other mouse click logic
	 */
	private void HoverMouseLogic() {
		if (!left_mouse_is_pressed) {

			// canvas_proposed_to_attach

			Widget widget_to_highlight = getSelectableWidgetAtPosition(mouse_x, mouse_y);
			if (widget_to_highlight != null) {
				setWidgetSelectionCandidate(widget_to_highlight);
			} else {
				if (widget_hovering != null) {
					widget_hovering.hovering = false;
					widget_hovering = null;
				}
			}
		}
	}

	/**
	 * Main Hover Logic... this FETCHES The widget selection candidate
	 */
	private Widget getSelectableWidgetAtPosition(int mouseX, int mouseY) {
		// Widget widget = null;
		// boolean found = false;

		List<Widget> found_widgets = getAllWidgetsAtLocation(mouseX, mouseY);

		if (found_widgets.isEmpty()) {
			// print("found widgets is empty");
			return null;
		}

		int total_widgets = found_widgets.size();

		if (edit_mode) {
			int offset = (int) (scrollSelectionOffset % total_widgets); // Wrap safely

			int i = (total_widgets - 1 + offset + total_widgets) % total_widgets;
			return found_widgets.get(i);
		} else {
			return found_widgets.get(total_widgets - 1);
		}
		/*
		 * for (int o = 0; o < total_widgets; o++) { int i = (total_widgets - 1 - o +
		 * offset + total_widgets) % total_widgets; // Rotate from topmost down with
		 * offset //print(""+i); Widget canvas = canvases.get(i);
		 * 
		 * //for(Widget w : canvas.getAllChildren()) { // if(w.isVisible() &&
		 * w.containsGlobal(getMouseX(), getMouseY())) { // //return w; // } //} }
		 */
		// then search the stragglers (which have not been added to any canvas)
		/*
		 * if(!found) { for(Widget w : widget_orphans) { if(w.containsGlobal(mouse_x,
		 * mouse_y)) { return w; } } setWidgetSelectionCandidate(null);
		 * //if(canvas_focused.containsGlobal(mouse_x, mouse_y)) {
		 * //setWidgetSelectionCandidate(canvas_focused); //}else { //}
		 * 
		 * }
		 */
		// return widget;
	}
	private List<Widget> getAllWidgetsAtLocation(int mouseX, int mouseY) {
	    List<Widget> found_widgets = new ArrayList<Widget>();

	    // A reusable rectangle to avoid allocation if you prefer micro-optimization:
	    // Rectangle hit = new Rectangle(); // optional: can be reused

	    for (Widget w : widgets) {
	        if (w.isVisible() && w.isTouchable()) {
	            Rectangle sel = w.getSelectionRegion(); // ask the widget for its selection region
	            if (sel != null) {
	                // Note: sel is in global coords per our Widget.getSelectionRegion contract
	                if (sel.contains(mouseX, mouseY)) {
	                    found_widgets.add(w);
	                }
	            } else {
	                // If widget returns null for some reason, safe fallback to existing containsGlobal
	                if (w.containsGlobal(mouseX, mouseY)) {
	                    found_widgets.add(w);
	                }
	            }
	        }
	    }
	    found_widgets.sort(Comparator.comparingInt(Widget::getZIndex));
	    return found_widgets;
	}
	/**
	 * Renders all canvases in the order they were created
	 * 
	 * @param renderer
	 */
	public void renderAll(ShapeRenderer shape_renderer, SpriteBatch sprite_batch) {

		Gdx.gl.glEnable(GL20.GL_BLEND);

		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if (debug_mode) {

		}
		// font.setColor(Color.WHITE);
		// font.draw(sprite_batch, "Widget Scroll Selector: "+scrollSelectionOffset, 20,
		// 500);
		// font.draw(sprite_batch, "Widget Candidate
		// "+(widget_hovering!=null?widget_hovering:"null"), 20, 524);

		/*
		 * Now Canvas Groups
		 */
		// if(font!=null) {
		for (Widget widget : widget_independants) {
			if (widget.isVisible()) {
				shape_renderer.begin();
				widget.style.drawShape(widget, shape_renderer);
				shape_renderer.end();
				sprite_batch.begin();
				widget.drawTexture(sprite_batch, true);
				widget.drawFont(sprite_batch, true);
				sprite_batch.end();
				
				shape_renderer.begin();
				widget.drawShapeChildren(shape_renderer, true);
				shape_renderer.end();
				sprite_batch.begin();
				widget.drawTextureChildren(sprite_batch, true);
				widget.drawFontChildren(sprite_batch, true);
				sprite_batch.end();
				Gdx.gl.glEnable(GL20.GL_BLEND);
			}
		}
		
		if(widget_hovering!=null) {
			shape_renderer.begin();
			if (edit_mode && widget_hovering.isEditable()) {
				widget_hovering.drawEditMode(shape_renderer, false);
			} else if (widget_hovering.isHoverable()) {
				widget_hovering.style.drawBorder(widget_hovering, shape_renderer);
			}
			shape_renderer.end();
		}
		// Draw this ontop to allow for visibility
		/*
		 * if(widget_currently_adjusting!=null) { shape_renderer.begin();
		 * widget_currently_adjusting.drawShape(shape_renderer, false);
		 * widget_currently_adjusting.drawEditMode(shape_renderer, true);
		 * shape_renderer.end(); sprite_batch.begin();
		 * widget_currently_adjusting.drawFont(sprite_batch, font, false);
		 * sprite_batch.end(); }
		 */
	}

	public void registerGroup(Widget w) {
		this.widget_independants.add(w);
		if (this.widget_focused == null) {
			// ensures there's an initialization of canvases
			this.widget_focused = w;
		}
	}

	/**
	 * Draws wireframes around widgets
	 * 
	 * @param mode
	 */
	public void setDebugMode(boolean mode) {
		debug_mode = mode;
	}

	@Override
	public boolean keyDown(int keycode) {

	    if (widget_focused instanceof InputProcessor) {
	    	return true;
	    }

		if (buttons_by_key.containsKey(keycode)) {
			Button button = buttons_by_key.get(keycode);

			//fire based on button type and activate event handler
			if(!button.isLocked()) {
				switch (button.button_type) {
				case RAPIDFIRE:
					button.is_key_down = true;
					if (button.cancelled) {
						button.pressing = false;
					} else {
						button.pressing = true;
						button.activate();
						buttons_rapidfiring.add(button);
					}
					break;
				case PRESS:
					button.is_key_down = true;
					if (button.cancelled) {
						button.pressing = false;
					} else {
						button.pressing = true;
					}
					break;
				case PRESS_AND_RELEASE:
					if (!button.is_key_down) {
						button.is_key_down = true;
						if (button.cancelled) {
							button.pressing = false;
						} else {
							button.pressing = true;
							button.activate();
						}
					}
					break;
				case TOGGLE:
					if (!button.activated) {
						button.activate();
						button.activated = true;
						print(button.name_for_display + " ACTIVATED");
					} else {
						button.deactivate();
						button.activated = false;
						print(button.name_for_display + " DEACTIVATED");
					}
					break;
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean keyUp(int keycode) {
		if (buttons_by_key.containsKey(keycode)) {
			Button b = buttons_by_key.get(keycode);
			// if(!b.visible) {
			// return false;
			// }

			switch (b.button_type) {
			case RAPIDFIRE:
				if (b.is_key_down) {
					b.is_key_down = false;
					b.pressing = false;
					if (b.cancelled) {
						b.cancelled = false;
					} else {
						b.deactivate();
						buttons_rapidfiring.remove(b);
					}
				}
				break;
			case PRESS:
				if (b.is_key_down) {
					b.is_key_down = false;
					b.pressing = false;
					if (b.cancelled) {
						b.cancelled = false;
					} else {
						b.activate();
					}
				}
				break;
			case PRESS_AND_RELEASE:
				if (b.is_key_down) {
					b.is_key_down = false;
					b.pressing = false;
					if (b.cancelled) {
						b.cancelled = false;
					} else {
						b.deactivate();
					}
				}
				break;
			case TOGGLE:

				break;
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		scrollSelectionOffset += amountY;

		if (!edit_mode) {
			if (widget_hovering != null) {
				widget_hovering.scroll(amountX, amountY);
				return true;
			}
		}
		return true;
	}

	/**
	 * A quick and dirty implementation for allowing drag and drop onto widgets .png
	 * files will set the texture of the widget directories will create a hyperlink
	 * for buttons to open the folder
	 * 
	 * @return true if the dropped file was appropriately utilized, allowing for
	 *         override logic if false
	 */
	public boolean importFileAutomaticAssignment(String[] files) {
		if (files[0] != null) {
			FileHandle file = new FileHandle(files[0]);
			Button button = null;

			Widget w = widget_hovering;

			if (w instanceof Button) {
				button = (Button) w;
				print("yah it's a booton");
			}

			if (file.isDirectory()) {

				if (button != null) {
					button.setHyperlink(file);
					return true;
				}
			} else if (file.extension().toLowerCase().equals("png")) {
				if (button != null) {
					if(button.can_change_texture) {
						button.setTexture(file);
						return true;
					}
				}
			}

//			Button b = null;
//			if(file.isDirectory()) {
//				b = new Button(file.getName(), 0, this) {
//					
//					File f = file;
//					@Override
//					protected void onActivate() {
//						try {
//							Desktop.getDesktop().open(f);
//						} catch (IOException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						super.onActivate();
//					}
//				};
//				b.setGlobalPosition(getMouseX(), getMouseY());
//			}else if(file.getName().endsWith(".png")) {
//				b = new Button(file.getName(), 0, this);
//				b.setGlobalPosition(getMouseX(), getMouseY());
//				b.setTexture(new FileHandle(file));
//				return b;
//			}
		}
		return false;
	}

	/**
	 * Registers a widget by adding it to appropriate lists and sets
	 * 
	 * @param widget to be registered
	 */
	void registerWidget(Widget widget) {
		widgets_to_add.add(widget);
	}

	String folder_widgets = "widgets/";
	private static Style default_style;

	/**
	 * Should be run on resize
	 */
	public void alignAllWidgets() {
		for (Widget w : widgets) {
			w.updateAlignment();
			w.updateGlobalPosition();
		}
	}

	public void saveAllWidgets() {
		FileHandle file = Gdx.files.local(folder_widgets);
		if (!file.exists())
			file.mkdirs();

		for (Widget w : widgets) {
			if(!w.doesSerialize())continue;
			if (w.getId() != null) {
				if(isValidFilename(w.getId())) {
					FileHandle widgetFile = Gdx.files.local(folder_widgets + w.getId() + ".json");
					Json json = new Json();
					widgetFile.writeString(w.saveToJson().toString(), false);
				}else {
					printError("Invalid Filename for Widget: "+w.getId()+"... keep ID and display name separate!");
				}
			}
		}
	}

	public void loadAllWidgets() {
		FileHandle file = Gdx.files.local(folder_widgets);
		if (!file.exists())
			return;

		for (Widget w : widgets) {
			if (w.getId() != null) {
				FileHandle widgetFile = Gdx.files.local(folder_widgets + w.getId() + ".json");
				if (widgetFile.exists()) {
					JsonReader reader = new JsonReader();
					JsonValue data = reader.parse(widgetFile);

					String type = data.getString("type", "widget");

					switch (type) {
					case "button":
						((Button) w).loadFromJson(data);
						break;
					case "widget":
					default:
						w.loadFromJson(data);
						break;
					}
				}
			}
		}

		for (Widget w : widgets) {
			w.reloadAllData();
			print("Reloaded all widgets!");
		}
		alignAllWidgets();
	}

	/**
	 * This should be called on shutdown to dispose of widget-related and other
	 * resources
	 */
	public void dispose() {
		for (Widget w : widgets) {
			w.dispose();
		}
	}
	
	

	public void setfullscreenMode(boolean isFullScreen, boolean decorated) {
		if (isFullScreen) {
			WIDTH = Gdx.graphics.getWidth();
			HEIGHT = Gdx.graphics.getHeight();
		}
		
		
		Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
		Lwjgl3Window window = graphics.getWindow();
		Monitor monitor = graphics.getMonitor(); // Get the current monitor
		
		graphics.setUndecorated(!decorated); //look here for SEVERAL other features
		
		
		int width = 0;
		int height = 0;
		int[] xpos = new int[1];
		int[] ypos = new int[1];

		long monitorHandle = ((Lwjgl3Monitor) monitor).getMonitorHandle();

		// Get monitor position
		GLFW.glfwGetMonitorPos(monitorHandle, xpos, ypos);
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitorHandle);
		
//		DisplayMode

		if (vidMode != null) {
			width = vidMode.width();
			height = vidMode.height();
			int refreshRate = vidMode.refreshRate();
			System.out.printf("Monitor: %s | Position: (%d, %d) | Size: %dx%d | Refresh Rate: %dHz%n", monitor.name,
					xpos[0], ypos[0], width, height, refreshRate);
		} else {
			System.out.println("Could not retrieve video mode for monitor: " + monitor.name);
		}

		if (width > 0 && height > 0) {
			if (isFullScreen) {
				
				Gdx.graphics.setWindowedMode(width, height);
				// Set window to borderless fullscreen
				window.setPosition(xpos[0], ypos[0]); // Position the window at the monitor's (0,0)
				// window.setSizeLimits(width, height, width, height); // Set the window size to
				// the monitor's resolution
			} else {
				// For windowed mode, set specific size limits
				Gdx.graphics.setWindowedMode(WIDTH, HEIGHT);
				// window.setSizeLimits(Lwjgl3Launcher.WIDTH,
				// Lwjgl3Launcher.HEIGHT,Lwjgl3Launcher.WIDTH, Lwjgl3Launcher.HEIGHT); // Set
				// windowed mode size
				// window.setPosition(100, 100); // Optional, set to desired position
			}
		} else {
			System.out.println("Error: Could not retrieve valid width/height for the monitor.");
		}
		alignAllWidgets();
	}

	public void markForDestruction(Widget widget) {
		widgets_to_destroy.add(widget);
		for (Widget w : widget.getDescendants()) {
			widgets_to_destroy.add(w);
		}
	}

	public static BitmapFont getDefaultFont() {
		return primary_font;
	}

	public static Style getDefaultStyle() {
		if(default_style==null) {
			default_style = new Style(primary_font);
		}
		return default_style;
	}

}
