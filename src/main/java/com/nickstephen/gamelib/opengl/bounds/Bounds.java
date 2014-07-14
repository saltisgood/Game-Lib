package com.nickstephen.gamelib.opengl.bounds;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 14/07/2014.
 */
public abstract class Bounds {
    protected final Shape mShape;

    protected float mX;
    protected float mY;

    protected Bounds(@NotNull Shape shape) {
        mShape = shape;
    }

    public float getX() {
        return mX;
    }

    public Bounds setX(float x) {
        mX = x;
        return this;
    }

    public float getY() {
        return mY;
    }

    public Bounds setY(float y) {
        mY = y;
        return this;
    }

    public abstract Bounds setWidth(float width);
    public abstract float getWidth();

    public abstract Bounds setHeight(float height);
    public abstract float getHeight();

    public abstract boolean withinBounds(float posX, float posY, float touchSlop);

}
