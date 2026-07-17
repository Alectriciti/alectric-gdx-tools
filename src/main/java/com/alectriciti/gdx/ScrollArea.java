package com.alectriciti.gdx;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.graphics.Camera; // Make sure to pass your camera here if needed, or use a global one

public class ScrollArea extends Widget {

    public float scroll_x = 0;
    public float scroll_y = 0;
    
    public float true_area_width = 0;
    public float true_area_height = 0;

    // Optional: References to your Sliders
    // public Slider horizontal_slider;
    // public Slider vertical_slider;

    public ScrollArea(String id, UIManager manager, int width, int height) {
        super(id, manager);
//        setTouchable(false);
        setSize(width, height);
    }
    
    public ScrollArea(String id, Widget parent, int width, int height) {
        super(id, parent);
//        setTouchable(false);
        setSize(width, height);
    }

    // --- 1. Coordinate Offsetting ---
    
    @Override
    public float getChildOriginX() {
        return getGlobalX() - scroll_x;
    }

    @Override
    public float getChildOriginY() {
        return getGlobalY() - scroll_y;
    }
    
    // Calculates the true area based on the furthest boundaries of all children
    public void calculateTrueArea() {
        float max_w = shape.width;
        float max_h = shape.height;
        
        for (Widget w : widgets_children) {
            float child_right = w.shape.x + w.shape.width;
            float child_top = w.shape.y + w.shape.height;
            if (child_right > max_w) max_w = child_right;
            if (child_top > max_h) max_h = child_top;
        }
        
        true_area_width = max_w;
        true_area_height = max_h;
    }

    // --- 2. Visual Cropping via ScissorStack ---

    private Rectangle scissors = new Rectangle();
    private Rectangle clipBounds = new Rectangle();

    private boolean pushClip() {
        clipBounds.set(getGlobalX(), getGlobalY(), shape.width, shape.height);
        
        // Note: You must provide your stage's/viewport's camera matrix here. 
        // If you are using a default SpriteBatch, it's usually batch.getTransformMatrix() 
        // combined with batch.getProjectionMatrix(), but ScissorStack.calculateScissors handles this.
        // For standard UI setups using a default Orthographic camera:
        // ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), clipBounds, scissors);
        
        // A simpler alternative if your UI is 1:1 with the screen resolution:
        scissors.set(clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height);
        return ScissorStack.pushScissors(scissors);
    }

    private void popClip() {
        ScissorStack.popScissors();
    }

    @Override
    protected void drawShapeChildren(ShapeRenderer renderer, boolean recursive) {
        renderer.flush(); 
        if (pushClip()) {
            // ONLY draw the cached visible children
            for(Widget w : visible_children_cache) {
                w.drawShape(renderer);
            }
            if(recursive) {
                for(Widget w : visible_children_cache) {
                    w.drawShapeChildren(renderer, recursive);
                }
            }
            renderer.flush();
            popClip();
        }
    }

    @Override
    protected void drawTextureChildren(SpriteBatch sprite_batch, boolean recursive) {
        sprite_batch.flush(); 
        if (pushClip()) {
            for(Widget w : visible_children_cache) {
                w.drawTexture(sprite_batch);
            }
            if(recursive) {
                for(Widget w : visible_children_cache) {
                    w.drawTextureChildren(sprite_batch, recursive);
                }
            }
            sprite_batch.flush();
            popClip();
        }
    }

    @Override
    protected void drawFontChildren(SpriteBatch sprite_batch, boolean recursive) {
        sprite_batch.flush();
        if (pushClip()) {
            for(Widget w : visible_children_cache) {
                w.drawFont(sprite_batch);
            }
            if(recursive) {
                for(Widget w : visible_children_cache) {
                    w.drawFontChildren(sprite_batch, recursive);
                }
            }
            sprite_batch.flush();
            popClip();
        }
    }
    
    // --- 3. Input Handling (Optional: Mouse Wheel) ---
    
    @Override
    public void scroll(float amountX, float amountY) {
        // Adjust internal scroll offsets based on mouse wheel
        scroll_y += (-amountY * 20f); // Arbitrary scroll speed
        
        // Clamp to prevent scrolling past the true area
        float max_y = Math.max(0, true_area_height - shape.height);
        if (scroll_y < 0) scroll_y = 0;
        if (scroll_y > max_y) scroll_y = max_y;
        
        // Sync the vertical slider visually
        if (vertical_slider != null) {
            vertical_slider.setValue(scroll_y);
        }
        
        // Push the new positions down to all children
        updateGlobalPosition(); 
    }
    
    
    // Keep track of the sliders so we can update them via mouse wheel scrolling
    public Slider horizontal_slider;
    public Slider vertical_slider;

    public void assignSliderSettings(Slider s) {
        // 1. Ensure the internal bounds are up-to-date before measuring
        calculateTrueArea();

        // 2. Clear existing listeners so they don't stack up if you call this multiple times after a resize
        s.change_listeners.clear();

        if (s.orientation == Orientation.HORIZONTAL) {
            horizontal_slider = s;
            
            // Calculate max scroll. We use Math.max(0.01f, ...) because Slider.setValueRange 
            // throws an IllegalArgumentException if max <= min.
            float max_x = Math.max(0.01f, true_area_width - shape.width);
            s.setValueRange(0, max_x);
            
            s.addChangeListener(() -> {
                this.scroll_x = s.getValue();
                this.updateGlobalPosition(); // Pushes the new offsets down to the children
            });
            
        } else if (s.orientation == Orientation.VERTICAL) {
            vertical_slider = s;
            
            float max_y = Math.max(0.01f, true_area_height - shape.height);
            s.setValueRange(0, max_y);
            
            s.addChangeListener(() -> {
                this.scroll_y = s.getValue();
                this.updateGlobalPosition();
            });
        }
    }
    
 // A cache holding ONLY the widgets currently visible within the ScrollArea bounds
    private transient List<Widget> visible_children_cache = new ArrayList<>();

    
    @Override
    public void updateGlobalPosition() {
        super.updateGlobalPosition();
        
        // After all children have their global positions updated by the scroll offsets, 
        // calculate which ones are actually on screen.
        updateCulling();
    }
    
    /**
     * Determines which children are currently inside the scissored view.
     */
    private void updateCulling() {
    	if(visible_children_cache==null) {
    		return; //hasn't been initialized yet
    	}
    	visible_children_cache.clear();
        
        // The literal screen space the ScrollArea occupies
        Rectangle viewBounds = new Rectangle(getGlobalX(), getGlobalY(), shape.width, shape.height);
        
        for (Widget w : widgets_children) {
            // The literal screen space the child occupies
            Rectangle childBounds = new Rectangle(w.getGlobalX(), w.getGlobalY(), w.shape.width, w.shape.height);
            
            // If they touch at all, it's visible. Cache it!
            if (viewBounds.overlaps(childBounds)) {
                visible_children_cache.add(w);
            }
        }
    }

	public float getContentWidth() {
		return true_area_width;
	}

	public float getContentHeight() {
		return true_area_height;
	}
    
}