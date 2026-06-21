package com.alectriciti.gdx;

import com.badlogic.gdx.graphics.Color;

/**
 * A a simple class that holds style information for widgets. This allows for easy theming and customization of the appearance of widgets. You can create multiple Style instances with different values to create different themes for your UI.
 */
public class Style {
    
    // --- Geometry ---
    public float corner_radius = 16f;
    public float border_thickness = 2f;
    
    // --- Colors ---
    public Color color_base = new Color(0.15f, 0.15f, 0.15f, 1f);
    public Color color_hover = new Color(0.35f, 0.75f, 0.35f, 1f);
    public Color color_press = new Color(0.1f, 0.1f, 0.1f, 1f);
    public Color color_activated = new Color(0.1f, 0.75f, 0.1f, 1f);
    
    public Color color_outline = new Color(0.05f, 0.05f, 0.05f, 1f);
//    public Color color_trim_hover = new Color(0.4f, 0.9f, 0.8f, 1f);
    
    public Color color_text = Color.WHITE.cpy();
    public Color color_text_pressed = new Color(0.7f, 0.7f, 0.7f, 0.8f);
    public Color color_text_activated = new Color(0.1f, 0.1f, 0.1f, 1);
    
    public Color color_edit = new Color(1, 0.25f, 0.25f, 1);
    
	public Color color_effect = new Color(Color.WHITE);

    public Style() {
    }

    public Style(Color color) {
    	//dynamically generate hover/press colors based on the base color
    	this.color_base = color.cpy();
    	this.color_hover = color.cpy().mul(1.2f, 1.2f, 1.2f, 1f);
    	this.color_press = color.cpy().mul(0.8f, 0.8f, 0.8f, 1f);
    	//trim colors can be a desaturated version of the base color
//    	this.color_trim = color.cpy().mul(0.5f, 0.5f, 0.5f, 1f);
//    	this.color_trim_hover = color.cpy().mul(0.7f, 0.7f, 0.7f, 1f);
    	
    }
}