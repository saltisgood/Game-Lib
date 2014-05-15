package com.nickstephen.gamelib.opengl.gestures;

import android.view.MotionEvent;

import com.nickstephen.gamelib.util.Direction;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 25/04/2014.
 */
public class GestureFling extends GestureEvent {
    public final float xVelocity;
    public final float yVelocity;

    public final Direction direction;

    GestureFling(@NotNull MotionEvent e, float xVel, float yVel) {
        super(e, Type.FLING);

        xVelocity = xVel;
        yVelocity = -yVel;

        float absX = Math.abs(xVelocity);
        float absY = Math.abs(yVelocity);

        if (yVelocity > 0 && absY > absX) {
            direction = Direction.DOWN;
        } else if (xVelocity < 0 && absX > absY) {
            direction = Direction.RIGHT;
        } else if (xVelocity > 0 && absX > absY) {
            direction = Direction.LEFT;
        } else {
            direction = Direction.UP;
        }
    }
}
