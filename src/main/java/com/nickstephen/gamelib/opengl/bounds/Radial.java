package com.nickstephen.gamelib.opengl.bounds;

import com.nickstephen.gamelib.opengl.shapes.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 14/07/2014.
 */
public class Radial extends Bounds {
    protected float mRadius;

    public Radial(@NotNull Shape shape) {
        super(shape);
    }

    @Override
    public Bounds setWidth(float width) {
        mRadius = width;
        return this;
    }

    @Override
    public float getWidth() {
        return mRadius;
    }

    @Override
    public Bounds setHeight(float height) {
        mRadius = height;
        return this;
    }

    @Override
    public float getHeight() {
        return mRadius;
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        return (Math.sqrt(((posX - mX) * (posX - mX)) + ((posY - mY) * (posY - mY))) <=
                (mRadius + touchSlop));
    }
}
