package com.alectriciti.gdx;

import static com.alectriciti.gdx.UIManager.*;

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
	
	public Canvas(String name, UIManager manager, Rectangle shape) {
		super();
		this.name_for_display = name;
		this.manager = manager;
		this.manager.registerWidget(this);
		//this.manager.registerCanvas(this);
		this.shape = new Rectangle(shape.x, shape.y, shape.width, shape.height);
		this.shape_global = new Rectangle(shape);
		//this.shape_edit_handle = new Rectangle(shape.x, shape.y+shape.height-EDIT_HANDLE_HEIGHT, shape.width, EDIT_HANDLE_HEIGHT);
		
		this.color = new Color(0.2f, 0.2f, 0.2f, 0.5f);
		this.font_offset = new Point(4, -2);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		if(!isVisible()){
			return;
		}
		
		renderer.setColor(color);
		renderer.set(ShapeType.Filled);
		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);

		if(hovering && manager.edit_mode) {
			drawEditMode(renderer, recursive);
			if(recursive) {
				drawEditModeChildren(renderer, recursive);
			}
		}else {
			renderer.set(ShapeType.Line);
			renderer.setColor(color_trim);
			renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
			if(recursive) {
				drawShapeChildren(renderer, recursive);
			}
		}
	}
	
	@Override
	public boolean drawFont(SpriteBatch sprite_batch, BitmapFont font, boolean recursive) {
		
		if(render_text) {
		//print(getGlobalX()+" "+getGlobalY());
			font.setColor(font_color);
			font.draw(sprite_batch, name_for_display, getGlobalX() + font_offset.x, getGlobalY() + shape.height - font_offset.y);
		}
		if(recursive) {
			drawFontChildren(sprite_batch, font, recursive);
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
