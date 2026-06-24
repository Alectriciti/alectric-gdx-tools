package com.alectriciti.gdx;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;

public interface Drawable {
    
    // The fallback draw method
    public void drawShape(ShapeRenderer renderer);
    
    public void drawBorder(ShapeRenderer renderer);
    
    // Geometry bounds needed for rendering
    public Rectangle getShape();
    public float getGlobalX();
    public float getGlobalY();
    
    // Interaction states needed for styling
    public boolean isHovered();
    public boolean isFocused();
    public boolean isPressed(); 
}