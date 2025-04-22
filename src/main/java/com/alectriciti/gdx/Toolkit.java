package com.alectriciti.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;

public class Toolkit {
	
	
	/**
	 * A handy little print function
	 * @param msg the message to display
	 */
	public static void print(String msg) {
		System.out.println(msg);
	}

	/**
	 * A handy little print function, but in red
	 * @param msg the message to display
	 */
	public static void printWarning(String msg) {
		System.out.println(Ansi.YELLOW+"⚠️ "+msg);
	}
	
	public static void printLibWarning(String msg) {
		System.out.println(Ansi.YELLOW+"[alectric-gdx-tools]⚠️ "+msg);
	}
	
	
	public static void printError(String msg) {
		System.out.println(Ansi.RED+"⚠️ "+msg);
	}
	
	public static void printLibError(String msg) {
		System.out.println(Ansi.RED+"[alectric-gdx-tools]⚠️ "+msg);
	}
	
	public static void printWaiting(String msg) {
		System.out.println(Ansi.CYAN+"⏳ "+msg);
	}
	
	public static void printNotification(String msg) {
		System.out.println(Ansi.BRIGHT_BLUE+"🔶 "+msg);
	}

	public static float range(float zero, float one, float delta) {
		float x = (delta - zero) / (one - zero);		
		return x;
	}
	
	public static float rangeClamped(float zero, float one, float delta) {
		float x = Math.min(Math.max ((delta - zero) / (one - zero),0),1);
		return x;
	}
	
	public static float mapRange(float value, float inMin, float inMax, float outMin, float outMax) {
	    return (value - inMin) / (inMax - inMin) * (outMax - outMin) + outMin;
	}
	public static float mapRangeClamped(float value, float inMin, float inMax, float outMin, float outMax) {
	    if (inMin == inMax) return outMin; // prevent divide by zero

	    float t = (value - inMin) / (inMax - inMin);
	    t = Math.max(0f, Math.min(1f, t)); // clamp to [0, 1]

	    return outMin + t * (outMax - outMin);
	}
	
	public static float lerp(float a, float z, float t) {
		return a + t * (z - a);
	}
	
	public static Color LerpColor(Color c, Color b, float delta) {
		Color new_color = new Color(c.r+delta * (b.r - c.r),
				c.g+delta * (b.g - c.g),
				c.b+delta * (b.b - c.b),
				c.a+delta * (b.a - c.a));
		return new_color;
	}
	
	public static int getMouseX() {
		return Gdx.input.getX();
	}
	
	public static int getMouseY() {
		return Gdx.graphics.getHeight() - Gdx.input.getY();
	}

}
