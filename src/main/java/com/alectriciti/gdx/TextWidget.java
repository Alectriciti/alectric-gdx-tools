package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.GL20.GL_FUNC_ADD;

import com.alectriciti.gdx.events.Draggable;
import com.alectriciti.gdx.events.DropTarget;

/**
 * A chat message is a divided collection of [ColoredText]s, which allow for multi-colored lines of text
 */
public class TextWidget extends Widget implements Draggable{
	
    ColoredText[] msgs; //The array of messages separated by color
    float[] opacities; // The array of opacities separated by individual glyph
    int length; //Actual length of the string

    public String msg_raw = ""; // The full message for console printing purposes

    BitmapFontCache font_cache;
    BitmapFontCache font_cache_shadow;
    GlyphLayout layout;
    
    BitmapFont font;
    
    boolean animating = false;
    
    
    private boolean auto_reconstruct = true;
    
    float offset_x = 4f;
    float offset_y = 4f;
    
    Color shadow;
    float offset_shadow_x = 0;
    float offset_shadow_y = 0;

//    public Message(BitmapFont font, ColoredText...msgs){
//        construct(last_used_font, msgs);
//    }
    
    public TextWidget(Widget parent, String s){
    	this(parent, new ColoredText(s, Color.WHITE));
    	setTouchable(false);
    }

    public TextWidget(Widget parent, ColoredText...msgs){
    	super(parent.id+"msg", parent);
    	setTouchable(false);
        construct(msgs);
    }
    public TextWidget(UIManager manager, ColoredText...msgs){
    	super("msg", manager);
    	setTouchable(false);
        construct(msgs);
    }
    
    /**
     * Used for initialization
     * @param msgs
     */
    public void construct(ColoredText...msgs){
        this.msgs = msgs;
        if(layout == null) layout = new GlyphLayout();
        if(font == null) {
            font = UIManager.getDefaultFont();
            if(font == null) {
                System.out.println("Warning: no font supplied for Message:" + this.id + "!");
                return;
            }
        }
        font_cache = new BitmapFontCache(font);
        font_cache.setUseIntegerPositions(true);
        
        

        // Let reconstruct() handle the string assembly, cap height positioning, and color updates uniformly.
        reconstruct(); 
    }
    
	@Override
	public boolean isHoverable() {
		return false;
	}
	
	public void enableDropShadow(Color c) {
		shadow = c.cpy();
        font_cache_shadow = new BitmapFontCache(font);
        font_cache_shadow.setUseIntegerPositions(true);
	}
    
    /**
     * Is only activated for console messages
     * This handles individual glyph color and opacity
     */
    void updateColors() {
//    	print("updating colors");
        int i = 0;
        length = 0;
        for(ColoredText ct : msgs) {
            length += ct.length();
            for(int x = 0; x < ct.length();x++) {
                Color color = new Color(ct.color.r, ct.color.g, ct.color.b, 1);//animated?opacities[i]:1);
                font_cache.setColors(color, i, i+1);
                i++;
            }
        }
    }
    
    /**
     * Setting this to false requires you to update reconstruct() after making changes
     * @param b
     */
    public void setAutoreconstruct(boolean b) {
    	this.auto_reconstruct = b;
    }
    
    protected void reconstruct() {
        // Rebuild raw text
        StringBuilder builder = new StringBuilder();
        for (ColoredText t : msgs) {
            builder.append(t.getText());
        }
        msg_raw = builder.toString();

        // Update font cache with new text
        font_cache.clear();
        font_cache.setText(msg_raw, 0, 0);
		font_cache.setPosition(getGlobalX()+offset_x, getGlobalY()+font.getCapHeight()+offset_y);
		
		if(shadow!=null) {
	        font_cache_shadow.clear();
	        font_cache_shadow.setText(msg_raw, 0, 0);
	        font_cache_shadow.setPosition(getGlobalX()+offset_x, getGlobalY()+font.getCapHeight()+offset_y);
	        font_cache_shadow.tint(shadow); 
		}
        
        // Update colors
        updateColors();
    }


    
    public void setText(String text) {
    	msgs[0].updateText(text);
    	if (auto_reconstruct)reconstruct();
    }
    
    public void setText(String text, Color c) {
    	msgs[0].updateText(text);
    	msgs[0].color = c;
    	if (auto_reconstruct)reconstruct();
    }

    
    public void setText(int line, String text) {
    	msgs[line].updateText(text);
    	if (auto_reconstruct)reconstruct();
    }
    
    public void setText(int line, Color c) {
    	msgs[line].color = c;
    	if (auto_reconstruct)reconstruct();
    }
    
    public String getText() {
    	if(msgs.length<=0)return id;
    	return msgs[0].getText();
    }
    
    
	@Override
    protected void onPositionUpdate() {
    	super.onPositionUpdate();
    	//the font cache is independent from the widget, so link it here when the widget moves.
		if(font_cache!=null) {
			font_cache.setPosition(getGlobalX()+offset_x, getGlobalY()+font.getCapHeight()+offset_y);
			if(shadow!=null)
				font_cache_shadow.setPosition(getGlobalX()+offset_x+offset_shadow_x, getGlobalY()+font.getCapHeight()+offset_y+offset_shadow_y);
		}
    }


	@Override
	public boolean drawFont(SpriteBatch sprite_batch) {
	    if(!isVisible()){
	        return false;
	    }

		if(shadow!=null) {
//	        float shadowOffsetX = 2f;
//	        float shadowOffsetY = -2f;
	        
	        // 1. Shift the cache's vertices and tint them to your shadow color
//	        font_cache.translate(offset_shadow_x, offset_shadow_y);
	        
	        // 2. Draw the shadow
	        font_cache_shadow.draw(sprite_batch);
	        
	        // 3. Revert the shift and restore your widget's original font color
//	        font_cache.translate(-offset_shadow_x, -offset_shadow_y);
//	        font_cache.tint(font_color); 
	    }

	    // 4. Draw the actual main text on top
	    font_cache.draw(sprite_batch);
	    
	    return true;
	}
	
    public int length() {
        // TODO Auto-generated method stub
        return length;
    }

    public void destroy(){
        //manager.unregisterMsg(this);
    }

	@Override
	public void onDrag(int mouseX, int mouseY) {
//		print("ya dragging at "+mouseX+", "+mouseY);
	}
	
	@Override
	public void drawDragGhost(SpriteBatch batch, float x, float y) {
		// Just use your existing font to draw the string right at the mouse cursor
		font.draw(batch, this.getText(), x, y);
	}

	@Override
	public void onDrop(DropTarget target, int mouseX, int mouseY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onDragStart() {
		// TODO Auto-generated method stub
		
	}

}