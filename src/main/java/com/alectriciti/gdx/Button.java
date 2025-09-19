package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.awt.Desktop;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.JsonValue;


/**
 * A Button widget which can be added directly to a canvas, or added standalone
 * @author alectriciti
 */
public class Button extends Widget implements Activatable{
	
	
	public enum ButtonType{
		PRESS,
		PRESS_AND_RELEASE,
		TOGGLE,
		RAPIDFIRE
	}

	transient List<Runnable> run_on_activate = new ArrayList<Runnable>();
	transient List<Runnable> run_on_deactivate = new ArrayList<Runnable>();
	
	public transient Runnable hyperlink = null;
	public FileHandle hyperlink_file = null;
	
	public boolean pressing = false;
	public boolean activated = false;
	
	public int rapidfire_frequency = 8;
	
	public float effect_offset_start = 2;
	public static float effect_move_speed = 0.3333f;

	public Color color_default = manager.COLOR_BUTTON_DEFAULT;
	public Color color_pressing = manager.COLOR_BUTTON_PRESSING.cpy();
	public Color color_activated = manager.COLOR_BUTTON_ACTIVATED.cpy();
	public Color effect_color = new Color(Color.WHITE);
	
	public boolean play_effect = true;
	
	public EffectPulse effect;
	
	public ButtonType button_type = ButtonType.PRESS;

	public Point font_offset = new Point(2, 2);

	int[] button_codes; //used for unregistering buttons in UIManager for widget_to_destroy
	
	/**
	 * 
	 * @param button_name
	 * @param key
	 * @param canvas
	 */
	public Button(String button_name, Widget parent, int...button_codes) {
		super(button_name, parent);
		//super(wonkaMain, button_name);
		this.color = Color.WHITE;
		registerButton(button_codes);
		updateGlobalPosition();
	}
	
	
	public Button(String name, UIManager widgetManager, int...button_codes) {
		super(name, widgetManager);
		this.color = Color.WHITE.cpy();
		registerButton(button_codes);
		updateGlobalPosition();
	}
	
	public Button(String button_name, Widget parent) {
		this(button_name, parent, null);
	}
	
	public Button(String name, UIManager widgetManager) {
		this(name, widgetManager, null);
	}
	
	
	private void registerButton(int...button_codes) {
		manager.buttons.add(this);
		manager.buttons_by_name.put(name_for_display, this);
		
		if(button_codes!=null) {
			for(int code : button_codes) {
				manager.buttons_by_key.put(code, this);
			}
			this.button_codes = button_codes;
		}
	}
	
	
	@Override
	public void reloadAllData() {
		// TODO Auto-generated method stub
		super.reloadAllData();
		if(hyperlink_file!=null) {
			setHyperlink(hyperlink_file);
		}
		if(activated) {
			if(this instanceof DropdownMenuButton) {
				
			}else {
				//activates a button when reloaded, used for settings
				activate();
			}
		}
	}
	
	public Button setType(ButtonType type) {
		this.button_type = type;
		return this;
	}
	
	@Override
	public void setSize(float width, float height) {
		// TODO Auto-generated method stub
		super.setSize(width, height);
		//effect_rect.width = shape.getWidth();
		//effect_rect.height = shape.getHeight();
	}

	/**
	 * This activates the button
	 */
	public void activate() {
		if(button_type == ButtonType.TOGGLE) {
			activated = true;
		}
		if(doesPlayEffectOnClick()) {
			spawnButtonEffect(Color.GREEN);
		}
		
		onActivate();
		
		for(Runnable r : run_on_activate) {
			r.run();
		}
		if(hyperlink!=null) {
			hyperlink.run();
		}
	}
	
	/**
	 * This deactivates the button
	 */
	public void deactivate() {
		if(button_type == ButtonType.TOGGLE) {
			activated = false;
		}
		
		onDeactivate();
		
		for(Runnable r : run_on_deactivate) {
			r.run();
		}
	}

	/**
	 * The overrideable click event
	 */
	protected void onActivate() {
		
	}

	/**
	 * The overrideable click event
	 */
	protected void onDeactivate() {
		
	}
	
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
			
