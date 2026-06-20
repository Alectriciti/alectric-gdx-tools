package com.alectriciti.gdx;

public enum Value {
	
	UNASSIGNED(true),
	TRUE(true),
	FALSE(false);
	
	
	boolean b;
	Value(boolean b){
		this.b = b;
	}
	
	public boolean get() {
		return b; 
	}

	static Value of(boolean value) {
		if(value)
			return TRUE;
		else
			return FALSE;
	}

}
