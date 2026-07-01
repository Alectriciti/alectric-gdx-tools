package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class TextInput extends TextWidget implements InputProcessor{
	
	public TextCursor cursor;
	
	public Widget target_widget;
	
	// --- Text Input Behavior Configuration ---
	public boolean enterActivatesTarget = false;
	public boolean enterFocusesTarget = false;
	public boolean allowNewlines = true; 
	
	// If true, holding Shift acts purely as a text-return, ignoring activation targets.
	public boolean shiftEnterBypassesActivation = true;
	
	public boolean escape_clears_focus = true; //if true, when this widget is focused, escape will remove it
	
	public int tick;
	
	transient Runnable run_activate;
	
	public TextInput(UIManager manager, ColoredText...msgs) {
		super(manager, msgs);
    	setTouchable(true);
		cursor = new TextCursor(this);
		cursor.index = this.length;
		// TODO Auto-generated constructor stub
	}
	
	public TextInput(Widget parent, ColoredText...msgs) {
		super(parent, msgs);
    	setTouchable(true);
		cursor = new TextCursor(this);
		cursor.index = this.length;
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
	
	
	@Override
	protected void reconstruct() {
		super.reconstruct();
		
		// Null check is required here! During object creation, TextWidget's 
		// constructor calls this method BEFORE the TextCursor is initialized.
		if (cursor != null) {
			if (cursor.index > length) {
				cursor.index = length;
			}
		}
	}
	
	@Override
	protected void update() {
		tick++;
		// TODO Auto-generated method stub
		if(isFocused()) {
			if(manager.allowKeyRepeat(tick, manager.right_pressed_ticks)) {
	                if (msgs != null && msgs.length > 0 && cursor.index < msgs[0].getText().length()) cursor.index++;
			}else
				if(manager.allowKeyRepeat(tick, manager.left_pressed_ticks)) {
	                if (cursor.index > 0) cursor.index--;
			}
		}
		super.update();
	}

	@Override
	public boolean isHoverable() {
		return true;
	}
	
	@Override
	public void drawShape(ShapeRenderer renderer) {
		
		if(isFocused()) {
			cursor.draw(renderer, this);
		}
		// TODO Auto-generated method stub
		super.drawShape(renderer);
		if(hovering)
		drawBorder(renderer);
	}
	
	@Override
	public boolean keyUp(int keycode) {
		// TODO Auto-generated method stub
		return false;
	}
	
	
	@Override
	public boolean keyDown(int keycode) {
	    if (!isFocused()) return false;

	    // ESCAPE (existing behavior)
	    if (keycode == Keys.ESCAPE) {
	        if (escape_clears_focus) {
	            manager.focus(null, false);
	            return true;
	        } else {
	            return false;
	        }
	    }
	    
	    // Navigation and editing keys
	    switch (keycode) {
	    
	        case Keys.HOME: {
	            cursor.index = 0;
	            return true;
	        }

	        case Keys.END: {
	            if (msgs != null && msgs.length > 0) cursor.index = msgs[0].getText().length();
	            return true;
	        }

	        case Keys.BACKSPACE: {
//	            // delete character BEFORE cursor
//	            if (msgs == null || msgs.length == 0) return false;
//	            String s = msgs[0].getText();
//	            if (cursor.index > 0 && s.length() > 0) {
//	                int delAt = Math.max(0, Math.min(cursor.index - 1, s.length() - 1));
//	                String newS = s.substring(0, delAt) + s.substring(delAt + 1);
//	                setText(0, newS);
//	                cursor.index = delAt; // move cursor back
//	            }
//	            return true;
	        }

	        case Keys.FORWARD_DEL: {
	            if (msgs == null || msgs.length == 0) return false;
	            String s = msgs[0].getText();
	            if (cursor.index < s.length()) {
	                int delAt = cursor.index;
	                String newS = s.substring(0, delAt) + s.substring(delAt + 1);
	                setText(0, newS);
	                // cursor.index remains at delAt
	            }
	            return true;
	        }
	    }

	    return false;
	}

	@Override
	public boolean keyTyped(char character) {
	    if (!isFocused()) return false;
	    if (msgs == null || msgs.length == 0) return false;

	    // Some platforms may deliver backspace as '\b' here; handle defensively
	    if (character == '\b') {
	        // replicate Backspace behavior
	        String s = msgs[0].getText();
	        if (cursor.index > 0 && s.length() > 0) {
	            int delAt = Math.max(0, Math.min(cursor.index - 1, s.length() - 1));
	            String newS = s.substring(0, delAt) + s.substring(delAt + 1);
	            setText(0, newS);
	            cursor.index = delAt;
	        }
	        return true;
	    }

	    // Handle Enter / Return specially (your existing logic)
	    if (character == '\n' || character == '\r') {
	        boolean shiftPressed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

	        boolean willActivate = enterActivatesTarget;
	        boolean willFocus = enterFocusesTarget;
	        boolean willNewline = allowNewlines;

	        // Modifier check: Shift overrides UI actions to strictly allow typing a newline
	        if (shiftPressed && shiftEnterBypassesActivation) {
	            willActivate = false;
	            willFocus = false;
	            willNewline = allowNewlines; 
	        } else if (willActivate || willFocus) {
	            // Standard behavior: If Enter is being used as a "Submit" button, consume the newline character.
	            willNewline = false;
	        }

	        // 1. Execute UI Actions
	        if (target_widget != null) {
	            if (willActivate && target_widget instanceof Activatable) {
	                ((Activatable) target_widget).activate();
	            }
	            if (willFocus) target_widget.focus();
	        } else if (willActivate) {
	            // Fallback: If it's an activating input but has no target, clear focus (Standard UI behavior)
	            manager.focus(null, false);
	        }
	        
	        if(willActivate && run_activate!=null) {
	        	run_activate.run();
	        }

	        // 2. Execute Text Insertion
	        if (!willNewline) {
	            return true; // Consume the event, preventing the newline from being typed
	        } 
	        
	        // If willNewline IS true, we simply let the code fall through!
	        // The block at the bottom of keyTyped() will naturally insert the '\n' into the string.
	    }

	    // For regular characters, insert at cursor position (don't just append)
	    if (!Character.isISOControl(character)) {
	        String s = msgs[0].getText();
	        int pos = Math.max(0, Math.min(cursor.index, s.length()));
	        String newS = s.substring(0, pos) + character + s.substring(pos);
	        setText(0, newS);
	        cursor.index = pos + 1; // advance cursor after inserted char
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
	
	public void setActivate(Runnable run) {
		this.run_activate = run;
	}
	
}
