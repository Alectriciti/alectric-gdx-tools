package com.alectriciti.gdx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import com.alectriciti.gdx.Button.ButtonType;
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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import static com.alectriciti.gdx.Toolkit.*;

/**
 * A widget manager can be instantiated to automatically run logic and or render
 * it
 */
public class UIManager implements InputProcessor {

	public Color COLOR_BUTTON_ACTIVATED = Color.GREEN;
	public Color COLOR_BUTTON_PRESSING = Color.GRAY;
	public Color COLOR_BUTTON_DEFAULT = new Color(0.05f, 0.05f, 0.05f, 1);

	public static final float EDIT_HANDLE_HEIGHT = 8;

	boolean mouse_is_down;
	boolean debug_mode = true;

	/**
	 * This is the widget that is currently being adjusted and dragged around (the
	 * red outline)
	 */
	Widget widget_currently_adjusting = null;
	Canvas canvas_proposed_to_attach = null;

	/**
	 * This is the button that is currently being clicked
	 */
	Widget mouse_clicked_widget = null;

	private float mouse_config_offset_x;
	private float mouse_config_offset_y;

	static BitmapFont primary_font;
	boolean font_activated = false;

	// List<Canvas> canvases = new ArrayList<Canvas>();
	List<Widget> widget_independants = new ArrayList<Widget>();
	List<Canvas> canvases_active = new ArrayList<Canvas>();
	Widget widget_focused;
	int global_canvas_z = 0;

	public int ui_tick = 0;

	int mouseScrollOffset = 0; // add this to your WidgetManager or UI state
	private float scrollSelectionOffset;

	int WIDTH, HEIGHT;

	int mouse_x, mouse_y;

	/**
	 * These references exist globally to allow for extra functionality
	 */
	public HashSet<Widget> widgets = new HashSet<Widget>();
	// public HashSet<Widget> widget_orphans = new HashSet<Widget>();

	private List<Widget> widgets_to_destroy = new ArrayList<Widget>();

	public List<Button> buttons = new ArrayList<Button>();
	public List<Button> buttons_rapidfiring = new ArrayList<Button>();
	public Map<String, Button> buttons_by_name = new HashMap<String, Button>();
	public Map<Integer, Button> buttons_by_key = new HashMap<Integer, Button>();
	
	private InputMultiplexer input_multiplexer;

	public UIManager(InputMultiplexer input, BitmapFont font) {
		input_multiplexer = input;
		input_multiplexer.addProcessor(this);
		primary_font = font;
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

	public void focus(Widget new_widget) {
		if (new_widget == null) {
			if(widget_focused!=null) {
				if(widget_focused instanceof InputProcessor) {
					input_multiplexer.removeProcessor(((InputProcessor) widget_focused));
				}
				widget_focused.focused = false;
				widget_focused = null;
				print("Unfocused");
			}
		} else {
			if (widget_focused != null) {
				if (widget_focused == new_widget) {
					return; // do nothing, focused widget is the same
				}
				widget_focused.focused = false; // unset the previous widget
			}
			global_canvas_z++;
			// TODO Do extra checks such as unclicking buttons etc.
			// mouse_adjusting_widfget = null;
			widget_focused = new_widget;
			widget_focused.pushNewZPosition(true);
			widget_independants.sort(Comparator.comparingInt(Widget::getZIndex));
			widget_focused.focused = true;
			if(widget_focused instanceof InputProcessor) {
				input_multiplexer.addProcessor(0, (InputProcessor) widget_focused);
			}
			widget_focused.callOnFocus();
			print("Widget focused: " + new_widget.name_for_display);
		}
	}

	boolean edit_mode;
	private boolean edit_mode_pressed; // a lock mechanism
	boolean constraint_mode;
	int constraint_amount = 4;

	Widget widget_hovering;

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
			// print("New Candidate: "+widget_hovering.name);
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

		// mouseScrollOffset += (scroll_y/4);s

		edit_mode = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		constraint_mode = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);

		if (edit_mode) {
			edit_mode_pressed = true;
		} else {
			if (edit_mode_pressed) {
				edit_mode_pressed = false;
				scrollSelectionOffset = 0;
			}
		}

