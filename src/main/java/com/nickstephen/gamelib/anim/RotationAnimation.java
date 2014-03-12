package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class RotationAnimation extends Animation {
    protected float mStartAngle;
    protected float mEndAngle;

    public RotationAnimation(@NotNull Shape shape, float startAngle, float endAngle) {
        super(shape);

        mStartAngle = startAngle;
        mEndAngle = endAngle;
    }

    @Override
    public void onUpdate(long now) {
        super.onUpdate(now);

        if (mInterpol == Interpolation.LINEAR) {
            float angle = mStartAngle + ((mEndAngle - mStartAngle) * mProgress);
            mShape.setAngle(angle);
        }
    }

    @Override
    public void onLoop() {
        super.onLoop();

        mShape.setAngle(mStartAngle);
    }

    @Override
    public void onFinish(long now) {
        if (mRevertOnFinish) {
            mShape.setAngle(mStartAngle);
        } else {
            mShape.setAngle(mEndAngle);
        }

        super.onFinish(now);
    }
}
