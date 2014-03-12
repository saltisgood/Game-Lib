package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Perform a rotation animation on a shape. Pretty much as it sounds it just turns the shape around
 * as looking at it on the screen. The only caveat is that if you want it to do a complete revolution
 * you should add 360 to the end angle.
 * @author Nick Stephen
 */
public class RotationAnimation extends Animation {
    protected float mStartAngle;
    protected float mEndAngle;

    /**
     * Constructor.
     * @param shape The shape to animate
     * @param startAngle The angle to start at (degrees)
     * @param endAngle The angle to finish at (degrees)
     */
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