			if(pressing) {
				color = LerpColor(color, color_pressing, 0.5f);
			}else if(activated) {
				color = LerpColor(color, color_activated, 0.5f);
			}else {
				color = LerpColor(color, color_default, 0.25f);
				//color.set(color_default.r*d, color_default.g*d, color_default.b*d, color_default.a);
			}
		super.update();
	}
	
	public boolean is_key_down;
	public boolean cancelled;
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		
		if(visible) {
	
			renderer.set(ShapeType.Filled);
			renderer.setColor(color);
			renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
			
	
			if(!hovering) {
				renderer.set(ShapeType.Line);
				renderer.setColor(color_trim);
				renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
			}
			
		}
		
		drawShapeChildren(renderer, recursive);
	}

	
	protected boolean doesPlayEffectOnClick() {
		return play_effect;
	}

	
	protected void spawnButtonEffect(Color c) {
		
		if(visible) {
			effect = new EffectPulse(this, new Rectangle(shape_global), color_activated);
		}
		
		//effect_rect = new Rectangle(shape_global);
		//effect_color = c.cpy();
		//effect_rect_a = 1f;
		//effect_delta = 0;
	}

	public boolean drawTexture(SpriteBatch batch, boolean recursive) {
		boolean valid = texture != null;
		if (valid) {
			if(visible) {
				if (pressing) {
					batch.setColor(Color.GRAY);
				} else {
					batch.setColor(color_texture_alpha);
				}
				batch.draw(texture, getGlobalX(), getGlobalY() + 1, shape.width - 1, shape.height - 1);
			}
		}
		if(recursive) {
			drawTextureChildren(batch, recursive);
		}
		return valid;
	}
	
	@Override
	public boolean isHoverable() {
		return true;
	}

	public boolean drawFont(SpriteBatch batch, BitmapFont font, boolean recursive) {
		// TODO Auto-generated method stub
		if(visible) {
			if(pressing) {
				font.setColor(Color.DARK_GRAY);
			}else if (activated) {
				font.setColor(Color.BLACK);
			}else {
				font.setColor(color_texture_alpha);
			}
			font.draw(batch, name_for_display, getGlobalX()+font_offset.x, getGlobalY()+font.getCapHeight()+font_offset.y);
		}
		if(recursive) {
			drawFontChildren(batch, font, recursive);
		}
		return true;
	}
	
	
	@Override
	protected void OnMouseClicked() {
		if(!is_key_down) {
			//if button's keybind is not pressed
			pressing = true;
			if(button_type == ButtonType.PRESS_AND_RELEASE) {
				activate();
			}else if (button_type == ButtonType.RAPIDFIRE) {
				manager.buttons_rapidfiring.add(this); //start rapidfiring
			}
		}
		manager.mouse_clicked_widget = this; // new button clicked on!
	}
	
	public void addOnActivate(Runnable r) {
		this.run_on_activate.add(r);
	}
	
	public void addOnDeactivate(Runnable r) {
		this.run_on_deactivate.add(r);
	}



	public void setHyperlink(FileHandle file) {
		this.hyperlink_file = file;
		this.hyperlink = new Runnable() {
			
			@Override
			public void run() {
				if(hyperlink_file!=null && hyperlink_file.exists() && hyperlink_file.isDirectory()) {
					try {
						Desktop.getDesktop().open(hyperlink_file.file());
					} catch (IOException e) {
						e.printStackTrace();
						print("Error! Directory for missing or null, unassigning.");
					}
				}
			}
		};
	}
	
	@Override
	public JsonValue saveToJson() {
		// TODO Auto-generated method stub
		JsonValue json = super.saveToJson();
		json.get("type").set("button");
		
		if(hyperlink_file!=null) {
			json.addChild("hyperlink", new JsonValue(hyperlink_file.path()));
		}
		
		json.addChild("activated", new JsonValue(activated));
		
		return json;
	}
	
	
	@Override
	public void loadFromJson(JsonValue data) {
		// TODO Auto-generated method stub
		super.loadFromJson(data);
		
		if(data.has("hyperlink")) {
			this.hyperlink_file = new FileHandle(data.getString("hyperlink"));
		}
		
		if(data.has("activated")) {
			if(!(this instanceof DropdownMenuButton)) {
			this.activated = data.getBoolean("activated");
			}
		}
	}
}