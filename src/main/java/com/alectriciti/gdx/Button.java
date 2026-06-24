package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.awt.Desktop;
import java.awt.Point;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
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
	
	public boolean activated = false;
	
	public int pressed_ticks = 0;
	
	public int rapidfire_frequency = 8;
	public int rapidfire_start_delay = 8;
	
	public float effect_offset_start = 2;
	public static float effect_move_speed = 0.3333f;
	
	
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
		init(button_codes);
	}
	
	public Button(String name, UIManager widgetManager, int...button_codes) {
		super(name, widgetManager);
		init(button_codes);
	}
	
	private void init(int...button_codes) {
		this.color = Color.WHITE.cpy();
		this.render_text = true;
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
		
		focus();
		
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
				pressed_ticks++;
				color = LerpColor(color, style.color_press, 0.5f);
			}else if(activated) {
				color = LerpColor(color, style.color_activated, 0.5f);
			}else {
				if(pressed_ticks>0) {
					pressed_ticks=0;
				}
				color = LerpColor(color, style.color_base, 0.25f);
				//color.set(color_default.r*d, color_default.g*d, color_default.b*d, color_default.a);
			}
		super.update();
	}
	
	public boolean is_key_down;
	/**
	 * Whether or not the user can drag and drop a png to change the button's texture
	 */
	public boolean can_change_texture = true;
	public boolean texture_stretch = false; 
	
	@Override
	public void drawShape(ShapeRenderer renderer) {
		
		super.drawShape(renderer);
		if(isVisible()) {
			renderer.set(ShapeType.Filled);
			renderer.setColor(color);
			drawRectRound(renderer, getGlobalX(), getGlobalY(), shape.width, shape.height, style.corner_radius);
			
			if(!hovering) {
				drawBorder(renderer);
			}
		}
//		drawShapeChildren(renderer);
	}
	
//	@Override
//	public void drawBorder(ShapeRenderer shape_renderer) {
//		// TODO Auto-generated method stub
//		shape_renderer.set(ShapeType.Line);
//		shape_renderer.setColor(color_outline);
//		if(style.corner_radius<=0) {
//			shape_renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
//		}else {
//			drawRoundedRectLine(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height, style.corner_radius);
//		}
//	}
	
	protected boolean doesPlayEffectOnClick() {
		return play_effect;
	}

	
	protected void spawnButtonEffect(Color c) {
		
		if(isVisible()) {
			new EffectPulse(this, new Rectangle(shape_global), style.color_hover);
		}
		
		//effect_rect = new Rectangle(shape_global);
		//effect_color = c.cpy();
		//effect_rect_a = 1f;
		//effect_delta = 0;
	}

	@Override
	public boolean drawTexture(SpriteBatch batch, boolean recursive) {
		boolean valid = texture != null;
		if (valid) {
			if(isVisible()) {
				if (pressing) {
					batch.setColor(Color.GRAY);
				} else {
					batch.setColor(color_texture_alpha);
				}
				if(texture_stretch) {
					batch.draw(texture, getGlobalX(), getGlobalY() + 1, shape.width - 1, shape.height - 1);
				}else {
					float texW = texture.getWidth(), texH = texture.getHeight();
				    float scale = Math.min((shape.width - 1) / texW, (shape.height - 1) / texH);
				    float w = texW * scale, h = texH * scale;
//				    float x = getGlobalX() + (shape.width - w) / 2f;
//				    float y = getGlobalY() + (shape.height - h) / 2f + 1;
				    batch.draw(texture, getGlobalX(), getGlobalY(), w, h);
				}
			}
		}
		if(recursive) {
			drawTextureChildren(batch, recursive);
		}
		return valid;
	}
	
	public boolean cancelled;

	@Override
	public boolean isHoverable() {
		return true;
	}

	@Override
	public boolean drawFont(SpriteBatch batch, boolean recursive) {
		// TODO Auto-generated method stub
		if(isVisible()) {
			if(pressing) {
				style.font.setColor(style.color_text_pressed);
			}else if (activated) {
				style.font.setColor(style.color_text_activated);
			}else {
				style.font.setColor(color_texture_alpha);
			}
			if(render_text && name_for_display!=null) {
				style.font.draw(batch, name_for_display, getGlobalX()+font_offset.x, getGlobalY()+style.font.getCapHeight()+font_offset.y);
			}
		}
		if(recursive) {
			drawFontChildren(batch, recursive);
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