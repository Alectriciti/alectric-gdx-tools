package com.alectriciti.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.Widget;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.WidgetManager;

public class MyLibGDXTestApp implements Lwjgl3WindowListener, ApplicationListener {

	
	
	InputMultiplexer input = new InputMultiplexer();
	WidgetManager widget_manager = new WidgetManager(input);
	WidgetWindowListener window_listener = new WidgetWindowListener(widget_manager);
	ShapeRenderer shape_renderer;
	SpriteBatch sprite_batch;
	
	public int width, height;
	
	@Override
	public void create() {
		
		
		Gdx.input.setInputProcessor(input);

		Canvas main_menu = new Canvas("Cool Canvas", widget_manager, new Rectangle(100, 100, 200, 200));
		Canvas stupid_canvas = new Canvas("Stupid Canvas", widget_manager, new Rectangle(150, 50, 300, 100));

		Button a = new Button("Ass", Keys.A, main_menu);
		a.setSize(64,42);
		
		Button b = new Button("Poop", Keys.SPACE, widget_manager);
		b.setRelativePosition(100, 00);

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
		widget_manager.renderAll(shape_renderer, sprite_batch, null);
		
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
		this.shape_renderer.dispose();
		this.sprite_batch.dispose();
		Gdx.app.exit();
		return true;
	}

	@Override
	public void filesDropped(String[] files) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void refreshRequested() {
		// TODO Auto-generated method stub
		
	}

}
