package com.alectriciti.gdx.events;

import com.badlogic.gdx.utils.ObjectSet;

/*
 * Basic API support
 */
@FunctionalInterface
public interface EventListener<E extends Event> {
    void handle(E event);
}