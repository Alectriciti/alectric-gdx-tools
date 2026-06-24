This is my personal use library for LibGDX.
It features a Custom UI Toolkit as well as several other odds and ends.


Note: This is a Desktop-Only API

Steps to apply this to your LibGDX Project:


> Personal Note: To build this within maven, do ```mvn clean install``` in a local terminal
```> C:\Users\alect\design\gamedev\projects_gdx\alectric-gdx-tools\target\alectric-gdx-tools-0.0.1-sources.jar```
```C:\Users\alect\.m2\repository\com\github\alectriciti\alectric-gdx-tools\0.0.1\alectric-gdx-tools-0.0.1-sources.jar```
1. Create a new LibGDX Project using gdx-liftoff
2. Add this line to the dependencies in the lwjgl3 build.gradle: ```implementation 'com.github.alectriciti:alectric-gdx-tools:0.0.1'```

3. Ensure you use `configuration.setOpenGLEmulation(Lwjgl3ApplicationConfiguration.GLEmulation.ANGLE_GLES20, 0, 0);` in your launcher to properly display widgets

# Toolkit.class
This class is intended to be imported statically to access to various useful common functions such as lerp, mapRange, etc.


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
A simple button with several different function types.
  - Press (fires when released, can be cancelled by draggin away)
  - Hold (activates when pressed, deactivates when released or dragged away)
  - Toggle (Toggles to an ON and OFF state)
  - Rapidfire (Fires continuously while pressed, with a customizable tick rate)
# Future Plans
More UI features, such as:
- Color Picker
- Sliders
- Listed or Nested Dropdown Menus

ColoredText.class (building blocks for Message.class)
Message.class (which can double as an input field)
Console.class (An overridable lightweight UI console that gives developers greater access to testing. It is designed with the ability to double as a chat system for multiplayer environments.)
Command.class (A class which can be instantiated by developers to register new Commands to the Console, complete with arguments and a /help command)




These will serve to create a broader vision for a utility console which can be used in any LibGDX project.



I'm just doing this for my personal projects and to grow as a developer.
If anyone else ever uses this, I hope you find it useful somehow.
