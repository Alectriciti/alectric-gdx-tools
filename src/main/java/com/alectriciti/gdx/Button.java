package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class Button extends Widget{
	
	int key;
	
	enum Type{
		PRESS,
		PRESS_AND_RELEASE,
		TOGGLE,
		RAPIDFIRE
	}

	boolean pressing = false;
	boolean activated = false;
	
	int rapidfire_speed = 2;
	
	public Type type = Type.PRESS;

	public Button(String button_name, int key, Canvas canvas) {
		super(button_name, canvas);
		//super(wonkaMain, button_name);
		this.key = key;
		this.color = Color.WHITE;
		updateGlobalPosition();
	}
	
	public Button(String name, int key, WidgetManager widgetManager) {
		super(name, widgetManager);
		this.key = key;
		this.color = Color.WHITE;
		updateGlobalPosition();
	}

	public Button setType(Type type) {
		this.type = type;
		return this;
	}

	/**
	 * This activates the button
	 */
	public void activate() {
		if(!visible) {
			return;
		}
		if(type == Type.TOGGLE) {
			activated = true;
		}
		System.out.println("["+name+"] activated");
		spawnEffect();
		onActivate();
	}
	
	/**
	 * This deactivates the button
	 */
	public void deactivate() {
		if(!visible) {
			return;
		}
		if(type == Type.TOGGLE) {
			activated = false;
		}
		System.out.println("["+name+"] deactivated");
		onDeactivate();
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
	

	Rectangle effect_rect;
	float effect_rect_a = 1f;
	float effect_rect_x = 0;

	private void spawnEffect() {
		effect_rect = new Rectangle();
		effect_rect_a = 1f;
		effect_rect_x = 0;
	}

	
	
	public boolean is_key_down;
	public boolean cancelled;
	
	
	public void drawShape(ShapeRenderer renderer) {
		
		if(!visible){
			return;
		}
		
		if(pressing) {
			color = manager.COLOR_BUTTON_PRESSING.cpy();
		}else if(activated) {
			color = manager.COLOR_BUTTON_ACTIVATED.cpy();
		}else {
			color = LerpColor(color, color_default, 0.25f);
			//color.set(color_default.r*d, color_default.g*d, color_default.b*d, color_default.a);
		}

		renderer.set(ShapeType.Filled);
		renderer.setColor(color);
		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
		

		if(hovering) {
			if(manager.edit_mode) {
				drawEditMode(renderer);
			}else {
				renderer.set(ShapeType.Line);
				renderer.setColor(color_trim_highlight);
				renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
			}
		}else {
			renderer.set(ShapeType.Line);
			renderer.setColor(color_trim);
			renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
		}
		
		
		if(effect_rect!=null) {
			effect_rect_a *= 0.85f;
			effect_rect_x += 1;
			renderer.setColor(new Color(1, 1, 1, effect_rect_a));
			renderer.rect(getGlobalX()-(effect_rect_x/2), getGlobalY()-(effect_rect_x/2), shape.width+effect_rect_x, shape.height+effect_rect_x);
			if(effect_rect_a<0.01) {
				effect_rect = null;
			}
		}
		
		drawChildren(renderer);
	}
	
	public void drawTexture(SpriteBatch batch) {
		if(pressing) {
			batch.setColor(Color.GRAY);
		}else {
			batch.setColor(Color.WHITE);
		}
		batch.draw(texture, getGlobalX(), getGlobalY()+1, shape.width-1, shape.height-1);
	}
	
	@Override
	public boolean isHoverable() {
		return true;
	}

	public void drawFont(SpriteBatch batch, BitmapFont font) {
		// TODO Auto-generated method stub

		if(!visible){
			return;
		}
		if(pressing) {
			font.setColor(Color.GRAY);
		}else if (activated) {
			font.setColor(Color.BLACK);
		}else {
			font.setColor(Color.WHITE);
		}
		font.draw(batch, name, getGlobalX()+2, getGlobalY()+font.getCapHeight()+2);
	}
	
	public Button setVisible(boolean b) {
		visible = b;
		return this;
	}

}