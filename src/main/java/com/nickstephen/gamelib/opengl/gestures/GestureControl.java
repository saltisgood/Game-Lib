package com.nickstephen.gamelib.opengl.gestures;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.nickstephen.gamelib.run.Game;

/**
 * Created by Nick Stephen on 23/04/2014.
 */
public class GestureControl implements GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Game.getInstanceUnsafe().addInput(GestureEvent.construct(e, null, GestureEvent.Type.SINGLE_TAP, 0f, 0f));
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        Game.getInstanceUnsafe().addInput(GestureEvent.construct(e, null, GestureEvent.Type.DOUBLE_TAP, 0f, 0f));
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onDown(MotionEvent e) {
        // Ignore for now
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        // Ignore for now
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent initial, MotionEvent current, float distanceX, float distanceY) {
        Game.getInstanceUnsafe().addInput(GestureEvent.construct(initial, current, GestureEvent.Type.SCROLL,
                distanceX, distanceY));
        return true;
    }

    @Override
    public void onLongPress(MotionEvent initial) {
        Game.getInstanceUnsafe().addInput(GestureEvent.construct(initial, null, GestureEvent.Type.LONG_PRESS, 0f, 0f));
    }

    @Override
    public boolean onFling(MotionEvent initial, MotionEvent move, float velocityX, float velocityY) {
        Game.getInstanceUnsafe().addInput(GestureEvent.construct(initial, move, GestureEvent.Type.FLING, velocityX, velocityY));
        return true;
    }
}
