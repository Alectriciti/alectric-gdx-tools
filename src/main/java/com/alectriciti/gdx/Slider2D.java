package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.ArrayList;
import java.util.List;

import com.alectriciti.gdx.Button.ButtonType;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 * A 2D UI Slider widget.
 *
 * Usage:
 *  - Drag the knob along the X and Y axes simultaneously.
 *  - Perfect for 2D maps, XY joysticks, or Color Picker (Saturation/Value) planes.
 */
public class Slider2D extends Widget {
    
    public String value_name;
    public TextWidget value_display;

    // Current numeric value (derived from normalized position)
    protected Vector2 value = new Vector2();
    protected Vector2 value_default = new Vector2();
    
    // Value range
    protected Vector2 minValue = new Vector2(0f, 0f);
    protected Vector2 maxValue = new Vector2(1f, 1f);

    public Vector2 quantize_amount = new Vector2(0.5f, 0.5f);
    public Vector2 quantize_amount_ctrl = new Vector2(0.125f, 0.125f);
    
    public GrabStyle grab_style = GrabStyle.LAZY;
    public float grab_strength = 0.5f; // Only relevant for GRADUAL
    
    protected Knob knob;
    protected List<Runnable> change_listeners = new ArrayList<Runnable>();

    // Tracks the raw, un-offset mouse position for GRADUAL updates
    private int last_mouse_x = 0;
    private int last_mouse_y = 0;

    public Slider2D(String id, UIManager manager) {
        super(id, manager);
        initialize(false, new Vector2(0, 0), 150, 150);
    }

    public Slider2D(String value_name, Widget parent, Vector2 default_value) {
        super(value_name, parent);
        this.value_name = value_name;
        initialize(value_name != null, default_value, 150, 150);
    }

    private void initialize(boolean render_text, Vector2 default_value, float width, float height) {
        knob = new Knob(id + "_knob", this, Orientation.NONE);
        // A square knob generally looks better for 2D bounds
        knob.setSize(16, 16); 
        setSize(width, height);
        setDefaultValue(default_value);
        
        value_display = new TextWidget(this, "");
        value_display.setVisible(render_text);
        updateTextDisplay();
    }

    public void addChangeListener(Runnable r) {
        change_listeners.add(r);
    }

    public void setValueRange(Vector2 min, Vector2 max) {
        this.minValue.set(min);
        this.maxValue.set(max);
        setValue(value); // clamp current
    }

    public Slider2D setDefaultValue(Vector2 default_value) {
        this.value_default.set(default_value);
        return this;
    }

    public Vector2 getValue() {
        return value;
    }

	/**
	 * Sets the grab style for the slider.
	 * @param style The desired grab style (LAZY, GRADUAL, INSTANT).
	 * @param strength The strength of the grab effect (only relevant for GRADUAL).
	 * @return The Slider instance for chaining.
	 */
    public void setGrabStyle(GrabStyle style, float strength) {
		this.grab_style = style;
		this.grab_strength = 1.0f - strength;
	}
    
    public void setGrabStyle(GrabStyle style) {
    	setGrabStyle(style, 0.75f);
	}

    public void reset() {
        setValue(value_default);
        triggerValueChange(false);
    }

    @Override
    protected void update() {
        super.update();
        
        // Handle GRADUAL grab style logic across both axes
        if (isPressed() && grab_style == GrabStyle.GRADUAL) {
            if (Math.abs(manager.mouse_click_offset_x) > 0.5f || Math.abs(manager.mouse_click_offset_y) > 0.5f) {
                manager.mouse_click_offset_x *= grab_strength;
                manager.mouse_click_offset_y *= grab_strength;
                onPointerDragged(last_mouse_x, last_mouse_y);
            } else {
                manager.mouse_click_offset_x = 0;
                manager.mouse_click_offset_y = 0;
            }
        }
    }

    @Override
    public boolean onPointerDown(int mouseX, int mouseY, int button) {
    	
    	// Check if the click coordinates fall directly within the 2D knob's square bounds
        boolean hitKnob = mouseX >= knob.getGlobalX() && mouseX <= knob.getGlobalX() + knob.shape.width &&
                          mouseY >= knob.getGlobalY() && mouseY <= knob.getGlobalY() + knob.shape.height;
        
        // Fail fast if they missed the handle in GRAB mode
        if (grab_style == GrabStyle.GRAB && !hitKnob) {
            return false;
        }
        
        this.last_mouse_x = mouseX;
        this.last_mouse_y = mouseY;

        float initial_offset_x = knob.getGlobalX() + (knob.shape.width / 2f) - mouseX;
        float initial_offset_y = knob.getGlobalY() + (knob.shape.height / 2f) - mouseY;

        switch(grab_style) {
            case INSTANT:
                manager.mouse_click_offset_x = 0;
                manager.mouse_click_offset_y = 0;
                onPointerDragged(mouseX, mouseY);
                break;
            case GRADUAL:
                manager.mouse_click_offset_x = initial_offset_x;
                manager.mouse_click_offset_y = initial_offset_y;
                onPointerDragged(mouseX, mouseY);
                break;
            case LAZY: 
            default:
                manager.mouse_click_offset_x = initial_offset_x;
                manager.mouse_click_offset_y = initial_offset_y;
                break;
        }
        return true;
    }

