package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.run.GameLoop;

import org.jetbrains.annotations.NotNull;

/**
 * <p>The base class for all Animations. Animations are slightly different to the Android Framework
 * version in that they don't do their work on the main thread, but instead work on the game thread
 * and are updated at every tick.</p>
 *
 * <p>Note that whilst only one shape is associated with any animation, many animations can be
 * performed at once on a shape.</p>
 *
 * <p>Whilst sub-classes should extend the methods {@link #onLoop()}, {@link #onFinish(long)} and
 * {@link #onUpdate(long)} with their own implementations, they should also call through to the
 * original method first to update the fields with the new info.</p>
 * @author Nick Stephen
 */
public abstract class Animation {
    public static final long DEFAULT_ANIM_DUR = 1000L;

    protected final Shape mShape;

    protected long mStart;
    protected long mLoopDuration = DEFAULT_ANIM_DUR;
    protected long mTotalDuration = DEFAULT_ANIM_DUR;
    protected boolean mLoop = false;
    protected boolean mIsRunning = false;
    protected Interpolation mInterpol = Interpolation.LINEAR;
    protected boolean mRevertOnFinish = false;
    protected float mProgress;
    protected boolean mIsInfiniteLoop = false;
    protected LoopStyle mOnLoopStyle = LoopStyle.RESTART;

    private boolean mForwardAnim = true;
    private IOnAnimationEnd mOnAnimationEndL;

    /**
     * Construct the animation and associate it with a shape.
     * @param shape
     */
    public Animation(@NotNull Shape shape) {
        mShape = shape;
    }

    public Animation setAnimationEndListener(IOnAnimationEnd listener) {
        mOnAnimationEndL = listener;
        return this;
    }

    /**
     * Get the duration (millis) of the loop. This is only relevant if the animation is set to loop.
     * Note that looping animations are interpolated based on the their loop duration, whilst
     * non-looping animations are interpolated based on their total duration.
     * @return The time length of the loop (millis)
     */
    public long getLoopDuration() {
        return mLoopDuration;
    }

    /**
     * Set the duration (millis) of the loop. See {@link #getLoopDuration()} for more info.
     * @param duration The time length of the loop (millis)
     * @return This instance, for method chaining.
     */
    public Animation setLoopDuration(long duration) {
        mLoopDuration = duration;
        return this;
    }

    /**
     * Get the total duration of the animation. This is only relevant if the animation is not set
     * to infinite loop.
     * @return The total time length of the animation (millis)
     */
    public long getTotalDuration() {
        return mTotalDuration;
    }

    /**
     * Set the total duration of the animation. This is only relevant if the animation is not set
     * to infinite loop.
     * @param duration The total time length of the animation (millis)
     * @return This instance, for method chaining.
     */
    public Animation setTotalDuration(long duration) {
        mTotalDuration = duration;
        return this;
    }

    /**
     * Get whether the animation will loop if it reaches the end of its loop duration before its
     * total duration.
     * @return True if it loops, false otherwise
     */
    public boolean getLoop() {
        return mLoop;
    }

    /**
     * Set whether the animation will loop if it reaches the end of its loop duration before its
     * total duration.
     * @param loop True to loop, false otherwise
     * @return This instance, for method chaining
     */
    public Animation setLoop(boolean loop) {
        mLoop = loop;
        return this;
    }

    /**
     * Get the style of looping that will occur. Note that this only applies if the animation is
     * set to loop.
     * @return The style of looping
     * @see com.nickstephen.gamelib.anim.Animation.LoopStyle
     */
    public LoopStyle getLoopingStyle() {
        return mOnLoopStyle;
    }

    /**
     * Set the style of looping that will occur. Note that this only applies if the animation is
     * set to loop.
     * @param style The style of looping
     * @return This instance, for method chaining
     * @see com.nickstephen.gamelib.anim.Animation.LoopStyle
     */
    public Animation setLoopingStyle(LoopStyle style) {
        mOnLoopStyle = style;
        return this;
    }

    /**
     * Set this animation to run in an infinite loop. Use with caution as it will continue to run
     * forever unless you keep a check on it.
     * @return This instance, for method chaining
     */
    public Animation infiniteLoop() {
        mLoop = true;
        mIsInfiniteLoop = true;
        return this;
    }

