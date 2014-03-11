package com.nickstephen.gamelib.opengl.text;

import android.content.Context;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;

/**
 * A convenience text widget that displays a FPS count in the bottom left of its parent container.
 * @author Nick Stephen
 */
public class FPSMeter extends Text {
    /**
     * The number of frames to wait before updating the GUI (also takes the average FPS over a longer
     * time)
     */
    private static final int FRAMES_BEFORE_UPDATE = 8;
    private long[] mTickTimes = new long[FRAMES_BEFORE_UPDATE];
    private long mLastTick;
    private int mTickIndex;

    /**
     * Default constructor.
     * @param context A context
     * @param parent The parent container (must not be null)
     * @param fontFile The path to the font file to load the font to use
     */
    public FPSMeter(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile) {
        super(context, parent, fontFile);

        mCentered = false;
    }

    /**
     * An override to draw just because it's the easiest place to hook a call on every rendering pass.
     * Doesn't do any drawing inside this method, just works out and sets the new text to display if
     * necessary and calls the super class.
     * @param vpMatrix The view/projection matrix
     */
    @Override
    public void draw(@NotNull float[] vpMatrix) {
        if (mTickIndex == FRAMES_BEFORE_UPDATE) {
            long ave = GeneralUtil.arrayAverage(mTickTimes);
            mText = Long.valueOf(1000 / ave).toString();
            mTickIndex = 0;
            //noinspection ConstantConditions
            moveTo(getParent().getScreenWidth() / -2.0f, getParent().getScreenHeight() / -2.0f);
        }

        long currentTime = System.currentTimeMillis();
        if (mLastTick != 0) {
            mTickTimes[mTickIndex++] = currentTime - mLastTick;
        }
        mLastTick = currentTime;



        super.draw(vpMatrix);
    }
}
