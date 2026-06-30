package com.alectriciti.gdx.events;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

public class EventManager {

    // The central registry. Maps an Event class to a list of listeners.
    private final ObjectMap<Class<? extends Event>, Array<EventListener<? extends Event>>> bus = new ObjectMap<>();

    /**
     * Registers a lambda or listener to a specific event class.
     */
    @SuppressWarnings("unchecked")
    public <T extends Event> void register(Class<T> eventClass, EventListener<T> listener) {
        if (!bus.containsKey(eventClass)) {
            // Initialize a LibGDX array (ordered = false for slight performance gain if order doesn't matter)
            bus.put(eventClass, new Array<>(false, 16));
        }
        
        // We handle the ugly generic casting here so the user never has to see it
        ((Array<EventListener<T>>) (Array<?>) bus.get(eventClass)).add(listener);
    }

    /**
     * Fires an event to all registered listeners.
     * Stops propagating instantly if a listener calls event.consume().
     */
    @SuppressWarnings("unchecked")
    public void fireEvent(Event event) {
        Array<EventListener<? extends Event>> listeners = bus.get(event.getClass());
        
        if (listeners != null) {
            // Standard loop avoids Iterator allocation
            for (int i = 0; i < listeners.size; i++) {
                if (event.isConsumed()) {
                    break; // Mimics Spigot's cancelled event behavior
                }
                
                EventListener<Event> listener = (EventListener<Event>) listeners.get(i);
                listener.handle(event);
            }
        }
    }
    
    /**
     * Clears all listeners. Useful for changing game states or scenes.
     */
    public void clear() {
        bus.clear();
    }
}