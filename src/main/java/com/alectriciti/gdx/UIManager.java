package com.alectriciti.gdx;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.alectriciti.gdx.Button.Type;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import static com.alectriciti.gdx.Toolkit.*;


/**
 * A widget manager can be instantiated to automatically run logic and or render it
 */
public class UIManager implements InputProcessor {

	public Color COLOR_BUTTON_ACTIVATED = Color.GREEN;
	public Color COLOR_BUTTON_PRESSING = Color.WHITE;
	public Color COLOR_BUTTON_DEFAULT = Color.GRAY;
	
	public static final float EDIT_HANDLE_HEIGHT = 8;
	
	
	boolean mouse_is_down;
	boolean debug_mode = true;
	

	/**
	 * This is the widget that is currently being adjusted and dragged around (the red outline)
	 */
	Widget widget_currently_adjusting = null;
	Canvas canvas_proposed_to_attach = null;
	
	/**
	 * This is the button that is currently being clicked
	 */
	Button mouse_clicked_button = null;

	private float mouse_config_offset_x;
	private float mouse_config_offset_y;
	
	BitmapFont font;
	boolean font_activated = false;

	List<Canvas> canvases = new ArrayList<Canvas>();
	List<Canvas> canvases_active = new ArrayList<Canvas>();
	private Canvas canvas_focused;
	int global_canvas_z = 0;
	
	public int ui_tick = 0;

	int mouseScrollOffset = 0; // add this to your WidgetManager or UI state
	private float scrollSelectionOffset;
	
	
	int mouse_x, mouse_y;
	
	/**
	 * These references exist globally to allow for extra functionality
	 */
	public HashSet<Widget> widgets = new HashSet<Widget>();
	public HashSet<Widget> widget_orphans = new HashSet<Widget>();
	
	
	public List<Button> buttons = new ArrayList<Button>();
	protected List<Button> buttons_rapidfiring = new ArrayList<Button>();
	public Map<String, Button> buttons_by_name = new HashMap<String, Button>();
	public Map<Integer, Button> buttons_by_key = new HashMap<Integer, Button>();
	
	public UIManager(InputMultiplexer input) {
		input.addProcessor(this);
	}
	
	public void focus(Canvas canvas) {
		if(canvas!=null) {
			global_canvas_z++;
			//TODO Do extra checks such as unclicking buttons etc.
			//mouse_adjusting_widfget = null;
			canvas_focused = canvas;
			canvas_focused.pushNewZPosition(true);
			canvases.sort(Comparator.comparingInt(Widget::getZIndex));
			print("Canvas focused: "+canvas.name);
		}
	}
	
	boolean edit_mode;
	private boolean edit_mode_pressed; // a lock mechanism
	boolean constraint_mode;
	int constraint_amount = 4;
	
	Widget widget_hovering;
	
	private void setWidgetSelectionCandidate(Widget widget_to_assign) {
		if(widget_hovering!=null) {
			widget_hovering.hovering = false;
		}
		if(widget_to_assign!=null) {
			widget_hovering = widget_to_assign;
			widget_hovering.hovering = true;
			//print("New Candidate: "+widget_hovering.name);
		}else {
			widget_hovering = null;
		}
	}
	
