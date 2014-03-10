package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.graphics.RectF;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.GenericProgram;
import com.nickstephen.gamelib.opengl.program.Program;
import com.nickstephen.gamelib.opengl.text.*;
import com.nickstephen.gamelib.opengl.widget.IOnClickL;
import com.nickstephen.gamelib.opengl.widget.ITouchL;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public abstract class Shape implements ITouchL {
    private static final int PFLAG_PREPRESSED = 0x02000000;
    private static final int PFLAG_PRESSED = 0x00004000;
    private static final int LONG_CLICKABLE = 0x00200000;

    protected float[] mColour = new float[4];
    protected final Program mProgram;

    private boolean mClickable = true;
    private boolean mLongClickable = false;
    private IOnClickL mOnClickListener;
    private IOnClickL mOnLongClickListener;
    private Container mParent;
    private boolean mHasPerformedLongPress = false;
    private int mPrivateFlags;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private UnsetPressedState mUnsetPressedState;
    private PerformClick mPerformClick;
    protected final int mTouchSlop;
    private GLSurfaceView mSurface;
    private int mTextureId;
    protected com.nickstephen.gamelib.opengl.text.Vertices mVertices;

    // Positions
    /**
     * The baseline X position relative to the container
     */
    private float mBaseX;
    /**
     * The baseline Y position relative to the container
     */
    private float mBaseY;
    /**
     * The rough distance to the left side of the shape
     */
    private float mLeft;
    /**
     * The rough distance to the right side of the shape
     */
    private float mRight;
    /**
     * The rough distance to the top of the shape
     */
    private float mUp;
    /**
     * The rough distance to the bottom of the shape
     */
    private float mDown;

    public Shape(@NotNull Context context, @Nullable Container parent) {
        mProgram = new GenericProgram();
        mProgram.init();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mParent = parent;

        if (parent != null) {
            mSurface = parent.getSurface();
        }
    }

    public Shape(@NotNull Context context, @Nullable Container parent, @NotNull Program program) {
        mProgram = program;
        if (!mProgram.isInitialized()) {
            mProgram.init();
        }

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mParent = parent;

        if (parent != null) {
            mSurface = parent.getSurface();
        }
    }

    public void setSurface(GLSurfaceView surface) {
        mSurface = surface;
    }

    public void move(float dx, float dy) {
        mBaseX += dx;
        mBaseY += dy;
    }

    public void moveTo(float newX, float newY) {
        mBaseX = newX;
        mBaseY = newY;
    }

    public void resize(float ratio) {
        mLeft *= ratio;
        mRight *= ratio;
        mUp *= ratio;
        mDown *= ratio;
    }

    public void setColour(float r, float g, float b, float a) {
        mColour[0] = r;
        mColour[1] = g;
        mColour[2] = b;
        mColour[3] = a;
    }

    public void setColour(float[] colour) {
        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be of length 4");
        }
        mColour = colour;
    }

    public float[] getColour() {
        return mColour;
    }

    public void setSize(float radius) {
        mLeft = mRight = mUp = mDown = radius;
    }

    public void setSize(float left, float right, float up, float down) {
        mLeft = left;
        mRight = right;
        mUp = up;
        mDown = down;
    }

    @Override
    public boolean withinBounds(float posX, float posY) {
        return withinBounds(posX, posY, 0);
    }

    public boolean withinBounds(float posX, float posY, float touchSlop) {
        float diff;
        if ((diff = mBaseX - posX) < 0.0f) {
            if (-diff > (mRight + touchSlop)) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > (mLeft + touchSlop)) {
                return false;
            }
        }

        if ((diff = mBaseY - posY) < 0.0f) {
            if (-diff > (mUp + touchSlop)) {
                return false;
            }
        } else if (diff > 0.0f) {
            if (diff > (mDown + touchSlop)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent e, float relativePosX, float relativePosY) {
        // If outside the bounds of the shape don't consume the event
        if (!withinBounds(relativePosX, relativePosY) && !isPressed()) {
            return false;
        }

        if (!mClickable) {
            return false;
        }

        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mHasPerformedLongPress = false;

                if (isInScrollingContainer()) {
                    mPrivateFlags |= PFLAG_PREPRESSED;
                    if (mPendingCheckForTap == null) {
                        mPendingCheckForTap = new CheckForTap();
                    }
                    postDelayed(mPendingCheckForTap, ViewConfiguration.getTapTimeout());
                } else {
                    setPressed(true);
                    checkForLongClick(0);
                }
                break;
            case MotionEvent.ACTION_UP:
                boolean prepressed = (mPrivateFlags & PFLAG_PREPRESSED) != 0;
                if ((mPrivateFlags & PFLAG_PRESSED) != 0 || prepressed) {
                    if (prepressed) {
                        // The button is being released before we actually
                        // showed it as pressed.  Make it show the pressed
                        // state now (before scheduling the click) to ensure
                        // the user sees it.
                        setPressed(true);
                    }

                    if (!mHasPerformedLongPress) {
                        // This is a tap, so remove the longpress check
                        removeLongPressCallback();

                        // Only perform take click actions if we were in the pressed state
                        //if (!focusTaken) {
                            // Use a Runnable and post this rather than calling
                            // performClick directly. This lets other visual state
                            // of the view update before click actions start.
                            if (mPerformClick == null) {
                                mPerformClick = new PerformClick();
                            }
                            if (!post(mPerformClick)) {
                                performClick();
                            }
                        //}
                    }

                    if (mUnsetPressedState == null) {
                        mUnsetPressedState = new UnsetPressedState();
                    }

                    if (prepressed) {
                        postDelayed(mUnsetPressedState,
                                ViewConfiguration.getPressedStateDuration());
                    } else if (!post(mUnsetPressedState)) {
                        // If the post failed, unpress right now
                        mUnsetPressedState.run();
                    }
                    removeTapCallback();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                // Be lenient about moving outside of buttons
                if (!withinBounds(relativePosX, relativePosY, mTouchSlop)) {
                    // Outside button
                    removeTapCallback();
                    if ((mPrivateFlags & PFLAG_PRESSED) != 0) {
                        // Remove any future long press/tap checks
                        removeLongPressCallback();

                        setPressed(false);
                    }
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                setPressed(false);
                removeTapCallback();
                removeLongPressCallback();
                break;
        }
        return true;
    }

    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }

    public void removeCallbacks(Runnable action) {
        mSurface.removeCallbacks(action);
    }

    public boolean isInScrollingContainer() {
        //TODO: Implement isInScrollingContainer
        return true;
    }

    public boolean post(Runnable action) {
        return mSurface.post(action);
    }

    public boolean postDelayed(Runnable action, long delayMillis) {
        return mSurface.postDelayed(action, delayMillis);
    }

    public void setClickable(boolean val) {
        mClickable = val;
    }

    public void setOnClickListener(IOnClickL listener) {
        if (listener != null) {
            mClickable = true;
        }
        mOnClickListener = listener;
    }

    public void setOnLongClickListener(IOnClickL listener) {
        if (listener != null) {
            mLongClickable = true;
        }
        mOnLongClickListener = listener;
    }

    public boolean performClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    public float getX() {
        return mBaseX;
    }

    public float getY() {
        return mBaseY;
    }

    public @Nullable Container getParent() {
        return mParent;
    }

    public void setPressed(boolean pressed) {
        if (pressed) {
            mPrivateFlags |= PFLAG_PREPRESSED;
        } else {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
        }
    }

    private void checkForLongClick(int delayOffset) {
        if (mLongClickable) {
            mHasPerformedLongPress = false;

            if (mPendingCheckForLongPress == null) {
                mPendingCheckForLongPress = new CheckForLongPress();
            }
            postDelayed(mPendingCheckForLongPress,
                    ViewConfiguration.getLongPressTimeout() - delayOffset);
        }
    }

    public boolean isPressed() {
        return (mPrivateFlags & PFLAG_PREPRESSED) == PFLAG_PREPRESSED;
    }

    public boolean performLongClick() {
        if (mOnLongClickListener != null) {
            GeneralUtil.vibrate(50);
            mOnLongClickListener.onClick(this);
        }
        return true;
    }

    public int getTextureId() {
        return mTextureId;
    }

    protected final void setTextureId(int texId) {
        mTextureId = texId;
    }

    public @Nullable GLSurfaceView getSurface() {
        return mSurface;
    }

    public @NotNull Program getProgram() {
        return mProgram;
    }

    public void draw(float[] mvpMatrix) {
        if (mVertices != null) {
            mVertices.draw(mvpMatrix);
        }
    }

    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            setPressed(true);
            checkForLongClick(ViewConfiguration.getTapTimeout());
        }
    }

    private final class CheckForLongPress implements Runnable {
        public void run() {
            if (isPressed()) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }

    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }
}
