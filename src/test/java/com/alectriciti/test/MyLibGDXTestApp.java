package com.alectriciti.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.DropdownMenuButton;
import com.alectriciti.gdx.Widget;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.UIManager;

public class MyLibGDXTestApp implements Lwjgl3WindowListener, ApplicationListener {

	
	
	InputMultiplexer input = new InputMultiplexer();
	UIManager widget_manager = new UIManager(input);
	ShapeRenderer shape_renderer;
	SpriteBatch sprite_batch;
	BitmapFont font;
	
	public int width, height;
	
	@Override
	public void create() {
		
		
		Gdx.input.setInputProcessor(input);

		FileHandle font_handle = Gdx.files.internal("lucida_console16.fnt");
		
		font = new BitmapFont(font_handle);

		Canvas main_menu = new Canvas("Cool Canvas", widget_manager, new Rectangle(100, 100, 200, 200));
		Canvas stupid_canvas = new Canvas("Stupid Canvas", widget_manager, new Rectangle(150, 50, 300, 100));
		
		DropdownMenuButton dropdown = new DropdownMenuButton("Dropdown", 0, main_menu);
		dropdown.setGlobalPosition(200, 300);

		Button a = new Button("Ass", Keys.A, dropdown);
		//a.setSize(64,42);

		Button b = new Button("Butt", Keys.SPACE, dropdown);
		Button c = new Button("Crap", Keys.B, dropdown);
		//b.setRelativePosition(100, 00);

		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		
		shape_renderer = new ShapeRenderer();
		shape_renderer.setAutoShapeType(true);
		sprite_batch = new SpriteBatch();
		
	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

	}

	@Override
	public void render() {
		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);

		widget_manager.update();
		//shape_renderer.setColor(Color.WHITE);shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//Gdx.gl.glDisable(GL20.GL_BLEND);
		widget_manager.renderAll(shape_renderer, sprite_batch, font);
		
	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

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
		widget_manager.dispose();
		//this.shape_renderer.dispose();
		//this.sprite_batch.dispose();
		//Gdx.app.exit();
		return true;
	}

	@Override
	public void filesDropped(String[] files) {
		widget_manager.importFileAsButton(files[0]);
	}

	@Override
	public void refreshRequested() {
		// TODO Auto-generated method stub
		
	}

}
