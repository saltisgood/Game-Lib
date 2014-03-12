package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.AnimatedSprite;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class SpriteAnimation extends Animation {
    protected final int mNumFrames;

    public SpriteAnimation(@NotNull AnimatedSprite shape) {
        super(shape);

        mNumFrames = shape.getNumFrames();
    }

    @Override
    public void onUpdate(long now) {
        super.onUpdate(now);

        ((AnimatedSprite)mShape).nextFrame(mProgress);
    }


}
