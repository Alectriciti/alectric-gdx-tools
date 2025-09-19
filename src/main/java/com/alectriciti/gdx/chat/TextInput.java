package com.alectriciti.gdx.chat;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Activatable;
import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class TextInput extends TextWidget implements InputProcessor{
	
	TextCursor cursor;
	
	Widget target_widget;
	
	public ReturnKeyMode return_mode = ReturnKeyMode.NEW_LINE;
	
	//NOTE: Activation can be itself if we need extra functionality
	
	public enum ReturnKeyMode{
		NEW_LINE, // Always creates a new line
		ACTIVATE_TARGET, // If there's a target widget, it activates it. Otherwise un-focus.
		ACTIVATE_TARGET_SHIFT_NEW_LINE, //Holding shift creates a newline, otherwise tries the above method
		FOCUS_TARGET, // If there's a target widget, it focuses it.
		ACTIVATE_AND_FOCUS_TARGET, // Combines
	}
	
	boolean escape_clears_focus = true; //if true, when this widget is focused, escape will remove it
	
	int tick;
	
	
	
	public TextInput(UIManager manager, ColoredText...msgs) {
		super(manager, msgs);
		cursor = new TextCursor(this);
		// TODO Auto-generated constructor stub
	}
	
	public TextInput(Widget parent, ColoredText...msgs) {
		super(parent, msgs);
		cursor = new TextCursor(this);
		// TODO Auto-generated constructor stub
	}
	
	
	/**
	 * If "Activate_Target", then this will activate it
	 * @param target_widget
	 */
	public void setTargetWidget(Widget target_widget) {
		this.target_widget = target_widget;
	}
	
	
	@Override
	protected void OnMouseClicked() {
		// TODO Auto-generated method stub
//		focus();
//		super.OnMouseClicked();
	}
	
	@Override
	protected void OnMouseReleased() {
		// TODO Auto-generated method stub
		System.out.println("Input Field Selected");
		super.OnMouseReleased();
	}
	
	
	protected void reconstruct() {
		super.reconstruct();
		if(cursor.index > length ) cursor.index = length;
	}
	
	@Override
	protected void update() {
		tick++;
		// TODO Auto-generated method stub
		if(isFocused()) {
			cursor.update();
		}
		super.update();
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		
		if(isFocused()) {
			if(tick%20<10) {
				cursor.draw(renderer, this);
			}
			
//			focus();
		}
		// TODO Auto-generated method stub
		super.drawShape(renderer, recursive);
	}
	

	@Override
	public boolean keyDown(int keycode) {
		if(keycode == Keys.ESCAPE) {
			if(escape_clears_focus) {
				manager.focus(null);
				return true;
			}else {
				return false;
			}
		}
		return false;
	}

	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean keyTyped(char character) {
//		print(""+character);
		
		if(Character.isDefined(character)) {
			if(character=='\n') {
				boolean activate = false;
				boolean focus = false;
				boolean newline = false;
				switch(return_mode) {
				case ACTIVATE_TARGET:
					activate = true;
					break;
				case ACTIVATE_TARGET_SHIFT_NEW_LINE:
					if(Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
						newline = true;
					}else {
						activate = true;
					}
					break;
				case NEW_LINE:
					newline = true;
					break;
				case FOCUS_TARGET:
					focus = true;
					break;
				case ACTIVATE_AND_FOCUS_TARGET:
					activate = true;
					focus = true;
					break;
				}

				if(target_widget!=null) {
					if(activate) {
						if(target_widget instanceof Activatable) {
							Activatable a = (Activatable) target_widget;
							a.activate();
						}
					}
					if(focus) {
						target_widget.focus();
					}
				}
				if(!newline) {
					return true;
				}
			}
			setText(0, msgs[0].getText()+character);
			return true;
		}
		
		return false;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean scrolled(float amountX, float amountY) {
		// TODO Auto-generated method stub
		return false;
	}
	
}
