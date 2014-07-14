package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.bounds.Quadrilateral;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class Sprite extends TexturedShape {
    protected static final int NUM_SIDES = 4; 
    
    public Sprite(@NotNull Context context, @NotNull Container parent, @NotNull String textureFile, float width, float height) {
        this(context, parent, textureFile, width, height, 1, 1);
    }

    protected Sprite(@NotNull Context context, @NotNull Container parent, @NotNull String textureFile,
                     float width, float height, int spritesX, int spritesY) {
        super(context, parent, Program.SpriteProgram.create());

        mVertices = new Vertices(this, 4, 6, GLES20.GL_TRIANGLES);
        mBoundsChecker = new Quadrilateral(this).setWidth(width).setHeight(height);

        setTextureName(textureFile);
        setTextureDimensions(spritesX, spritesY);

        setIndices();
        setVertices();
    }

    protected void setIndices() {
        short[] indices = new short[6];

        indices[1] = 2;
        indices[2] = 1;
        indices[4] = 3;
        indices[5] = 2;

        mVertices.setIndices(indices, 0, 6);
    }

    protected void setVertices() {
        float[] vertexMatrix = new float[NUM_SIDES * Vertices.POSITION_CNT_2D];

        vertexMatrix[0] = mBoundsChecker.getWidth() / -2.0f;
        vertexMatrix[1] = mBoundsChecker.getHeight() / 2.0f;

        vertexMatrix[2] = mBoundsChecker.getWidth() / 2.0f;
        vertexMatrix[3] = mBoundsChecker.getHeight() / 2.0f;

        vertexMatrix[4] = mBoundsChecker.getWidth() / 2.0f;
        vertexMatrix[5] = mBoundsChecker.getHeight() / -2.0f;

        vertexMatrix[6] = mBoundsChecker.getWidth() / -2.0f;
        vertexMatrix[7] = mBoundsChecker.getHeight() / -2.0f;

        mVertices.setVertices(vertexMatrix);
    }

    @Override
    protected void setTextureCoords(TextureRegion region) {
        float[] coords = mVertices.getTextureCoords();

        coords[0] = region.u1;        // Add U for Vertex 0
        coords[1] = region.v1;        // Add V for Vertex 0
        coords[2] = region.u2;        // Add U for Vertex 1
        coords[3] = region.v1;        // Add V for Vertex 1
        coords[4] = region.u2;        // Add U for Vertex 2
        coords[5] = region.v2;        // Add V for Vertex 2
        coords[6] = region.u1;        // Add U for Vertex 3
        coords[7] = region.v2;          // Add V for Vertex 3

        mVertices.resetFloatBuffer();
    }
}
