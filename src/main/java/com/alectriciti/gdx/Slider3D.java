package com.alectriciti.gdx;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import static com.alectriciti.gdx.Toolkit.*;

/**
 * A 3D UI Slider widget.
 *
 * Usage:
 *  - X and Y axes are controlled by dragging the knob (Inherited from Slider2D).
 *  - Z axis is controlled by using the mouse scroll wheel while hovering the widget.
 *  - Excellent for complete 3D Color Pickers (e.g., Hue on Z, Saturation/Value on X/Y).
 */
public class Slider3D extends Slider2D {

    protected float zValue = 0f;
    protected float zValue_default = 0f;
    
    protected float minZ = 0f;
    protected float maxZ = 1f;

    // Controls how much Z changes per scroll tick
    public float scroll_amount_z = 0.05f;
    public float scroll_amount_ctrl_z = 0.01f;

    public Slider3D(String id, UIManager manager) {
        super(id, manager);
    }

    public Slider3D(String value_name, Widget parent, Vector3 default_value) {
        // Pass X and Y to the 2D superclass
        super(value_name, parent, new Vector2(default_value.x, default_value.y));
        this.zValue_default = default_value.z;
        this.zValue = default_value.z;
    }

    public void setZRange(float min, float max) {
        this.minZ = min;
        this.maxZ = max;
        setZValue(zValue); // Clamp current
    }

    public float getZValue() {
        return zValue;
    }

    /** Returns the full 3-dimensional state of the slider */
    public Vector3 get3DValue() {
        return new Vector3(value.x, value.y, zValue);
    }

    public void set3DValue(Vector3 newValue) {
        super.setValue(new Vector2(newValue.x, newValue.y)); // Updates X and Y
        setZValue(newValue.z);                               // Updates Z
    }

    public void setZValue(float newZ) {
        this.zValue = MathUtils.clamp(newZ, minZ, maxZ);
        updateTextDisplay();
        
        // Fire listeners so the rest of your UI knows Z changed
        for (Runnable r : change_listeners) {
            r.run();
        }
    }

    @Override
    public void reset() {
        super.reset(); // Resets X/Y and triggers listener
        setZValue(zValue_default); // Resets Z and triggers listener
    }

    @Override
    public void scroll(float amountX, float amountY) {
        // Calculate the increment based on modifier keys
        float scroll_step = isControlPressed() ? scroll_amount_ctrl_z : scroll_amount_z;
        
        // Adjust the direction. Generally, scrolling "up" (negative amountY) increases the value.
        float delta = (amountY < 0) ? scroll_step : -scroll_step;
        
        setZValue(zValue + delta);
    }

    @Override
    protected void updateTextDisplay() {
        if (value_display != null && value_display.isVisible()) {
            value_display.setText((value_name != null ? value_name + ": " : "") + 
                String.format("%.2f", value.x) + ", " + 
                String.format("%.2f", value.y) + ", " + 
                String.format("%.2f", zValue));
        }
    }
}