package com.alectriciti.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.alectriciti.gdx.Toolkit.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.ColoredText;
import com.alectriciti.gdx.ContextWidget;
import com.alectriciti.gdx.Direction;
import com.alectriciti.gdx.DropdownMenuButton;
import com.alectriciti.gdx.Slider;
import com.alectriciti.gdx.Style;
import com.alectriciti.gdx.TextDialog;
import com.alectriciti.gdx.TextInput;
import com.alectriciti.gdx.Toolkit;
import com.alectriciti.gdx.Widget;
import com.alectriciti.gdx.styles.StyleOldschool;
import com.alectriciti.gdx.Button.ButtonType;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.UIManager;
import com.alectriciti.gdx.InheritanceRule;

public class MyLibGDXTestApp implements Lwjgl3WindowListener, ApplicationListener {
	
	InputMultiplexer input = new InputMultiplexer();
	UIManager ui_manager;
	ShapeRenderer shape_renderer;
	SpriteBatch sprite_batch;
	BitmapFont font;
	
	OrthographicCamera camera;
	Viewport viewport;
	
	public int width, height;	
	
	Widget info;

	Style oldschool_style;
	
	Style cool_style;

	Button button_mode_1, button_mode_2, button_mode_3, button_mode_4;
	
	@Override
	public void create() {
		
		Gdx.input.setInputProcessor(input);
		FileHandle font_handle = Gdx.files.internal("lucida_console16.fnt");
		
		font = new BitmapFont(font_handle);
		ui_manager = new UIManager(input, font);
		
		DropdownMenuButton main_menu, other_menu;
		
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		
		cool_style = new Style();
		oldschool_style = new StyleOldschool();
		

		button_mode_1 = new Button("1", ui_manager, Input.Keys.NUM_1);
		button_mode_2 = new Button("2", ui_manager, Input.Keys.NUM_2);
		button_mode_3 = new Button("3", ui_manager, Input.Keys.NUM_3);
		button_mode_4 = new Button("4", ui_manager, Input.Keys.NUM_4);
		button_mode_1.setType(ButtonType.TOGGLE);
		button_mode_2.setType(ButtonType.TOGGLE);
		button_mode_3.setType(ButtonType.TOGGLE);
		button_mode_4.setType(ButtonType.TOGGLE);
		button_mode_1.alignment = Direction.UP_LEFT;
		button_mode_2.alignment = Direction.UP_LEFT;
		button_mode_3.alignment = Direction.UP_LEFT;
		button_mode_4.alignment = Direction.UP_LEFT;
		button_mode_1.setGlobalPosition(40, height-120);
		button_mode_2.setGlobalPosition(80, height-120);
		button_mode_3.setGlobalPosition(120, height-120);
		button_mode_4.setGlobalPosition(160, height-120);

//		button_mode_1.addOnActivate(()->{mode=0;});
//		button_mode_2.addOnActivate(()->{mode=1;});
//		button_mode_3.addOnActivate(()->{mode=2;});
//		button_mode_4.addOnActivate(()->{mode=3;});
		
		
		Button test_button_a = new Button("Test Button A", ui_manager) {
			public ContextWidget spawnContextWidget() {
				ContextWidget w = new ContextWidget(this);;
				new Button("option 1", w).setSizeToFont();
				new Button("option 2", w);
				new Button("exit", w) {
					@Override
					protected void onActivate() {
						((Button)parent).deactivate();
					}
				};
				return w;	
			}
		};
		test_button_a.setGlobalPosition(100, 100);
		test_button_a.setSize(142, 32);
		
		Button test_button_b = new Button("Test Button B", ui_manager) {

			public ContextWidget spawnContextWidget() {
				ContextWidget w = new ContextWidget(this);;
				new Button("ass", w);
				return w;	
			}
		};
		test_button_b.setGlobalPosition(280, 100);
		test_button_b.setSize(142, 32);

		test_button_a.addOnActivate(new Runnable() {
			
			@Override
			public void run() {
				testA();
			}
		});


		test_button_b.addOnActivate(new Runnable() {
			
			@Override
			public void run() {
				testB();
			}
		});
		
		info = new Widget("info", ui_manager);
		info.show_text = true;
		info.alignment = Direction.UP;
		info.setSize(120, 53);
		info.setRelativePosition(10, 10);
		
		
		//Stress Test
//		for(int x = 0; x<1200;x++) {
//			Button button = new Button("x_"+x, ui_manager);
//			button.setGlobalPosition((x%30)*32, ((int)(x/30))*32);
//		}
		

		
		/**
		 * Dropdown Menus
		 */
    	main_menu = new DropdownMenuButton("Main Menu", ui_manager, Keys.ESCAPE);
    	main_menu.alignment = Direction.UP;
    	main_menu.setRelativePosition(0, 0);
    	main_menu.setSize(120, 32);
    	other_menu = new DropdownMenuButton("Other Menu", ui_manager, Keys.F1);
    	other_menu.alignment = Direction.UP;
    	other_menu.setRelativePosition(120, 0);
    	other_menu.setSize(120, 32);
    	
    	
    	
    	
    	Button button_new_skin = new Button("New Menu", main_menu);
    	button_new_skin.addOnActivate(main_menu.getAutocloseRunnable());
    	
    	DropdownMenuButton save_confirm_dropdown = new DropdownMenuButton("Save UI", main_menu);
    	save_confirm_dropdown.setDirection(Direction.RIGHT);
    	Button save_ui = new Button("Confirm Save", save_confirm_dropdown);
    	save_ui.addOnActivate(new Runnable() {
			@Override
			public void run() {
				ui_manager.saveAllWidgets();
				save_confirm_dropdown.deactivate();
				main_menu.deactivate();
			}
		});
    	Button load_ui = new Button("Load UI", main_menu);
    	load_ui.addOnActivate(new Runnable() {
			@Override
			public void run() {
				ui_manager.loadAllWidgets();
			}
		});
    	
    	Button button_hideui = new Button("Hide UI (F11)", main_menu, Keys.F11) {
    		
    		@Override
    		protected void onActivate() {
    			for(Widget w : ui_manager.widgets) {
    				w.setVisible(false, InheritanceRule.RECURSIVE);
    			}
    		}
    		
    		@Override
    		protected void onDeactivate() {
    			for(Widget w : ui_manager.widgets) {
    				w.setVisible(true, InheritanceRule.RECURSIVE);
    			}
    		}
    	};
    	button_hideui.setType(ButtonType.TOGGLE);
    	
    	

    	new Button("blah", other_menu);
    	DropdownMenuButton dropdownMenuButton = new DropdownMenuButton("super blarg extreme", other_menu);
    	dropdownMenuButton.setDirection(Direction.RIGHT);

    	new Button("aaah", dropdownMenuButton);
    	new Button("aaah2", dropdownMenuButton);
    	new Button("aaah3", dropdownMenuButton);

		SneakyButton sneaky_button = new SneakyButton("sneaker", ui_manager);
		
		Button a, b, c;

		a = new Button("default", ui_manager);
		b = new Button("styled", ui_manager);
		a.setSize(100, 32);
		b.setSize(100, 32);
		a.setGlobalPosition(100, 280);
		b.setGlobalPosition(100, 240);

		b.style = oldschool_style;
		
    	cool_style.color_outline = new Color(0.2f, 0.2f, 1, 1);
    	test_button_a.style = cool_style;
    	test_button_b.style = cool_style;
    	
    	
		Slider slider = new Slider(ui_manager, true);
		slider.getKnob().setSize(10, slider.getKnob().getHeight());
		slider.setLength(200);
		slider.setValueRange(4,12);
		slider.setRelativePosition(400, 220);

    	a.addOnActivate(()->{slider.setValue(4f);});
    	b.addOnActivate(()->{slider.setValue(12f);});
    	
    	
    	
    	
		ui_manager.automaticallyAssignIDsToWidgets();
		
		shape_renderer = new ShapeRenderer();
		shape_renderer.setAutoShapeType(true);
		sprite_batch = new SpriteBatch();

		camera = new OrthographicCamera();
		viewport = new ScreenViewport(camera);

		
		viewport.setCamera(camera);
		viewport.apply();
		
		
		ui_manager.loadAllWidgets();
		ui_manager.alignAllWidgets();
		
	}
	
