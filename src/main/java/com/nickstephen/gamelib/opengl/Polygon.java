package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.program.GenericProgram;
import com.nickstephen.gamelib.opengl.program.Program;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Nick Stephen on 6/03/14.
 * A class for drawing generic regular concave polygons (triangle, square, circle)
 */
public class Polygon extends Shape {
    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;

    private static final float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawListBuffer;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private float mRadius;
    private float mAngle;

    private float mTheta;
    private float mTanFactor;
    private float mRadialFactor;

    private final int mSideCount;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public Polygon(Context context, float posX, float posY, float radius, float angle, int numberOfSides, float[] colour) {
        super(context);

        mRadius = radius;
        setSize(radius);

        mSideCount = numberOfSides;
        mTheta = 2.0f * 3.14159f / (float)mSideCount;
        mTanFactor = (float)Math.tan(mTheta);
        mRadialFactor = (float)Math.cos(mTheta);

        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be 4 long");
        }
        mColour = colour;

        // Initialise vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect((3 * (mSideCount + 1)) * 4); // Number of vertices * 4 bytes per float
        // Use the device's native byte order
        bb.order(ByteOrder.nativeOrder());

        // Create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();

        moveTo(posX, posY, angle);

        // initialize byte buffer for the draw list
        // (Number of segments * 3 lines per triangle * 2 bytes per short)
        ByteBuffer dlb = ByteBuffer.allocateDirect(mSideCount * 3 * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();

        short[] order = new short[3 * mSideCount];
        for (int i = 0; i < mSideCount; i++) {
            order[3 * i] = 0;
            order[3 * i + 1] = (short)(i + 1);
            order[3 * i + 2] = (i + 2 <= mSideCount) ? (short)(i + 2) : 1;
        }
        mDrawListBuffer.put(order);
        mDrawListBuffer.position(0);
    }

    public Polygon(Context context, float posX, float posY, float radius, int numberOfSides) {
        this(context, posX, posY, radius, 0, numberOfSides, color);
    }

    public Polygon(Context context, float posX, float posY, float radius, float angle, int numberOfSides) {
        this(context, posX, posY, radius, angle, numberOfSides, color);
    }

    @Override
    public void moveTo(float newCenterX, float newCenterY) {
        super.moveTo(newCenterX, newCenterY);

        // Calculate the vertex positions
        float x = (float)Math.cos(mAngle) * mRadius;
        float y = (float)Math.sin(mAngle) * mRadius;
        float[] buff = new float[3 * (mSideCount + 1)];

        buff[0] = newCenterX;
        buff[1] = newCenterY;
        buff[2] = 0;
        for (int i = 1; i < mSideCount + 1; i++) {
            buff[3 * i] = x + newCenterX;
            buff[3 * i + 1] = y + newCenterY;
            buff[3 * i + 2] = 0;

            float tx = -y;
            float ty = x;

            x += tx * mTanFactor;
            y += ty * mTanFactor;

            x *= mRadialFactor;
            y *= mRadialFactor;
        }
        // Add the coordinates to the FloatBuffer
        mVertexBuffer.put(buff);
        // Reset the buffer's position back to the first coordinate
        mVertexBuffer.position(0);
    }

    public void moveTo(float newCenterX, float newCenterY, float newAngle) {
        mAngle = (float) Math.toRadians(newAngle);
        moveTo(newCenterX, newCenterY);
    }

    public void rotate(float newAngle) {
        mAngle = (float) Math.toRadians(newAngle);
        move(this.getX(), this.getY());
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram.getHandle()); // Add program to OpenGL environment
        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram.getHandle(), "vPosition");
        // Enable a handle to the circle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the circle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        // Get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "vColor");

        // Set color for drawing circle
        GLES20.glUniform4fv(mColorHandle, 1, mColour, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "uMVPMatrix");
        Utilities.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        Utilities.checkGlError("glUniformMatrix4fv");

        // Draw the circle
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, NUM_SEGMENTS);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 3 * mSideCount, GLES20.GL_UNSIGNED_SHORT,
                mDrawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
