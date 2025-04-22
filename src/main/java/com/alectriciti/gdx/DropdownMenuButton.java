package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.LinkedList;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DropdownMenuButton extends Button{
	
	float expand_amount = 0;
	float expand_amount_target = 0;
	
	boolean animating;

	public DropdownMenuButton(String button_name, int key, Widget w) {
		super(button_name, key, w);
		type = Type.TOGGLE;
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		dropdownOpen();
		
	}

	@Override
	public void deactivate() {
		// TODO Auto-generated method stub
		super.deactivate();
		dropdownClose();
	}
	
	
	protected void dropdownOpen() {
		expand_amount_target = 1;
		animating = true;
		for(Widget w : widgets) {
			w.setVisible(true);
		}
		
		
	}

	protected void dropdownClose() {
		expand_amount_target = 0;
		animating = true;
		// TODO Auto-generated method stub
		
	}
	
	
	protected void updatePositionForChildren() {
		float offset = 0;
		for(Widget w : widgets) {
			w.setRelativePosition(0, offset);
			offset += w.getHeight()*expand_amount;
			w.setRelativePosition(0, offset);
		}
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawShape(renderer, false);
		
		if(animating) {
			expand_amount = lerp(expand_amount, expand_amount_target, 0.1f);
			if(Math.abs(expand_amount_target - expand_amount) > 0.02) {
				updatePositionForChildren();
			}else {
				animating = false;
				if(activated) {
					
				}else {
					for(Widget w : widgets) {
						w.setVisible(false);
					}
				}
			}
		}
		
		if(activated) {
			drawShapeChildren(renderer, recursive);
		}
	}
	
	@Override
	public boolean drawFont(SpriteBatch batch, BitmapFont font, boolean recursive) {
		// TODO Auto-generated method stub
		if (!super.drawFont(batch, font, false)) {
			//font failed to draw
			return false;
		}

		if(activated) {
			drawFontChildren(batch, font, recursive);
		}
		return true;
	}
	
	@Override
	protected void attachChildWidget(Widget widget_to_attach) {
		super.attachChildWidget(widget_to_attach);
		widget_to_attach.editable = false;
	}
	
	
	
	
	

}
