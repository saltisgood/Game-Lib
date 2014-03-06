package com.nickstephen.gamelib.opengl.layout;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.lib.VersionControl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Arrays;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class Container extends Shape {
    public static final float SIZE_UNLIMITED = -1.0f;

    private float mPosX;
    private float mPosY;

    private float mWidth;
    private float mHeight;

    private final FloatBuffer mVertexBuffer;

    private boolean mIsScrollable = true;

    public Container(float width, float height) {
        this(width, height, 0.0f, 0.0f);
    }

    public Container(float width, float height, float startingPosX, float startingPosY) {
        super();

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
        GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, 4);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
