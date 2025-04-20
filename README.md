This is my personal use library for LibGDX.

# Toolkit.class
This provides access to various useful functions such as lerp, mapRange, etc.
Add it with:
```
import static com.alectriciti.gdx.Toolkit.*;
```

# WidgetManager.class
This class holds the primary logic for Alectric-UI.
Canvases, Buttons, and all other Widgets can be used without it, but you'll have to write your own implementation.
Here's how you can use WidgetManager.
```
	WidgetManager widget_manager = new WidgetManager(input); // use an InputMultiplexer to combine with your own InputProcessor


public void render(){
  widget_manager.update(); //runs mouse and keybind logic

  //Render the UI
  widget_manager.renderAll(shape_renderer, sprite_batch, bitmap_font);

  //Or render each part individually
  widget_manager.renderShapes(shape_renderer);
}
```

From there, you can now add Widgets
Widgets are the building blocks of Alectric-UI

# Canvas.class
  These are basic containers for widgets, and they themselves are widgets
# Button.class
  A button with several different types.
  - Press
  - Hold
  - Toggle (Toggles to an ON and OFF state)
  - Rapidfire (Fires off continuously while pressed)



I'm just doing this for my personal projects and to grow as a developer.
If anyone else ever uses this, I hope you find it useful somehow.
