package com.nickstephen.gamelib.opengl.gestures;

import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 24/04/2014.
 */
public class GestureEvent {
    public static GestureEvent construct(@NotNull MotionEvent e, @Nullable MotionEvent param2, @NotNull Type t, float param4, float param5) {
        switch (t) {
            case SCROLL:
                return new GestureScroll(e, t, param4, param5);
            case FLING:
                return new GestureFling(e, param4, param5);

            case FINISH:
            case DOWN:
            case LONG_PRESS:
            case SHOW_PRESS:
            case SINGLE_TAP:
            case DOUBLE_TAP:
            default:
                return new GestureEvent(e, t);
        }
    }


    public final Type type;

    public final long originalTime;

    public final int pointerId;

    public final float originalX;
    public final float originalY;

    protected GestureEvent(@NotNull MotionEvent e, @NotNull Type t) {
        type = t;
        originalTime = e.getDownTime();
        pointerId = e.getPointerId(0);
        originalX = e.getX();
        originalY = e.getY();
    }

    public static enum Type {
        DOWN, FLING, LONG_PRESS, SCROLL, SHOW_PRESS, SINGLE_TAP, DOUBLE_TAP, FINISH
    }
}
