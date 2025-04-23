package com.alectriciti.gdx.chat;

import com.badlogic.gdx.graphics.Color;

/**
 * A component used for building Messages
 */
public class ColoredText {

	//TextInterface parent;
	private String text;
	Color color;
	int break_count;

	public ColoredText(String t, Color c) {
		this.text = t;
		this.color = c;
		fixReturnKeyIndex();
	}

	public ColoredText(String t, float r, float g, float b) {
		this.text = t;
		this.color = new Color(r, g, b, 1);
		fixReturnKeyIndex();
	}
//
//	public void setParent(TextInterface p) {
//		this.parent = p;
//	}

	public int length() {
		return text.length()-break_count;
	}

	public String getText() {
		return text;
	}

	public void updateText(String s) {
		this.text = s;
		fixReturnKeyIndex();
		//if(parent!=null) {

            // reconstruct the text

			//parent.reconstruct();
		//}
	}


	public void fixReturnKeyIndex() {
		break_count = 0;
		int index = 0;
		if(text.contains("\n"))
		while((index = text.indexOf("\n", index)) != -1) {
			break_count+=1;
			index+=2;
		}
	}


	public String toString() {
		return text;
	}


    /**
     * Rainbow Stuff
     */
    static Color[] rainbow = {Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.PURPLE, Color.MAGENTA, Color.PINK};

    public static ColoredText[] formatRainbow(String msg) {
        ColoredText[] formated_text = new ColoredText[msg.length()];
        for(int i = 0; i<msg.length(); i++) {
            formated_text[i] = new ColoredText((""+msg.charAt(i)), rainbow[i%(rainbow.length)]);
        }
        return formated_text;
    }

}
