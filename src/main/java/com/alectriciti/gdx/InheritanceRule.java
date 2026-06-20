package com.alectriciti.gdx;


/**
 * InheritanceRule AKA VisibilityRule to act as a logic mask
 * For widget.setVisible(Boolean, VisibilityRule)
 */
public enum InheritanceRule {
	
	STANDARD, //Is neutral
	RECURSIVE, //Effectively sends the parameter to all children
	PROTECT_CHILDREN,
	IGNORE_PARENT, //Rebelious child, holds it's own ground
	LOYAL //Follows parent in every way

}
