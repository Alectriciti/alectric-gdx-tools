package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import java.util.ArrayList;
import java.util.List;

import com.alectriciti.gdx.Button.ButtonType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Input.Orientation;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

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
    
    boolean horizontal = true;

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
    
    public GrabStyle grab_style = GrabStyle.LAZY;
    

	protected List<Runnable> change_listeners = new ArrayList<Runnable>();
	
	public void addChangeListener(Runnable r) {
		change_listeners.add(r);
	}
    


    public Slider(UIManager manager) {
        super("slider", manager);
        initialize(false, 0.0f, DEFAULT_SLIDER_SIZE);
    }


    public Slider(UIManager manager, boolean horizontal) {
        super("slider", manager);
        this.horizontal = horizontal;
        initialize(false, 0.0f, DEFAULT_SLIDER_SIZE);
    }
    
    public Slider(String value_name, Widget parent) {
    	this(value_name, parent, 0.0f);
    }

    public Slider(String value_name, Widget parent, float default_value) {
        super(value_name, parent);
        this.value_name = value_name;
        initialize(value_name!=null, default_value, DEFAULT_SLIDER_SIZE);
    }
    
    public Slider(String value_name, Widget parent, float default_value, float min, float max) {
    	this(value_name, parent, default_value, min, max, true);
    }

    public Slider(String value_name, Widget parent, float default_value, float min, float max, boolean horizontal) {
        super(value_name, parent);
        this.value_name = value_name;
        this.minValue = min;
        this.maxValue = max;
        this.horizontal = horizontal;
        initialize(value_name!=null, default_value, DEFAULT_SLIDER_SIZE);
    }

    public Slider(Widget parent, float default_value) {
        super(parent.id+"-slider", parent);
        initialize(false, default_value, DEFAULT_SLIDER_SIZE);
    }

    private void initialize(boolean render_text, float default_value, int slider_size) {
        // sensible defaults already set above; ensure value consistent
    	knob = new Knob(id+"_knob", this);
    	if(horizontal) {
    		setSize(slider_size, DEFAULT_WIDGET_SIZE);
    	}else {
    		setSize(DEFAULT_WIDGET_SIZE, slider_size);
    	}
    	setDefaultValue(default_value);
    	value_display = new TextWidget(this, (value_name!=null?value_name+": ":"")+value);
    	value_display.setVisible(render_text);
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
     * Sets the physical length of this slider
     */
    public Slider setLength(float length) {
    	if(horizontal) {
    		setSize(length, getHeight());
    	}else {
    		setSize(getWidth(), length);
    	}
    	return this;
    }
    
    /**
     * Starting value and reset value
     */
    public Slider setDefaultValue(float value) {
    	this.value_default = value;
    	return this;
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
    	switch(grab_style) {
		case GRADUAL:
			//slowly move toward the location clicked
			break;
		case INSTANT:
			//get clicked location and move the slider to it instantly
			
			break;
		case LAZY: //default implementation
			break;
		default:
			break;
    	}
    	return true;
    }
    
//    
//    /**
//     * Gets the offset relative to the slider's knob
//     * @return
//     */
//    public Vector2 getNewSliderPosition(int mouseX, int mouseY) {
//    	Vector2 v = Vector2.Zero.cpy();
//    	
//		return v;
//    }
//    
    @Override
    public boolean onPointerDragged(int mouseX, int mouseY) {
    	//apply the offset relative to where the mouse clicked on the widget
    	mouseX += manager.mouse_click_offset_x;
    	mouseY += manager.mouse_click_offset_y;
    	
    	float globalPosition = horizontal?getGlobalX():getGlobalY();
    	float knob_length = horizontal?knob.shape.width:knob.shape.height;
    	
    	
    	int factor = (int)((horizontal?mouseX:mouseY) - (knob_length/2));
    	int max = (int) (globalPosition+getLength()-knob_length);
    	factor = (int) Math.max(factor, globalPosition);
    	factor = (int) Math.min(factor, max);

    	boolean quantized = isShiftPressed();
    	float qa = isControlPressed()?quantize_amount_ctrl:quantize_amount;
    	triggerValueChange(quantized);
    	
    	// Get a certain kind of quantize amount
    	if(horizontal) {
    		knob.setGlobalPosition((quantized?(factor/qa)*qa:factor), knob.getGlobalY());
    	}else {
    		knob.setGlobalPosition(knob.getGlobalX(), (quantized?(factor/qa)*qa:factor));
    	}
    	return true;
    }
    
    private float getLength() {
    	return horizontal?getWidth():getHeight();
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
    	
    	for(Runnable r : change_listeners) {
    		r.run();
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