	Canvas confirm_box;
	
	int global_test_button_index;
	
	public List<Button> createTestButtonArray(Widget parent, int amount, String...names){
		List<Button> buttons = new ArrayList<Button>();
		if(names.length == 0) {
			for(int i = 0; i <amount; i++) {
				Button b = new Button((""+i), ui_manager);
				buttons.add(b);
			}
		}else {
			for(int i = 0; i < amount; i++) {
				String name = names[i];
				if(names!=null) {
					print("NEW BUTTON: "+name);
					Button b = new Button(name, ui_manager);
					buttons.add(b);
				}
			}
		}
		for(Button b: buttons) {
			b.attachToWidget(parent);
		}
		return buttons;
	}
	
	public void testA() {
//		TextInput widget = new TextInput(ui_manager);
		TextInput m = new TextInput(ui_manager, new ColoredText("Type Here!", Color.GRAY));
		m.enterActivatesTarget = true;
		m.setRelativePosition(50, 50);
	}
	
	public void testB() {
		new TextDialog("TestDialog", ui_manager, "Type Here!", (newText) -> {
			// This block executes when the user hits 'confirm' in the TextDialog.
			Toolkit.print("User entered: " + newText);
		});
	}
	
	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		ui_manager.alignAllWidgets();
		viewport.update(width, height, true);
	}
	
	public void update() {
		int fps = Gdx.graphics.getFramesPerSecond();
		Gdx.graphics.setTitle("fps: "+fps);
		info.setText("widgets: "+ui_manager.widgets.size);
	}

	@Override
	public void render() {
		update();
		ui_manager.update();
		camera.zoom = 1f;
		camera.update();
		viewport.setCamera(camera);
		viewport.apply(true);
		shape_renderer.setProjectionMatrix(camera.combined);
		sprite_batch.setProjectionMatrix(camera.combined);

		ScreenUtils.clear(0.1f, 0.1f, 0.1f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		//shape_renderer.setColor(Color.WHITE);shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		
		//Gdx.gl.glDisable(GL20.GL_BLEND);
		ui_manager.renderAll(shape_renderer, sprite_batch);
		debugDraw(shape_renderer);
	}

	private void debugDraw(ShapeRenderer shape_renderer) {
		int x = 500, y = 300, w = 64, h = 64;
		
		shape_renderer.begin();
		
		shape_renderer.set(ShapeType.Point);
		shape_renderer.setColor(1, 1, 1, 0.5f);

		shape_renderer.point(x, y, 0);
		shape_renderer.point(x+w, y, 0);
		shape_renderer.point(x+w, y+h, 0);
		shape_renderer.point(x, y+h, 0);
		
		
		if(button_mode_1.activated) {
			shape_renderer.set(ShapeType.Line);
			shape_renderer.setColor(1, 0, 0, 0.5f);
			drawRect(shape_renderer, x, y, w, h);
		}
		if(button_mode_2.activated) {
			shape_renderer.set(ShapeType.Filled);
			shape_renderer.setColor(1, 1, 0, 0.5f);
//			shape_renderer.rect(x, y, w, h);
			drawRect(shape_renderer, x, y, w, h);
		}
		if(button_mode_3.activated) {
			shape_renderer.set(ShapeType.Line);
			shape_renderer.setColor(1, 0, 0, 0.25f);
			drawRectRound(shape_renderer, x, y, w, h, cool_style.corner_radius);
		}
		if(button_mode_4.activated) {
			shape_renderer.set(ShapeType.Filled);
			shape_renderer.setColor(0, 1, 0, 0.25f);
			drawRectRound(shape_renderer, x, y, w, h, cool_style.corner_radius);
//			drawRect(shape_renderer, x, y, w, h);
		}

//		drawRect(shape_renderer, x, y, w, h);
//		shape_renderer.setColor(0, 1, 0, 0.45f);
//		shape_renderer.rect(x+100, y, w, h);
		shape_renderer.end();
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
		ui_manager.dispose();
		//this.shape_renderer.dispose();
		//this.sprite_batch.dispose();
		//Gdx.app.exit();
		return true;
	}

	@Override
	public void filesDropped(String[] files) {
		if (!ui_manager.importFileAutomaticAssignment(files)) {
			//failed to import other things... so use default method
			
		}
	}

	@Override
	public void refreshRequested() {
		// TODO Auto-generated method stub
		
	}

}
