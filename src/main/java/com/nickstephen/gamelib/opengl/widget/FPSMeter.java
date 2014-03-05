package com.nickstephen.gamelib.opengl.widget;

import android.content.res.AssetManager;

import com.nickstephen.gamelib.opengl.GLText;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class FPSMeter extends GLText {
    private long mLastTick;
    private float mX;
    private float mY;

    public FPSMeter(AssetManager assets) {
        super(assets);
    }

    public void onDrawFrame(float[] vpMatrix) {
        this.begin(vpMatrix);

        long currentTime = System.currentTimeMillis();
        if (mLastTick != 0) {
            draw(Float.valueOf(1000.0f / (currentTime - mLastTick)).toString().split("\\.")[0], mX, mY);
        }
        mLastTick = currentTime;

        this.end();
    }

    public void onSurfaceChanged(int width, int height) {
        mX = -1.0f * (width / 2.0f);
        mY = height / -2.0f;
    }
}
