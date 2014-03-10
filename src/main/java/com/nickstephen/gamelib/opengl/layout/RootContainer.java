package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 10/03/14.
 */
public class RootContainer extends Container {
    public RootContainer(@NotNull Context context, float width, float height, float parentOffsetX, float parentOffsetY) {
        super(context, null, width, height, parentOffsetX, parentOffsetY);
    }

    public RootContainer(@NotNull Context context, float width, float height, float startingPosX, float startingPosY, float parentOffsetX, float parentOffsetY) {
        super(context, null, width, height, startingPosX, startingPosY, parentOffsetX, parentOffsetY);
    }

    @Override
    public int getAbsoluteBLCornerX() {
        return 0;
    }

    @Override
    public int getAbsoluteBLCornerY() {
        return 0;
    }

    public boolean onTouchEvent(MotionEvent e) {
        float relX = e.getRawX() - (getScreenWidth() / 2.0f);
        float relY = -(e.getRawY() - (getScreenHeight() / 2.0f));

        return onTouchEvent(e, relX, relY);
    }
}
