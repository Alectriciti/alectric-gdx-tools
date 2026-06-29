package com.alectriciti.gdx.events;

import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;

public class WidgetEvent extends Event {
	
	private final Widget widget;
	
	public WidgetEvent(Widget widget) {
		this.widget = widget;
	}
	
	public Widget getWidget() {
		return widget;
	}
    
}

