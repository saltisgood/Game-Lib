package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.shapes.AnimatedSprite;

import org.jetbrains.annotations.NotNull;

/**
 * An animation extension used for animating sprites.
 * @author Nick Stephen
 */
public class SpriteAnimation extends Animation {
    protected final int mNumFrames;

    /**
     * Constructor.
     * @param shape The sprite to animate
     */
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
