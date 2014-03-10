package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 6/03/14.
 * A class for drawing generic regular concave polygons (triangle, square, circle)
 */
public class Polygon extends Shape {
    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private static final float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    private float mRadius;
    private float mAngle;

    private float mTanFactor;
    private float mRadialFactor;

    private final int mSideCount;

    public Polygon(Context context, Container parent, float posX, float posY, float radius, float angle, int numberOfSides, float[] colour) {
        super(context, parent);

        mRadius = radius;
        setSize(radius);

        mSideCount = numberOfSides;
        float theta = 2.0f * 3.14159f / (float) mSideCount;
        mTanFactor = (float)Math.tan(theta);
        mRadialFactor = (float)Math.cos(theta);

        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be 4 long");
        }
        mColour = colour;

        mVertices = new Vertices(this, mSideCount + 1, mSideCount * 3, GLES20.GL_TRIANGLE_FAN);

        moveTo(posX, posY, angle);

        short[] order = new short[3 * mSideCount];
        for (int i = 0; i < mSideCount; i++) {
            order[3 * i] = 0;
            order[3 * i + 1] = (short)(i + 1);
            order[3 * i + 2] = (i + 2 <= mSideCount) ? (short)(i + 2) : 1;
        }
        mVertices.setIndices(order, 0, 3 * mSideCount);
    }

    public Polygon(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, int numberOfSides) {
        this(context, parent, posX, posY, radius, 0, numberOfSides, color);
    }

    public Polygon(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle, int numberOfSides) {
        this(context, parent, posX, posY, radius, angle, numberOfSides, color);
    }

    @Override
    public void moveTo(float newCenterX, float newCenterY) {
        super.moveTo(newCenterX, newCenterY);

        // Calculate the vertex positions
        float x = (float)Math.cos(mAngle) * mRadius;
        float y = (float)Math.sin(mAngle) * mRadius;
        float[] buff = new float[COORDS_PER_VERTEX * (mSideCount + 1)];

        buff[0] = newCenterX;
        buff[1] = newCenterY;
        for (int i = 1; i < mSideCount + 1; i++) {
            buff[COORDS_PER_VERTEX * i] = x + newCenterX;
            buff[COORDS_PER_VERTEX * i + 1] = y + newCenterY;

            float tx = -y;
            float ty = x;

            x += tx * mTanFactor;
            y += ty * mTanFactor;

            x *= mRadialFactor;
            y *= mRadialFactor;
        }
        mVertices.setVertices(buff);
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
     * @param vpMatrix - The Model View Project matrix in which to draw
     * this shape.
     */
    public void draw(float[] vpMatrix) {
        mVertices.draw(vpMatrix);
    }
}