	/**
	 * Handles input for Buttons and Mouse
	 */
	public void update() {
		//Mouse handling
		ui_tick++;
		
		//mouseScrollOffset += (scroll_y/4);s
		
		edit_mode = Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT);
		constraint_mode = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Keys.SHIFT_RIGHT);
		
		if(edit_mode) {
			edit_mode_pressed = true; 
		}else {
			if(edit_mode_pressed) {
				edit_mode_pressed = false;
				scrollSelectionOffset = 0;
			}
		}
		
		for(Widget w : widgets) {
			w.update();
		}
		
		/*
		if(canvas_focused == null) {
			canvas_focused = canvases.get(0);
			if(canvas_focused == null) {
				printLibError("canvas_focused == null! use WidgetManager.focus(canvas)");
				return;
			}
		}
		*/
		
		mouse_x = getMouseX();
		mouse_y = getMouseY();
		
		if(Gdx.input.isButtonPressed(Buttons.LEFT)) {
			mouse_is_down = true;
			
			if(mouse_clicked_button == null) {
				/*
				 * No Button is clicked yet
				 */
				if(Gdx.input.isButtonJustPressed(Buttons.LEFT)) {
					if(widget_hovering!=null) {
						if(edit_mode) {
							
							if(widget_hovering.isEditable()) {
								//moving button config mode
								widget_currently_adjusting = widget_hovering;
								mouse_config_offset_x = widget_hovering.getGlobalX()-mouse_x;
								mouse_config_offset_y = widget_hovering.getGlobalY()-mouse_y;
								if(widget_hovering instanceof Canvas) {
									focus((Canvas) widget_hovering);
								}else if(widget_hovering.parent instanceof Canvas) {
									focus((Canvas) widget_hovering.parent);
								}
							}
						}else {
							widget_hovering.callOnClicked(); //API call
						}
					}
				}
			}
			
			
			//while mouse is held and adjusting widget
			if(widget_currently_adjusting!=null) {
				//moves the widget around
				AdjustWidgetPosition();
				
			}if(mouse_clicked_button!=null) { //If a button is already selected
				
				
				/*
				 * IF mouse moves out of position while held...
				 */
				if(!mouse_clicked_button.containsGlobal(mouse_x, mouse_y)) {
					mouse_clicked_button.pressing = false;
					if(mouse_clicked_button.type==Type.RAPIDFIRE) {
						buttons_rapidfiring.remove(mouse_clicked_button);
						mouse_clicked_button.deactivate();
					}
					mouse_clicked_button = null; //finally nullify clicked button
					//print("deselected");
				}
			}else{ // Just now selecting a new widget
				
				}
		}else if(mouse_is_down) {
			//RELEASE MOUSE EVENT
			//print("released");
			mouse_is_down = false;
			
			
			if(widget_currently_adjusting!=null) {
				widget_currently_adjusting = null;
			}
			if(mouse_clicked_button != null) {
				if(mouse_clicked_button.is_key_down) {
					mouse_clicked_button.pressing = false;
					mouse_clicked_button.cancelled = true;
				}else {
					switch(mouse_clicked_button.type) {
					case PRESS:
						mouse_clicked_button.activate();
						break;
					case RAPIDFIRE:
						buttons_rapidfiring.remove(mouse_clicked_button);
						mouse_clicked_button.deactivate();
						break;
					case PRESS_AND_RELEASE:
						mouse_clicked_button.deactivate();
						break;
					case TOGGLE:
						if(!mouse_clicked_button.activated) {
							mouse_clicked_button.activate();
							print(mouse_clicked_button.name+ " ACTIVATED");
						}else {
							mouse_clicked_button.deactivate();
							print(mouse_clicked_button.name+ " DEACTIVATED");
						}
						break;
					}
				}
				mouse_clicked_button.pressing = false;
				mouse_clicked_button = null;
			}
			if(widget_hovering!=null) {
				widget_hovering.hovering = false;
				widget_hovering = null;
			}
			scrollSelectionOffset = 0;
			//print("releasing... all should be set to null");
			
			//release all buttons, trigger button if still highlighted
			for(Button b : buttons) {
				b.pressing = false;
			}
		}
		
		
		HoverMouseLogic();
		
	}
	private void AdjustWidgetPosition() {
		if(constraint_mode) {
			widget_currently_adjusting.setGlobalPosition(
					((mouse_x+(int)(mouse_config_offset_x))/constraint_amount)*constraint_amount,
					((mouse_y+(int)(mouse_config_offset_y))/constraint_amount)*constraint_amount);
		}else {
			widget_currently_adjusting.setGlobalPosition((int)(mouse_x+mouse_config_offset_x), (int)(mouse_y+mouse_config_offset_y));
		}
	}

	private void HoverMouseLogic() {

		
		
		if(!mouse_is_down) {
			
			//canvas_proposed_to_attach
			
			
			
			Widget widget_to_highlight = getSelectableWidgetAtPosition(mouse_x, mouse_y);
			if(widget_to_highlight!=null) {
				setWidgetSelectionCandidate(widget_to_highlight);
			}
		}
	}

	/**
	 * Main Hover Logic... this FETCHES The widget selection candidate
	 */
	private Widget getSelectableWidgetAtPosition(int mouseX, int mouseY) {
		//Widget widget = null;
		//boolean found = false;
		
		List<Widget> found_widgets = getAllWidgetsAtLocation(mouseX, mouseY);

		if(found_widgets.isEmpty()) {
			//print("found widgets is empty");
			return null;
		}

		int total_widgets = found_widgets.size();
		
		if(edit_mode) {
			int offset = (int) (scrollSelectionOffset % total_widgets); // Wrap safely
			
	
			int i = (total_widgets - 1 + offset + total_widgets) % total_widgets;
			return found_widgets.get(i);
		}else {
			return found_widgets.get(total_widgets-1);
		}
		/*
		for (int o = 0; o < total_widgets; o++) {
		    int i = (total_widgets - 1 - o + offset + total_widgets) % total_widgets; // Rotate from topmost down with offset
		    //print(""+i);
		    Widget canvas = canvases.get(i);
			
		    //for(Widget w : canvas.getAllChildren()) {
			//	if(w.isVisible() && w.containsGlobal(getMouseX(), getMouseY())) {
			//		//return w;
			//	}
			//}
		}
		*/
		//then search the stragglers (which have not been added to any canvas)
		/*
		if(!found) {
			for(Widget w : widget_orphans) {
				if(w.containsGlobal(mouse_x, mouse_y)) {
					return w;
				}
			}
			setWidgetSelectionCandidate(null);
			//if(canvas_focused.containsGlobal(mouse_x, mouse_y)) {
				//setWidgetSelectionCandidate(canvas_focused);
			//}else {
			//}
			
		}
		*/
		//return widget;
	}

	private List<Widget> getAllWidgetsAtLocation(int mouseX, int mouseY) {
		List<Widget> found_widgets = new ArrayList<Widget>();
		for(Widget w : widgets) {
			if(w.isVisible()) {
				if(w.containsGlobal(mouseX, mouseY)) {
					found_widgets.add(w);
				}
			}
		}
		found_widgets.sort(Comparator.comparingInt(Widget::getZIndex));
		return found_widgets;
	}

	/**
	 * Renders all canvases in the order they were created
	 * @param renderer
	 */
	public void renderAll(ShapeRenderer shape_renderer, SpriteBatch sprite_batch, BitmapFont font) {
		
		boolean font_valid = font!=null;


		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

		
		/*
		 * START WITH ORPHANS
		 */
		shape_renderer.begin();
		for(Widget w : widget_orphans) {
			w.drawShape(shape_renderer, true);
		}
		shape_renderer.end();
		sprite_batch.begin();
		if(font_valid) {
			for(Widget w : widget_orphans) {
				w.drawFont(sprite_batch, font, true);
			}
		}
		Gdx.gl.glEnable(GL20.GL_BLEND);
		for(Widget w : widget_orphans) {
			if(w.texture!=null) {
				w.drawTexture(sprite_batch);
			}
		}
		
		if(debug_mode) {
			
		}

		font.draw(sprite_batch, "Widget Scroll Selector: "+scrollSelectionOffset, 20, 500);
		font.draw(sprite_batch, "Widget Candidate "+(widget_hovering!=null?widget_hovering:"null"), 20, 524);
		sprite_batch.end();
		Gdx.gl.glEnable(GL20.GL_BLEND);
		
		
		/*
		 * Now Canvas Groups
		 */
		if(font!=null) {
			for(Canvas canvas : canvases) {
				if(canvas.visible) {
					shape_renderer.begin();
					canvas.drawShape(shape_renderer, true);
					shape_renderer.end();
					sprite_batch.begin();
					canvas.drawFont(sprite_batch, font, true);
					sprite_batch.end();
					Gdx.gl.glEnable(GL20.GL_BLEND);
				}
			}
		}

		//Draw this ontop to allow for visibility
		/*
		if(widget_currently_adjusting!=null) {
			shape_renderer.begin();
			widget_currently_adjusting.drawShape(shape_renderer, false);
			widget_currently_adjusting.drawEditMode(shape_renderer, true);
			shape_renderer.end();
			sprite_batch.begin();
			widget_currently_adjusting.drawFont(sprite_batch, font, false);
			sprite_batch.end();
		}
		*/
	}
	
	/*
	public void renderShapes(ShapeRenderer shape_renderer) {
		for(Canvas canvas : canvases) {
			if(canvas.visible) {
				canvas.drawShape(shape_renderer);
			}
		}
		for(Widget w : widget_orphans) {
			w.drawShape(shape_renderer, true);
		}
		
		//Draw this ontop to allow for visibility
		if(widget_currently_adjusting!=null) {
			widget_currently_adjusting.drawEditMode(shape_renderer, false);
		}
	}
	*/


	public void registerCanvas(Canvas canvas) {
		this.canvases.add(canvas);
		if(this.canvas_focused == null) {
			//ensures there's an initialization of canvases
			this.canvas_focused = canvas;
		}
	}
	
	
	/**
	 * Draws wireframes around widgets
	 * @param mode
	 */
	public void setDebugMode(boolean mode) {
		debug_mode = mode;
	}

	@Override
	public boolean keyDown(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
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
		return false;
	}
	
	
	/**
	 * @param file_path If linked to a .png, it will load a new button with that png as the image. If it's a folder, it will open that folder on desktop.
	 */
	public Button importFileAsButton(String file_path) {
		if(file_path!=null) {
			File file = new File(file_path);
			Button b = null;
			if(file.isDirectory()) {
				b = new Button(file.getName(), 0, this) {
					
					File f = file;
					@Override
					protected void onActivate() {
						try {
							Desktop.getDesktop().open(f);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						super.onActivate();
					}
				};
				b.setGlobalPosition(getMouseX(), getMouseY());
			}else if(file.getName().endsWith(".png")) {
				b = new Button(file.getName(), 0, this);
				b.setGlobalPosition(getMouseX(), getMouseY());
				b.setTexture(new FileHandle(file));
				return b;
			}
		}
		return null;
	}
	
	/**
	 * Registers a widget by adding it to appropriate lists and sets
	 * @param widget to be registered
	 */
	void registerWidget(Widget widget) {
		
		if(widget.getParent()==null) {
			widget_orphans.add(widget);
		}
		
		widgets.add(widget);
		
		if(widget instanceof Button) {
			Button b = (Button)widget;
			buttons.add(b);
			buttons_by_name.put(b.name, b);
			buttons_by_key.put(b.key, b);
		}
	}
	
	
	/**
	 * This should be called on shutdown to dispose of widget-related and other resources
	 */
	public void dispose() {
		for(Widget w : widgets) {
			w.dispose();
		}
	}
	
	
	
	

}
