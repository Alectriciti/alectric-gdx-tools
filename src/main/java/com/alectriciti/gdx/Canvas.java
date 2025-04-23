package com.alectriciti.gdx;

import static com.alectriciti.gdx.UIManager.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;


/**
 * A container widget which can be 
 */
public class Canvas extends Widget{
	
	
	
	public boolean focused = true;
	
	public Canvas(String name, UIManager manager, Rectangle shape) {
		super();
		this.name = name;
		this.manager = manager;
		this.manager.registerWidget(this);
		this.manager.registerCanvas(this);
		this.shape = new Rectangle(shape.x, shape.y, shape.width, shape.height);
		this.shape_global = new Rectangle(shape);
		//this.shape_edit_handle = new Rectangle(shape.x, shape.y+shape.height-EDIT_HANDLE_HEIGHT, shape.width, EDIT_HANDLE_HEIGHT);
		this.color = new Color(0.2f, 0.2f, 0.2f, 0.5f);
		updateGlobalPosition();
		pushNewZPosition(false);
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		if(!visible){
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
	public boolean isHoverable() {
		return true;
	}
	
	
	public void getConfiguration() {
		
	}
	
	
	@Override
	protected void OnMouseClicked() {
		if(!focused) {
			manager.focus(this);
		}
	}
	
	

}
