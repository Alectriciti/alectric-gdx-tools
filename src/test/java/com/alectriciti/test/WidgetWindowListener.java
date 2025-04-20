package com.alectriciti.test;

import static com.alectriciti.gdx.Toolkit.print;

import com.alectriciti.gdx.WidgetManager;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;

public class WidgetWindowListener implements Lwjgl3WindowListener {

	WidgetManager manager;
	
	public WidgetWindowListener(WidgetManager manager) {
		this.manager = manager;
	}

	@Override
	public void created(Lwjgl3Window window) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void iconified(boolean isIconified) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void maximized(boolean isMaximized) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusGained() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean closeRequested() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void filesDropped(String[] files) {
		manager.importFiles(files);
	}

	@Override
	public void refreshRequested() {
		// TODO Auto-generated method stub
		
	}

}
