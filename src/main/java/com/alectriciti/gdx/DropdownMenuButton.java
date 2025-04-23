package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class DropdownMenuButton extends Button{
	
	float expand_amount = 0;
	float expand_amount_target = 0;
	float expand_speed = 0.1f;
	
	Direction direction = Direction.DOWN;
	
	public void setDirection(Direction d) {
		this.direction = d;
	}
	
	Rectangle dropdown_region = new Rectangle();
	
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
	
	@Override
	public void setGlobalPosition(float x, float y) {
		super.setGlobalPosition(x, y);
	}
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		super.update();

		if(animating) {
			expand_amount = lerp(expand_amount, expand_amount_target, expand_speed);
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
		
	}
	
	protected void updatePositionForChildren() {
		float offset = 0;
		//Adjust the actual widgets
		for(Widget w : widgets) {
			offset += w.getHeight()*expand_amount;
			w.setRelativePosition(direction.x*offset, direction.y*offset);
			w.setOpacity(Math.max(0, (expand_amount*2)-1));
		}
		
		if(effect_rect!=null) {
			for(Widget w : widgets) {
				effect_rect = effect_rect.merge(w.shape_global);
				//effect_rect.height += w.getHeight()*(expand_amount);
			}
			switch(direction) {
			case DOWN:
				break;
			case LEFT:
				break;
			case RIGHT:
				break;
			case UP:
				for(Widget w : widgets) {
					//effect_rect.height += w.getHeight()*(expand_amount);
				}
				break;
			default:
				break;
			
			}
		}
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawShape(renderer, false);
		
		//renderer.set(ShapeType.Line);
		//renderer.setColor(Color.BLUE);
		//renderer.rect(dropdown_region.x-2, dropdown_region.y-2, dropdown_region.width+2, dropdown_region.height+2);
		
		if(activated) {
			drawShapeChildren(renderer, recursive);
		}
	}
	
	@Override
	protected void spawnButtonEffect() {
		super.spawnButtonEffect();
		//effect_rect.width += shape.getWidth();
		//effect_rect.height = shape.getHeight();
	}

	@Override
	protected void drawButtonEffect(ShapeRenderer renderer) {
		if(effect_rect!=null) {
			renderer.setColor(new Color(1, 1, 1, effect_rect_a));

			renderer.rect(
					effect_rect.x-effect_delta-effect_offset_start,
					effect_rect.y-effect_delta-effect_offset_start,
					effect_rect.width+((effect_delta+effect_offset_start)*2),
					effect_rect.height+((effect_delta+effect_offset_start)*2)
					);
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
		widget_to_attach.visible = false;
	}
	
	
	
	
	

}
