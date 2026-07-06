package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.LerpColor;
import static com.alectriciti.gdx.Toolkit.isControlPressed;
import static com.badlogic.gdx.graphics.GL20.GL_FUNC_ADD;
import static com.badlogic.gdx.graphics.GL20.GL_FUNC_REVERSE_SUBTRACT;
import static com.badlogic.gdx.graphics.GL20.GL_ONE;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_CONSTANT_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_DST_COLOR;
import static com.badlogic.gdx.graphics.GL20.GL_ONE_MINUS_SRC_COLOR;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA;
import static com.badlogic.gdx.graphics.GL20.GL_SRC_ALPHA_SATURATE;

import java.text.DecimalFormat;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * A 3D Color Picker widget extending Slider3D.
 */
public class SliderColorPicker extends Slider3D {
	
	class ColorKnob extends Knob{

		public ColorKnob(String id, Widget parent, Orientation orientation) {
			super(id, parent, orientation);
			setStyle(Style.BASIC, true);
		}
		
		@Override
		public void drawShape(ShapeRenderer shape_renderer) {
			// TODO Auto-generated method stub
    		if(!isVisible()) {
    			return;
    		}
			color_outline = getCurrentColor();
			drawBorder(shape_renderer);
		}
		
		@Override
		public void drawBorder(ShapeRenderer shape_renderer) {
//			Gdx.gl.glBlendEquation(GL20.GL_FUNC_ADD);
    		if(!isVisible()) {
    			return;
    		}
			shape_renderer.setColor(color_outline);
    		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);

			shape_renderer.flush();
//			Gdx.gl.glEnable(GL20.GL_BLEND);
//			Gdx.gl.glBlendFunc(GL20.GL_ONE_MINUS_DST_COLOR, GL20.GL_ZERO);
//			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl.glEnable(GL20.GL_BLEND);
		    Gdx.gl.glBlendFunc(GL20.GL_ONE_MINUS_DST_COLOR, GL20.GL_ZERO);
    		shape_renderer.set(ShapeType.Line);
    		shape_renderer.setColor(Color.WHITE);
			
    		
    		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);
//			shape_renderer.rect(getGlobalX(), getGlobalY(), shape.width, shape.height);
//    		style.drawRect(shape_renderer, getGlobalX(), getGlobalY(), shape.width, shape.height);

    	    // 4. Flush AGAIN to force the border to render using our custom blend function
    	    shape_renderer.flush();
    	    
    	    // 5. Restore standard alpha blending so the rest of your UI renders normally
    	    Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl.glDisable(GL20.GL_BLEND);

