package com.alectriciti.gdx.styles;

import com.alectriciti.gdx.Drawable;
import com.alectriciti.gdx.Style;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class StyleTarget extends Style {

    @Override
    public void drawShape(Drawable drawable, ShapeRenderer renderer) {
        
        // We can check state directly from the interface! No casting required.
        if (drawable.isPressed()) {
            renderer.setColor(this.color_press);
        } else if (drawable.isHovered()) {
            renderer.setColor(this.color_hover);
        } else {
            renderer.setColor(this.color_base);
        }

        // We can get exact coordinates directly from the interface
        float x = drawable.getGlobalX();
        float y = drawable.getGlobalY();
        float w = drawable.getShape().width;
        float h = drawable.getShape().height;

        // Custom oldschool sharp drawing logic here
        renderer.rect(x, y, w, h);
    }
}