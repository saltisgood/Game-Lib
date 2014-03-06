package com.nickstephen.gamelib.opengl.widget;

import android.view.MotionEvent;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public interface ITouchL {
    public boolean withinBounds(float posX, float posY);

    public boolean onTouchEvent(MotionEvent e, float relativePosX, float relativePosY);
}
