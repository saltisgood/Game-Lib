package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.SpriteProgram;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class Sprite extends Quadrilateral {
    protected TextureRegion[] mTextureRegion;

    public Sprite(@NotNull Context context, @NotNull Container parent, @NotNull String textureFile, float width, float height) {
        this(context, parent, textureFile, width, height, 1, 1);
    }

    protected Sprite(@NotNull Context context, @NotNull Container parent, @NotNull String textureFile,
                     float width, float height, int spritesX, int spritesY) {
        super(context, parent, new SpriteProgram(), 0, 0, width, height);

        AssetManager assets = context.getAssets();
        InputStream is;
        try {
            is = assets.open(textureFile);
        } catch (IOException e) {
            return;
        }

        Bitmap raw = BitmapFactory.decodeStream(is);

        try {
            is.close();
        } catch (IOException e) {
            return;
        }

        setTextureId(TextureHelper.loadTexture(raw));

        mTextureRegion = setupTextureRegion(raw, spritesX, spritesY);
        raw.recycle();

        setTextureCoords(mTextureRegion[0]);
        setIndices();
    }

    protected @NotNull TextureRegion[] setupTextureRegion(@NotNull Bitmap bitmap, int spritesX, int spritesY) {
        TextureRegion[] region = new TextureRegion[1];
        region[0] = new TextureRegion(bitmap.getWidth(), bitmap.getHeight(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        return region;
    }

    @Override
    protected void setupVertices() {
        mVertices = new Vertices(this, 4, 6, GLES20.GL_TRIANGLES);
    }

    protected void setIndices() {
        short[] indices = new short[6];

        indices[1] = 2;
        indices[2] = 1;
        indices[4] = 3;
        indices[5] = 2;

        mVertices.setIndices(indices, 0, 6);
    }

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
