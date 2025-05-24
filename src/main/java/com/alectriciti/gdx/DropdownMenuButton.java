package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class DropdownMenuButton extends Button{
	
	public String type = "dropdownmenubutton";
	
	float expand_amount = 0;
	float expand_amount_target = 0;
	float expand_speed = 0.3f;
	
	Direction direction = Direction.DOWN;
	
	public void setDirection(Direction d) {
		this.direction = d;
	}
	
	Rectangle dropdown_region = new Rectangle();


	
	private boolean auto_close_on_button_press = true;
	private boolean autoclose_settings_initialized = false;
	
	private Runnable run_autoclose = new Runnable() {
		
		@Override
		public void run() {
			deactivate();
		}
	};
	
	public DropdownMenuButton(String button_name, int key, Widget w) {
		super(button_name, key, w);
		button_type = ButtonType.TOGGLE; //default type for this class should be toggle
	}
	
	public DropdownMenuButton(String button_name, Widget w) {
		super(button_name, w);
		button_type = ButtonType.TOGGLE;
	}
	
	public DropdownMenuButton(String button_name, int keycode, UIManager manager) {
		super(button_name, keycode, manager);
		button_type = ButtonType.TOGGLE;
	}
	
	public DropdownMenuButton(String button_name, UIManager manager) {
		super(button_name, manager);
		button_type = ButtonType.TOGGLE;
	}
	
	@Override
	public void activate() {
		// TODO Auto-generated method stub
		if(!autoclose_settings_initialized) {
			for(Widget w : widgets) {
				if(w.getClass().equals(Button.class)) {
					Button b = (Button) w;
					b.addOnActivate(run_autoclose);
				}
			}
			autoclose_settings_initialized = true;
		}
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
		updatePositionForChildren();
		for(Widget w : widgets) {
			w.setVisible(true, false);
			w.setTouchable(true, false);
		}
		
		
	}

	protected void dropdownClose() {
		expand_amount_target = 0;
		animating = true; //enables animation within update()
		for(Widget w : widgets) {
			if(w instanceof DropdownMenuButton) {
				DropdownMenuButton db = (DropdownMenuButton) w;
				db.deactivate();
			}
			w.setTouchable(false, true);
		}
		// TODO Auto-generated method stub
		
	}
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		super.update();

		if(animating) {
			expand_amount = lerp(expand_amount, expand_amount_target, expand_speed);
			if(Math.abs(expand_amount_target - expand_amount) > 0.02) {
				updatePositionForChildren();
				if(activated) {
					for(Widget w : widgets) {
						w.color_texture_alpha.a = expand_amount;
					}
				}else {
					for(Widget w : widgets) {
						w.color_texture_alpha.a = expand_amount;
					}
				}
			}else {
				animating = false;
				if(activated) {
					
				}else {
					for(Widget w : widgets) {
						w.setVisible(false, true);
					}
				}
			}
		}
		
	}
	
	/**
	 * Sets the new position of the Dropdown's Children based
	 * on the values previously set in update()
	 */
	protected void updatePositionForChildren() {
		float offset = 0;
		//Adjust the actual widgets
		for(Widget w : widgets) {
			offset += w.getHeight()*expand_amount;
			w.setRelativePosition(direction.x*offset, direction.y*offset);
			w.setOpacity(Math.max(0, (expand_amount*2)-1));
		}
		
		//apply the button effect to the overall size of the dropdown
		if(effect!=null) {
			for(Widget w : widgets) {
				effect.shape = effect.shape.merge(w.shape_global);
			}
		}
	}
	
	@Override
	public boolean drawTexture(SpriteBatch batch, boolean recursive) {
		// TODO Auto-generated method stub
		return super.drawTexture(batch, recursive);
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawShape(renderer, false);
		
		//renderer.set(ShapeType.Line);
		//renderer.setColor(Color.BLUE);
		//renderer.rect(dropdown_region.x-2, dropdown_region.y-2, dropdown_region.width+2, dropdown_region.height+2);
		if(activated)
			drawShapeChildren(renderer, recursive);
	}
	
	@Override
	public boolean drawFont(SpriteBatch batch, BitmapFont font, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawFont(batch, font, false);
			//font failed to draw

		drawFontChildren(batch, font, recursive);
		return true;
	}
	
	@Override
	protected void attachChildWidget(Widget widget_to_attach) {
		super.attachChildWidget(widget_to_attach);
		widget_to_attach.editable = false;
		widget_to_attach.setVisible(false, true);
		print(widget_to_attach.name_for_display+" : "+widget_to_attach.isVisible());
	}
	
	public boolean doesAutocloseOnButtonPress() {
		return auto_close_on_button_press;
	}
	
	public void setAutocloseOnButtonPress(boolean b) {
		this.auto_close_on_button_press = b;
	}
	
	
	
	
	

}
