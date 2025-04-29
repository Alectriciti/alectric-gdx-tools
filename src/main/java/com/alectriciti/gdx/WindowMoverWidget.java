package com.alectriciti.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;

import static com.alectriciti.gdx.Toolkit.*;

/**
 * A header widget that allows the user to move the window, acts as a title
 * This widget can also hold other widgets such as a menu button
 */
public class WindowMoverWidget extends Widget{
	
	
	
	public WindowMoverWidget(String name, UIManager manager) {
		super(name, manager);
		this.alignment = Direction.UP;
	}
	
	
	@Override
		public void updateAlignment() {
			// TODO Auto-generated method stub
			super.updateAlignment();
			switch(alignment) {
			case DOWN:
				break;
			case DOWN_LEFT:
				break;
			case DOWN_RIGHT:
				break;
			case LEFT:
				break;
			case NONE:
				break;
			case RIGHT:
				break;
			case UP:
				break;
			case UP_LEFT:
				break;
			case UP_RIGHT:
				break;
			default:
				break;
			
			}
		}
	
	@Override
	public boolean isAlwaysEditable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	public boolean isEditable() {
		// TODO Auto-generated method stub
		return true;
	}
	
	@Override
	protected void OnMouseClicked() {
		// TODO Auto-generated method stub
		super.OnMouseClicked();
		mouse_x = -getMouseX();
		mouse_y = -getMouseY();
	}

	int mouse_x, mouse_y;
	
	@Override
	protected void OnMouseReleased() {
		// TODO Auto-generated method stub
		super.OnMouseReleased();
	}
	
	public void moveWindow(int x, int y) {
        int screenmouse[] = getAbsoluteMousePosition();

		Graphics graphics = Gdx.graphics;
        Lwjgl3Window window = ((Lwjgl3Graphics) graphics).getWindow();
        window.setPosition(mouse_x+screenmouse[0], mouse_y+screenmouse[1]); // Optional, set to desired position
	}
	

}
