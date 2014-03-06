package com.nickstephen.gamelib.opengl;

import android.graphics.RectF;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.program.GenericProgram;
import com.nickstephen.gamelib.opengl.program.Program;
import com.nickstephen.gamelib.opengl.widget.IOnClickL;
import com.nickstephen.gamelib.opengl.widget.ITouchL;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public abstract class Shape implements ITouchL {
    protected float[] mColour = new float[4];
    protected final Program mProgram;

    private boolean mClickable = true;
    private IOnClickL mOnClickListener;

    // Positions
    /**
     * The baseline X position relative to the container
     */
    private float mBaseX;
    /**
     * The baseline Y position relative to the container
     */
    private float mBaseY;
    /**
     * The rough distance to the left side of the shape
     */
    private float mLeft;
    /**
     * The rough distance to the right side of the shape
     */
    private float mRight;
    /**
     * The rough distance to the top of the shape
     */
    private float mUp;
    /**
     * The rough distance to the bottom of the shape
     */
    private float mDown;

    public Shape() {
        mProgram = new GenericProgram();
        mProgram.init();
    }

    public Shape(Program program) {
        mProgram = program;
        if (!mProgram.isInitialized()) {
            mProgram.init();
        }
    }

    public void move(float dx, float dy) {
        mBaseX += dx;
        mBaseY += dy;
    }

    public void moveTo(float newX, float newY) {
        mBaseX = newX;
        mBaseY = newY;
    }

    public void resize(float ratio) {
        mLeft *= ratio;
        mRight *= ratio;
        mUp *= ratio;
        mDown *= ratio;
    }

    public void setColour(float r, float g, float b, float a) {
        mColour[0] = r;
        mColour[1] = g;
        mColour[2] = b;
        mColour[3] = a;
    }

    public void setColour(float[] colour) {
        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be of length 4");
        }
        mColour = colour;
    }

    public void setSize(float radius) {
        mLeft = mRight = mUp = mDown = radius;
    }

    public void setSize(float left, float right, float up, float down) {
        mLeft = left;
        mRight = right;
        mUp = up;
        mDown = down;
    }

    @Override
    public boolean withinBounds(float posX, float posY) {
        float diff;
        if ((diff = mBaseX - posX) < 0.0f) {
            if (diff > mRight) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > mLeft) {
                return false;
            }
        }

        if ((diff = mBaseY - posY) < 0.0f) {
            if (diff > mUp) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > mDown) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, float relativePosX, float relativePosY) {
        if (!mClickable || withinBounds(relativePosX, relativePosY)) {
            return false;
        }

        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
        }
        return true;
    }

    public void setClickable(boolean val) {
        mClickable = val;
    }

    public void setClickListener(IOnClickL listener) {
        if (listener != null) {
            mClickable = true;
        }
        mOnClickListener = listener;
    }

    public boolean performClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    public float getX() {
        return mBaseX;
    }

    public float getY() {
        return mBaseY;
    }

    public abstract void draw(float[] VPMatrix);
}
