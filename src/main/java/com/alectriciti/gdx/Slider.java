package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Button.ButtonType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
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
	
	
	enum GrabStyle{
    	LAZY, // Grabs relative to the mouse
    	GRADUAL, // Moves to the mouse gradually while click is held
    	INSTANT // Snaps to where you click
    }
	
    String value_name;
    TextWidget value_display;

    // Current numeric value (derived from normalized)
    private float value = 0f;
    private float value_default = 0f;
	
    // Value range:
    private float minValue = 0f;
    private float maxValue = 1f;

    public float scroll_amount = 4f;
    public float scroll_amount_ctrl = 1f;
    
    Knob knob;

    public float quantize_amount = 0.5f;
    public float quantize_amount_ctrl = 0.125f;
    
    public GrabStyle grab_style;


    public Slider(UIManager manager) {
        super("slider", manager);
        initialize(false, 0.0f);
    }
    
    public Slider(String value_name, Widget parent) {
    	this(value_name, parent, 0.0f);
    }

    public Slider(String value_name, Widget parent, float default_value) {
        super(value_name, parent);
        this.value_name = value_name;
        initialize(value_name!=null, default_value);
    }

    public Slider(String value_name, Widget parent, float default_value, float min, float max) {
        super(value_name, parent);
        this.value_name = value_name;
        this.minValue = min;
        this.maxValue = max;
        initialize(value_name!=null, default_value);
    }

    public Slider(Widget parent, float default_value) {
        super(parent.id+"-slider", parent);
        initialize(false, default_value);
    }

    private void initialize(boolean render_text, float default_value) {
        // sensible defaults already set above; ensure value consistent
    	knob = new Knob(id+"_knob", this);
    	setSize(DEFAULT_SLIDER_SIZE, DEFAULT_WIDGET_SIZE);
    	setDefaultValue(default_value);
    	value_display = new TextWidget(this, (value_name!=null?value_name+": ":"")+value);
    	value_display.setVisible(render_text);
    }
    
    public Slider setDefaultValue(float value) {
    	this.value_default = value;
    	setValue(value);
    	return this;
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
    
    public void reset() {
    	value = value_default;
    	triggerValueChange(false);
    }
    
    @Override
    public boolean onPointerDown(int mouseX, int mouseY, int button) {
    	manager.mouse_click_offset_x = knob.getGlobalX() + (knob.shape.width/2) - mouseX;
    	manager.mouse_click_offset_y = knob.getGlobalY() + (knob.shape.height/2) - mouseY;
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

    	boolean quantized = isShiftPressed();
    	float qa = isControlPressed()?quantize_amount_ctrl:quantize_amount;
    	triggerValueChange(quantized);
    	
    	// Get a certain kind of quantize amount
    	
    	knob.setGlobalPosition((quantized?(x/qa)*qa:x), knob.getGlobalY());
    	return true;
    }
    
    @Override
    public boolean onPointerUp(int globalX, int globalY, int button) {
    	// TODO Auto-generated method stub
//    	triggerValueChange();
    	return true;
    }
    
    @Override
	public void scroll(float amountX, float amountY) {
    	int max = (int) (getGlobalX()+getWidth()-knob.shape.width);
    	int min = (int)getGlobalX();
    	float sa = isControlPressed()?scroll_amount_ctrl:scroll_amount;
    	int new_x = (int) (knob.getGlobalX() + ((amountY<0)?sa:-sa));
    	new_x = Math.min(new_x, max);
    	new_x = Math.max(new_x, min);
    	
    	knob.setGlobalPosition(new_x, knob.getGlobalY());
    	triggerValueChange(isShiftPressed());
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
    	if(value_display.isVisible()) {
    		value_display.setText((value_name!=null?value_name+": ":"")+value);
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
			style.drawRect(renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
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