		if (!widgets_to_destroy.isEmpty()) {
			widgets.removeAll(widgets_to_destroy);
			// widget_orphans.removeAll(widgets_to_destroy);
			widget_independants.removeAll(widgets_to_destroy);
			buttons.removeAll(widgets_to_destroy);

			for (Widget wtd : widgets_to_destroy) {
				if (wtd instanceof Button) {
					Button b = (Button) wtd;
					buttons_by_key.remove(b.key_code);
					buttons_by_name.remove(b.name_for_display);
				}
			}
			for (Widget w : widgets) {
				w.widgets.removeAll(widgets_to_destroy);
			}

			widgets_to_destroy.clear();
		}

		for (Button b : buttons_rapidfiring) {
			if (ui_tick % b.rapidfire_frequency == 0) {
				b.activate();
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

		mouse_x = getMouseX();
		mouse_y = getMouseY();

		if (Gdx.input.isButtonPressed(Buttons.LEFT)) {
			left_click_down();
		} else if (mouse_is_down) {
			left_click_release();
		}

		HoverMouseLogic();

	}

	private void left_click_down() {
		mouse_is_down = true;

		if (mouse_clicked_widget == null) {
			/*
			 * No Button is clicked yet
			 */
			if (Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
				if (widget_hovering != null) {
					if (edit_mode || widget_hovering.isAlwaysEditable()) {

						if (widget_hovering.isEditable()) {
							// moving button config mode
							widget_currently_adjusting = widget_hovering;
							mouse_config_offset_x = widget_hovering.getGlobalX() - mouse_x;
							mouse_config_offset_y = widget_hovering.getGlobalY() - mouse_y;
						}
					} else {
						widget_hovering.focus();
						mouse_clicked_widget = widget_hovering;
						widget_hovering.callOnClicked(); // API call
					}
				} else {
					focus(null);
				}
			}
		}

		// while mouse is held and adjusting widget
		if (widget_currently_adjusting != null) {
			// moves the widget around
			AdjustWidgetPosition();

		}
		if (mouse_clicked_widget != null) { // If a button is already selected

			/*
			 * IF mouse moves out of position while held...
			 */
			if (mouse_clicked_widget instanceof Button) {
				Button button_clicked = (Button) mouse_clicked_widget;
				if (!button_clicked.containsGlobal(mouse_x, mouse_y)) {
					button_clicked.pressing = false;
					if (button_clicked.button_type == ButtonType.RAPIDFIRE) {
						buttons_rapidfiring.remove(button_clicked);
						button_clicked.deactivate();
					}
					mouse_clicked_widget = null; // finally nullify clicked button
					// print("deselected");
				}
			}
		} else { // Just now selecting a new widget

		}
	}

	private void left_click_release() {
		// RELEASE MOUSE EVENT
		// print("released");
		mouse_is_down = false;

		if (widget_currently_adjusting != null) {
			widget_currently_adjusting = null;
		}
		if (mouse_clicked_widget == null) {
//			focus(null);
		} else {
			if (mouse_clicked_widget instanceof Button) {
				Button button_clicked = (Button) mouse_clicked_widget;
				if (button_clicked.is_key_down) {
					button_clicked.pressing = false;
					button_clicked.cancelled = true;
				} else {
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
							print(button_clicked.name_for_display + " ACTIVATED");
						} else {
							button_clicked.deactivate();
							print(button_clicked.name_for_display + " DEACTIVATED");
						}
						break;
					}
				}
				button_clicked.pressing = false;
			}
			mouse_clicked_widget.callOnReleased();
			mouse_clicked_widget = null;
		}
		if (widget_hovering != null) {
			widget_hovering.hovering = false;
			widget_hovering = null;
		}
		scrollSelectionOffset = 0;
		// print("releasing... all should be set to null");

