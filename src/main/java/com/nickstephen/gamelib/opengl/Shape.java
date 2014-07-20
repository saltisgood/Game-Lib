package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import com.nickstephen.gamelib.GeneralUtil;
import com.nickstephen.gamelib.anim.FlingAnimation;
import com.nickstephen.gamelib.opengl.bounds.Bounds;
import com.nickstephen.gamelib.opengl.bounds.Default;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.gestures.GestureFling;
import com.nickstephen.gamelib.opengl.gestures.GestureScroll;
import com.nickstephen.gamelib.opengl.gestures.IGestures;
import com.nickstephen.gamelib.opengl.gestures.IOnGestureL;
import com.nickstephen.gamelib.opengl.interfaces.IDisposable;
import com.nickstephen.gamelib.opengl.interfaces.IDraw;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;
import com.nickstephen.gamelib.opengl.gestures.IOnClickL;
import com.nickstephen.gamelib.opengl.textures.Texture;
import com.nickstephen.gamelib.run.GameLoop;

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
public abstract class Shape implements IGestures, IDraw, IDisposable {
    protected final Program mProgram;
    /**
     * A 4x4 float matrix pre-allocated here and whose contents can not be trusted in between
     * method calls. Useful as a matrix whose contents are needed only briefly and repeatedly.
     */
    protected final float[] mScratch = new float[16];

    private final float[] mModelMatrix = new float[16];

    protected float[] mColour = new float[4];
    protected Vertices mVertices;
    protected FlingAnimation mCurrentFlingAnim;
    protected IOnClickL mOnClickListener;
    protected IOnClickL mOnLongClickListener;
    protected IOnClickL mOnDoubleClickListener;
    protected IOnGestureL mOnScrollListener;
    protected IOnGestureL mOnFlingListener;

    private float mAlpha = 1.0f;
    private float mAngle;

    protected Bounds mBoundsChecker;

    private boolean mClickable = true;
    private boolean mLongClickable = false;
    private boolean mModelMatrixInvalidated = true;
    private boolean mIsFixed = true;
    
    private Container mParent;
    private GLSurfaceView mSurface;
    protected final Context mContext;

    protected Texture.Client mTexture;

    /**
     * Construct the shape with a {@link com.nickstephen.gamelib.opengl.program.Program.GenericProgram} used
     * as the program.
     * @param context A context
     * @param parent A possible container
     */
    public Shape(@NotNull Context context, @Nullable Container parent) {
        mProgram = Program.GenericProgram.create();

        mContext = context;

        mParent = parent;

        if (parent != null) {
            mSurface = parent.getSurface();
        }

        mBoundsChecker = new Default(this);
    }

    /**
     * Construct with shape with a given {@link com.nickstephen.gamelib.opengl.program.Program}.
     * @param context A context
     * @param parent A possible container
     * @param program The program to use
     */
    public Shape(@NotNull Context context, @Nullable Container parent, @NotNull Program program) {
        mProgram = program;

        mContext = context;
        mParent = parent;

        if (parent != null) {
            mSurface = parent.getSurface();
        }

        mBoundsChecker = new Default(this);
    }


    //TODO: Move to shapes package so can package protect Bounds!
    public Bounds getBounds() {
        return mBoundsChecker;
    }

    public float getAlpha() {
        return mAlpha;
    }

