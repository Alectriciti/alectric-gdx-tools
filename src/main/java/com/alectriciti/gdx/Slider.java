package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * A simple UI Slider widget.
 *
 * Usage:
 *  - Call setValueRange(min, max) to set numeric range.
 *  - Call setBaseSize(width, height) to set the base track visual & motion limits.
 *  - Call setKnobSize(width, height) to set knob drawing/interaction size.
 *  - Wire pointerDown/pointerDragged/pointerUp with your input system using global coordinates.
 *
 * The Slider stores its own value and updates its knob accordingly.
 */
public class Slider extends Widget {
    // Value range:
    private float minValue = 4f;
    private float maxValue = 32f;
    
    Knob knob;

    // Current numeric value (derived from normalized)
    private float value = 0f;
    private float value_default = 0f;
    
    float quantize_amount = 0.125f;

    // Constructor: requires parent or manager per your Widget API
    public Slider(Widget parent) {
        super("slider", parent);
        initialize();
    }

    public Slider(UIManager manager) {
        super("slider", manager);
        initialize();
    }
    
    public void setDefaultValue(float value) {
    	this.value_default = value;
    	setValue(value);
    }

    private void initialize() {
        // sensible defaults already set above; ensure value consistent
    	knob = new Knob(id+"_knob", this);
    	setSize(128, 32);
        setValue(minValue);
    }
    
    
    // ---------- Public API ----------

    /**
     * Sets the numeric value range for the slider.
     * min = knob at far left, max = knob at far right.
     */
    public void setValueRange(float min, float max) {
        if (max <= min) throw new IllegalArgumentException("max must be > min");
        this.minValue = min;
        this.maxValue = max;
        // keep current value in-range
        setValue(value);
    }
    /**
     * Returns the current numeric value (mapped from knob position).
     */
    public float getValue() {
        return value;
    }
    
    @Override
    protected void update() {
    	// TODO Auto-generated method stub
    	super.update();
    }
    
    @Override
    public boolean onPointerDown(int globalX, int globalY, int button) {
    	manager.mouse_click_offset_x = knob.getGlobalX() + (knob.shape.width/2) - globalX;
    	manager.mouse_click_offset_y = knob.getGlobalY() + (knob.shape.height/2) - globalY;
//        mouse_click_offset_x = widget_hovering.getGlobalX() - mx;
//        mouse_click_offset_y = widget_hovering.getGlobalY() - my;
    	return true;
    }
    
    @Override
    public boolean onPointerDragged(int mouseX, int mouseY) {
    	mouseX += manager.mouse_click_offset_x;
    	mouseY += manager.mouse_click_offset_y;
    	int x = (int)(mouseX - (knob.shape.width/2));
    	int max = (int) (getGlobalX()+getWidth()-knob.shape.width);
    	x = (int) Math.max(x, getGlobalX());
    	x = (int) Math.min(x, max);

    	boolean quantized = Gdx.input.isKeyPressed(Keys.SHIFT_LEFT);
    	triggerValueChange(quantized);
    	
    	knob.setGlobalPosition((quantized?(x/quantize_amount)*quantize_amount:x), knob.getGlobalY());
    	return true;
    }
    
    @Override
    public boolean onPointerUp(int globalX, int globalY, int button) {
    	// TODO Auto-generated method stub
//    	triggerValueChange();
    	return true;
    }
    
    /**
     * Applies literal location of widget to determine value change
     */
    private void triggerValueChange(boolean quantize) {
    	float normalized_value = ((float)(knob.getGlobalX() - getGlobalX())) / (getWidth()-knob.getWidth());
    	value = minValue + (normalized_value*(maxValue-minValue));
    	if(quantize) {
    		value = Math.round(value/quantize_amount)*quantize_amount;
    	}
//    	print("value: "+value);
	}

    /**
     * Programmatically set the slider's value (will clamp into range and update knob).
     * This should do the inverse of UI -> Value
     */
    public void setValue(float newValue) {
        this.value = MathUtils.clamp(newValue, minValue, maxValue);
        //evaluate normalized value
        float normalized_value = (value-minValue) / (maxValue-minValue);// / (maxValue+minValue);

    	int slider_max = (int) (getWidth()-knob.shape.width);
    	
    	int new_x = (int) (getGlobalX()+ (slider_max*normalized_value));
        
//        print("normalized: "+normalized_value);
    	knob.setGlobalPosition(new_x, knob.getGlobalY());
    }

	@Override
    public void drawShape(ShapeRenderer renderer) {
		super.drawShape(renderer);
		if(isVisible()) {
			renderer.set(ShapeType.Filled);
			renderer.setColor(color);
			style.drawRect(renderer, getGlobalX(), getGlobalY(), shape.width, knob.getHeight());
//			if(!hovering) {
			drawBorder(renderer);
//			}
		}
    }
    
    @Override
    public void drawBorder(ShapeRenderer shape_renderer) {
    	// TODO Auto-generated method stub
		shape_renderer.set(ShapeType.Line);
		shape_renderer.setColor(color_outline);
    	super.drawBorder(shape_renderer);
//		drawShapeChildren(shape_renderer, true);
    }

	public Knob getKnob() {
		return knob;
	}
    
}
