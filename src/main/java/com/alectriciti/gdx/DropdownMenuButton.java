package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.HashSet;
import java.util.Set;

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
	
	public boolean auto_close = true;
	
	Direction direction = Direction.DOWN;
	
	int default_button_width = DEFAULT_WIDGET_SIZE;
	int default_button_height = DEFAULT_WIDGET_SIZE;
	
	public void setDirection(Direction d) {
		this.direction = d;
	}
	
	Rectangle dropdown_region = new Rectangle();
	
	private Runnable run_autoclose = new Runnable() {
		
		@Override
		public void run() {
			deactivate();
		}
	};
	
	public DropdownMenuButton(String button_name, Widget w, int...button_codes) {
		super(button_name, w, button_codes);
		button_type = ButtonType.TOGGLE; //default type for this class should be toggle
	}
	
	public DropdownMenuButton(String button_name, Widget w) {
		super(button_name, w);
		button_type = ButtonType.TOGGLE;
	}
	
	public DropdownMenuButton(String button_name, UIManager manager, int...button_codes) {
		super(button_name, manager, button_codes);
		button_type = ButtonType.TOGGLE;
	}
	
	public DropdownMenuButton(String button_name, UIManager manager) {
		super(button_name, manager);
		button_type = ButtonType.TOGGLE;
	}
	
	@Override
	public void activate() {
		// TODO Auto-generated method stub
		super.activate();
		manager.transient_widgets.add(this);
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
		for(Widget w : getChildren()) {
			if(w instanceof DropdownMenuButton) {
				w.setVisible(true, InheritanceRule.STANDARD);
			}else {
				w.setVisible(true, InheritanceRule.RECURSIVE);
			}
			w.setTouchable(true);
		}
		
		
	}

	protected void dropdownClose() {
		expand_amount_target = 0;
		animating = true; //enables animation within update()
		for(Widget w : getDescendants()) {
			if(w instanceof DropdownMenuButton) {
				DropdownMenuButton db = (DropdownMenuButton) w;
				db.deactivate();
			}
			w.setTouchable(false);
		}
		
	}
	
	@Override
	protected void update() {
		// TODO Auto-generated method stub
		super.update();

		if(animating) {
			//animation is moving
			expand_amount = lerp(expand_amount, expand_amount_target, expand_speed);
			if(Math.abs(expand_amount_target - expand_amount) > 0.02) {
				updatePositionForChildren();
				if(activated) {
					for(Widget w : getDescendants()) {
						w.color_texture_alpha.a = expand_amount;
					}
				}else {
					for(Widget w : getDescendants()) {
						w.color_texture_alpha.a = expand_amount;
					}
				}
			}else {
				//animation has stopped
				animating = false;
				if(activated) {
					
				}else {
					for(Widget w : getChildren()) {
						if(w instanceof DropdownMenuButton) {
							w.setVisible(false, InheritanceRule.STANDARD);
						}else {
							w.setVisible(false, InheritanceRule.RECURSIVE);
						}
					}
					finishedAnimation();
				}
			}
		}
		
	}
	
	@Override
	public void unfocus(Widget new_focus) {
		super.unfocus(new_focus);
		//If the new widget isn't related, then deactivate this dropdown
		if(!isRelated(new_focus)) {
//			deactivate();
		}
	}
	
	public void finishedAnimation() {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Sets the new position of the Dropdown's Children based
	 * on the values previously set in update()
	 */
	protected void updatePositionForChildren() {
		float offset = 0;
		//Adjust the actual widgets
		for(Widget w : widgets_children) {
			offset += (w.getHeight()+1)*expand_amount;
			w.setRelativePosition(direction.x*offset, direction.y*offset);
		}
		for(Widget w : getDescendants()) {
			w.setOpacity(Math.max(0, (expand_amount*2)-1));
		}
		
		//apply the button effect to the overall size of the dropdown
		if(effect!=null) {
			for(Widget w : widgets_children) {
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
	public void drawShape(ShapeRenderer renderer) {
		// TODO Auto-generated method stub
		super.drawShape(renderer);
		
		//renderer.set(ShapeType.Line);
		//renderer.setColor(Color.BLUE);
		//renderer.rect(dropdown_region.x-2, dropdown_region.y-2, dropdown_region.width+2, dropdown_region.height+2);
//		if(activated)
//			drawShapeChildren(renderer, true);
	}
	
	@Override
	public boolean drawFont(SpriteBatch batch, boolean recursive) {
		// TODO Auto-generated method stub
		super.drawFont(batch, false);
			//font failed to draw

		drawFontChildren(batch, recursive);
		return true;
	}
	
	@Override
	protected void attachChildWidget(Widget widget_to_attach) {
		super.attachChildWidget(widget_to_attach);
		widget_to_attach.editable = false;
		widget_to_attach.setVisible(false);
		print(widget_to_attach.name_for_display+" : "+widget_to_attach.isVisible());
	}

	
	/**
	 * Apply this to a button onActivate to have it register as autoclose
	 * @return
	 */
	public Runnable getAutocloseRunnable() {
		return run_autoclose;
	}
	
	protected boolean doesSendInheritanceUponAttach() {
		return false;
	}
	
	
	
	
	
	

}
