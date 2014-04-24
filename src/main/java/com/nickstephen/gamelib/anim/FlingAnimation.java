package com.nickstephen.gamelib.anim;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.widget.OverScroller;
import android.widget.Scroller;

import com.nickstephen.gamelib.opengl.Shape;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 25/04/2014.
 */
public class FlingAnimation extends Animation {
    private interface FlingAnimationImpl {
        public void startFling(int startX, int startY, int velX, int velY, int minX, int maxX,
                               int minY, int maxY, int overX, int overY);

        public void onUpdate(@NotNull Shape shape);

        public boolean isFinished();
    }

    private static class FlingAnimationImplBase implements FlingAnimationImpl {
        private Scroller mScroller;

        public FlingAnimationImplBase(@NotNull Context context) {
            mScroller = new Scroller(context);
        }

        @Override
        public void startFling(int startX, int startY, int velX, int velY, int minX, int maxX, int minY, int maxY,
                               int overX, int overY) {
            mScroller.fling(startX, startY, velX, velY, minX, maxX, minY, maxY);
        }

        @Override
        public void onUpdate(@NotNull Shape shape) {
            mScroller.computeScrollOffset();
            shape.moveTo(mScroller.getCurrX(), mScroller.getCurrY());
        }

        @Override
        public boolean isFinished() {
            return mScroller.isFinished();
        }
    }

    @TargetApi(9)
    private static class FlingAnimationImplGingerbread implements FlingAnimationImpl {
        private OverScroller mScroller;

        FlingAnimationImplGingerbread(@NotNull Context context) {
            mScroller = new OverScroller(context);
        }

        @Override
        public void startFling(int startX, int startY, int velX, int velY, int minX, int maxX, int minY,
                               int maxY, int overX, int overY) {
            mScroller.fling(startX, startY, velX, velY, minX, maxX, minY, maxY, overX, overY);
        }

        @Override
        public void onUpdate(@NotNull Shape shape) {
            mScroller.computeScrollOffset();
            shape.moveTo(mScroller.getCurrX(), mScroller.getCurrY());
        }

        @Override
        public boolean isFinished() {
            return mScroller.isFinished();
        }
    }

    private FlingAnimationImpl mImpl;

    private int mStartX, mStartY;
    private int mVelocityX, mVelocityY;
    private int mMinX = Integer.MIN_VALUE, mMinY = Integer.MIN_VALUE;
    private int mMaxX = Integer.MAX_VALUE, mMaxY = Integer.MAX_VALUE;
    private int mOverscrollX, mOverscrollY;

    public FlingAnimation(@NotNull Shape shape, @NotNull Context context) {
        super(shape);

        if (Build.VERSION.SDK_INT >= 9) {
            mImpl = new FlingAnimationImplGingerbread(context);
        } else {
            mImpl = new FlingAnimationImplBase(context);
        }
    }

    /**
     * Durations are ignored for flings
     * @param duration Ignored
     * @return This animation
     */
    @Override
    public Animation setLoopDuration(long duration) {
        return this;
    }

    /**
     * Durations are ignored for flings
     * @param duration Ignored
     * @return This animation
     */
    @Override
    public Animation setTotalDuration(long duration) {
        return this;
    }

    /**
     * Set the starting position of the object
     * @param startX The starting x position
     * @param startY The starting y position
     * @return This animation
     */
    public FlingAnimation setStartPositions(int startX, int startY) {
        mStartX = startX;
        mStartY = startY;
        return this;
    }

    /**
     * Set the starting velocities of the object
     * @param ux The starting x velocity
     * @param uy The starting y velocity
     * @return This animation
     */
    public FlingAnimation setStartVelocities(int ux, int uy) {
        mVelocityX = ux;
        mVelocityY = uy;
        return this;
    }

    /**
     * Set the fling boundaries for the x-axis
     * @param minx The minimum x value
     * @param maxx The maximum x value
     * @return This animation
     */
    public FlingAnimation setBoundariesX(int minx, int maxx) {
        mMinX = minx;
        mMaxX = maxx;
        return this;
    }

    /**
     * Set the fling boundaries for the y-axis
     * @param miny The minimum y value
     * @param maxy The maximum y value
     * @return This animation
     */
    public FlingAnimation setBoundariesY(int miny, int maxy) {
        mMinY = miny;
        mMaxY = maxy;
        return this;
    }

    /**
     * Set the amount of pixels for the fling to overscroll the boundaries (if available)
     * @param overx The maximum overscroll amount (set <= 0 for no overscroll) in the x-axis
     * @param ovary The maximum overscroll amount (set <= 0 for no overscroll) in the y-axis
     * @return This animation
     */
    public FlingAnimation setOverscroll(int overx, int ovary) {
        mOverscrollX = overx;
        mOverscrollY = ovary;
        return this;
    }

    @Override
    public void start() {
        mImpl.startFling(mStartX, mStartY, mVelocityX, mVelocityY, mMinX, mMaxX, mMinY, mMaxY,
                mOverscrollX, mOverscrollY);
        super.start();
    }

    @Override
    public void start(long delay) {
        start();
    }

    @Override
    public void onUpdate(long now) {
        mImpl.onUpdate(mShape);
    }

    @Override
    public boolean shouldFinish(long now) {
        return mImpl.isFinished();
    }
}
