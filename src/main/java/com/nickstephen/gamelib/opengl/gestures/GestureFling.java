package com.nickstephen.gamelib.opengl.gestures;

import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 25/04/2014.
 */
public class GestureFling extends GestureEvent {
    public final float xVelocity;
    public final float yVelocity;

    GestureFling(@NotNull MotionEvent e, float xVel, float yVel) {
        super(e, Type.FLING);

        xVelocity = xVel;
        yVelocity = -yVel;
    }
}
