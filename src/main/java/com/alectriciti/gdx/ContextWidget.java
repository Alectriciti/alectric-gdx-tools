package com.alectriciti.gdx;

import com.badlogic.gdx.Gdx;

import static com.alectriciti.gdx.Toolkit.*;

public class ContextWidget extends DropdownMenuButton{
	
	public ContextWidget(Widget creator) {
		super(creator.id+"_context", creator);
		serializable = false;
		setGlobalPosition(getMouseX(), getMouseY()-shape.height);
	}


	@Override
	public boolean isEditable() {
		return false;
	}
	
	@Override
	public ContextWidget displayContextWidget() {
		return null;
	}
	
	
	
	
	

}
