package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.LerpColor;
import static com.alectriciti.gdx.Toolkit.print;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class Knob extends Widget{
    
	
	
	Orientation orientation;
    	
	public Knob(String id, Widget parent, Orientation orientation) {
		super(id, parent);
		this.orientation = orientation;
		color.mul(1.4f);
    	updateKnobSize();
	}
	
    	@Override
    	public void drawShape(ShapeRenderer shape_renderer) {
    		// TODO Auto-generated method stub
    		if(!isVisible()) {
    			return;
    		}
    		shape_renderer.set(ShapeType.Filled);
    		shape_renderer.setColor(color);
    		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
    		drawBorder(shape_renderer);
    	}
    	
    	@Override
    	public void drawBorder(ShapeRenderer shape_renderer) {
    		// TODO Auto-generated method stub
//    		super.drawBorder(shape_renderer);
    		shape_renderer.set(ShapeType.Line);
    		shape_renderer.setColor(color_outline);
    		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
    	}
    	
    	@Override
    	protected void update() {
    		// TODO Auto-generated method stub
    		super.update();
    		if(getParent().isPressed()) {
    			color_outline = LerpColor(color_outline, style.color_hover, style.color_fade_in);
    		}else {
    			color_outline = LerpColor(color_outline, style.color_outline, style.color_fade_out);
    		}
    	}
    	
    	@Override
    	public boolean isEditable() {
    		return false;
    	}
    	
    	@Override
    	public boolean isTouchable() {
    		return false;
    	}
    	
    	@Override
    	public boolean isAlwaysEditable() {
    		// TODO Auto-generated method stub
    		return super.isAlwaysEditable();
    	}

		public void updateKnobSize() {
			if(parent instanceof Slider) {
				Slider s = ((Slider)parent);
				if(s.orientation == Orientation.HORIZONTAL) {
					this.setSize(getWidth(), s.getHeight());
				} else {
					this.setSize(s.getWidth(), getHeight());
				}
			}
		}

		public void setSize(int i) {
			switch(orientation) {
				case HORIZONTAL:
					this.setSize(i, this.getHeight());
					break;
				case VERTICAL:
					this.setSize(this.getWidth(), i);
					break;
				default:
					setSize(i, i);
					break;
			}
		}
    	
    }