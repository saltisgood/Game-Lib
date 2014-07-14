package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.Nullable;

import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Nick Stephen on 14/07/2014.
 */
public abstract class TexturedShape extends Shape {
    protected @NotNull TextureRegion[] setupTextureRegion(@NotNull Bitmap bitmap, int spritesX, int spritesY) {
        TextureRegion[] regions = new TextureRegion[spritesX * spritesY];

        float texWidth = bitmap.getWidth(), texHeight = bitmap.getHeight();
        float cellWidth = texWidth / (float) spritesX, cellHeight = texHeight / (float) spritesY;

        for (int j = 0, c = 0; j < spritesY; j++) {
            for (int i = 0; i < spritesX; i++) {
                regions[c++] = new TextureRegion(texWidth, texHeight, i * cellWidth, j * cellHeight,
                        cellWidth, cellHeight);
            }
        }

        return regions;
    }

    protected int mTextureId;
    private String mTextureName;
    protected TextureRegion[] mTextureRegions;
    private int mSpritesX;
    private int mSpritesY;

    public TexturedShape(@NotNull Context context, @Nullable Container parent) {
        super(context, parent);
    }

    public TexturedShape(@NotNull Context context, @Nullable Container parent, @NotNull Program program) {
        super(context, parent, program);
    }

    public TexturedShape setTextureDimensions(int x, int y) {
        mSpritesX = x;
        mSpritesY = y;

        return this;
    }

    public TexturedShape setTextureName(@NotNull String fileName) {
        mTextureName = fileName;
        return this;
    }

    /**
     * Get the texture id associated with this shape. Sub-classes aren't required to uses textures, this
     * is just a mechanism for allowing them to all have a texture id if necessary.
     * @return The texture id if it's set
     */
    public int getTextureId() {
        if (mTextureId == 0) {
            AssetManager assets = mContext.getAssets();
            InputStream is;
            try {
                is = assets.open(mTextureName);
            } catch (IOException e) {
                return 0;
            }

            Bitmap raw = BitmapFactory.decodeStream(is);

            try {
                is.close();
            } catch (IOException e) {
                return 0;
            }

            mTextureId = TextureHelper.loadTexture(raw);

            mTextureRegions = setupTextureRegion(raw, mSpritesX, mSpritesY);
            setTextureCoords(mTextureRegions[0]);
        }

        return mTextureId;
    }

    protected abstract void setTextureCoords(TextureRegion region);
}
