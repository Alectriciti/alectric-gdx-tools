package com.alectriciti.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Rectangle;
import java.util.function.Consumer;

public class TextDialog extends Canvas {
    
    Runnable run_destroy = new Runnable() {
        @Override
        public void run() {
            destroy();
        }
    };
    
    public TextInput text_input;
    public Button confirm;
    public Button cancel;

    /**
     * @param id The unique widget ID
     * @param manager The UI Manager
     * @param initial_text The starting text to display in the input box
     * @param onConfirm Callback executed when the confirm button is pressed, passing the raw string.
     */
    public TextDialog(String id, UIManager manager, String initial_text, Consumer<String> onConfirm) {
        super(id, manager, new Rectangle(100, 100, 200, 100));
        
        // Center the dialog dynamically based on the current window size
        setGlobalPosition(Gdx.graphics.getWidth() / 2f - 100, Gdx.graphics.getHeight() / 2f - 50);
        
        confirm = new Button("confirm", this) {
            @Override
            protected void onActivate() {
                if (onConfirm != null) {
                    onConfirm.accept(text_input.msg_raw);
                }
            }
        };
        
        confirm.color_default = new Color(0, 0.3f, 0, 1.0f);
        cancel = new Button("cancel", this);
        
        confirm.addOnActivate(run_destroy);
        cancel.addOnActivate(run_destroy);
        
        confirm.setRelativePosition(104, 30);
        cancel.setRelativePosition(32, 30);
        
        text_input = new TextInput(this, new ColoredText(initial_text, Color.GREEN));
        text_input.setSize(200, 32);
        text_input.enterActivatesTarget = true;
        text_input.enterFocusesTarget = true;
        text_input.target_widget = confirm;
        text_input.setRelativePosition(0, 62);
        text_input.setColor(Color.CYAN);
        text_input.focus();
    }
}