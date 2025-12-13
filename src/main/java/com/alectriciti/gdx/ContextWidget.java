package com.alectriciti.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

import static com.alectriciti.gdx.Toolkit.*;

public class ContextWidget extends DropdownMenuButton{
	
	public ContextWidget(Widget creator) {
		super(creator.id+"_context", creator);
		serializable = false;
		setGlobalPosition(getMouseX(), getMouseY());
		visible = false;
	}
	
	@Override
	public boolean isEditable() {
		return false;
	}
	
	@Override
	public ContextWidget spawnContextWidget() {
		return null;
	}
	
	@Override
	protected void OnCreate() {
		// TODO Auto-generated method stub
		activate();
//		print("asdfascf");
//		dropdownOpen();
	}
	
	@Override
	public void finishedAnimation() {
		destroy();
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawShape(renderer, recursive);
//		renderer.set(ShapeType.Line);
//		renderer.setColor(Color.GRAY);
//		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
	}
	
	

}
