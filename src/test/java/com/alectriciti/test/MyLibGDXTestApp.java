package com.alectriciti.test;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import static com.alectriciti.gdx.Toolkit.*;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import com.alectriciti.gdx.Canvas;
import com.alectriciti.gdx.Direction;
import com.alectriciti.gdx.DropdownMenuButton;
import com.alectriciti.gdx.Widget;
import com.alectriciti.gdx.chat.ColoredText;
import com.alectriciti.gdx.chat.TextWidget;
import com.alectriciti.gdx.chat.TextInput;
import com.alectriciti.gdx.chat.TextInput.ReturnKeyMode;
import com.alectriciti.gdx.Button;
import com.alectriciti.gdx.UIManager;

public class MyLibGDXTestApp implements Lwjgl3WindowListener, ApplicationListener {
	
	
	
	InputMultiplexer input = new InputMultiplexer();
	UIManager ui_manager;
	ShapeRenderer shape_renderer;
	SpriteBatch sprite_batch;
	BitmapFont font;
	
	OrthographicCamera camera;
	Viewport viewport;
	
	public int width, height;
	
	public Runnable run_new_skin = new Runnable() {
		
		@Override
		public void run() {
			print("making nu skin");
		}
	};
	
	public Runnable run_poop = new Runnable() {
		
		@Override
		public void run() {
			print("making nu poop poooo");
		}
	};
	
	@Override
	public void create() {
		
		
		Gdx.input.setInputProcessor(input);

		FileHandle font_handle = Gdx.files.internal("lucida_console16.fnt");
		
		font = new BitmapFont(font_handle);
		ui_manager = new UIManager(input, font);
		
		DropdownMenuButton main_menu;
		
		Button test_button_a = new Button("Test Button A", ui_manager);
		test_button_a.setGlobalPosition(100, 100);
		test_button_a.setSize(142, 32);
		
		Button test_button_b = new Button("Test Button B", ui_manager);
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
		
		
		
		//hello_widget.editable = true;
		//hello_widget.setTouchable(true, true);

    	main_menu = new DropdownMenuButton("Main Menu", ui_manager, Keys.ESCAPE, Keys.B);
    	main_menu.alignment = Direction.UP;
    	main_menu.setRelativePosition(0, 0);
    	main_menu.setSize(120, 32);
    	Button button_new_skin = new Button("New Skin", main_menu);
    	button_new_skin.addOnActivate(main_menu.getAutocloseRunnable());
    	button_new_skin.addOnActivate(new Runnable() {

			@Override
    		public void run() {
    			confirmDialogueBox(run_new_skin);
    		}
		});
    	
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
		//b.setRelativePosition(100, 00);

		ui_manager.automaticallyAssignIDsToWidgets();
		
		
		width = Gdx.graphics.getWidth();
		height = Gdx.graphics.getHeight();
		
		
		
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
	
	private void confirmDialogueBox(Runnable run_new_skin) {
		if(confirm_box == null) {
			confirm_box = new Canvas("Start a New Skin?", ui_manager, new Rectangle(100, 100, 200, 100));
			confirm_box.setGlobalPosition(width/2 - 100, height/2 - 50);
			//Widget img = new Widget("img", confirm_box);
			
			Widget hello_widget = new Widget(ui_manager);
			//hello_widget.render_text
			//Message hello_world = new Message(new ColoredText());
			
			Button yes = new Button("Yes", confirm_box, Keys.Y) {
				@Override
				protected void onActivate() {
					run_new_skin.run();
					confirm_box.destroy();
					confirm_box = null;
				}
			};
			yes.font_offset = new Point(4, 4);
			yes.setRelativePosition(32, 30);
			yes.setSize(42, 32);
			Button no = new Button("No", confirm_box, Keys.N) {
				@Override
				protected void onActivate() {
					// TODO Auto-generated method stub
					super.onActivate();
					confirm_box.destroy();
					confirm_box = null;
				}
			};
			no.setRelativePosition(104, 30);
			no.font_offset = new Point(4, 4);
			no.setSize(42, 32);
			
			
			
			
			ui_manager.focus(confirm_box);
		}
	}
	
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
		m.return_mode = ReturnKeyMode.ACTIVATE_TARGET_SHIFT_NEW_LINE;
		m.setRelativePosition(50, 50);
	}
	
	public void testB() {
		
	}
	
	@Override
	public void resize(int width, int height) {
		this.width = width;
		this.height = height;
		ui_manager.alignAllWidgets();
		viewport.update(width, height, true);
	}

	@Override
	public void render() {

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
