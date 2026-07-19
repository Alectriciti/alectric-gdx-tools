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
		setVisible(false);
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
	protected void attachChildWidget(Widget widget_to_attach) {
		super.attachChildWidget(widget_to_attach);
		widget_to_attach.setValue(Parameter.VISIBLE, Value.TRUE, InheritanceRule.PROTECT_CHILDREN);
	}
	
	@Override
	protected void OnCreate() {
		activate();
	}
	
	@Override
	public void finishedAnimation() {
		destroy();
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer) {
		super.drawShape(renderer);
//		renderer.set(ShapeType.Line);
//		renderer.setColor(Color.GRAY);
//		renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
	}
	
	

}
