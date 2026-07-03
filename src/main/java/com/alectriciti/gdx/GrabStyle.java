package com.alectriciti.gdx;

public enum GrabStyle{
	GRAB, // Requires the user to click on the knob to grab it
	LAZY, // Grabs relative to the mouse
	GRADUAL, // Moves to the mouse gradually while click is held
	INSTANT, // Snaps to where you click
}