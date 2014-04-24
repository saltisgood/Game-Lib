package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.gestures.IGestureL;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.GenericProgram;
import com.nickstephen.gamelib.opengl.program.Program;
import com.nickstephen.gamelib.opengl.widget.IOnClickL;
import com.nickstephen.gamelib.opengl.widget.ITouchL;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>The base class for everything displayed in the OpenGL environment. Equivalent to
 * {@link android.view.View} in the Android Framework and indeed reuses some of the code, especially
 * in the touch event area. </p>
 *
 *  <p>The class is not meant to be a proper treatment of all of its methods so sub-classes should
 *  override some to better suit their intended use.</p>
 *
 * @author Nick Stephen
 */
public abstract class Shape implements ITouchL, IGestureL {
    private static final int PFLAG_PREPRESSED = 0x02000000;
    private static final int PFLAG_PRESSED = 0x00004000;

    protected final Program mProgram;
    /**
     * A 4x4 float matrix pre-allocated here and whose contents can not be trusted in between
     * method calls. Useful as a matrix whose contents are needed only briefly and repeatedly.
     */
    protected final float[] mScratch = new float[16];
    protected final int mTouchSlop;

    private final float[] mModelMatrix = new float[16];

    protected float[] mColour = new float[4];
    protected Vertices mVertices;

    private float mAlpha = 1.0f;
    private float mAngle;
    /**
     * The baseline X position relative to the container
     */
    private float mBaseX;
    /**
     * The baseline Y position relative to the container
     */
    private float mBaseY;

    private boolean mClickable = true;
    /**
     * The rough distance to the bottom of the shape
     */
    private float mDown;
    private boolean mHasPerformedLongPress = false;
    /**
     * The rough distance to the left side of the shape
     */
    private float mLeft;
    private boolean mLongClickable = false;
    private boolean mModelMatrixInvalidated = true;
    private boolean mIsFixed = true;
    private IOnClickL mOnClickListener;
    private IOnClickL mOnLongClickListener;
    private Container mParent;
    private CheckForLongPress mPendingCheckForLongPress;
    private CheckForTap mPendingCheckForTap;
    private PerformClick mPerformClick;
    private int mPrivateFlags;
    /**
     * The rough distance to the right side of the shape
     */
    private float mRight;
    private GLSurfaceView mSurface;
    private int mTextureId;
    private UnsetPressedState mUnsetPressedState;
    /**
     * The rough distance to the top of the shape
     */
    private float mUp;

    /**
     * Construct the shape with a {@link com.nickstephen.gamelib.opengl.program.GenericProgram} used
     * as the program.
     * @param context A context
     * @param parent A possible container
     */
    public Shape(@NotNull Context context, @Nullable Container parent) {
        mProgram = new GenericProgram();
        mProgram.init();

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();

        mParent = parent;

        if (parent != null) {
            mSurface = parent.getSurface();
        }
    }

