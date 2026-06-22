package com.alectriciti.gdx;

import java.awt.MouseInfo;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWVidMode;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics.Lwjgl3Monitor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;

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
	
	public static int[] getAbsoluteMousePosition() {
        Point p = MouseInfo.getPointerInfo().getLocation();

        int screenX = (int) p.getX();
        int screenY = (int) p.getY();
        return new int[]{screenX, screenY};
    }
	 // Reserved device names (Windows) — case-insensitive
    private static final Set<String> RESERVED_NAMES = new HashSet<>(
        Arrays.asList(
            "CON", "PRN", "AUX", "NUL",
            "COM1","COM2","COM3","COM4","COM5","COM6","COM7","COM8","COM9",
            "LPT1","LPT2","LPT3","LPT4","LPT5","LPT6","LPT7","LPT8","LPT9"
        )
    );

    /**
     * Return true if the given filename is valid/safe for Windows filesystem usage.
     * This enforces:
     *  - not null/empty
     *  - length <= 255
     *  - no ASCII control chars (0..31)
     *  - no characters:  < > : " / \ | ? *
     *  - does not end with space or dot
     *  - not a reserved device name (CON, PRN, AUX, NUL, COM1..COM9, LPT1..LPT9)
     */
    public static boolean isValidFilename(String filename) {
        if (filename == null) return false;
        if (filename.isEmpty()) return false;

        // conservative max length for a single filename component
        if (filename.length() > 255) return false;

        // cannot end with space or dot on Windows
        if (filename.endsWith(" ") || filename.endsWith(".")) return false;

        // disallowed characters
        String forbidden = "<>:\"/\\|?*";
        for (int i = 0; i < filename.length(); i++) {
            char ch = filename.charAt(i);
            // control characters
            if (ch <= 31) return false;
            if (forbidden.indexOf(ch) >= 0) return false;
        }

        // check reserved names (compare base name before first dot)
        int dot_index = filename.indexOf('.');
        String name_without_extension = dot_index == -1 ? filename : filename.substring(0, dot_index);
        String name_upper = name_without_extension.toUpperCase(Locale.ROOT);
        if (RESERVED_NAMES.contains(name_upper)) return false;

        return true;
    }

    /**
     * Optional: quick sanitizer that replaces invalid chars with '_' and trims trailing dots/spaces.
     * Use this if you want to auto-fix widget.id instead of rejecting it.
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null) return null;
        // replace control chars and forbidden chars with underscore
        StringBuilder sb = new StringBuilder();
        String forbidden = "<>:\"/\\|?*";
        for (int i = 0; i < filename.length(); i++) {
            char ch = filename.charAt(i);
            if (ch <= 31 || forbidden.indexOf(ch) >= 0) {
                sb.append('_');
            } else {
                sb.append(ch);
            }
        }
        // trim trailing spaces/dots
        String cleaned = sb.toString();
        while (cleaned.endsWith(" ") || cleaned.endsWith(".")) {
            cleaned = cleaned.substring(0, cleaned.length() - 1);
            if (cleaned.isEmpty()) break;
        }
        // avoid reserved names: prefix with underscore if reserved
        int dot_index = cleaned.indexOf('.');
        String base = dot_index == -1 ? cleaned : cleaned.substring(0, dot_index);
        if (base != null && RESERVED_NAMES.contains(base.toUpperCase(Locale.ROOT))) {
            cleaned = "_" + cleaned;
        }
        // enforce max length
        if (cleaned.length() > 255) {
            cleaned = cleaned.substring(0, 255);
        }
        return cleaned;
    }
    
    /**
     * Draws just the outer curve of an arc without the connecting center lines.
     * Must be called between renderer.begin(ShapeType.Line) and renderer.end().
     */
    public static void drawArcCurve(ShapeRenderer renderer, float x, float y, float radius, float startAngle, float degrees, int segments) {
        if (radius <= 0) return;
        
        // Fix: Never drop below 12 segments to ensure tiny curves are perfectly round on the pixel grid.
        int actualSegments = Math.max(12, segments);
        float angleStep = degrees / actualSegments;

        float startX = x + radius * MathUtils.cosDeg(startAngle);
        float startY = y + radius * MathUtils.sinDeg(startAngle);

        for (int i = 1; i <= actualSegments; i++) {
            // Multiply step instead of adding to avoid floating-point drift over the loop
            float currentAngle = startAngle + (i * angleStep); 
            
            float nextX = x + radius * MathUtils.cosDeg(currentAngle);
            float nextY = y + radius * MathUtils.sinDeg(currentAngle);

            renderer.line(startX, startY, nextX, nextY);

            startX = nextX;
            startY = nextY;
        }
    }
    
    /**
     * Draws a perfectly aligned hollow rounded border using Line mode.
     */
    public static void drawRoundedRectLine(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        if (radius < 1f) {
            renderer.rect(x, y, width, height);
            return;
        }

        // Clamp radius so it never exceeds half of the widget's dimensions
        float maxRadius = Math.min(width, height) / 2f;
        if (radius > maxRadius) radius = maxRadius;

        // Fix: OpenGL Line mode thickness adjustment.
        // Subtracting 1 ensures the right/top lines are drawn ON the final pixel, not past it.
        float rightEdge = x + width;
        float topEdge = y + height;

//        renderer.setColor(0, 0, 1, 0.5f);
//        renderer.rect(x, y, width, height);

//        renderer.setColor(1, 0, 0, 0.5f);
        // 4 Perimeter lines
        renderer.line(x + radius, y, rightEdge - radius-1, y);                  // Bottom GOOD
        renderer.line(x + radius, topEdge, rightEdge - radius-1, topEdge);      // Top
        renderer.line(x, y + radius+1, x, topEdge - radius);                    // Left GOOD
        renderer.line(rightEdge, y + radius+1, rightEdge, topEdge - radius);    // Right
        
//        renderer.setColor(0, 1, 0, 0.5f);
        // 4 Balanced corner curves
        drawArcCurve(renderer, x + radius, topEdge - radius, radius, 90f, 90f, 16);        // Top-left
        drawArcCurve(renderer, x + radius, y + radius+1, radius, 180f, 90f, 16);             // Bottom-left
        drawArcCurve(renderer, rightEdge - radius -1, topEdge - radius, radius, 0f, 90f, 16); // Top-right
        drawArcCurve(renderer, rightEdge - radius -1, y + radius+1, radius, 270f, 90f, 16);     // Bottom-right
    }
    
    
    public static void drawRoundedRectFilled(ShapeRenderer renderer, float x, float y, float width, float height, float radius) {
        if (radius < 1f) {
            renderer.rect(x, y, width, height);
            return;
        }

        float maxRadius = Math.min(width, height) / 2f;
        if (radius > maxRadius) radius = maxRadius;

        float rightEdge = x + width;
        float topEdge = y + height;

        // 1. Extract the exact same centers you calculated for the Line method
        float cLeft = x + radius;
        float cRight = rightEdge - radius - 1f;
        float cBottom = y + radius + 1f;
        float cTop = topEdge - radius;

        // 2. Central Vertical Column
        // Spans the full height, bounded horizontally between the left and right curves
        renderer.rect(cLeft, y, cRight - cLeft, height);

        // 3. Left Middle Block
        // Touches the left edge, bounded vertically between the top and bottom curves
        renderer.rect(x, cBottom, cLeft - x, cTop - cBottom);

        // 4. Right Middle Block
        // Touches the right edge, bounded vertically between the top and bottom curves
        renderer.rect(cRight, cBottom, rightEdge - cRight, cTop - cBottom);

        int arcsSegments = Math.max(16, (int)(radius * 2f));

        // 5. The Exact Same Corner Curves
        renderer.arc(cLeft, cTop, radius, 90f, 90f, arcsSegments);        // Top-left
        renderer.arc(cLeft, cBottom, radius, 180f, 90f, arcsSegments);    // Bottom-left
        renderer.arc(cRight, cTop, radius, 0f, 90f, arcsSegments);        // Top-right
        renderer.arc(cRight, cBottom, radius, 270f, 90f, arcsSegments);   // Bottom-right
    }
}
