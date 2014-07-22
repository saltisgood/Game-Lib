package com.nickstephen.gamelib.opengl.shapes;

import android.content.Context;

import com.nickstephen.gamelib.anim.SpriteAnimation;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.shapes.Sprite;
import com.nickstephen.gamelib.opengl.textures.TextureRegion;

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

    public void nextFrame(float percentage) {
        int nextFrame = (int)(percentage * mNumFrames);
        if (nextFrame >= (mNumFrames)) {
            nextFrame = mNumFrames - 1;
        }

        if (nextFrame != mCurrentFrame) {
            mCurrentFrame = nextFrame;
            setTextureCoords(mTexture.getTexRegions()[mCurrentFrame]);
        }
    }

    public void gotoFrame(int frame) {
        if (frame >= 0 && frame < mNumFrames && frame != mCurrentFrame) {
            mCurrentFrame = frame;

            if (mTexture.getTexRegions() != null) {
                setTextureCoords(mTexture.getTexRegions()[mCurrentFrame]);
            }
        }
    }

    @Override
    public void setTextureCoords(TextureRegion region) {
        super.setTextureCoords(mTexture.getTexRegions()[mCurrentFrame]);
    }

    public int getNumFrames() {
        return mNumFrames;
    }

    public void setupAnimation() {
        new SpriteAnimation(this).setLoop(true).infiniteLoop().start();
    }
}
