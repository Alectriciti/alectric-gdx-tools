package com.alectriciti.gdx;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
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
public class WidgetManager implements InputProcessor {

	public Color COLOR_BUTTON_ACTIVATED = Color.GREEN;
	public Color COLOR_BUTTON_PRESSING = Color.WHITE;
	public Color COLOR_BUTTON_DEFAULT = Color.GRAY;
	
	public static final float EDIT_HANDLE_HEIGHT = 8;
	
	
	boolean mouse_is_down;

	Widget mouse_adjusting_widget = null;
	Button mouse_clicked_button = null;

	private float mouse_config_offset_x;
	private float mouse_config_offset_y;
	
	

	List<Canvas> canvases = new ArrayList<Canvas>();
	List<Canvas> canvases_active = new ArrayList<Canvas>();
	private Canvas canvas_focused;
	private int global_canvas_z = 0;
	
	public int ui_tick = 0;

	int mouseScrollOffset = 0; // add this to your WidgetManager or UI state
	private float scrollSelectionOffset;
	
	
	int mouse_x, mouse_y;
	
	/**
	 * These references exist globally to allow for extra functionality
	 */
	public List<Widget> widgets = new ArrayList<Widget>();
	public List<Button> buttons = new ArrayList<Button>();
	protected List<Button> buttons_rapidfiring = new ArrayList<Button>();
	public Map<String, Button> buttons_by_name = new HashMap<String, Button>();
	public Map<Integer, Button> buttons_by_key = new HashMap<Integer, Button>();
	
	public WidgetManager(InputMultiplexer input) {
		input.addProcessor(this);
	}
	
	public void focus(Canvas canvas) {
		if(canvas!=null) {
			global_canvas_z++;
			//TODO Do extra checks such as unclicking buttons etc.
			//mouse_adjusting_widget = null;
			canvas_focused = canvas;
			canvas_focused.z = global_canvas_z;
			canvases.sort(Comparator.comparingInt(Widget::getZIndex));
			print("Canvas focused: "+canvas.name);
		}
	}
	
	boolean edit_mode;
	private boolean edit_mode_pressed; // a lock mechanism
	boolean constraint_mode;
	int constraint_amount = 4;
	
	Widget widget_hovering;
	
