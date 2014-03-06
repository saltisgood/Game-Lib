package com.nickstephen.gamelib.opengl;

import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class Circle {
    private static final int NUM_SEGMENTS = 100;
    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 3;

    private final String mVertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private final String mFragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    private final FloatBuffer mVertexBuffer;
    private final ShortBuffer mDrawListBuffer;
    private final int mProgram;
    private int mPositionHandle;
    private int mColorHandle;
    private int mMVPMatrixHandle;

    private float mRadius;
    private float mX;
    private float mY;

    private float mTheta;
    private float mTanFactor;
    private float mRadialFactor;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    public Circle(float posX, float posY, float radius) {
        mRadius = radius;
        mX = posX;
        mY = posY;

        mTheta = 2.0f * 3.14159f / (float)NUM_SEGMENTS;
        mTanFactor = (float)Math.tan(mTheta);
        mRadialFactor = (float)Math.cos(mTheta);

        // Initialise vertex byte buffer for shape coordinates
        ByteBuffer bb = ByteBuffer.allocateDirect((3 * (NUM_SEGMENTS + 1)) * 4); // Number of vertices * 4 bytes per float
        // Use the device's native byte order
        bb.order(ByteOrder.nativeOrder());

        // Create a floating point buffer from the ByteBuffer
        mVertexBuffer = bb.asFloatBuffer();

        // Calculate the vertex positions
        float x = radius;
        float y = 0;
        float[] buff = new float[3 * (NUM_SEGMENTS + 1)];

        buff[0] = posX;
        buff[1] = posY;
        buff[2] = 0;
        for (int i = 1; i < NUM_SEGMENTS + 1; i++) {
            buff[3 * i] = x + posX;
            buff[3 * i + 1] = y + posY;
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

        // initialize byte buffer for the draw list
        // (Number of segments * 3 lines per triangle * 2 bytes per short)
        ByteBuffer dlb = ByteBuffer.allocateDirect(NUM_SEGMENTS * 3 * 2);
        dlb.order(ByteOrder.nativeOrder());
        mDrawListBuffer = dlb.asShortBuffer();

        short[] order = new short[3 * NUM_SEGMENTS];
        for (int i = 0; i < NUM_SEGMENTS; i++) {
            order[3 * i] = 0;
            order[3 * i + 1] = (short)(i + 1);
            order[3 * i + 2] = (i + 2 <= NUM_SEGMENTS) ? (short)(i + 2) : 1;
        }
        mDrawListBuffer.put(order);
        mDrawListBuffer.position(0);

        // prepare shaders and OpenGL program
        int vertexShader = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        int fragmentShader = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);

        mProgram = GLES20.glCreateProgram(); // create empty OpenGL program
        GLES20.glAttachShader(mProgram, vertexShader); // add the vertex shader to the program
        GLES20.glAttachShader(mProgram, fragmentShader); // add the fragment shader to program
        GLES20.glLinkProgram(mProgram); // create OpenGL program executables
    }

    /**
     * Encapsulates the OpenGL ES instructions for drawing this shape.
     *
     * @param mvpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram); // Add program to OpenGL environment
        // Get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the circle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the circle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false, vertexStride, mVertexBuffer);

        // Get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing circle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // Get handle to shape's transformation matrix
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        Utilities.checkGlError("glGetUniformLocation");

        // Apply the projection and view transformation
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);
        Utilities.checkGlError("glUniformMatrix4fv");

        // Draw the circle
        //GLES20.glDrawArrays(GLES20.GL_LINE_LOOP, 0, NUM_SEGMENTS);
        GLES20.glDrawElements(GLES20.GL_TRIANGLE_FAN, 3 * NUM_SEGMENTS, GLES20.GL_UNSIGNED_SHORT,
                mDrawListBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
