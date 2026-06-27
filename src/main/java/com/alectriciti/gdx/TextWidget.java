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
    
    float offset_x = 4f;
    float offset_y = 4f;

//    public Message(BitmapFont font, ColoredText...msgs){
//        construct(last_used_font, msgs);
//    }
    
    public TextWidget(Widget parent, String s){
    	this(parent, new ColoredText(s, Color.WHITE));
		touchable = false;
    }

    public TextWidget(Widget parent, ColoredText...msgs){
    	super(parent.id+"msg", parent);
		touchable = false;
        construct(msgs);
    }
    public TextWidget(UIManager manager, ColoredText...msgs){
    	super("msg", manager);
		touchable = false;
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

        // Update colors
        updateColors();
    }


    
    
    public void setText(String text) {
    	msgs[0].updateText(text);
    	reconstruct();
    }
    
    
    public void setText(int line, String text) {
    	msgs[line].updateText(text);
    	reconstruct();
    }
    
    
    
    
	@Override
    protected void onPositionUpdate() {
    	super.onPositionUpdate();
    	//the font cache is independent from the widget, so link it here when the widget moves.
		if(font_cache!=null) {
			font_cache.setPosition(getGlobalX()+offset_x, getGlobalY()+font.getCapHeight()+offset_y);
		}
    }


	@Override
	public boolean drawFont(SpriteBatch sprite_batch, boolean recursive) {
		
		if(!isVisible()){
			return false;
		}
		
		font_cache.draw(sprite_batch);
		
//		if(show_text && name_for_display != null) {
		//print(getGlobalX()+" "+getGlobalY());
//			font.setColor(font_color);
//			font.draw(sprite_batch, name_for_display, getGlobalX()+font_offset.x, getGlobalY()+font.getCapHeight()+font_offset.y);
//		}
		if(recursive) {
			drawFontChildren(sprite_batch, recursive);
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