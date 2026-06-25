package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.LerpColor;
import static com.alectriciti.gdx.Toolkit.drawRectRound;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * A a simple class that holds style information for widgets. This allows for easy theming and customization of the appearance of widgets. You can create multiple Style instances with different values to create different themes for your UI.
 */
public class Style {
    
    // --- Geometry ---
    public float corner_radius = 3f;
    public float border_thickness = 2f;
    
    // --- Timing ---
    public float color_fade_in = 0.25f;
    public float color_fade_out = 0.5f;
    
    // --- Colors ---
    public Color color_base = new Color(0.15f, 0.15f, 0.15f, 1f);
    public Color color_hover = new Color(0.35f, 0.75f, 0.35f, 1f); // Used for Hover AND Click Effects
    public Color color_press = new Color(0.1f, 0.1f, 0.1f, 1f);
    public Color color_activated = new Color(0.1f, 0.5f, 0.1f, 1f);
    
    // --- Default Outline of Widgets ---
    public Color color_outline = new Color(0.05f, 0.05f, 0.05f, 1f);
    
    public Color color_text = Color.WHITE.cpy();
    public Color color_text_pressed = new Color(0.7f, 0.7f, 0.7f, 0.8f);
    public Color color_text_activated = new Color(0.1f, 0.1f, 0.1f, 1);
    
    public Color color_edit = new Color(1, 0.25f, 0.25f, 1);
    
	public Color color_effect = new Color(Color.WHITE);
	
	public BitmapFont font;

    public Style() {
    	this.font = UIManager.primary_font;
    }


    public Style(BitmapFont font) {
    	this.font = font;
    }
    

	public void drawShape(Drawable drawable, ShapeRenderer renderer) {
		drawable.drawShape(renderer);
	}
	
	public void drawBorder(Drawable drawable, ShapeRenderer renderer) {
		drawable.drawBorder(renderer);
	}

	
	/**
	 * Draws a rectangle based on the existing setup
	 */
	public void drawRect(ShapeRenderer renderer, float x, float y, float w, float h) {
		drawRectRound(renderer, x, y, w, h, corner_radius);
	}
    
    
}