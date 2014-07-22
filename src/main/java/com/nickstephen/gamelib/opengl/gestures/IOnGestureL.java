package com.nickstephen.gamelib.opengl.gestures;

import com.nickstephen.gamelib.opengl.shapes.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 25/04/2014.
 */
public interface IOnGestureL {
    public void onGesture(@NotNull Shape shape, @NotNull GestureEvent e);
}
