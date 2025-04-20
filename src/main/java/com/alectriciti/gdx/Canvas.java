package com.alectriciti.gdx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.alectriciti.gdx.WidgetManager.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class Canvas extends Widget{
	
	
	
	public boolean focused = true;
	
	public Canvas(String name, WidgetManager manager, Rectangle shape) {
		super();
		this.name = name;
		this.manager = manager;
		this.manager.registerCanvas(this);
		this.shape = new Rectangle(shape.x, shape.y, shape.width, shape.height);
		this.shape_global = new Rectangle(shape);
		//this.shape_edit_handle = new Rectangle(shape.x, shape.y+shape.height-EDIT_HANDLE_HEIGHT, shape.width, EDIT_HANDLE_HEIGHT);
		this.color = new Color(0.2f, 0.2f, 0.2f, 0.5f);
		updateGlobalPosition();
	}
	
	


	/**
	 * Automatically adds objects
	 */
	void registerWidget(Widget widget) {
		widgets.add(widget);
		widget.parent = this;
		
		if(widget instanceof Button) {
			Button b = (Button)widget;
			buttons.add(b);
			manager.buttons.add(b);
			manager.buttons_by_name.put(b.name, b);
			manager.buttons_by_key.put(b.key, b);
		}
	}
	
	
	public void drawShape(ShapeRenderer renderer) {
		if(!visible){
			return;
		}

		renderer.setColor(color);
		renderer.set(ShapeType.Filled);
		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);

		if(hovering && manager.edit_mode) {
			drawEditMode(renderer);
		}else {
			renderer.set(ShapeType.Line);
			renderer.setColor(color_trim);
			renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
		}
		drawChildren(renderer);
	}
	
	@Override
	public boolean isHoverable() {
		return true;
	}
	
	
	public void getConfiguration() {
		
	}
	
	

}
