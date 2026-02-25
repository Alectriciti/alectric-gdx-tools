package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

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
	
	public ReturnKeyMode return_mode = ReturnKeyMode.NEW_LINE;
	
	//NOTE: Activation can be itself if we need extra functionality
	
	public enum ReturnKeyMode{
		NEW_LINE, // Always creates a new line
		ACTIVATE_TARGET, // If there's a target widget, it activates it. Otherwise un-focus.
		ACTIVATE_TARGET_SHIFT_NEW_LINE, //Holding shift creates a newline, otherwise tries the above method
		FOCUS_TARGET, // If there's a target widget, it focuses it.
		ACTIVATE_AND_FOCUS_TARGET, // Combines
	}
	
	public boolean escape_clears_focus = true; //if true, when this widget is focused, escape will remove it
	
	public int tick;
	
	
	
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
			if(manager.right_is_pressed) {
				if(tick%4==0) {
	                if (msgs != null && msgs.length > 0 && cursor.index < msgs[0].getText().length()) cursor.index++;
				}
			}else
			if(manager.left_is_pressed) {
				if(tick%4==0) {
	                if (cursor.index > 0) cursor.index--;
				}
			}
		}
		super.update();
	}
	
	
	@Override
	public void drawShape(ShapeRenderer renderer, boolean recursive) {
		
		if(isFocused()) {
			cursor.draw(renderer, this);
		}
		// TODO Auto-generated method stub
		super.drawShape(renderer, recursive);
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
//	        case Keys.LEFT: {
//	            // Ctrl+Left -> move by word
//	            if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
//	                // move cursor left to previous word boundary
//	                if (msgs != null && msgs.length > 0) {
//	                    String s = msgs[0].getText();
//	                    int i = Math.max(0, Math.min(cursor.index, s.length()));
//	                    // skip any whitespace to the left
//	                    while (i > 0 && Character.isWhitespace(s.charAt(i - 1))) i--;
//	                    // skip non-whitespace to the left (the word)
//	                    while (i > 0 && !Character.isWhitespace(s.charAt(i - 1))) i--;
//	                    cursor.index = i;
//	                }
//	            } else {
//	                if (cursor.index > 0) cursor.index--;
//	            }
//	            return true;
//	        }
//
//	        case Keys.RIGHT: {
//	            // Ctrl+Right -> move to next word boundary
//	            if (Gdx.input.isKeyPressed(Keys.CONTROL_LEFT) || Gdx.input.isKeyPressed(Keys.CONTROL_RIGHT)) {
//	                if (msgs != null && msgs.length > 0) {
//	                    String s = msgs[0].getText();
//	                    int i = Math.max(0, Math.min(cursor.index, s.length()));
//	                    // skip non-whitespace to the right
//	                    while (i < s.length() && !Character.isWhitespace(s.charAt(i))) i++;
//	                    // skip whitespace to the right
//	                    while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
//	                    cursor.index = i;
//	                }
//	            } else {
//	                if (msgs != null && msgs.length > 0 && cursor.index < msgs[0].getText().length()) cursor.index++;
//	            }
//	            return true;
//	        }

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
	        boolean activate = false;
	        boolean focus = false;
	        boolean newline = false;

	        switch (return_mode) {
	            case ACTIVATE_TARGET:
	                activate = true;
	                break;
	            case ACTIVATE_TARGET_SHIFT_NEW_LINE:
	                if (Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT)) {
	                    newline = true;
	                } else {
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

	        if (target_widget != null) {
	            if (activate && target_widget instanceof Activatable) {
	                ((Activatable) target_widget).activate();
	            }
	            if (focus) target_widget.focus();
	        }
	        // if not newline, consume and don't insert a newline
	        return !newline;
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
	
}
