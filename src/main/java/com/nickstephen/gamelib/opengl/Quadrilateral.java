package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 11/03/14.
 */
public class Quadrilateral extends Shape {
    private float mWidth;
    private float mHeight;

    public Quadrilateral(@NotNull Context context, @Nullable Container parent, float x, float y, float width, float height) {
        super(context, parent);

        mWidth = width;
        mHeight = height;

        mVertices = new Vertices(this, 4, 0, GLES20.GL_LINE_LOOP);

        this.moveTo(x, y);
    }

    public Quadrilateral(@NotNull Context context, @Nullable Container parent, @NotNull Program program,
                         float x, float y, float width, float height) {
        super(context, parent, program);

        mWidth = width;
        mHeight = height;

        mVertices = new Vertices(this, 4, 0, GLES20.GL_LINE_LOOP);

        this.moveTo(x, y);
    }

    @Override
    public void moveTo(float newX, float newY) {
        super.moveTo(newX, newY);

        float[] vertexMatrix = new float[8];

        vertexMatrix[0] = getX() - (mWidth / 2.0f);
        vertexMatrix[1] = getY() + (mHeight / 2.0f);

        vertexMatrix[2] = getX() + (mWidth / 2.0f);
        vertexMatrix[3] = getY() + (mHeight / 2.0f);

        vertexMatrix[4] = getX() + (mWidth / 2.0f);
        vertexMatrix[5] = getY() - (mHeight / 2.0f);

        vertexMatrix[6] = getX() - (mWidth / 2.0f);
        vertexMatrix[7] = getY() - (mHeight / 2.0f);

        mVertices.setVertices(vertexMatrix);
    }

    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        if (Math.abs(posX - getX()) > (mWidth + touchSlop)) {
            return false;
        } else if (Math.abs(posY - getY()) > (mHeight + touchSlop)) {
            return false;
        }
        return true;
    }
}
