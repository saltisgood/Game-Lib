package com.nickstephen.gamelib.opengl.text;

import android.content.Context;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * A convenience text widget that displays a FPS count in the bottom left of its parent container.
 * @author Nick Stephen
 */
public class FPSMeter extends Text {
    public static @NotNull FPSMeter get(@NotNull Context context, @NotNull Container parent,
                                        @NotNull Font font) {
        return Text.FontManager.getTextGeneric(context, parent, font, getConstructor());
    }

    private static Constructor<FPSMeter> sConstructor;
    private static Constructor<FPSMeter> getConstructor() {
        if (sConstructor != null) {
            return sConstructor;
        }

        Constructor<?>[] constrs = FPSMeter.class.getDeclaredConstructors();

        if (constrs != null) {
            for (int i = 0; i < constrs.length; ++i) {
                Class<?>[] clazs = constrs[i].getParameterTypes();
                if (clazs == null || clazs.length != 3) {
                    continue;
                }

                if (clazs[0].getClass() == Context.class.getClass() && clazs[1].getClass() == Container.class.getClass()
                        && clazs[2].getClass() == Font.class.getClass()) {
                    sConstructor = (Constructor<FPSMeter>) constrs[i];
                    return sConstructor;
                }
            }
        }

        throw new RuntimeException("ldskfaj");
    }

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
    public FPSMeter(@NotNull Context context, @NotNull Container parent, @NotNull Font fontFile) {
        super(context, parent, fontFile);

        mCentered = false;
        setColour(1.0f, 1.0f, 1.0f, 1.0f);
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
            setText(Long.valueOf(1000 / ave).toString());
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