    		shape_renderer.set(ShapeType.Filled);
    		
//    	    super.drawBorder(shape_renderer);
		}
		
	}

    private ColorPickerMode mode = ColorPickerMode.SATURATION;

    // Reusable color instances for the 4-point gradient to prevent memory allocation every frame
    private Color colorBL = new Color(Color.BLACK);
    private Color colorBR = new Color(Color.BLACK);
    private Color colorTL = new Color(Color.WHITE);
    private Color colorTR = new Color(Color.RED);

    public SliderColorPicker(String id, UIManager manager) {
        super(id, manager);
        init();
        // Ensure the Z-axis (scroll wheel) defaults to a normalized 0-1 range[cite: 23]
    }

    public SliderColorPicker(String id, Widget w) {
        super(id, w);
        init();
        // Ensure the Z-axis (scroll wheel) defaults to a normalized 0-1 range[cite: 23]
    }

    public SliderColorPicker(String value_name, Widget parent, Vector3 default_value) {
        super(value_name, parent, default_value); //[cite: 23]
        init();
    }

    public void init() {
        this.knob.destroy();
        this.knob = new ColorKnob(id + "_colorknob", this, Orientation.NONE);
        getKnob().setSize(5);
		setStyle(Style.BASIC, true);
        setGrabStyle(GrabStyle.GRADUAL, 0.25f);
        setZRange(0f, 1f); 
		this.scroll_amount_z = 0.05f;
		this.scroll_amount_ctrl_z = 0.01f;

		value_display = new TextWidget(this, new ColoredText("R ", Color.RED),
				new ColoredText("G ", Color.GREEN),
				new ColoredText("B ", Color.BLUE)
				);
		value_display.enableDropShadow(Color.WHITE);
		value_display.setAutoreconstruct(false);
        updateTextDisplay();
    }
    /**
     * Instantly swaps the dimensional behavior of the slider.
     */
    public void setColorPickerMode(ColorPickerMode mode) {
        this.mode = mode;
        // Optionally trigger a re-render or listener update here
    }

    public ColorPickerMode getColorPickerMode() {
        return this.mode;
    }

    /**
     * Calculates the exact color at the current knob position.
     * Maps X, Y, and Z to HSV based on the current mode.
     */
    public Color getCurrentColor() {
        float hue = 0f, sat = 0f, val = 0f;

        // value.x and value.y are inherited from Slider2D/Slider3D[cite: 23]
        if (mode == ColorPickerMode.SATURATION) { //[cite: 24]
            sat = value.x;
            val = value.y;
            hue = zValue * 360f; // LibGDX Hue expects 0-360[cite: 23]
        } else if (mode == ColorPickerMode.HUE) { //[cite: 24]
            hue = value.x * 360f;
            val = value.y;
            sat = zValue; //[cite: 23]
        }
        
        Color c = new Color().fromHsv(hue, sat, val);
        c.a = 1.0f;
        return c;
    }

    /**
     * Updates the slider's 3D knob position based on an externally provided color.
     * Useful for syncing with text inputs, other sliders, or external events.
     */
    public void updateColorChanged(Color newColor) {
        float[] hsv = new float[3];
        newColor.toHsv(hsv);
        
        float normalizedHue = hsv[0] / 360f; // Convert 0-360 back to 0-1
        float sat = hsv[1];
        float val = hsv[2];

        if (mode == ColorPickerMode.SATURATION) { //[cite: 24]
            // X=Sat, Y=Val, Z=Hue
            set3DValue(new Vector3(sat, val, normalizedHue)); //[cite: 23]
        } else if (mode == ColorPickerMode.HUE) { //[cite: 24]
            // X=Hue, Y=Val, Z=Sat
            set3DValue(new Vector3(normalizedHue, val, sat)); //[cite: 23]
        }
    }

    /**
     * Overrides the standard Widget rendering to draw a 4-point color gradient.
     */
    @Override
    public void drawShape(ShapeRenderer renderer) {
        if (!isVisible()) return; //[cite: 25]

        renderer.set(ShapeRenderer.ShapeType.Filled);

        // Update gradient corners mathematically based on the current Z (scroll) value
        updateGradientCorners();

        // Retrieve global coordinates and dimensions from the parent Widget class[cite: 25]
        float gx = getGlobalX(); //[cite: 25]
        float gy = getGlobalY(); //[cite: 25]
        float gw = shape.width;  //[cite: 25]
        float gh = shape.height; //[cite: 25]

        // LibGDX rect signature for 4 colors: x, y, width, height, colBottomLeft, colBottomRight, colTopRight, colTopLeft
        renderer.rect(gx, gy, gw, gh, colorBL, colorBR, colorTR, colorTL);

        // Call super if Slider2D/Slider3D handles drawing the actual interactive knob, 
        // or draw your custom knob directly here!
//        super.drawShape(renderer); 
    }

    
    @Override
    protected void updateTextDisplay() {
        	
    	if(value_display != null && value_display.isVisible()) {
    		Color c = getCurrentColor();
    		DecimalFormat f = new DecimalFormat("#.##");
    		f.setMinimumFractionDigits(2);
        	value_display.setText(0, f.format(c.r));
        	value_display.setText(1, f.format(c.g)+" ");
        	value_display.setText(2, f.format(c.b)+" ");
        	value_display.setText(0, new Color(c.r, 0, 0, 1));
        	value_display.setText(1, new Color(0, c.g, 0, 1));
        	value_display.setText(2, new Color(0, 0, c.b, 1));
        	value_display.reconstruct();
        }
    }

    /**
     * Calculates the 4 corner colors of the background gradient box based on the mode.
     */
    private void updateGradientCorners() {
        if (mode == ColorPickerMode.SATURATION) {
            // X axis = Saturation, Y axis = Luminance/Value
            colorBL.set(Color.BLACK); // Sat 0, Val 0
            colorBR.set(Color.BLACK); // Sat 1, Val 0
            colorTL.set(Color.WHITE); // Sat 0, Val 1
            colorTR.fromHsv(zValue * 360f, 1f, 1f); // Sat 1, Val 1 at current Hue (Z)[cite: 23]
            
        } else if (mode == ColorPickerMode.HUE) {
            // X axis = Hue, Y axis = Luminance/Value
            colorBL.set(Color.BLACK);
            colorBR.set(Color.BLACK);
            
            // NOTE: A standard 4-point quad interpolation in LibGDX mixes RGB directly.
            // Left is Hue 0 (Red), Right is Hue 360 (also Red). 
            colorTL.fromHsv(0f, zValue, 1f); 
            colorTR.fromHsv(360f, zValue, 1f); 
        }
    }

    @Override
    public void scroll(float amountX, float amountY) {
        // Calculate the increment based on modifier keys
        float scroll_step = isControlPressed() ? scroll_amount_ctrl_z : scroll_amount_z;
        
        // Adjust the direction. Generally, scrolling "up" (negative amountY) increases the value.
        float delta = (amountY < 0) ? scroll_step : -scroll_step;
        if(delta<0) {
        	delta+=1;
        }
        setZValue((zValue + delta )% 1.0f);
    }
    
    
    @Override
    public void drawBorder(ShapeRenderer shape_renderer) {
    	// TODO Auto-generated method stub
//    	super.drawBorder(shape_renderer);
    }
}