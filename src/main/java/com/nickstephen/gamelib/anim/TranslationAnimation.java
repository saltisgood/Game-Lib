package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class TranslationAnimation extends Animation {
    protected float mStartX, mStartY;
    protected float mEndX, mEndY;

    public TranslationAnimation(@NotNull Shape shape, float startX, float startY, float endX, float endY) {
        super(shape);

        mStartX = startX;
        mStartY = startY;
        mEndX = endX;
        mEndY = endY;
    }

    @Override
    public void onUpdate(long now) {
        super.onUpdate(now);

        if (mInterpol == Interpolation.LINEAR) {
            float posX = mStartX + ((mEndX - mStartX) * mProgress);
            float posY = mStartY + ((mEndY - mStartY) * mProgress);

            mShape.moveTo(posX, posY);
        }
    }

    @Override
    public void onLoop() {
        super.onLoop();

        if (mOnLoopStyle == LoopStyle.RESTART) {
            mShape.moveTo(mStartX, mStartY);
        }
    }

    @Override
    public void onFinish(long now) {
        super.onFinish(now);

        if (mRevertOnFinish) {
            mShape.moveTo(mStartX, mEndY);
        } else {
            mShape.moveTo(mEndX, mEndY);
        }
    }
}
