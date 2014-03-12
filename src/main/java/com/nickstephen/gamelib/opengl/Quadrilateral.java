package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>A simple extension of Shape that is meant for general quadrilaterals that will take care of their
 * own drawing, i.e. doesn't rely on {@link com.nickstephen.gamelib.opengl.Polygon}'s inbuilt drawing.
 * It does have a very simple outline drawing but any extensions will probably not need it and should
 * override {@link #moveTo(float, float)} to stop any overwrites to {@link #mVertices}.</p>
 *
 * <p>This is mainly useful for its {@link #withinBounds(float, float, float)} method that easily
 * checks for touch events within itself.</p>
 * @author Nick Stephen
 */
public class Quadrilateral extends Shape {
    public static final int NUM_SIDES = 4;

    private float mHeight;
    private float mWidth;

    /**
     * Default constructor.
     * @param context A context
     * @param parent A parent to this shape
     * @param x The x position of this shape within its parent
     * @param y The y position of this shape within its parent
     * @param width The width of the new shape
     * @param height The height of the new shape
     */
    public Quadrilateral(@NotNull Context context, @Nullable Container parent, float x, float y, float width, float height) {
        super(context, parent);

        mWidth = width;
        mHeight = height;

        this.moveTo(x, y);

        setupVertices();
        setVertices();
    }

    public Quadrilateral(@NotNull Context context, @Nullable Container parent, @NotNull Program program,
                         float x, float y, float width, float height) {
        super(context, parent, program);

        mWidth = width;
        mHeight = height;

        this.moveTo(x, y);

        setupVertices();
        setVertices();
    }

    /**
     * Check whether a position is within the bounds of this container (as shown on the screen)
     *
     * @param posX      The x position (relative to the centre of the parent)
     * @param posY      The y position (relative to the centre of the parent)
     * @param touchSlop The amount of leeway a user has for exiting the bounds
     * @return True if inside (or nearly inside) the container, false otherwise
     */
    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        if (Math.abs(posX - getX()) > (mWidth + touchSlop)) {
            return false;
        } else if (Math.abs(posY - getY()) > (mHeight + touchSlop)) {
            return false;
        }
        return true;
    }

    protected void setupVertices() {
        mVertices = new Vertices(this, NUM_SIDES, 0, GLES20.GL_LINE_LOOP);
    }

    protected void setVertices() {
        float[] vertexMatrix = new float[NUM_SIDES * Vertices.POSITION_CNT_2D];

        vertexMatrix[0] = mWidth / -2.0f;
        vertexMatrix[1] = mHeight / 2.0f;

        vertexMatrix[2] = mWidth / 2.0f;
        vertexMatrix[3] = mHeight / 2.0f;

        vertexMatrix[4] = mWidth / 2.0f;
        vertexMatrix[5] = mHeight / -2.0f;

        vertexMatrix[6] = mWidth / -2.0f;
        vertexMatrix[7] = mHeight / -2.0f;

        mVertices.setVertices(vertexMatrix);
    }
}