    @Override
    public boolean onPointerDragged(int mouseX, int mouseY) {
        this.last_mouse_x = mouseX;
        this.last_mouse_y = mouseY;

        mouseX += manager.mouse_click_offset_x;
        mouseY += manager.mouse_click_offset_y;
        
        float globalX = getGlobalX();
        float globalY = getGlobalY();
        float knob_w = knob.shape.width;
        float knob_h = knob.shape.height;
        
        int targetX = (int)(mouseX - (knob_w / 2));
        int targetY = (int)(mouseY - (knob_h / 2));
        
        int maxX = (int)(globalX + getWidth() - knob_w);
        int maxY = (int)(globalY + getHeight() - knob_h);

        // Clamp to 2D Bounds
        targetX = MathUtils.clamp(targetX, (int)globalX, maxX);
        targetY = MathUtils.clamp(targetY, (int)globalY, maxY);

        boolean quantized = isShiftPressed();
        float qa_x = isControlPressed() ? quantize_amount_ctrl.x : quantize_amount.x;
        float qa_y = isControlPressed() ? quantize_amount_ctrl.y : quantize_amount.y;

        if (quantized) {
            targetX = (int) ((targetX / qa_x) * qa_x);
            targetY = (int) ((targetY / qa_y) * qa_y);
        }
        
        knob.setGlobalPosition(targetX, targetY);
        triggerValueChange(quantized);
        
        return true;
    }

    protected void triggerValueChange(boolean quantize) {
        float norm_x = (knob.getGlobalX() - getGlobalX()) / (getWidth() - knob.getWidth());
        float norm_y = (knob.getGlobalY() - getGlobalY()) / (getHeight() - knob.getHeight());

        value.x = minValue.x + (norm_x * (maxValue.x - minValue.x));
        value.y = minValue.y + (norm_y * (maxValue.y - minValue.y));

        if(quantize) {
            value.x = Math.round(value.x / quantize_amount.x) * quantize_amount.x;
            value.y = Math.round(value.y / quantize_amount.y) * quantize_amount.y;
        }

        updateTextDisplay();
        
        for(Runnable r : change_listeners) {
            r.run();
        }
    }

    protected void updateTextDisplay() {
        if(value_display != null && value_display.isVisible()) {
            value_display.setText((value_name != null ? value_name + ": " : "") + 
                String.format("%.2f", value.x) + ", " + String.format("%.2f", value.y));
        }
    }

    public void setValue(Vector2 newValue) {
        this.value.x = MathUtils.clamp(newValue.x, minValue.x, maxValue.x);
        this.value.y = MathUtils.clamp(newValue.y, minValue.y, maxValue.y);

        float norm_x = (maxValue.x == minValue.x) ? 0 : (value.x - minValue.x) / (maxValue.x - minValue.x);
        float norm_y = (maxValue.y == minValue.y) ? 0 : (value.y - minValue.y) / (maxValue.y - minValue.y);

        int max_px_x = (int)(getWidth() - knob.shape.width);
        int max_px_y = (int)(getHeight() - knob.shape.height);
        
        int new_x = (int)(getGlobalX() + (max_px_x * norm_x));
        int new_y = (int)(getGlobalY() + (max_px_y * norm_y));

        knob.setGlobalPosition(new_x, new_y);
        updateTextDisplay();
    }

    @Override
    public void drawShape(ShapeRenderer renderer) {
        super.drawShape(renderer);
        if(isVisible()) {
            renderer.set(ShapeType.Filled);
            renderer.setColor(color);
            style.drawRect(renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
			if(!hovering) {
			drawBorder(renderer);
			}
        }
    }
    
    @Override
    public void drawBorder(ShapeRenderer shape_renderer) {
    	if(!isVisible()) {
    		return;
    	}
    	super.drawBorder(shape_renderer);
        boolean hitKnob = getMouseX() >= knob.getGlobalX() && getMouseX() <= knob.getGlobalX() + knob.shape.width &&
                getMouseY() >= knob.getGlobalY() && getMouseY() <= knob.getGlobalY() + knob.shape.height;
        if(hitKnob) {
			shape_renderer.set(ShapeType.Line);
			shape_renderer.setColor(color_outline);
			knob.style.drawRect(shape_renderer, knob.getGlobalX(), knob.getGlobalY(), knob.shape.width, knob.shape.height);
        }
    }


    
    @Override
    protected void setHoverColor() {
    	if(grab_style == GrabStyle.GRAB) {
    		if(!knob.isMouseOver() && !isPressed())return;
    	}
		super.setHoverColor();
    }

    public Knob getKnob() {
        return knob;
    }
    
    
	@Override
	public ContextWidget spawnContextWidget() {
		// TODO Auto-generated method stub
		ContextWidget context = new ContextWidget(this);
		Button show_value = new Button("show value", context);
		show_value.setType(ButtonType.TOGGLE);
		show_value.activated = value_display.isVisible();
		show_value.addOnActivate(()->{value_display.setVisible(true);});
		show_value.addOnDeactivate(()->{value_display.setVisible(false);});
		return context;
	}
}