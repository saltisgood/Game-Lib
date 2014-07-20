package com.nickstephen.gamelib.opengl.bounds;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Stephen on 18/07/2014.
 */
public class Multiple<T extends Bounds> extends Bounds {
    protected final List<T> mBoundsList;
    protected float mTargetWidth;
    protected float mTargetHeight;

    public Multiple(@NotNull Shape shape) {
        super(shape);

        mBoundsList = new ArrayList<T>();
    }

    @Override
    public Bounds setWidth(float width) {
        mTargetWidth = width;

        if (mBoundsList.size() != 0) {
            //TODO: Resize children
        }

        return this;
    }

    @Override
    public float getWidth() {
        if (mBoundsList.size() == 0) {
            return mTargetWidth;
        }

        T r = getMostRight();
        T l = getMostLeft();

        if (r == null || l == null) {
            return 0.f;
        } else {
            return (r.getX() + (r.getWidth() / 2.f)) - (l.getX() - (l.getWidth() / 2.f));
        }
    }

    @Override
    public Bounds setHeight(float height) {
        mTargetHeight = height;

        if (mBoundsList.size() != 0) {
            //TODO: Resize children
        }

        return this;
    }

    @Override
    public float getHeight() {
        if (mBoundsList.size() == 0) {
            return mTargetHeight;
        }

        T t = getTop();
        T b = getBottom();

        if (t == null || b == null) {
            return 0.f;
        } else {
            return (t.getY() + (t.getHeight() / 2.f)) - (b.getY() - (b.getHeight() / 2.f));
        }
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        for (int i = mBoundsList.size() - 1; i >= 0; --i) {
            if (mBoundsList.get(i).withinBounds(posX, posY, touchSlop)) {
                return true;
            }
        }

        return false;
    }

    public void addBound(@NotNull T newBound) {
        mBoundsList.add(newBound);
    }

    protected @Nullable T getMostLeft() {
        T rt = null;

        for (int i = mBoundsList.size() - 1; i >= 0; --i) {
            T curr = mBoundsList.get(i);
            if (rt == null || (rt.getX() - rt.getWidth()) > (curr.getX() - curr.getWidth())) {
                rt = curr;
            }
        }

        return rt;
    }

    protected @Nullable T getMostRight() {
        T rt = null;

        for (int i = mBoundsList.size() - 1; i >= 0; --i) {
            T curr = mBoundsList.get(i);

            if (rt == null || (rt.getX() + rt.getWidth()) < (curr.getX() + curr.getWidth())) {
                rt = curr;
            }
        }

        return rt;
    }

    protected @Nullable T getTop() {
        T rt = null;

        for (int i = mBoundsList.size() - 1; i >= 0; --i) {
            T curr = mBoundsList.get(i);

            if (rt == null || (rt.getY() + rt.getHeight()) < (curr.getY() + curr.getHeight())) {
                rt = curr;
            }
        }

        return rt;
    }

    protected @Nullable T getBottom() {
        T rt = null;

        for (int i = mBoundsList.size() - 1; i >= 0; --i) {
            T curr = mBoundsList.get(i);

            if (rt == null || (rt.getY() - rt.getHeight()) > (curr.getY() - curr.getHeight())) {
                rt = curr;
            }
        }

        return rt;
    }
}
