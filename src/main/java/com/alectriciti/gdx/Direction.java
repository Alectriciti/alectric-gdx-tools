package com.alectriciti.gdx;

public enum Direction {
	
	NONE(0,0),
	UP (0, 1),
	UP_RIGHT(1, 1),
	RIGHT(1, 0),
	DOWN_RIGHT(1, -1),
	DOWN (0, -1),
	DOWN_LEFT(-1, -1),
	LEFT(-1, 0),
	UP_LEFT(-1, 1);
	
	float x;
	float y;
	Direction(float x, float y){
		this.x = x;
		this.y = y;
	}

}