    /**
     * Construct with shape with a given {@link com.nickstephen.gamelib.opengl.program.Program}.
     * @param context A context
     * @param parent A possible container
     * @param program The program to use
     */
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

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
    }

    /**
     * Get the current orientation of the shape (degrees)
     * @return The orientation of the shape
     */
    public float getAngle() {
        return mAngle;
    }

    /**
     * Set the new angle of the shape
     * @param angle The angle in degrees
     */
    public void setAngle(float angle) {
        mAngle = angle;

        mModelMatrixInvalidated = true;
    }

    /**
     * Check for a long click
     * @param delayOffset The delay offset to use
     */
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

    /**
     * Set whether the shape should be clickable
     * @param val True to be clickable, false otherwise
     */
    public void setClickable(boolean val) {
        mClickable = val;
    }

    /**
     * Get the colour to be used with this shape.
     * @return A float array representation of the colour (0 = 0, 1 = 255)
     */
    public float[] getColour() {
        return mColour;
    }

    /**
     * Set the colour to be used with this shape. The argument must be non-null and of length 4
     * (at the moment).
     * @param colour The float array representation of the colour
     */
    public void setColour(@NotNull float[] colour) {
        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be of length 4");
        }
        mColour = colour;
    }

    /**
     * Colour helper setter. Doesn't have to allocated a new array so is marginally more efficient
     * if you care about that kind of stuff.
     * @param r The red component
     * @param g The green component
     * @param b The blue component
     * @param a The alpha component
     */
    public void setColour(float r, float g, float b, float a) {
        mColour[0] = r;
        mColour[1] = g;
        mColour[2] = b;
        mColour[3] = a;
    }

    /**
     * Implement this to release any OpenGL resources that have been created in using this object.
     * <strong>Don't release shared resources or you'll get crashes!</strong>
     */
    public void destroy() {
        mProgram.delete();
    }

    /**
     * The default drawing method for shapes. Takes the model matrix from {@link #getModelMatrix()}
     * and uses it to multiply the argument vpMatrix to get the mvpMatrix which is then passed to
     * {@link #mVertices#draw(float[])}.
     * @param vpMatrix The combined view/projection matrix to apply to the shape
     */
    public void draw(@NotNull float[] vpMatrix) {
        if (mVertices != null) {
            float[] modelMatrix = getModelMatrix();
            Matrix.multiplyMM(mScratch, 0, vpMatrix, 0, modelMatrix, 0);
            mVertices.draw(mScratch);
        }
    }

    /**
     * Gets whether the shape is inside a container with scrolling capabilities
     * @return True is the container is scrollable, false otherwise
     */
    public boolean isInScrollingContainer() {
        return mParent.isScrollable();
    }

    /**
     * Gets whether the shape is currently in a pressed state
     * @return True if it's pressed, false otherwise
     */
    public boolean isPressed() {
        return (mPrivateFlags & PFLAG_PREPRESSED) == PFLAG_PREPRESSED;
    }

    /**
     * Get the model matrix for this shape. The base implementation is simply a translation matrix
     * based on the {@link #mBaseX} and {@link #mBaseY} positions, (z ignored).
     * @return The float matrix
     */
    public @NotNull float[] getModelMatrix() {
        if (mModelMatrixInvalidated) {
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, mBaseX, mBaseY, 0);
            //Matrix.rotateM(mModelMatrix, 0, mAngle, 0, 0, -1.0f);
            GeneralUtil.rotateM(mModelMatrix, 0, mAngle, 0, 0, -1.0f);

            mModelMatrixInvalidated = false;
        }
        return mModelMatrix;
    }

    /**
     * Move the shape to a new offset relative to its old position
     * @param dx The x offset relative to its old position (pixels)
     * @param dy The y offset relative to its old position (pixels)
     */
    public void move(float dx, float dy) {
        moveTo(mBaseX + dx, mBaseY + dy);
    }

    /**
     * Move the shape to a new offset as an absolute position
     * @param newX The new x offset relative to its parent
     * @param newY The new y offset relative to its parent
     */
    public void moveTo(float newX, float newY) {
        mBaseX = newX;
        mBaseY = newY;

        mModelMatrixInvalidated = true;
    }

    @Override
    public boolean onGestureEvent(@NotNull GestureEvent e, float relativePosX, float relativePosY) {
        // If outside the bounds of the shape don't consume the event
        if (!withinBounds(relativePosX, relativePosY)) {
            return false;
        }

        switch (e.type) {
            case SCROLL:
                if (!mClickable || mIsFixed) {
                    return false;
                } else {
                    // TODO: Do move
                    return true;
                }
            case FLING:
                if (!mClickable || mIsFixed) {
                    return false;
                } else {
                    // TODO: Do fling
                    return true;
                }
            case SINGLE_TAP:
                if (mClickable) {
                    return performClick();
                } else {
                    return false;
                }
            case DOUBLE_TAP:
                if (mClickable) {
                    // TODO: Double click handler
                    // return performDoubleClick();
                    return false;
                } else {
                    return false;
                }
            case LONG_PRESS:
                if (mClickable && mLongClickable) {
                    return performLongClick();
                } else {
                    return false;
                }
        }

        return false;
    }

    /**
     * The implementation of {@link com.nickstephen.gamelib.opengl.widget.ITouchL} that is mostly just
     * a port of {@link android.view.View#onTouchEvent(android.view.MotionEvent)} but uses the relative
     * position arguments instead of those from e.
     * @param e The original motion event
     * @param relativePosX The position of the touch event relative to the parent container's offset
     *                     (x-axis, pixels)
     * @param relativePosY The position of the touch event relative to the parent container's offset
     *                     (y-axis, pixels)
     * @return True if the event was consumed, false otherwise
     */
    @Override
    public boolean onTouchEvent(@NotNull MotionEvent e, float relativePosX, float relativePosY) {
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

    /**
     * Get the parent container of this shape (if it exists). The only time that it shouldn't exist
     * is if this is an instance of {@link com.nickstephen.gamelib.opengl.layout.RootContainer}.
     * @return The parent container
     */
    public @Nullable Container getParent() {
        return mParent;
    }

    /**
     * Perform a click. Will call the {@link #mOnClickListener} if its set.
     * @return True if there was a listener and it was called.
     */
    public boolean performClick() {
        if (mOnClickListener != null) {
            mOnClickListener.onClick(this);
            return true;
        }
        return false;
    }

    /**
     * Perform a long click. Will vibrate and then call the {@link #mOnLongClickListener} as long as
     * there is a long click listener set.
     * @return True always.
     */
    public boolean performLongClick() {
        if (mOnLongClickListener != null) {
            GeneralUtil.vibrate(50);
            mOnLongClickListener.onClick(this);
        }
        return true;
    }

    /**
     * Post a runnable action to be executed on the GUI thread.
     * @param action The callback to run
     * @return True if the runnable was successfully placed in the message queue, false otherwise
     * @see #postDelayed(Runnable, long)
     */
    public boolean post(Runnable action) {
        return mSurface.post(action);
    }

    /**
     * Post a runnable action to be executed at least at some point in the future. This is always
     * run on the GUI thread, not the GL thread.
     * @param action The callback to run
     * @param delayMillis The minimum delay before running
     * @return True if the runnable was successfully placed in the message queue, false otherwise
     */
    public boolean postDelayed(Runnable action, long delayMillis) {
        return mSurface.postDelayed(action, delayMillis);
    }

    /**
     * Set the shape as being pressed or not
     * @param pressed True to be pressed, false otherwise
     */
    public void setPressed(boolean pressed) {
        if (pressed) {
            mPrivateFlags |= PFLAG_PREPRESSED;
        } else {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
        }
    }

    /**
     * Return the OpenGL program associated with this shape.
     * @return The program
     */
    public @NotNull Program getProgram() {
        return mProgram;
    }

    /**
     * Remove a callback from the surface associated with this shape
     * @param action The callback to remove
     */
    public void removeCallbacks(Runnable action) {
        mSurface.removeCallbacks(action);
    }

    /**
     * Remove a long press callback
     */
    private void removeLongPressCallback() {
        if (mPendingCheckForLongPress != null) {
            removeCallbacks(mPendingCheckForLongPress);
        }
    }

    /**
     * Remove a tap callback
     */
    private void removeTapCallback() {
        if (mPendingCheckForTap != null) {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            removeCallbacks(mPendingCheckForTap);
        }
    }

    /**
     * Resize the shape by a set ratio on all sides
     * @param ratio The ratio to multiply the current side lengths by
     */
    public void resize(float ratio) {
        mLeft *= ratio;
        mRight *= ratio;
        mUp *= ratio;
        mDown *= ratio;
    }

    /**
     * Set the listener to be invoked upon clicking this shape. If there was previously no listener,
     * the shape is set to be clickable. The argument can be set to null to remove a listener.
     * @param listener The listener to add
     */
    public void setOnClickListener(@Nullable IOnClickL listener) {
        if (listener != null) {
            mClickable = true;
        }
        mOnClickListener = listener;
    }

    /**
     * Set the listener to be invoked upon long clicking this shape. If there was previously no listener,
     * the shape is set to be long clickable. The argument can be set to null to remove a listener.
     * @param listener The listener to add
     */
    public void setOnLongClickListener(@Nullable IOnClickL listener) {
        if (listener != null) {
            mLongClickable = true;
        }
        mOnLongClickListener = listener;
    }

    /**
     * Set the size of the shape as a radius around the centre
     * @param radius The new radius of the shape
     */
    public void setSize(float radius) {
        mLeft = mRight = mUp = mDown = radius;
    }

    /**
     * Set the individual sizes of the distance to each side of the shape from the centre
     * @param left The distance to the left side
     * @param right The distance to the right side
     * @param up The distance to the top
     * @param down The distance to the bottom
     */
    public void setSize(float left, float right, float up, float down) {
        mLeft = left;
        mRight = right;
        mUp = up;
        mDown = down;
    }

    /**
     * Get the surface associated with this shape. This is a non-null call as long as the root container
     * immediately set its surface (which it should since it's in the constructor).
     * @return The GLSurfaceView that hosts the OpenGL display
     */
    public @NotNull GLSurfaceView getSurface() {
        return mSurface;
    }

    /**
     * Set the surface that's used as a display for this OpenGL environment. Should only be called
     * by the root container in its constructor.
     * @param surface The GLSurfaceView that hosts the OpenGL display
     */
    public void setSurface(@NotNull GLSurfaceView surface) {
        mSurface = surface;
    }

    /**
     * Get the texture id associated with this shape. Sub-classes aren't required to uses textures, this
     * is just a mechanism for allowing them to all have a texture id if necessary.
     * @return The texture id if it's set
     */
    public int getTextureId() {
        return mTextureId;
    }

    /**
     * Set the texture id associated with this shape.
     * @see #getTextureId()
     * @param texId The id of the texture used in this shape
     */
    protected final void setTextureId(int texId) {
        mTextureId = texId;
    }

    /**
     * Check whether a touch position is within the bounds of this shape. Just calls
     * {@link #withinBounds(float, float, float)} witha touchSlop of 0.
     * @param posX The x position of the touch
     * @param posY The y position of the touch
     * @return True if within the bounds of this shape, false otherwise
     */
    @Override
    public boolean withinBounds(float posX, float posY) {
        return withinBounds(posX, posY, 0);
    }

    /**
     * Check whether a position is within the bounds of this container (as shown on the screen)
     *
     * @param posX      The x position (relative to the centre of the parent)
     * @param posY      The y position (relative to the centre of the parent)
     * @param touchSlop The amount of leeway a user has for exiting the bounds
     * @return True if inside (or nearly inside) the container, false otherwise
     */
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

    /**
     * Get the x offset of this shape from its parent container
     * @return X offset (pixels)
     */
    public float getX() {
        return mBaseX;
    }

    /**
     * Get the y offset of this shape from its parent container
     * @return Y offset (pixels)
     */
    public float getY() {
        return mBaseY;
    }

    /**
     * A small implementation of {@link java.lang.Runnable} that is posted to the GUI thread's
     * handler to check for a long press.
     */
    private final class CheckForLongPress implements Runnable {
        public void run() {
            if (isPressed()) {
                if (performLongClick()) {
                    mHasPerformedLongPress = true;
                }
            }
        }
    }

    /**
     * A small implementation of {@link java.lang.Runnable} that is posted to the GUI thread's
     * handler to check for a tap.
     */
    private final class CheckForTap implements Runnable {
        public void run() {
            mPrivateFlags &= ~PFLAG_PREPRESSED;
            setPressed(true);
            checkForLongClick(ViewConfiguration.getTapTimeout());
        }
    }

    /**
     * A small implementation of {@link java.lang.Runnable} that is posted to the GUI thread's
     * handler to perform a click.
     */
    private final class PerformClick implements Runnable {
        public void run() {
            performClick();
        }
    }

    /**
     * A small implementation of {@link java.lang.Runnable} that is posted to the GUI thread's
     * handler to stop the shape being pressed.
     */
    private final class UnsetPressedState implements Runnable {
        public void run() {
            setPressed(false);
        }
    }
}