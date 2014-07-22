package com.nickstephen.gamelib.opengl.bounds;

import com.nickstephen.gamelib.opengl.shapes.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 14/07/2014.
 */
public class Default extends Bounds {
    protected float mLeft;
    protected float mRight;
    protected float mUp;
    protected float mDown;

    public Default(@NotNull Shape shape) {
        super(shape);
    }

    @Override
    public Bounds setWidth(float width) {
        mLeft = width / 2.f;
        mRight = mLeft;
        return this;
    }

    @Override
    public float getWidth() {
        return mLeft + mRight;
    }

    @Override
    public Bounds setHeight(float height) {
        mUp = height / 2.f;
        mDown = mUp;
        return this;
    }

    @Override
    public float getHeight() {
        return mUp + mDown;
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        float diff;
        if ((diff = mX - posX) < 0.0f) {
            if (-diff > (mRight + touchSlop)) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > (mLeft + touchSlop)) {
                return false;
            }
        }

        if ((diff = mY - posY) < 0.0f) {
            if (-diff > (mUp + touchSlop)) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > (mDown + touchSlop)) {
                return false;
            }
        }

        return true;
    }
}
