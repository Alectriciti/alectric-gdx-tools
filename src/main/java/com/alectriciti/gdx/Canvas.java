package com.alectriciti.gdx;

import static com.alectriciti.gdx.UIManager.*;
import static com.alectriciti.gdx.Toolkit.*;

import java.awt.Point;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;


/**
 * A container widget which is designed to hold other widgets. It can be used to group related widgets together, and can also be used to create a background for a set of widgets.
 */
public class Canvas extends Widget{
	
	
	
	public boolean focused = true;
	
	public Canvas(String name, UIManager manager, int width, int height) {
		super();
		this.name_for_display = name;
		this.manager = manager;
		this.manager.registerWidget(this);
		//this.manager.registerCanvas(this);
		this.shape = new Rectangle(0,0, width, height);
		this.shape_global = new Rectangle(shape);
		
		this.color = new Color(0.2f, 0.2f, 0.2f, 0.5f);
		this.font_offset = new Point(4, -2);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer) {
		if(!isVisible()){
			return;
		}
		
		renderer.setColor(color);
		renderer.set(ShapeType.Filled);
		drawRectRound(renderer, getGlobalX(), getGlobalY(), shape.width, shape.height, style.corner_radius);

		if(hovering && manager.edit_mode) {
			drawEditMode(renderer, true);
			drawEditModeChildren(renderer, true);
		}else {
			drawBorder(renderer);
		}
	}
	
	@Override
	public void drawBorder(ShapeRenderer shape_renderer) {
		// TODO Auto-generated method stub
		super.drawBorder(shape_renderer);
	}
	
	@Override
	public boolean drawFont(SpriteBatch sprite_batch, boolean recursive) {
		
		if(show_text) {
		//print(getGlobalX()+" "+getGlobalY());
			style.font.setColor(font_color);
			style.font.draw(sprite_batch, name_for_display, getGlobalX() + font_offset.x, getGlobalY() + shape.height - font_offset.y);
		}
		if(recursive) {
			drawFontChildren(sprite_batch, recursive);
		}
		return true;
	}
	
	@Override
	public boolean isHoverable() {
		return false;
	}
	
	public void getConfiguration() {
		
	}
	
	
	@Override
	protected void OnMouseClicked() {
		if(!focused) {
			manager.focus(this, true);
		}
	}
	
	

}