	private void setWidgetSelectionCandidate(Widget widget_to_set_hovering) {
		if(widget_hovering!=null) {
			widget_hovering.hovering = false;
		}
		if(widget_to_set_hovering!=null) {
			widget_hovering = widget_to_set_hovering;
			widget_hovering.hovering = true;
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
							//moving button config mode
							mouse_adjusting_widget = widget_hovering;
							mouse_config_offset_x = widget_hovering.getGlobalX()-mouse_x;
							mouse_config_offset_y = widget_hovering.getGlobalY()-mouse_y;
							if(widget_hovering instanceof Canvas) {
								focus((Canvas) widget_hovering);
							}
						}else {
							widget_hovering.callOnClicked(); //API call
						}
					}
				}
			}
			
			
			//while mouse is held and adjusting widget
			if(mouse_adjusting_widget!=null) {
				//moves the widget around
				if(constraint_mode) {
					mouse_adjusting_widget.setGlobalPosition(
							
							
							((mouse_x+(int)(mouse_config_offset_x))/constraint_amount)*constraint_amount,
							((mouse_y+(int)(mouse_config_offset_y))/constraint_amount)*constraint_amount);
				}else {
					mouse_adjusting_widget.setGlobalPosition((int)(mouse_x+mouse_config_offset_x), (int)(mouse_y+mouse_config_offset_y));
				}
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
			
			
			if(mouse_adjusting_widget!=null) {
				mouse_adjusting_widget = null;
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
	private void HoverMouseLogic() {

		
		
		if(!mouse_is_down) {
			/**
			 * Main Hover Logic... this FETCHES The widget selection candidate
			 */
			boolean found = false;
			int total = canvases.size();
			int offset = (int) (scrollSelectionOffset % total); // Wrap safely
			for (int o = 0; o < total; o++) {
			    int i = (total - 1 - o + offset + total) % total; // Rotate from topmost down with offset
			    //print(""+i);
			    Canvas canvas = canvases.get(i);
				if(canvas.visible) {
					//first check children widgets
					for(Widget w : canvas.widgets) {
						if(w.isVisible() && w.containsGlobal(mouse_x, mouse_y)) {
							setWidgetSelectionCandidate(w);
							found = true;
							break;
						}
					}
					if(found) {
						break;
					}
					if(canvas.containsGlobal(mouse_x, mouse_y)) {
						//then search through the canvases
						setWidgetSelectionCandidate(canvas);
						found = true;
						break;
					}
				}
			}
			
			//then search the stragglers (which have not been added to any canvas)
			if(!found) {
				for(Widget w : widget_orphans) {
					if(w.containsGlobal(mouse_x, mouse_y)) {
						setWidgetSelectionCandidate(w);
						return;
					}
				}
				setWidgetSelectionCandidate(null);
				//if(canvas_focused.containsGlobal(mouse_x, mouse_y)) {
					//setWidgetSelectionCandidate(canvas_focused);
				//}else {
				//}
				
			}
		}
	}

	/**
	 * Renders all canvases in the order they were created
	 * @param renderer
	 */
	public void renderAll(ShapeRenderer shape_renderer, SpriteBatch sprite_batch, BitmapFont font) {
		

		shape_renderer.begin();
		
		for(Widget w : widget_orphans) {
			w.drawShape(shape_renderer);
		}
		shape_renderer.end();
		

		sprite_batch.begin();
		for(Widget w : widget_orphans) {
			if(w.texture!=null) {
				w.drawTexture(sprite_batch);
			}
		}
		sprite_batch.end();

		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		if(font!=null) {
			for(Canvas canvas : canvases) {
				if(canvas.visible) {
					shape_renderer.begin();
					canvas.drawShape(shape_renderer);
					shape_renderer.end();
					sprite_batch.begin();
					canvas.drawFont(sprite_batch, font);
					sprite_batch.end();
				}
			}
		}else {
			for(Canvas canvas : canvases) {
				if(canvas.visible) {
					shape_renderer.begin();
					canvas.drawShape(shape_renderer);
					shape_renderer.end();
				}
			}
		}
	}
	
	/**
	 * Renders all canvases in the order they were created
	 * @param renderer
	 */
	public void renderShapes(ShapeRenderer shape_renderer) {
		for(Canvas canvas : canvases) {
			if(canvas.visible) {
				canvas.drawShape(shape_renderer);
			}
		}
		for(Widget w : widget_orphans) {
			w.drawShape(shape_renderer);
		}
	}


	public void registerCanvas(Canvas canvas) {
		this.canvases.add(canvas);
		if(this.canvas_focused == null) {
			//ensures there's an initialization of canvases
			this.canvas_focused = canvas;
		}
	}
	
	
	boolean debug_mode;
	
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

	public void importFiles(String[] files) {
		if(files[0]!=null) {
			String s = files[0];
			print(s);
			File f = new File(s);
			if(f.isDirectory()) {
				
			}else if(f.getName().endsWith(".png")) {
				Button b = new Button(f.getName(), 0, this);
				b.setGlobalPosition(getMouseX(), getMouseY());
				b.setTexture(new FileHandle(f));
			}
		}
	}
	
	public List<Widget> widget_orphans = new ArrayList<Widget>();

	public void registerWidget(Widget widget) {
		widget_orphans.add(widget);
		
		if(widget instanceof Button) {
			Button b = (Button)widget;
			buttons.add(b);
			buttons_by_name.put(b.name, b);
			buttons_by_key.put(b.key, b);
		}
	}

	public void dispose() {
		for(Widget w : widgets) {
			w.texture.dispose();
		}
	}
	
	

}
