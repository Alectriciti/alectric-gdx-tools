package com.alectriciti.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

import static com.alectriciti.gdx.Toolkit.*;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.WidgetManager;

public class MyLibGDXTestApp implements ApplicationListener {

	
	
	InputMultiplexer input = new InputMultiplexer();
	WidgetManager widget_manager = new WidgetManager(input);
	WidgetWindowListener window_listener = new WidgetWindowListener(widget_manager);
	ShapeRenderer shape_renderer;
	SpriteBatch sprite_batch;
	
	public int width, height;
	
	@Override
	public void create() {
		
		
		Gdx.input.setInputProcessor(input);

		//Canvas main_menu = new Canvas("Cool Canvas", widget_manager, new Rectangle(100, 100, 200, 200));
		//Canvas stupid_canvas = new Canvas("Stupid Canvas", widget_manager, new Rectangle(150, 50, 300, 100));
		
		Button b = new Button("Poop", Keys.SPACE, widget_manager);
		b.setRelativePosition(100, 00);
		Button a = new Button("Ass", Keys.A, widget_manager);

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

}
