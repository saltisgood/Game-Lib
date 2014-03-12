package com.nickstephen.gamelib.anim;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.run.GameLoop;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 12/03/14.
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

    public Animation(@NotNull Shape shape) {
        mShape = shape;
    }

    public long getLoopDuration() {
        return mLoopDuration;
    }

    public Animation setLoopDuration(long duration) {
        mLoopDuration = duration;
        return this;
    }

    public long getTotalDuration() {
        return mTotalDuration;
    }

    public Animation setTotalDuration(long duration) {
        mTotalDuration = duration;
        return this;
    }

    public boolean isFinishing(long now) {
        if (mLoop) {
            return false;
        }

        return (mStart + mLoopDuration >= now);
    }

    public boolean getLoop() {
        return mLoop;
    }

    public Animation setLoop(boolean loop) {
        mLoop = loop;
        return this;
    }

    public LoopStyle getLoopingStyle() {
        return mOnLoopStyle;
    }

    public Animation setLoopingStyle(LoopStyle style) {
        mOnLoopStyle = style;
        return this;
    }

    public Animation infiniteLoop() {
        mIsInfiniteLoop = true;
        return this;
    }

    public void start() {
        mStart = System.currentTimeMillis();
        GameLoop.getInstanceUnsafe().addAnimation(this);
    }

    public void start(long delay) {
        mStart = System.currentTimeMillis() + delay;
        GameLoop.getInstanceUnsafe().addAnimation(this);
    }

    public  void onUpdate(long now) {
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

    public void onFinish(long now) {
        mIsRunning = false;
    }

    public void onLoop() {
        mStart = System.currentTimeMillis();

        if (mOnLoopStyle == LoopStyle.REVERSE) {
            mForwardAnim = !mForwardAnim;
            mProgress = 1.0f;
        } else {
            mProgress = 0.0f;
        }
    }

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

    public enum Interpolation {
        LINEAR
    }

    public enum LoopStyle {
        RESTART, REVERSE
    }
}
