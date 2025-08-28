package com.alectriciti.gdx.chat;

import static com.alectriciti.gdx.Toolkit.*;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class TextInput extends TextWidget implements InputProcessor{
	
	
	
	TextCursor cursor = new TextCursor();
	
	Widget target_widget;
	
	ReturnKeyMode mode = ReturnKeyMode.NEW_LINE;
	
	public enum ReturnKeyMode{
		ACTIVATE_TARGET,
		ACTIVATE_TARGET_SHIFT_NEW_LINE,
		NEW_LINE,
	}
	

	boolean escape_clears_focus = true;
	

	public TextInput(UIManager manager, ColoredText...msgs) {
		super(manager, msgs);
		// TODO Auto-generated constructor stub
	}

	public TextInput(Widget parent, ColoredText...msgs) {
		super(parent, msgs);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Only utilized
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
	
	int tick;
	
	@Override
	protected void update() {
		tick++;
		// TODO Auto-generated method stub
		super.update();
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		
		if(isFocused()) {
			if(tick%20<10) {
				renderer.set(ShapeType.Filled);
				renderer.setColor(color);
				renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
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
