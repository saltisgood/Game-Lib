package com.nickstephen.gamelib.opengl.bounds;

import com.nickstephen.gamelib.opengl.shapes.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 14/07/2014.
 */
public class Quadrilateral extends Bounds {
    protected float mWidth;
    protected float mHeight;

    public Quadrilateral(@NotNull Shape shape) {
        super(shape);
    }

    @Override
    public Bounds setWidth(float width) {
        mWidth = width;
        return this;
    }

    @Override
    public float getWidth() {
        return mWidth;
    }

    @Override
    public Bounds setHeight(float height) {
        mHeight = height;
        return this;
    }

    @Override
    public float getHeight() {
        return mHeight;
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        if (Math.abs(posX - mX) > (mWidth + touchSlop)) {
            return false;
        } else if (Math.abs(posY - mY) > (mHeight + touchSlop)) {
            return false;
        }
        return true;
    }
}
