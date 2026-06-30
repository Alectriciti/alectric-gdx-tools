package com.alectriciti.gdx;

import com.alectriciti.gdx.events.Cancellable;
import com.alectriciti.gdx.events.WidgetEvent;

public class WidgetClickEvent extends WidgetEvent implements Cancellable{
	
	final int x;
	final int y;
	boolean edit_mode;

	public WidgetClickEvent(Widget widget, int clickX, int clickY, boolean edit_mode) {
		super(widget);
		this.x = clickX;
		this.y = clickX;
		this.edit_mode = edit_mode;
	}
	
	public boolean isEditMode() {
		return edit_mode;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		this.cancelled = cancel;
	}

}
