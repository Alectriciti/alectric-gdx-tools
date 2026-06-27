package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.math.Rectangle;

/**
 * Cursor caret for TextInput.
 * 
 * Behavior:
 * - Uses the `shape` Rectangle as the *local* caret rectangle (x/y are local offsets from
 *   the owning TextInput's global position). This rectangle is kept up-to-date each frame.
 * - Drawing uses widget.getGlobalX() + shape.x and widget.getGlobalY() + shape.y so visuals
 *   and any code that inspects `shape` stay consistent.
 * - Single-line caret behavior. For multi-line text you would compute line offsets and set shape.x/y accordingly.
 */
public class TextCursor extends Widget {

    // blink / timing (your existing tick style)
    int i = 0;
    int blink_rate = 40;

    // owning text widget
    final TextInput text_widget;

    // caret index (position between characters, 0..length)
    public int index;
    public float width;

    // reuse a GlyphLayout to measure widths (no allocation each frame)
    private final GlyphLayout layout = new GlyphLayout();

    Rectangle smoothed_shape;
    
    /**
     * Construct a cursor for the supplied TextInput.
     * We create the shape rectangle as a local rectangle (0,0,width,height).
     * The actual width/height/x/y will be kept in sync each draw.
     */
    public TextCursor(TextInput widget) {
    	super(widget.id+".cursor", widget);
        this.text_widget = widget;

        // initialize `shape` as local rectangle — do NOT set it to null.
        // width is a thin caret by default; height is approximated from font if available.
        float defaultThickness = 1f;
        float defaultHeight = 16f;
        if (widget != null && widget.font != null) {
            defaultHeight = widget.font.getLineHeight();
        }
        this.shape = new Rectangle(0f, 0f, defaultThickness, defaultHeight);
        this.smoothed_shape = new Rectangle(0f, 0f, defaultThickness, defaultHeight);
        this.color = new Color(1,1,1,1);
        // start index at 0 (or you may prefer to follow widget.cursor)
        this.index = 0;
    }

    @Override
    public boolean isEditable() {
        return false;
    }
    
    @Override
    public boolean isTouchable() {
    	return false;
    }

    @Override
    public void update() {
        super.update();
        if(!text_widget.isFocused()) {
        	return;
        }
        i++;
        
        String text = text_widget.msgs[0].getText();
        int safeIndex = Math.max(0, Math.min(index, text.length()));

        // measure prefix width up to caret index
        String prefix = text.substring(0, safeIndex);
        layout.setText(text_widget.font, prefix);
        float prefixWidth = layout.width;

        // Compute baseline / bottom Y consistent with how you position font_cache:
        // In your code you set font_cache position to (getGlobalX(), getGlobalY()+font.getCapHeight())
        // So the baseline is getGlobalY() + font.getCapHeight()
        float baselineY = text_widget.getGlobalY() + text_widget.font.getCapHeight();

        // We will set caret height to font.getLineHeight() and bottom to widget.getGlobalY()
        float caretHeight = text_widget.font.getLineHeight();
        float caretBottomGlobal = baselineY - text_widget.font.getCapHeight(); // equals widget.getGlobalY()

        // thickness for caret (use shape.width)
        float thickness = Math.max(1f, shape.width);

        // update shape (local coords relative to widget)
        shape.x = prefixWidth;
        shape.y = caretBottomGlobal - text_widget.getGlobalY(); // typically 0, but safe formula
        shape.width = thickness;
        shape.height = caretHeight;

        smoothed_shape.x = lerp(smoothed_shape.x, shape.x, 0.25f);
        smoothed_shape.y = lerp(smoothed_shape.y, shape.y, 0.25f);
        
    }
    

    /**
     * Update the internal shape rectangle to match where the caret should be (local coords),
     * then draw the caret using the shape rectangle in global coordinates.
     */
    public void draw(ShapeRenderer renderer, TextInput widget) {
        if (widget == null || widget.font == null || widget.msgs == null || widget.msgs.length == 0)
            return;
        
        setOpacity(Math.abs((float) Math.sin(((float)i)/10)));
        // blink logic: visible only half the cycle
        int period = blink_rate * 2;
        
//        color = new Color(1, 1, 1, x);
//        if ((i % 30)>16) {
//            return; // hidden this half-cycle
//        }

        // Get text (we assume editable text lives in msgs[0])

        float cap_height = text_widget.font.getCapHeight();
        
        // Draw using shape in global coordinates
        float drawX1 = widget.getGlobalX() + shape.x;
        float drawY1 = widget.getGlobalY() + shape.y + (cap_height/2);
        float drawX2 = widget.getGlobalX() + smoothed_shape.x;
        float drawY2 = widget.getGlobalY() + smoothed_shape.y + (cap_height/2);

        renderer.set(ShapeType.Filled);
        renderer.setColor(color);
//        renderer.set
        renderer.rectLine(drawX1, drawY1, drawX2 + 2, drawY2, text_widget.font.getCapHeight());
//        renderer.rect(drawX, drawY, smoothed_shape.width, smoothed_shape.height);
//        renderer.end();
    }

    /** Move cursor one char left. */
    public void moveLeft() {
        if (index > 0) index--;
    }

    /** Move cursor one char right. */
    public void moveRight() {
        if (text_widget != null) {
            index = Math.min(index + 1, text_widget.length());
        }
    }

    /** Set cursor index (clamped). */
    public void setCursorIndex(int newIndex) {
        if (text_widget != null) {
            this.index = Math.max(0, Math.min(newIndex, text_widget.length()));
        } else {
            this.index = Math.max(0, newIndex);
        }
    }

    /** Jump to end */
    public void moveCursorToEnd() {
        if (text_widget != null) index = text_widget.length();
    }

    /** Jump to start */
    public void moveCursorToStart() {
        index = 0;
    }
}
