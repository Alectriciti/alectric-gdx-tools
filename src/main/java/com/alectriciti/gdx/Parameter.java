package com.alectriciti.gdx;

import static com.alectriciti.gdx.Value.*;

/**
 * For Alectric-GDX Widget
 */
public enum Parameter {
	
	VISIBLE,
	TOUCHABLE,
	RENDER_TEXT;

	Value getDefault(boolean value) {
		// TODO Auto-generated method stub
		switch(this) {
		case VISIBLE:
			return TRUE;
		case RENDER_TEXT:
			return UNASSIGNED;
		case TOUCHABLE:
			return UNASSIGNED;
			default: return UNASSIGNED;
		}
	}

}
