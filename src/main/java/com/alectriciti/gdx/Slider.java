package com.alectriciti.gdx;

import java.util.ArrayList;
import java.util.List;

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
    private float minValue = 0f;
    private float maxValue = 1f;

    // Visual sizes (base = track; knob = draggable "thumb")
    private float baseWidth = 200f;
    private float baseHeight = 8f;
    private float knobWidth = 12f;
    private float knobHeight = 18f;

    // Current normalized position of knob (0..1). Derived from value.
    private float normalized = 0f;

    // Current numeric value (derived from normalized)
    private float value = 0f;

    // Interaction state
    private boolean dragging = false;
    private float dragOffsetX = 0f; // offset from knob left when pointer down

    // Visual colors (customize as desired)
    private Color baseColor = new Color(0.2f, 0.2f, 0.2f, 1f);
    private Color knobColor = new Color(0.9f, 0.9f, 0.9f, 1f);
    private Color knobBorderColor = new Color(0.2f, 0.2f, 0.2f, 1f);

    // Change listeners that fire when value changes
    private final List<Runnable> changeListeners = new ArrayList<>();

    // Constructor: requires parent or manager per your Widget API
    public Slider(Widget parent) {
        super("slider", parent);
        initialize();
    }

    public Slider(com.alectriciti.gdx.UIManager manager) {
        super("slider", manager);
        initialize();
    }

    private void initialize() {
        // sensible defaults already set above; ensure value consistent
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
     * Set the visual size of the knob (thumb).
     */
    public void setKnobSize(float width, float height) {
        this.knobWidth = Math.max(1f, width);
        this.knobHeight = Math.max(1f, height);
    }

    /**
     * Set the visual size of the base (track). width determines the motion range.
     * height only affects visual thickness.
     */
    public void setBaseSize(float width, float height) {
        this.baseWidth = Math.max(1f, width);
        this.baseHeight = Math.max(1f, height);
    }

    /**
     * Returns the current numeric value (mapped from knob position).
     */
    public float getValue() {
        return value;
    }

    /**
     * Programmatically set the slider's value (will clamp into range and update knob).
     */
    public void setValue(float newValue) {
        this.value = MathUtils.clamp(newValue, minValue, maxValue);
        // compute normalized position 0..1
        this.normalized = (value - minValue) / (maxValue - minValue);
        // optionally notify listeners because setValue is explicit
        fireChange();
    }

    /**
     * Add a listener that runs when the value changes due to user interaction
     * or programmatic changes via setValue.
     */
    public void addChangeListener(Runnable listener) {
        if (listener != null) changeListeners.add(listener);
    }

    /**
     * Remove a previously added listener.
     */
    public void removeChangeListener(Runnable listener) {
        changeListeners.remove(listener);
    }

    // ---------- Input handling helpers ----------
    // These accept global coordinates, to be called from your input routing.

    /**
     * Should be called when pointer/mouse is pressed down.
     * globalX/globalY = coordinates in same space as getGlobalX()/getGlobalY()
     */
    public boolean pointerDown(float globalX, float globalY) {
        if (!visible) return false;

        // If pointer is over knob, begin dragging
        if (isPointerOverKnob(globalX, globalY)) {
            dragging = true;
            // track offset of pointer inside knob so the knob doesn't jump to pointer left
            float knobLeft = getKnobLeft();
            dragOffsetX = globalX - knobLeft;
            return true; // consumed
        }

        // If pointer is over the base (but not over knob), jump knob to that location and start drag
        if (isPointerOverBase(globalX, globalY)) {
            float knobCenterX = clampKnobCenter(globalX);
            // compute normalized & set
            setNormalizedFromKnobCenter(knobCenterX);
            // set drag state such that dragging continues with pointer offset centered
            dragging = true;
            float knobLeft = getKnobLeft();
            dragOffsetX = globalX - knobLeft;
            return true;
        }

        return false;
    }

    /**
     * Call when pointer is dragged/moved while pressed.
     */
    public boolean pointerDragged(float globalX, float globalY) {
        if (!visible || !dragging) return false;

        // compute desired knobLeft such that pointer position corresponds with saved drag offset
        float desiredKnobLeft = globalX - dragOffsetX;

        // convert left -> center and clamp, then update normalized/value
        float knobCenter = clampKnobCenterFromLeft(desiredKnobLeft);
        setNormalizedFromKnobCenter(knobCenter);
        return true;
    }

    /**
     * Call when pointer is released.
     */
    public boolean pointerUp(float globalX, float globalY) {
        if (!visible) return false;
        if (dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    // ---------- Rendering ----------
    /**
     * Draw the slider using a ShapeRenderer.
     * You probably call this from your widget render loop (drawShape or similar).
     */
    @Override
    public void drawShape(ShapeRenderer renderer, boolean recursive) {
        if (!visible) return;

        // We draw the base (track) first, then the knob on top.
        float gx = getGlobalX();
        float gy = getGlobalY();

        // Base: draw centered vertically on widget's Y (we'll treat getGlobalY() as bottom of track)
//        renderer.begin(ShapeType.Filled);
		renderer.set(ShapeType.Filled);
        renderer.setColor(baseColor);
        renderer.rect(gx, gy + (knobHeight - baseHeight) * 0.5f, baseWidth, baseHeight);
//        renderer.end();

        // Knob
        float knobLeft = getKnobLeft();
        float knobBottom = gy; // align bottom of knob with widget Y; you can adjust as desired
//        renderer.begin(ShapeType.Filled);
        renderer.setColor(knobColor);
        renderer.rect(knobLeft, knobBottom, knobWidth, knobHeight);
//        renderer.end();

        // Knob border
//        renderer.begin(ShapeType.Line);
		renderer.set(ShapeType.Line);
        renderer.setColor(knobBorderColor);
        renderer.rect(knobLeft, knobBottom, knobWidth, knobHeight);
//        renderer.end();

        if (recursive) {
            // If Widget has children drawing responsibilities, call super to draw them.
            super.drawShape(renderer, recursive);
        }
    }

    // ---------- Internal helpers ----------

    /** Fire change listeners (call on UI thread). */
    private void fireChange() {
        for (Runnable r : changeListeners) {
            try {
                r.run();
            } catch (Exception ex) {
                // swallow exceptions to keep UI stable; log if you have a logging system
                ex.printStackTrace();
            }
        }
    }

    /** Return left X coordinate of the knob based on current normalized value. */
    private float getKnobLeft() {
        // knob center ranges from baseLeft .. baseLeft+baseWidth
        float baseLeft = getGlobalX();
        float center = baseLeft + normalized * baseWidth;
        // represent knob by left edge
        return center - knobWidth * 0.5f;
    }

    /** Given desired knob left, clamp and return center position. */
    private float clampKnobCenterFromLeft(float knobLeft) {
        float baseLeft = getGlobalX();
        float minCenter = baseLeft;
        float maxCenter = baseLeft + baseWidth;
        float center = knobLeft + knobWidth * 0.5f;
        center = MathUtils.clamp(center, minCenter, maxCenter);
        return center;
    }

    /** Clamp the given desired center to allowable range. */
    private float clampKnobCenter(float desiredCenter) {
        float baseLeft = getGlobalX();
        float minCenter = baseLeft;
        float maxCenter = baseLeft + baseWidth;
        return MathUtils.clamp(desiredCenter, minCenter, maxCenter);
    }

    /** Is pointer inside knob rectangle? */
    private boolean isPointerOverKnob(float px, float py) {
        float left = getKnobLeft();
        float bottom = getGlobalY();
        return px >= left && px <= left + knobWidth && py >= bottom && py <= bottom + knobHeight;
    }

    /** Is pointer over base track (allow some padding vertically). */
    private boolean isPointerOverBase(float px, float py) {
        float left = getGlobalX();
        float bottom = getGlobalY() + (knobHeight - baseHeight) * 0.5f;
        float right = left + baseWidth;
        float top = bottom + baseHeight;
        // add vertical tolerance equal to knobHeight to make clicking easier
        float tol = knobHeight * 0.8f;
        return px >= left && px <= right && py >= (bottom - tol) && py <= (top + tol);
    }

    /** Convert knob center position to normalized and update value (and notify listeners). */
    private void setNormalizedFromKnobCenter(float knobCenterX) {
        float baseLeft = getGlobalX();
        float norm = (knobCenterX - baseLeft) / baseWidth;
        norm = MathUtils.clamp(norm, 0f, 1f);
        if (Math.abs(norm - normalized) > 1e-5f) {
            normalized = norm;
            // update numeric value
            value = minValue + normalized * (maxValue - minValue);
            fireChange();
        }
    }

    // ---------- Optional: convenience methods ----------
    /** Set base color. */
    public void setBaseColor(Color color) {
        this.baseColor = color != null ? color : this.baseColor;
    }

    /** Set knob color. */
    public void setKnobColor(Color color) {
        this.knobColor = color != null ? color : this.knobColor;
    }

    /** Set knob border color. */
    public void setKnobBorderColor(Color color) {
        this.knobBorderColor = color != null ? color : this.knobBorderColor;
    }
    
    @Override
    public boolean onPointerDown(int globalX, int globalY, int pointer, int button) {
        // convert to float if your slider methods expect floats
        return pointerDown(globalX, globalY); // true = captured
    }

    @Override
    public boolean onPointerDragged(int globalX, int globalY, int pointer) {
        return pointerDragged(globalX, globalY);
    }

    @Override
    public boolean onPointerUp(int globalX, int globalY, int pointer, int button) {
        return pointerUp(globalX, globalY);
    }
    
    @Override
    public void drawEditMode(ShapeRenderer renderer, boolean recursive) {
    	// TODO Auto-generated method stub
    	super.drawEditMode(renderer, recursive);
    }
    
    
    @Override
    public Rectangle getSelectionRegion() {
        // getKnobLeft() is the left X coordinate of the knob based on normalized position
        float left = getKnobLeft();
        float bottom = getGlobalY();      // your slider used getGlobalY() as knob bottom in draw
        return new Rectangle(left, bottom, knobWidth, knobHeight);
    }
    
	public void drawHover(ShapeRenderer shape_renderer) {
//		shape_renderer.set(ShapeType.Line);
//		shape_renderer.setColor(color_trim_highlight);
//		shape_renderer.rect(getKnobLeft(), getGlobalY(),
//				knobWidth, knobHeight);
	}

	protected String getTextToRender() {
		// TODO Auto-generated method stub
		return ""+getValue();
	}

    
}
