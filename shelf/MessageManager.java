package com.alectriciti.gdx.chat;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.LinkedList;

/**
 * Handles [Message] instances and renders them
 */
public class MessageManager {

    BitmapFont font; //the font used by this MessageManager
    LinkedList<Message> messages = new LinkedList<Message>();
    
    public MessageManager(BitmapFont font) {
        Message.manager = this;
        this.font = font;
    }
    
    public void render(SpriteBatch batch){
        for(Message msg : messages){
           // msg.renderText(batch);
        }
    }
    
    void registerMsg(Message msg){
        messages.add(msg);
    }
    
    public void unregisterMsg(Message msg) {
        messages.remove(msg);
    }
    
    public void dispose(){
        Message.manager = null;
    }
    
	public BitmapFont getFont() {
		return font;
	}
	
}
