package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.text.FPSMeter;
import com.nickstephen.lib.VersionControl;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 10/03/14.
 */
public class RootContainer extends Container {
    public RootContainer(@NotNull Context context, @NotNull GLSurfaceView surface, float width, float height, float parentOffsetX, float parentOffsetY, String fontFile) {
        super(context, null, width, height, parentOffsetX, parentOffsetY);

        this.setSurface(surface);

        if (!VersionControl.IS_RELEASE) {
            FPSMeter fps = new FPSMeter(context, this, fontFile);
            this.mChildren.add(fps);
        }
    }

    public RootContainer(@NotNull Context context, @NotNull GLSurfaceView surface, float width, float height, float startingPosX, float startingPosY, float parentOffsetX, float parentOffsetY, String fontFile) {
        super(context, null, width, height, startingPosX, startingPosY, parentOffsetX, parentOffsetY);

        this.setSurface(surface);

        if (!VersionControl.IS_RELEASE) {
            FPSMeter fps = new FPSMeter(context, this, fontFile);
            this.mChildren.add(fps);
        }
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
