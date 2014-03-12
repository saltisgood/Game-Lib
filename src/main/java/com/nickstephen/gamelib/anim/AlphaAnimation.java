package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 13/03/14.
 */
public class AlphaAnimation extends Animation {
    protected float mStartAlpha;
    protected float mEndAlpha;

    public AlphaAnimation(@NotNull Shape shape, float startAlpha, float endAlpha) {
        super(shape);

        mStartAlpha = startAlpha;
        mEndAlpha = endAlpha;
    }

    @Override
    public void onUpdate(long now) {
        super.onUpdate(now);

        mShape.setAlpha(mStartAlpha + ((mEndAlpha - mStartAlpha) * mProgress));
    }

    @Override
    public void onFinish(long now) {
        super.onFinish(now);

        if (mRevertOnFinish) {
            mShape.setAlpha(mStartAlpha);
        } else {
            mShape.setAlpha(mEndAlpha);
        }
    }
}
