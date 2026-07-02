package com.alectriciti.gdx.events;

public class DragStartEvent extends Event {
	
	Draggable source;
	int mouseX;
	int mouseY;
	
	public DragStartEvent(Draggable source, int mouseX, int mouseY) {
        this.source = source;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }
    
    public Draggable getSource() { return source; }

}
