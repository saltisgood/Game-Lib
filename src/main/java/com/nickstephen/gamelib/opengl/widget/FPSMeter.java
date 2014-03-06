package com.nickstephen.gamelib.opengl.widget;

import android.content.res.AssetManager;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.GLText;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class FPSMeter extends GLText {
    private static final int FRAMES_BEFORE_UPDATE = 5;

    private float mX;
    private float mY;
    private String mText = "0";

    private long mLastTick;
    private long[] mTickTimes = new long[FRAMES_BEFORE_UPDATE];
    private int mTickIndex;

    public FPSMeter(AssetManager assets) {
        super(assets);
    }

    public void onDrawFrame(float[] vpMatrix) {
        this.begin(vpMatrix);

        if (mTickIndex == FRAMES_BEFORE_UPDATE) {
            long ave = GeneralUtil.arrayAverage(mTickTimes);
            mText = Long.valueOf(1000 / ave).toString();
            mTickIndex = 0;
        }

        long currentTime = System.currentTimeMillis();
        if (mLastTick != 0) {
            mTickTimes[mTickIndex++] = currentTime - mLastTick;
        }
        mLastTick = currentTime;
        draw(mText, mX, mY);

        this.end();
    }

    public void onSurfaceChanged(int width, int height) {
        mX = -1.0f * (width / 2.0f);
        mY = height / -2.0f;
    }
}
