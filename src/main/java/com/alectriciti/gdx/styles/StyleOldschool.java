package com.alectriciti.gdx.styles;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Drawable;
import com.alectriciti.gdx.Style;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

public class StyleOldschool extends Style {
	
	public StyleOldschool() {
		super();
	}

    @Override
    public void drawShape(Drawable drawable, ShapeRenderer renderer) {
    	super.drawShape(drawable, renderer);
        
        // We can check state directly from the interface! No casting required.
//        if (drawable.isPressed()) {
//            renderer.setColor(this.color_press);
//        } else if (drawable.isHovered()) {
//            renderer.setColor(this.color_hover);
//        } else {
//            renderer.setColor(this.color_base);
//        }
//
//        // We can get exact coordinates directly from the interface
//        float x = drawable.getGlobalX();
//        float y = drawable.getGlobalY();
//        float w = drawable.getShape().width;
//        float h = drawable.getShape().height;
//
//        // Custom oldschool sharp drawing logic here
//        renderer.rect(x, y, w, h);
    }
    
    @Override
    public void drawBorder(Drawable drawable, ShapeRenderer shape_renderer) {
    	// TODO Auto-generated method stub
//    	super.drawBorder(drawable, shape_renderer);
        float x = drawable.getGlobalX();
        float y = drawable.getGlobalY();
        float w = drawable.getShape().width;
        float h = drawable.getShape().height;
        
		shape_renderer.set(ShapeType.Line);
		shape_renderer.setColor(color_outline);
		
		drawRectRound(shape_renderer, x+1, y+1, w-2, h-2, corner_radius);

    }
}