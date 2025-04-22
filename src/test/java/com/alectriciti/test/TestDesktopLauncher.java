package com.alectriciti.test;

import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetListener;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;

public class TestDesktopLauncher {
	
	public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Test App");
        config.setForegroundFPS(60);
        config.setWindowedMode(800, 600);
        MyLibGDXTestApp gdx_app = new MyLibGDXTestApp();
        config.setWindowListener(gdx_app);
        Lwjgl3Application app = new Lwjgl3Application(gdx_app, config);
    }
}
