package com.alectriciti.gdx.events;

/**
 * The root class for all events in the engine.
 */
public abstract class Event {
    
    private boolean consumed = false;
    protected boolean cancelled = false;

    /**
     * Marks this event as handled. 
     * The EventManager will stop passing it to subsequent listeners.
     */
    public void consume() {
        this.consumed = true;
    }

    public boolean isConsumed() {
        return consumed;
    }
}