package com.alectriciti.gdx;

public enum Direction {
	
	
	UP (0, 1),
	DOWN (0, -1),
	LEFT(-1, 0),
	RIGHT(1, 0);
	
	float x;
	float y;
	Direction(float x, float y){
		this.x = x;
		this.y = y;
	}

}