    public void setAlpha(float alpha) {
        mAlpha = alpha;
        mColour[3] = alpha;
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

    public @Nullable float[] getChannel() {
        return null;
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
     * Implement this to dispose any OpenGL resources that have been created in using this object.
     * <strong>Don't dispose shared resources or you'll get crashes!</strong>
     */
    public void dispose() {
        mProgram.dispose();

        if (mTexture != null) {
            mTexture.dispose();
        }
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
     * Get the model matrix for this shape. The base implementation is simply a translation matrix
     * based on the x and y positions, (z ignored).
     * @return The float matrix
     */
    public @NotNull float[] getModelMatrix() {
        if (mModelMatrixInvalidated) {
            Matrix.setIdentityM(mModelMatrix, 0);
            Matrix.translateM(mModelMatrix, 0, mBoundsChecker.getX(), mBoundsChecker.getY(), 0);
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
        moveTo(mBoundsChecker.getX() + dx, mBoundsChecker.getY() + dy);
    }

    /**
     * Move the shape to a new offset as an absolute position
     * @param newX The new x offset relative to its parent
     * @param newY The new y offset relative to its parent
     */
    public void moveTo(float newX, float newY) {
        mBoundsChecker.setX(newX);
        mBoundsChecker.setY(newY);

        mModelMatrixInvalidated = true;
    }

    public boolean giveGestureEvent(@NotNull GestureEvent e) {
        if (e.type != GestureEvent.Type.FINISH) {
            if (mCurrentFlingAnim != null) {
                if (!mCurrentFlingAnim.shouldFinish(0)) {
                    mCurrentFlingAnim.forceFinish();
                    GameLoop.getInstanceUnsafe().removeAnimation(mCurrentFlingAnim);
                }
                mCurrentFlingAnim = null;
            }
        }

        switch (e.type) {
            case SCROLL:
                if (mClickable && !mIsFixed) {
                    GestureScroll scroll = (GestureScroll) e;
                    move(scroll.scrollX, scroll.scrollY);
                    if (mOnScrollListener != null) {
                        mOnScrollListener.onGesture(this, e);
                    }
                    GameLoop.getInstanceUnsafe().setFocusShape(this);
                    return true;
                }
                break;
            case FLING:
                if (mClickable && !mIsFixed) {
                    GestureFling fling = (GestureFling) e;
                    mCurrentFlingAnim = new FlingAnimation(this, mContext)
                            .setStartPositions((int) mBoundsChecker.getX(), (int) mBoundsChecker.getY())
                            .setStartVelocities((int) fling.xVelocity, (int) fling.yVelocity);
                    mCurrentFlingAnim.start();
                    return true;
                }
                break;
            case SINGLE_TAP:
                if (mClickable && mOnClickListener != null) {
                    mOnClickListener.onClick(this);
                    return true;
                }
                break;
            case DOUBLE_TAP:
                if (mClickable && mOnDoubleClickListener != null) {
                    mOnDoubleClickListener.onClick(this);
                    return true;
                }
                break;
            case LONG_PRESS:
                if (mClickable && mLongClickable) {
                    return performLongClick();
                }
                break;
        }

        return false;
    }

    @Override
    public boolean onGestureEvent(@NotNull GestureEvent e, float relativePosX, float relativePosY) {
        // If outside the bounds of the shape don't consume the event
        if (!withinBounds(relativePosX, relativePosY)) {
            return false;
        }

        return giveGestureEvent(e);
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
     * Return the OpenGL program associated with this shape.
     * @return The program
     */
    public @NotNull Program getProgram() {
        return mProgram;
    }

    public void removeFromParent() {
        if (mParent != null) {
            synchronized (mParent.getChildren()) {
                mParent.getChildren().remove(this);
            }
        }
    }

    /**
     * Remove a callback from the surface associated with this shape
     * @param action The callback to remove
     */
    public void removeCallbacks(Runnable action) {
        mSurface.removeCallbacks(action);
    }

    public void setFixed(boolean fixed) {
        mIsFixed = fixed;
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

    public void setOnDoubleClickListener(@Nullable IOnClickL listener) {
        mOnDoubleClickListener = listener;
    }

    public void setOnFlingListener(@Nullable IOnGestureL listener) {
        mOnFlingListener = listener;
    }

    /**
     * Set the listener to be invoked upon long clicking this shape. If there was previously no listener,
     * the shape is set to be long clickable. The argument can be set to null to remove a listener.
     * @param listener The listener to add
     */
    public void setOnLongClickListener(@Nullable IOnClickL listener) {
        mLongClickable = listener != null;
        mOnLongClickListener = listener;
    }

    public void setOnScrollListener(@Nullable IOnGestureL listener) {
        mOnScrollListener = listener;
    }

    public int getTextureId() {
        if (mTexture != null) {
            return mTexture.getId();
        }

        return Texture.TEX_ID_UNASSIGNED;
    }

    public void setTextureCoords(TextureRegion region) {

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
        return mBoundsChecker.withinBounds(posX, posY, touchSlop);
    }

    /**
     * Get the x offset of this shape from its parent container
     * @return X offset (pixels)
     */
    public float getX() {
        return mBoundsChecker.getX();
    }

    /**
     * Get the y offset of this shape from its parent container
     * @return Y offset (pixels)
     */
    public float getY() {
        return mBoundsChecker.getY();
    }
}