    /**
     * Get the action to be performed upon finishing the animation. True means the animation will
     * revert to its starting state upon finishing, false means it will not change on finish.
     * @return True to revert upon finish, false otherwise
     */
    public boolean getRevertOnFinish() {
        return mRevertOnFinish;
    }

    /**
     * Set the action to be performed upon finishing the animation. True means the animation will
     * revert to its starting state upon finishing, false means it will not change on finish.
     * @param val True to revert upon finish, false otherwise
     * @return This instance, for method chaining
     */
    public Animation setRevertOnFinish(boolean val) {
        mRevertOnFinish = val;
        return this;
    }

    /**
     * Start the animation. It sets the start time to be now and adds it to the list of animations
     * in the {@link com.nickstephen.gamelib.run.GameLoop}
     */
    public void start() {
        mStart = System.currentTimeMillis();
        GameLoop.getInstanceUnsafe().addAnimation(this);
    }

    /**
     * Start the animation with a delay. Note that the current implementation effectively starts
     * it immediately but it won't show progress until it reaches the start time. This has the
     * side effect of setting the shape to be in its starting animation state at the next tick, and
     * if this is different from its current state may look wrong.
     * @param delay The delay (millis) to start the animation
     */
    public void start(long delay) {
        mStart = System.currentTimeMillis() + delay;
        GameLoop.getInstanceUnsafe().addAnimation(this);
    }

    /**
     * Callback to update the animation at a certain point. Sub-classes should call through to this
     * method before performing any other actions as it will give them an updated {@link #mProgress}
     * value.
     * @param now The current system time (millis)
     */
    public void onUpdate(long now) {
        mIsRunning = true;

        if (mLoop) {
            mProgress = (float)(now - mStart) / (float) mLoopDuration;

            if (!mForwardAnim) {
                mProgress = 1.0f - mProgress;
            }
        } else {
            mProgress = (float)(now - mStart) / (float) mTotalDuration;
        }
        if (mProgress > 1.0f) {
            mProgress = 1.0f;
        } else if (mProgress < 0.0f) {
            mProgress = 0.0f;
        }
    }

    /**
     * Callback to notify the animation that it has finished and should perform any reverting actions
     * now. Sub-classes should call through to this method at some point.
     * @param now The current system time (millis)
     */
    public void onFinish(long now) {
        mIsRunning = false;

        if (mOnAnimationEndL != null) {
            mOnAnimationEndL.onAnimationEnd(mShape);
        }
    }

    /**
     * Callback to notify the animation that is has reached its loop point. Sub-classes should call
     * through to this method before performing any other actions as it will give them an update
     * {@link #mProgress} value. Most animations won't have to override this method.
     */
    public void onLoop() {
        mStart = System.currentTimeMillis();

        if (mOnLoopStyle == LoopStyle.REVERSE) {
            mForwardAnim = !mForwardAnim;
            mProgress = 1.0f;
        } else {
            mProgress = 0.0f;
        }
    }

    /**
     * Check whether the animation should finish at the time set as the argument.
     * @param now The current system time (millis)
     * @return True to exit the animation, false otherwise
     */
    public boolean shouldFinish(long now) {
        if (mLoop) {
            if (!mIsInfiniteLoop && (mStart + mTotalDuration >= now)) {
                onFinish(now);
                return true;
            }

            if ((mForwardAnim && mProgress == 1.0f) || (!mForwardAnim && mProgress == 0.0f)) {
                onLoop();
            }
            return false;
        }

        if (mProgress == 1.0f) {
            onFinish(now);
            return true;
        }
        return false;
    }

    public @NotNull Shape getShape() {
        return mShape;
    }

    /**
     * The interpolation style of the animation
     */
    public enum Interpolation {
        LINEAR
    }

    /**
     * The way in which the animation acts upon looping.
     */
    public enum LoopStyle {
        /**
         * The animation immediately returns to its starting value and then runs again.
         */
        RESTART,
        /**
         * The animation reverses and moves back to its starting state gradually.
         */
        REVERSE
    }
}
