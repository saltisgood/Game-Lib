package com.nickstephen.gamelib.opengl.gestures;

import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 24/04/2014.
 */
public class GestureScroll extends GestureEvent {
    GestureScroll(@NotNull MotionEvent e, @NotNull GestureEvent.Type t, float distX, float distY) {
        super(e, t);


    }
}
