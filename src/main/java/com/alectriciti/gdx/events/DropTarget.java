package com.alectriciti.gdx.events;

/**
 * Implemented by widgets or objects that can receive a Draggable item.
 */
public interface DropTarget {
    
    /**
     * Determines if this target is compatible with the dragged item.
     * Use this to check instance types, tags, or names.
     */
    public boolean doesAccept(Draggable draggable);

    /**
     * Called when a compatible Draggable is released over this target.
     * @param draggable The item being dropped
     * @param mouseX The x coordinate of the drop
     * @param mouseY The y coordinate of the drop
     * @return 
     */
    public boolean onDropReceived(Draggable draggable, int mouseX, int mouseY);
}