package com.nickstephen.gamelib.opengl.text;

import android.content.Context;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 10/03/14.
 */
public class FPSMeter extends Text {
    private static final int FRAMES_BEFORE_UPDATE = 8;

    private long mLastTick;
    private long[] mTickTimes = new long[FRAMES_BEFORE_UPDATE];
    private int mTickIndex;

    public FPSMeter(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile) {
        super(context, parent, fontFile);

        mCentered = false;
    }

    @Override
    public void draw(float[] vpMatrix) {
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

        moveTo(getParent().getScreenWidth() / -2.0f, getParent().getScreenHeight() / -2.0f);

        super.draw(vpMatrix);
    }
}
