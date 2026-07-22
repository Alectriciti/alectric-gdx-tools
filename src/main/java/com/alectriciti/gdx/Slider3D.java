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

    public Slider3D(String id, Widget parent) {
        super(id, parent); //i was parenting stuff
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

    public void setValue(Vector3 newValue) {
        this.value.x = MathUtils.clamp(newValue.x, minValue.x, maxValue.x);
        this.value.y = MathUtils.clamp(newValue.y, minValue.y, maxValue.y);
        this.zValue = MathUtils.clamp(newValue.z, minZ, maxZ);

        float norm_x = (maxValue.x == minValue.x) ? 0 : (value.x - minValue.x) / (maxValue.x - minValue.x);
        float norm_y = (maxValue.y == minValue.y) ? 0 : (value.y - minValue.y) / (maxValue.y - minValue.y);

        int max_px_x = (int)(getWidth() - knob.shape.width);
        int max_px_y = (int)(getHeight() - knob.shape.height);
        
        int new_x = (int)(getGlobalX() + (max_px_x * norm_x));
        int new_y = (int)(getGlobalY() + (max_px_y * norm_y));

        knob.setGlobalPosition(new_x, new_y);
        updateTextDisplay();
        fireEvents();
    }

    public void setZValue(float newZ) {
        this.zValue = MathUtils.clamp(newZ, minZ, maxZ);
        updateTextDisplay();
        fireEvents();
    }

    @Override
    public void reset() {
        super.reset(); // Resets X/Y and triggers listener
        setZValue(zValue_default); // Resets Z and triggers listener
    }

    @Override
    public boolean scroll(float amountX, float amountY) {
    	if(!scroll_enabled) return false;
        // Calculate the increment based on modifier keys
        float scroll_step = isControlPressed() ? scroll_amount_ctrl_z : scroll_amount_z;
        
        // Adjust the direction. Generally, scrolling "up" (negative amountY) increases the value.
        float delta = (amountY < 0) ? scroll_step : -scroll_step;
        
        setZValue(zValue + delta);
        return true;
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