package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.lib.VersionControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class Container extends Shape {
    private static final int INVALID_POINTER = -1;

    protected final List<Shape> mChildren;
    protected final List<Container> mChildContainers;

    private float mScreenWidth;
    private float mScreenHeight;
    private float mBoundsWidth;
    private float mBoundsHeight;
    private boolean mInifiniteBounds = true;

    private float mParentOffsetX;
    private float mParentOffsetY;

    private int mBottom;
    private int mLeft;

    private final FloatBuffer mVertexBuffer;

    private boolean mIsScrollable = false;
    private boolean mIsBeingDragged = false;
    private int mActivePointerId = INVALID_POINTER;
    private float mLastMotionY;
    private float mLastMotionX;

    public Container(@NotNull Context context, @Nullable Container parent, float width, float height, float parentOffsetX, float parentOffsetY) {
        this(context, parent, width, height, 0.0f, 0.0f, parentOffsetX, parentOffsetY);
    }

    public Container(@NotNull Context context, @Nullable Container parent, float width, float height, float startingPosX, float startingPosY, float parentOffsetX, float parentOffsetY) {
        super(context, parent);

        mChildren = new ArrayList<Shape>();
        mChildContainers = new ArrayList<Container>();

        ByteBuffer bb = ByteBuffer.allocateDirect(3 * 4 * 4); // 3 coords/vertex * 4 vertices * 4 bytes/float
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();

        setParentOffset(parentOffsetX, parentOffsetY);

        setScreenSize(width, height);

        moveTo(startingPosX, startingPosY);

        mColour = new float[]{ 1.0f, 0, 0, 1.0f};
    }

    public void setScreenSize(float width, float height) {
        mScreenWidth = width;
        mScreenHeight = height;

        float[] buff = new float[3 * 4];
        Arrays.fill(buff, 0);

        buff[0] = -width / 2.0f;
        buff[1] = height / 2.0f;
        buff[3] = width / 2.0f;
        buff[4] = height / 2.0f;
        buff[6] = width / 2.0f;
        buff[7] = -height / 2.0f;
        buff[9] = -width / 2.0f;
        buff[10] = -height / 2.0f;

        mVertexBuffer.put(buff);
        mVertexBuffer.position(0);

        if (mBoundsWidth < mScreenWidth) {
            mBoundsWidth = mScreenWidth;
        }
        if (mBoundsHeight < mScreenHeight) {
            mBoundsHeight = mScreenHeight;
        }

        if (getParent() != null) {
            mBottom = (int)((getParent().getScreenHeight() / 2.0f) + mParentOffsetY - (getScreenHeight() / 2.0f));
            mLeft = (int)((getParent().getScreenWidth() / 2.0f) + mParentOffsetX - (getScreenWidth() / 2.0f));
        }
    }

    private void setParentOffset(float x, float y) {
        mParentOffsetX = x;
        mParentOffsetY = y;
    }

    public void setBoundsSize(float width, float height) {
        mInifiniteBounds = false;
        mBoundsWidth = width;
        mBoundsHeight = height;
    }

    public void setUnlimitedBounds(boolean val) {
        mInifiniteBounds = val;
    }

    public boolean isScrollable() {
        return mIsScrollable;
    }

    public void setScrollable(boolean val) {
        mIsScrollable = val;
    }

    public void draw(float[] projMatrix, float[] viewMatrix) {
        float[] scratch = new float[16];
        //Matrix.translateM(scratch, 0, viewMatrix, 0, this.getX() + mParentOffsetX, this.getY() + mParentOffsetY, 0);
        Matrix.translateM(scratch, 0, viewMatrix, 0, mParentOffsetX, mParentOffsetY, 0);
        float[] vpMatrix = new float[16];
        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, scratch, 0);
        draw(vpMatrix);

        Matrix.translateM(scratch, 0, viewMatrix, 0, this.getX() + mParentOffsetX, this.getY() + mParentOffsetY, 0);

        for (Container c : mChildContainers) {
            GLES20.glScissor((854 / 2) - 270 - 100, (480 / 2) + 100 - 100, 200, 200);
            c.draw(projMatrix, scratch);
        }

        Matrix.multiplyMM(vpMatrix, 0, projMatrix, 0, scratch, 0);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(getAbsoluteBLCornerX(), getAbsoluteBLCornerY(), (int)mScreenWidth, (int)mScreenHeight);
        for (Shape shape : mChildren) {
            shape.draw(vpMatrix);
        }
        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    @Override
    public void draw(float[] mvpMatrix) {
        if (VersionControl.IS_RELEASE) { // Only draw bounding box in Debug mode
            return;
        }

        GLES20.glUseProgram(mProgram.getHandle()); // Add program to OpenGL environment
        // Get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram.getHandle(), "a_Position");
        // Enable a handle to the circle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the circle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);

        // Get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_Color");

        // Set color for drawing circle
        GLES20.glUniform4fv(mColorHandle, 1, mColour, 0);

        // Get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_MVPMatrix");
        Utilities.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        Utilities.checkGlError("glUniformMatrix4fv");

        // Draw the box
        GLES20.glLineWidth(5.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);
        GLES20.glLineWidth(1.0f);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, float relX, float relY) {
        if (!withinBounds(relX, relY)) {
            mIsBeingDragged = false;
            return false;
        }

        relX -= mParentOffsetX;
        relY -= mParentOffsetY;

        if (!onInterceptTouchEvent(e, relX - this.getX(), relY - this.getY())) {
            for (Shape shape : mChildren) {
                if (shape.onTouchEvent(e, relX - this.getX(), relY - this.getY())) {
                    return true;
                }
            }
            for (Container c : mChildContainers) {
                if (c.onTouchEvent(e, relX - this.getX(), relY - this.getY())) {
                    return true;
                }
            }
        }

        if (mIsScrollable) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    move(relX - mLastMotionX, relY - mLastMotionY);
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = relX;
                    mLastMotionY = relY;
                    break;
            }
            return true;
        }

        return false;
    }

    public boolean onTouchEvent(MotionEvent e) {
        if (this.getParent() != null) {
            throw new RuntimeException("This method should only be called from the root container");
        }

        float relX = e.getRawX() - (mScreenWidth / 2.0f);
        float relY = -(e.getRawY() - (mScreenHeight / 2.0f));

        return onTouchEvent(e, relX, relY);

        /* if (!onInterceptTouchEvent(e, relX - this.getX(), relY - this.getY())) {
            for (Shape shape : mChildren) {
                if (shape.onTouchEvent(e, relX - this.getX(), relY - this.getY())) {
                    return true;
                }
            }
            for (Shape c : mChildContainers) {
                if (c.onTouchEvent(e, relX - this.getX(), relY - this.getY())) {
                    return true;
                }
            }
        }

        if (mIsScrollable) {
            switch (e.getAction()) {
                case MotionEvent.ACTION_MOVE:
                    move(relX - mLastMotionX, relY - mLastMotionY);
                case MotionEvent.ACTION_DOWN:
                    mLastMotionX = relX;
                    mLastMotionY = relY;
                    break;
            }
            return true;
        }

        return false; */
    }

    public boolean onInterceptTouchEvent(MotionEvent e, float relX, float relY) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
        final int action = e.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        /*
         * Don't try to intercept touch if we can't scroll anyway.
         */
        if (!mIsScrollable) {
            return false;
        }

        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                float yDiff = Math.abs(relY - mLastMotionY);
                if (yDiff > mTouchSlop) {
                    mIsBeingDragged = true;
                }
                break;
            }

            case MotionEvent.ACTION_DOWN: {
                if (!inChild(relX, relY)) {
                    mIsBeingDragged = false;
                    break;
                }

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mActivePointerId = e.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                onSecondaryPointerUp(e);
                break;
        }

        /*
        * The only time we want to intercept motion events is if we are in the
        * drag mode.
        */
        return mIsBeingDragged;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
                MotionEvent.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = ev.getPointerId(pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) ev.getY(newPointerIndex);
            mActivePointerId = ev.getPointerId(newPointerIndex);
            /* if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            } */
        }
    }

    private boolean inChild(float x, float y) {
        for (Shape shape : mChildren) {
            if (shape.withinBounds(x, y)) {
                return true;
            }
        }
        for (Container c : mChildContainers) {
            if (c.withinBounds(x, y)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void move(float dx, float dy) {
        super.move(dx, dy);

        if (mInifiniteBounds) {
            return;
        }

        // If there are non-infinite bounds, make sure that the user can't drag outside those bounds

        float posX = this.getX();
        float posY = this.getY();
        float newX = posX;
        float newY = posY;

        if ((posX + (mScreenWidth / 2.0f)) - (mBoundsWidth / 2.0f) > 0.0f) {
            newX = (mBoundsWidth - mScreenWidth) / 2.0f;
        } else if ((posX - (mScreenWidth / 2.0f)) + (mBoundsWidth / 2.0f) < 0.0f) {
            newX = (mScreenWidth - mBoundsWidth) / 2.0f;
        }

        if ((posY + (mScreenHeight / 2.0f)) - (mBoundsHeight / 2.0f) > 0.0f) {
            newY = (mBoundsHeight - mScreenHeight) / 2.0f;
        } else if ((posY - (mScreenHeight / 2.0f)) + (mBoundsHeight / 2.0f) < 0.0f) {
            newY = (mScreenHeight - mBoundsHeight) / 2.0f;
        }

        this.moveTo(newX, newY);
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        if ((Math.abs(-posY + mParentOffsetY) - (mScreenHeight / 2.0f)) > touchSlop) {
            return false;
        }

        if ((Math.abs(-posX + mParentOffsetX) - (mScreenWidth / 2.0f)) > touchSlop) {
            return false;
        }

        return true;
    }

    public int getAbsoluteBLCornerX() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getAbsoluteBLCornerX() + mLeft;
    }

    public int getAbsoluteBLCornerY() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getAbsoluteBLCornerY() + mBottom;
    }

    public float getScreenHeight() {
        return mScreenHeight;
    }

    public float getScreenWidth() {
        return mScreenWidth;
    }
}
