package com.alectriciti.gdx;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.GL20.GL_FUNC_ADD;

/**
 * A chat message is a divided collection of [ColoredText]s, which allow for multi-colored lines of text
 */
public class TextWidget extends Widget{
	
    ColoredText[] msgs; //The array of messages separated by color
    float[] opacities; // The array of opacities separated by individual glyph
    int length; //Actual length of the string

    public String msg_raw = ""; // The full message for console printing purposes
    
    BitmapFontCache font_cache;
    GlyphLayout layout;
    
    BitmapFont font;
    
    boolean animating = false;

//    public Message(BitmapFont font, ColoredText...msgs){
//        construct(last_used_font, msgs);
//    }

    public TextWidget(Widget parent, ColoredText...msgs){
    	super("msg", parent);
        construct(msgs);
    }
    public TextWidget(UIManager manager, ColoredText...msgs){
    	super("msg", manager);
        construct(msgs);
    }
    
    /**
     * Used for initialization
     * @param msgs
     */
    public void construct(ColoredText...msgs){
        this.msgs = msgs;
        msg_raw = "";
        for(ColoredText t : msgs){
            msg_raw += t.getText();
        }
        if(layout == null) layout = new GlyphLayout();
    	if(font==null) {
    		font = UIManager.getDefaultFont();
    		if(font==null) {
    			printWarning("no font supplied for Message:"+this.id+"!");
    			return;
    		}
//    		return;
    	}
        font_cache = new BitmapFontCache(font);
        font_cache.setUseIntegerPositions(true);
        //manager.registerMsg(this);
        font_cache.setText(msg_raw, getGlobalX(), getGlobalY());

        updateColors();
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
            //i +=ct.length();
        }
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
		font_cache.setPosition(getGlobalX(), getGlobalY()+font.getCapHeight());

        // Update colors
        updateColors();
    }

    
    
    public void setText(int line, String text) {
    	msgs[line].updateText(text);
    	reconstruct();
    }
    
    
    
    
	@Override
    protected void onPositionUpdate() {
    	super.onPositionUpdate();
    	//the font cache is independent from the widget, so link it here when the widget moves.
    	//TODO optimize this to only be called when the position actually gets moved
		if(font_cache!=null) {
			font_cache.setPosition(getGlobalX(), getGlobalY()+font.getCapHeight());
		}
    }

	
	public boolean drawFont(SpriteBatch sprite_batch, BitmapFont font, boolean recursive) {
		
		if(!visible){
			return false;
		}
		
		font_cache.draw(sprite_batch);
		
		if(render_text && name_for_display != null) {
		//print(getGlobalX()+" "+getGlobalY());
//			font.setColor(font_color);
//			font.draw(sprite_batch, name_for_display, getGlobalX()+font_offset.x, getGlobalY()+font.getCapHeight()+font_offset.y);
		}
		if(recursive) {
			drawFontChildren(sprite_batch, font, recursive);
		}
		return true;
	}
	
    public int length() {
        // TODO Auto-generated method stub
        return length;
    }

    public void destroy(){
        //manager.unregisterMsg(this);
    }

}