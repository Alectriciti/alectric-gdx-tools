package com.alectriciti.test;

import com.alectriciti.gdx.Button;
import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.UIManager;
import com.badlogic.gdx.Gdx;

public class SneakyButton extends Button {

	public SneakyButton(String name, UIManager widgetManager) {
		super(name, widgetManager);
	}
	
	
	float target_x, target_y;
	
	
	@Override
	protected void OnCreate() {
		super.OnCreate();
	}
	
	boolean latch;
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		if(hovering) {
			if(latch) {
				latch = false;
			target_x = (float) (Math.random()*Gdx.graphics.getWidth());
			target_y = (float) (Math.random()*Gdx.graphics.getHeight());
			}
		}else{
			latch = true;
				target_x = shape_base.getX();
				target_y = shape_base.getY();
		}
		setGlobalPosition(lerp(getGlobalX(), target_x, 0.015f), lerp(getGlobalY(), target_y, 0.015f));
		super.update();
	}

}
