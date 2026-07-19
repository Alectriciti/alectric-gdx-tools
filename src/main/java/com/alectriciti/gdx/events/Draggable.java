package com.alectriciti.gdx.events;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * anything that can be dragged and dropped should implement this interface
 */
public interface Draggable {


	public void onDragStart();
	
	/**
	 * called when the mouse is being dragged with this draggable
	 * @param mouseX the x coordinate of the mouse at it's current position
	 * @param mouseY the y coordinate of the mouse at it's current position
	 */
	public void onDrag(int mouseX, int mouseY);
	
	/**
	 * called when the mouse is released after dragging this draggable
	 * @param target the object it was dropped onto, this may be null
	 * @param mouseX the x coordinate of the mouse at it's current position
	 * @param mouseY the y coordinate of the mouse at it's current position
	 */
	public void onDrop(DropTarget target, int mouseX, int mouseY);
	
	
	/**
     * Called every frame while the object is being dragged.
     * @param batch The SpriteBatch to draw with (already begun)
     * @param x The calculated ghost X position
     * @param y The calculated ghost Y position
     */
    public void drawDragGhost(SpriteBatch batch, float x, float y);

}
