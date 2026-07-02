package com.alectriciti.test;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;
import com.alectriciti.gdx.events.Draggable;
import com.alectriciti.gdx.events.DropTarget;
import com.badlogic.gdx.math.Rectangle;

import static com.alectriciti.gdx.Toolkit.*;

public class AllConsumingCanvas extends Canvas implements DropTarget {

	public AllConsumingCanvas(String name, UIManager manager, int x, int y) {
		super(name, manager, x, y);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean doesAccept(Draggable draggable) {
		return true; // Accept all draggables
	}

	@Override
	public boolean onDropReceived(Draggable draggable, int mouseX, int mouseY) {
		if(draggable instanceof Widget) {
			Widget w = (Widget) draggable;
			print(""+w.getName()+" is being dragged over "+this.getName());
			return true;
		}
		return false;
	}

}
