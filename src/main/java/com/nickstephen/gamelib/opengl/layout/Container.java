package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.lib.VersionControl;

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
    public static final float SIZE_UNLIMITED = -1.0f;

    protected final List<Shape> mChildren;

    private float mPosX;
    private float mPosY;

    private float mWidth;
    private float mHeight;

    private final FloatBuffer mVertexBuffer;

    private boolean mIsScrollable = true;

    public Container(Context context, float width, float height) {
        this(context, width, height, 0.0f, 0.0f);
    }

    public Container(Context context, float width, float height, float startingPosX, float startingPosY) {
        super(context);

        mChildren = new ArrayList<Shape>();

        ByteBuffer bb = ByteBuffer.allocateDirect(3 * 4 * 4); // 3 coords/vertex * 4 vertices * 4 bytes/float
        bb.order(ByteOrder.nativeOrder());
        mVertexBuffer = bb.asFloatBuffer();

        setSize(width, height);

        mPosX = startingPosX;
        mPosY = startingPosY;

        mColour = new float[]{ 1.0f, 0, 0, 1.0f};
    }

    public void setSize(float width, float height) {
        mWidth = width;
        mHeight = height;

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
    }

    public boolean isScrollable() {
        return mIsScrollable;
    }

    public void setScrollable(boolean val) {
        mIsScrollable = val;
    }

    @Override
    public void draw(float[] VPMatrix) {
        for (Shape shape : mChildren) {
            shape.draw(VPMatrix);
        }

        if (VersionControl.IS_RELEASE) {
            return;
        }

        GLES20.glUseProgram(mProgram.getHandle()); // Add program to OpenGL environment
        // Get handle to vertex shader's vPosition member
        int mPositionHandle = GLES20.glGetAttribLocation(mProgram.getHandle(), "vPosition");
        // Enable a handle to the circle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the circle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, 3,
                GLES20.GL_FLOAT, false, 3 * 4, mVertexBuffer);

        // Get handle to fragment shader's vColor member
        int mColorHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "vColor");

        // Set color for drawing circle
        GLES20.glUniform4fv(mColorHandle, 1, mColour, 0);

        // Get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "uMVPMatrix");
        Utilities.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, VPMatrix, 0);
        Utilities.checkGlError("glUniformMatrix4fv");

        // Draw the box
        GLES20.glLineWidth(5.0f);
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);
        GLES20.glLineWidth(1.0f);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }


    public boolean onTouchEvent(MotionEvent e) {
        if (this.getParent() != null) {
            throw new RuntimeException("This method should only be called from the root container");
        }

        float relX = e.getRawX() - (mWidth / 2.0f);
        float relY = -(e.getRawY() - (mHeight / 2.0f));

        for (Shape shape : mChildren) {
            if (shape.onTouchEvent(e, relX, relY)) {
                return true;
            }
        }

        if (mIsScrollable) {
            //TODO: Implement
        }

        return false;
    }
}
