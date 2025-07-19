package com.alectriciti.gdx.chat;

import static com.alectriciti.gdx.Toolkit.*;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.BitmapFontCache;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import static com.badlogic.gdx.graphics.GL20.*;
import static com.badlogic.gdx.graphics.GL20.GL_FUNC_ADD;

import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.Widget;

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

    public void construct(ColoredText...msgs){
        this.msgs = msgs;
        msg_raw = "";
        for(ColoredText t : msgs){
            msg_raw += t.getText();
        }
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
        font_cache.setText(msg_raw, 0, 0);

        updateColors();
    }
    
    /**
     * Is only activated for console messages
     * This handles individual glyph color and opacity
     */
    void updateColors() {
    	print("updating colors");
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
    
    
    @Override
    protected void onPositionUpdate() {
    	super.onPositionUpdate();
    	//the font cache is independent from the widget, so link it here when the widget moves.
		if(font_cache!=null) {
			System.out.println("POS UDPATED to "+getGlobalX());
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
    
    
//    
//    
//    public void renderText(SpriteBatch batch){
//
//        updateColors();
//
//
//        batch.setBlendFunctionSeparate(
//            GL_ONE_MINUS_DST_COLOR,
//            GL_ONE,
//            GL_ONE_MINUS_CONSTANT_ALPHA,
//            GL_ONE_MINUS_SRC_COLOR);
//        Gdx.gl.glBlendEquation(GL_FUNC_REVERSE_SUBTRACT);
//        font_cache.setPosition(getX()+1, getY()-1);
//        font_cache.draw(batch, 0, length());
//
//        batch.setBlendFunctionSeparate(
//            GL_ONE_MINUS_DST_COLOR,
//            GL_ONE_MINUS_SRC_COLOR,
//            GL_SRC_ALPHA,
//            GL_SRC_ALPHA_SATURATE);
//        Gdx.gl.glBlendEquation(GL_FUNC_ADD);
//
//
//
//        font_cache.setPosition(getX(), getY());
//        font_cache.draw(batch, 0, length());
//
//
//
//        //font_cache.setPosition(x, y);
//        //font_cache.draw(batch, 0, msg_raw.length());
//    }

    public int length() {
        // TODO Auto-generated method stub
        return length;
    }

    public void destroy(){
        //manager.unregisterMsg(this);
    }

}