		// release all buttons, trigger button if still highlighted
		for (Button b : buttons) {
			b.pressing = false;
		}
	}

	private void AdjustWidgetPosition() {
		if (widget_currently_adjusting instanceof WindowMoverWidget) {
			// move window logic
			WindowMoverWidget window = (WindowMoverWidget) widget_currently_adjusting;
			window.moveWindow((int) mouse_config_offset_x, (int) mouse_config_offset_y);

		} else {
			if (constraint_mode) {
				widget_currently_adjusting.setGlobalPosition(
						((mouse_x + (int) (mouse_config_offset_x)) / constraint_amount) * constraint_amount,
						((mouse_y + (int) (mouse_config_offset_y)) / constraint_amount) * constraint_amount);
			} else {
				widget_currently_adjusting.setGlobalPosition((int) (mouse_x + mouse_config_offset_x),
						(int) (mouse_y + mouse_config_offset_y));
			}
		}
	}

	private void HoverMouseLogic() {
		if (!mouse_is_down) {

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
		for (Widget w : widgets) {
			if (w.isVisible() && w.isTouchable()) {
				if (w.containsGlobal(mouseX, mouseY)) {
					found_widgets.add(w);
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

		boolean font_valid = primary_font != null;

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
			if (widget.visible) {
				shape_renderer.begin();
				widget.drawShape(shape_renderer, true);
				shape_renderer.end();
				sprite_batch.begin();
				widget.drawTexture(sprite_batch, true);
				widget.drawFont(sprite_batch, primary_font, true);
				sprite_batch.end();
				Gdx.gl.glEnable(GL20.GL_BLEND);
			}
		}

		if (widget_hovering != null) {
			shape_renderer.begin();
			if (edit_mode && widget_hovering.editable) {
				widget_hovering.drawEditMode(shape_renderer, false);
			} else if (widget_hovering.isHoverable()) {
				shape_renderer.set(ShapeType.Line);
				shape_renderer.setColor(widget_hovering.color_trim_highlight);
				shape_renderer.rect(widget_hovering.getGlobalX(), widget_hovering.getGlobalY(),
						widget_hovering.shape.width, widget_hovering.shape.height);
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

		if (buttons_by_key.containsKey(keycode)) {
			Button b = buttons_by_key.get(keycode);
			// if(!b.visible) {
			// return false;
			// }

			switch (b.button_type) {
			case RAPIDFIRE:
				b.is_key_down = true;
				if (b.cancelled) {
					b.pressing = false;
				} else {
					b.pressing = true;
					b.activate();
					buttons_rapidfiring.add(b);
				}
				break;
			case PRESS:
				b.is_key_down = true;
				if (b.cancelled) {
					b.pressing = false;
				} else {
					b.pressing = true;
				}
				break;
			case PRESS_AND_RELEASE:
				if (!b.is_key_down) {
					b.is_key_down = true;
					if (b.cancelled) {
						b.pressing = false;
					} else {
						b.pressing = true;
						b.activate();
					}
				}
				break;
			case TOGGLE:
				if (!b.activated) {
					b.activate();
					b.activated = true;
					print(b.name_for_display + " ACTIVATED");
				} else {
					b.deactivate();
					b.activated = false;
					print(b.name_for_display + " DEACTIVATED");
				}
				break;
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
					button.setTexture(file);
					return true;
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

		if (widget.getParent() == null) {
			// widget_orphans.add(widget);
			widget_independants.add(widget);
		}

		widgets.add(widget);
	}

	String folder_widgets = "widgets/";

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
			if (w.getId() != null) {
				FileHandle widgetFile = Gdx.files.local(folder_widgets + w.getId() + ".json");
				Json json = new Json();
				widgetFile.writeString(w.saveToJson().toString(), false);
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

	public void setfullscreenMode(boolean isFullScreen) {
		if (isFullScreen) {
			WIDTH = Gdx.graphics.getWidth();
			HEIGHT = Gdx.graphics.getHeight();
		}

		Lwjgl3Graphics graphics = (Lwjgl3Graphics) Gdx.graphics;
		Lwjgl3Window window = graphics.getWindow();
		Monitor monitor = graphics.getMonitor(); // Get the current monitor

		int width = 0;
		int height = 0;
		int[] xpos = new int[1];
		int[] ypos = new int[1];

		long monitorHandle = ((Lwjgl3Monitor) monitor).getMonitorHandle();

		// Get monitor position
		GLFW.glfwGetMonitorPos(monitorHandle, xpos, ypos);
		GLFWVidMode vidMode = GLFW.glfwGetVideoMode(monitorHandle);

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
		for (Widget w : widget.getAllChildren()) {
			widgets_to_destroy.add(w);
		}
	}

	public static BitmapFont getDefaultFont() {
		return primary_font;
	}

}
