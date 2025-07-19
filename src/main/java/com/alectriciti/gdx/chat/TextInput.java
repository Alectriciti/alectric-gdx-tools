package com.alectriciti.gdx.chat;

import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class TextInput extends TextWidget {
	
	
	
	TextCursor cursor = new TextCursor();
	

	public TextInput(UIManager manager, ColoredText...msgs) {
		super(manager, msgs);
		// TODO Auto-generated constructor stub
	}

	public TextInput(Widget parent, ColoredText...msgs) {
		super(parent, msgs);
		// TODO Auto-generated constructor stub
	}
	
	
	
	@Override
	protected void OnMouseClicked() {
		// TODO Auto-generated method stub
//		System.out.println("Input Field Selected");
//		focus();
		
		super.OnMouseClicked();
	}
	
	
	int tick;
	
	@Override
	protected void update() {
		tick++;
		// TODO Auto-generated method stub
		super.update();
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		
		if(isFocused()) {
			if(tick%20<10) {

				renderer.set(ShapeType.Filled);
				renderer.setColor(color);
				renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
				
			}
//			focus();
		}
		// TODO Auto-generated method stub
		super.drawShape(renderer, recursive);
	}
	
	
	

}
