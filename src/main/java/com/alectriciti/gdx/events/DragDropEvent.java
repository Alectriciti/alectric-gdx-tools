package com.alectriciti.gdx.events;

/**
 * Fired when a Draggable is being dragged or dropped.
 */
public class DragDropEvent extends Event {
	
	Draggable source;
	private DropTarget target;
	int mouseX;
	int mouseY;
	
	
	/*
	 * this determines whether this event is a drag event or a drop event. If it is a drag event, then the mouse is still being held down. If it is a drop event, then the mouse has been released.
	 */
	boolean release;
	
	public DragDropEvent(Draggable source, DropTarget target, int mouseX, int mouseY, boolean release) {
        this.source = source;
        this.target = target;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.release = release;
    }
    
    public Draggable getSource() { return source; }
    
    /**
     * @return The DropTarget that consumed this item, or null if dropped in open space.
     */
    public DropTarget getTarget() { return target; }
    
    public boolean isRelease() { return release; }

}
