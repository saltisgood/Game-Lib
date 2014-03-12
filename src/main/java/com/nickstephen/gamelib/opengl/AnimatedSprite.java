package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.graphics.Bitmap;

import com.nickstephen.gamelib.anim.SpriteAnimation;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class AnimatedSprite extends Sprite {
    protected int mNumFrames;
    protected int mCurrentFrame;

    public AnimatedSprite(@NotNull Context context, @NotNull Container parent, @NotNull String textureFile,
                          float width, float height, int numSpritesX, int numSpritesY) {
        super(context, parent, textureFile, width, height, numSpritesX, numSpritesY);

        mNumFrames = numSpritesX * numSpritesY;
    }

    @Override
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

    public void nextFrame(float percentage) {
        int nextFrame = (int)(percentage * mNumFrames);
        if (nextFrame >= (mNumFrames)) {
            nextFrame = mNumFrames - 1;
        }

        if (nextFrame != mCurrentFrame) {
            mCurrentFrame = nextFrame;
            setTextureCoords(mTextureRegion[mCurrentFrame]);
        }
    }

    public int getNumFrames() {
        return mNumFrames;
    }

    public void setupAnimation() {
        new SpriteAnimation(this).setLoop(true).infiniteLoop().start();
    }
}
