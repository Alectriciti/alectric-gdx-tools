package com.alectriciti.test;


import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
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
