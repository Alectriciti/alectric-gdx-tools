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
public class Message extends Widget{


    ColoredText[] msgs; //The array of messages separated by color
    float[] opacities; // The array of opacities separated by individual glyph
    int length; //Actual length of the string

    public String msg_raw = ""; // The full message for console printing purposes
    
    
    BitmapFontCache font_cache;
    GlyphLayout layout;
    
    BitmapFont last_used_font;
    
    boolean animating = false;

    public Message(BitmapFont font, ColoredText...msgs){
    	last_used_font = font;
        construct(last_used_font, msgs);
    }

    public Message(ColoredText...msgs){
        construct(last_used_font, msgs);
    }

    public void construct(BitmapFont font, ColoredText...msgs){
        this.msgs = msgs;
        msg_raw = "";
        for(ColoredText t : msgs){
            msg_raw += t.getText();
        }
    	if(last_used_font==null) {
    		last_used_font = UIManager.getDefaultFont();
    		printWarning("no font supplied!");
    		return;
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
