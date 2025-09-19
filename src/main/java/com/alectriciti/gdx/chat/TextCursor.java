package com.alectriciti.gdx.chat;

import com.alectriciti.gdx.Widget;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;

public class TextCursor extends Widget{
	
	int i = 0;
	
	int blink_rate = 20;
	
	TextInput text_widget;
//	Rectangle shape;

	public int index;
	
	public TextCursor(TextInput widget) {
		this.text_widget = widget;
		this.shape = new Rectangle(widget.getGlobalX(), widget.getGlobalY(), 8, 16);
	}
	
	@Override
	public boolean isEditable() {
		return false;
	}

	public void update() {
		super.update();
		i++;
	}

	public void draw(ShapeRenderer renderer, TextInput widget) {
		renderer.set(ShapeType.Filled);
		renderer.setColor(Color.WHITE);
		renderer.rect(widget.getGlobalX()+shape.getX(), widget.getGlobalY()+shape.getY(), shape.width, shape.height);
	}


	/** Move cursor one char left. */
	public void moveLeft() {
	    if (index > 0) index--;
	}

	/** Move cursor one char right. */
	public void moveRight() {
	    if (index < text_widget.length) index++;
	}
	
	/** Set cursor index (clamped). Recompute anything necessary. */
	public void setCursorIndex(int index) {
	    this.index = Math.max(0, Math.min(index, text_widget.length)); // clamp to 0..length
	}

	/** Jump to end */
	public void moveCursorToEnd() { index = text_widget.length; }

	/** Jump to start */
	public void moveCursorToStart() { index = 0; }
	
	

}
