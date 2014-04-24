package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;
import android.view.MotionEvent;

import com.nickstephen.gamelib.anim.FlingAnimation;
import com.nickstephen.gamelib.anim.TranslationAnimation;
import com.nickstephen.gamelib.opengl.Quadrilateral;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.gestures.GestureFling;
import com.nickstephen.gamelib.opengl.gestures.GestureScroll;
import com.nickstephen.gamelib.run.GameLoop;
import com.nickstephen.lib.Twig;
import com.nickstephen.lib.VersionControl;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * General class to be used as a base for all other classes that contain other shapes to be displayed
 * in the OpenGL display.
 * <p/>
 * The centre of the Container is used as the new origin for any child shapes (including other containers),
 * so it can be seen as a sort of relative position.
 * <p/>
 * There are 2 different sets of dimensions for the container, which are screen and bounds. Screen is
 * the actual dimensions of the container as shown on screen (in pixels). If you set
 * {@link com.nickstephen.lib.VersionControl#IS_RELEASE } to false then this bounding box will be
 * drawn to the screen on rendering, otherwise it's invisible. Bounds are only applicable if
 * {@link #mIsScrollable} is set to true, at which point touch events inside the Container's screen
 * dimensions will be intercepted and can scroll the container in 2D. This doesn't change the position
 * of the screen box, but changes the relative positions of the children of this container. The bounding
 * dimensions provide a limit to the amount that the container can be scrolled.
 * NOTE: Bounding dimensions must be >= screen dimensions!
 *
 * @author Nick Stephen
 */
public class Container extends Quadrilateral {
    private static final int INVALID_POINTER = -1;

    protected final List<Container> mChildContainers;
    protected final List<Shape> mChildren;

    protected final float[] mVPMatrix = new float[16];

    private int mActivePointerId = INVALID_POINTER;
    private int mBottom;
    private float mBoundsHeight;
    private float mBoundsWidth;
    private boolean mInfiniteBounds = true;
    private boolean mIsBeingDragged = false;
    private boolean mIsScrollable = false;
    private float mLastMotionX;
    private float mLastMotionY;
    private int mLeft;
    private float mParentOffsetX;
    private float mParentOffsetY;
    private float mScreenHeight;
    private float mScreenWidth;

    /**
     * Default constructor for {@link com.nickstephen.gamelib.opengl.layout.Container}
     *
     * @param context       A non-null context
     * @param parent        A parent container (if applicable). Should always be passed unless this is the
     *                      root container
     * @param width         The screen width of the container
     * @param height        The screen height of the container
     * @param parentOffsetX The relative x offset of the centre of this container from the parent container
     * @param parentOffsetY The relative y offset of the centre of this container from the parent container
     */
    public Container(@NotNull Context context, @Nullable Container parent, float width, float height, float parentOffsetX, float parentOffsetY) {
        super(context, parent, 0.0f, 0.0f, width, height);

        mChildren = new ArrayList<Shape>(); // Initialise list of child shapes (not containers!)
        mChildContainers = new ArrayList<Container>(); // Initialise list of child containers (not regular shapes!)

        setParentOffset(parentOffsetX, parentOffsetY); // Set the parent offsets

        setScreenSize(width, height); // Set the screen size and initialise some other fields

        mColour = new float[]{1.0f, 0, 0, 1.0f}; // Set the bounding box colour to be red
    }

    /**
     * Set the offset of the container from it's parent. This is the distance between the centre of
     * this container from the centre of the other container. It's from the point of view of the parent
     * container.
     *
     * @param x The x-axis offset (pixels)
     * @param y The y-axis offset (pixels)
     */
    private void setParentOffset(float x, float y) {
        mParentOffsetX = x;
        mParentOffsetY = y;
    }

    /**
     * Set the dimensions of the screen. These are the dimensions (in pixels) of the Container as it's
     * displayed on screen. Will also raise the size of the bounds dimensions if they're smaller than
     * the new screen dimensions.
     *
     * @param width  The width of the container on the screen (pixels)
     * @param height The height of the container on the screen (pixels)
     */
    public void setScreenSize(float width, float height) {
        mScreenWidth = width;
        mScreenHeight = height;

        if (mBoundsWidth < mScreenWidth) {
            mBoundsWidth = mScreenWidth;
        }
        if (mBoundsHeight < mScreenHeight) {
            mBoundsHeight = mScreenHeight;
        }

        if (getParent() != null) {
            mBottom = (int) ((getParent().getScreenHeight() / 2.0f) + mParentOffsetY - (getScreenHeight() / 2.0f));
            mLeft = (int) ((getParent().getScreenWidth() / 2.0f) + mParentOffsetX - (getScreenWidth() / 2.0f));
        }
    }

    /**
     * Get the width of the container on the screen in pixels.
     *
     * @return The width of the container
     */
    public float getScreenWidth() {
        return mScreenWidth;
    }

    /**
     * Get the height of the container on the screen in pixels.
     *
     * @return The height of the container
     */
    public float getScreenHeight() {
        return mScreenHeight;
    }

    /**
     * The primary drawing method for containers. Note that this does not actually draw the bounding
     * box as seen on a debug build. That is left to {@link com.nickstephen.gamelib.opengl.Shape#draw(float[])}.
     * <p/>
     * This method will first draw itself (if {@link com.nickstephen.lib.VersionControl#IS_RELEASE}
     * is false), then translates the viewMatrix argument by the parent offsets of this container as
     * well as the x and y positions it has been scrolled to. This is then passed into the child container's
     * equivalent method along with the unmodified projection matrix.
     * <p/>
     * Then the glScissor function is applied around the bounds of this container and the child shape's
     * of this container are drawn with the combined projection and translated view matrices.
     *
     * @param projMatrix The projection matrix (passed unmodified from the renderer)
     * @param viewMatrix The view matrix (modified by containers to account for different offsets)
     */
    public void draw(float[] projMatrix, float[] viewMatrix) {
        if (!VersionControl.IS_RELEASE) {
            Matrix.translateM(mScratch, 0, viewMatrix, 0, mParentOffsetX, mParentOffsetY, 0);
            System.arraycopy(mScratch, 0, mVPMatrix, 0, 16);
            draw(projMatrix);
        }

        Matrix.translateM(mScratch, 0, viewMatrix, 0, this.getX() + mParentOffsetX, this.getY() + mParentOffsetY, 0);

        int len = mChildContainers.size();
        //noinspection ForLoopReplaceableByForEach
        for (int i = 0; i < len; i++) {
            mChildContainers.get(i).draw(projMatrix, mScratch);
        }

        Matrix.multiplyMM(mVPMatrix, 0, projMatrix, 0, mScratch, 0);

        GLES20.glEnable(GLES20.GL_SCISSOR_TEST);
        GLES20.glScissor(getAbsoluteBLCornerX(), getAbsoluteBLCornerY(), (int) mScreenWidth, (int) mScreenHeight);

        len = mChildren.size();
        for (int i = 0; i < len; i++) {
            mChildren.get(i).draw(mVPMatrix);
        }

        GLES20.glDisable(GLES20.GL_SCISSOR_TEST);
    }

    /**
     * Get the absolute position (in pixels) of the bottom left corner of the container. Used for the
     * glScissor function.
     *
     * @return The x-position of the bottom left corner of the container
     */
    public int getAbsoluteBLCornerX() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getAbsoluteBLCornerX() + mLeft;
    }

    /**
     * Get the absolute position (in pixels) of the bottom left corner of the container. Used for the
     * glScissor function.
     *
     * @return The y-position of the bottom left corner of the container
     */
    public int getAbsoluteBLCornerY() {
        if (getParent() == null) {
            return 0;
        }
        return getParent().getAbsoluteBLCornerY() + mBottom;
    }

    /**
     * Checks whether a touch event is inside a child (shape or container) of this container's bounds.
     *
     * @param x The x position (relative to the centre of this container)
     * @param y The y position (relative to the centre of this container)
     * @return True if the touch position is within the bounds of a child, false otherwise
     */
    private boolean isPointInChild(float x, float y) {
        for (Shape shape : mChildren) {
            if (shape.withinBounds(x, y)) {
                return true;
            }
        }
        for (Container c : mChildContainers) {
            if (c.withinBounds(x, y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get whether the container is scrollable.
     *
     * @return True if the container is scrollable, false otherwise
     */
    public boolean isScrollable() {
        return mIsScrollable;
    }

    /**
     * Set whether the container is scrollable.
     *
     * @param val True to let the container scroll, false otherwise
     */
    public void setScrollable(boolean val) {
        mIsScrollable = val;
    }

    /**
     * Move a relative distance. Overrode here in order to account for bounds behaviours and will
     * stop the screen from moving past those bounds.
     *
     * @param dx The relative x distance to move (pixels)
     * @param dy The relative y distance to move (pixels)
     */
    @Override
    public void move(float dx, float dy) {

        if (mInfiniteBounds) {
            super.move(dx, dy);
            return;
        }

        // If there are non-infinite bounds, make sure that the user can't drag outside those bounds

        float posX = this.getX() + dx;
        float posY = this.getY() + dy;
        float newX = posX;
        float newY = posY;

        if ((posX + (mScreenWidth / 2.0f)) - (mBoundsWidth / 2.0f) > 0.0f) {
            newX = (mBoundsWidth - mScreenWidth) / 2.0f;
        } else if ((posX - (mScreenWidth / 2.0f)) + (mBoundsWidth / 2.0f) < 0.0f) {
            newX = (mScreenWidth - mBoundsWidth) / 2.0f;
        }

        if ((posY + (mScreenHeight / 2.0f)) - (mBoundsHeight / 2.0f) > 0.0f) {
            newY = (mBoundsHeight - mScreenHeight) / 2.0f;
        } else if ((posY - (mScreenHeight / 2.0f)) + (mBoundsHeight / 2.0f) < 0.0f) {
            newY = (mScreenHeight - mBoundsHeight) / 2.0f;
        }

        this.moveTo(newX, newY);
    }

    @Override
    public boolean onGestureEvent(@NotNull GestureEvent e, float relativePosX, float relativePosY) {
        if (e.type == GestureEvent.Type.FINISH) {
            mIsBeingDragged = false;
        } else if (!withinBounds(relativePosX, relativePosY, mTouchSlop)) {
            mIsBeingDragged = false;
            return false;
        }

        if (e.type != GestureEvent.Type.FINISH) {
            if (mCurrentFlingAnim != null) {
                if (!mCurrentFlingAnim.shouldFinish(0)) {
                    mCurrentFlingAnim.forceFinish();
                    GameLoop.getInstanceUnsafe().removeAnimation(mCurrentFlingAnim);
                }
                mCurrentFlingAnim = null;
            }
        }

        if (onInterceptGestureEvent(e)) {
            return true;
        }

        relativePosX -= mParentOffsetX;
        relativePosY -= mParentOffsetY;

        float childX = relativePosX - this.getX();
        float childY = relativePosY - this.getY();

        for (Shape shape : mChildren) {
            if (shape.onGestureEvent(e, childX, childY)) {
                return true;
            }
        }

        for (Container c : mChildContainers) {
            if (c.onGestureEvent(e, childX, childY)) {
                return true;
            }
        }

        if (mIsScrollable) {
            if (e.type == GestureEvent.Type.SCROLL) {
                mIsBeingDragged = true;
                GestureScroll scroll = (GestureScroll) e;
                move(scroll.scrollX, scroll.scrollY);

                if (mOnScrollListener != null) {
                    mOnScrollListener.onGesture(this, e);
                }
                return true;
            } else if (e.type == GestureEvent.Type.FLING) {
                mIsBeingDragged = false;
                mCurrentFlingAnim = new FlingAnimation(this, mContext);

                if (mOnFlingListener != null) {
                    mOnFlingListener.onGesture(this, e);
                }
                return true;
            }
        }
        // TODO: General gesture handling

        return false;
    }

    /**
     * Container provides it's own implementation of this method to make sure the correct matrix
     * transformations occur.
     *
     * @return The model matrix for this container's bounding box
     */
    @Override
    public @NotNull float[] getModelMatrix() {
        return mVPMatrix;
    }

    protected boolean onInterceptGestureEvent(GestureEvent e) {
        if (e.type == GestureEvent.Type.SCROLL && mIsBeingDragged) {
            GestureScroll scroll = (GestureScroll) e;
            move(scroll.scrollX, scroll.scrollY);
            return true;
        }
        if (e.type == GestureEvent.Type.FLING && mIsBeingDragged) {
            GestureFling fling = (GestureFling) e;
            mCurrentFlingAnim = new FlingAnimation(this, mContext)
                    .setStartPositions((int) getX(), (int) getY())
                    .setStartVelocities((int) fling.xVelocity, (int) fling.yVelocity);
            mCurrentFlingAnim.start();
            return true;
        }

        return false;
    }

    /**
     * Set the size of the bounding box of the container. This is the amount to which the container
     * can be scrolled (if applicable)
     *
     * @param width  The bounding box width (pixels)
     * @param height The bounding box height (pixels)
     * @see com.nickstephen.gamelib.opengl.layout.Container
     */
    public void setBoundsSize(float width, float height) {
        mInfiniteBounds = false;
        mBoundsWidth = width;
        mBoundsHeight = height;
    }

    /**
     * Set the container to have unlimited bounds. This means that the container will continue scrolling
     * indefinitely (if applicable)
     *
     * @param val True to set the container's bounds to be infinite, false otherwise
     * @see #setBoundsSize(float, float)
     */
    public void setUnlimitedBounds(boolean val) {
        mInfiniteBounds = val;
    }

    /**
     * Check whether a position is within the bounds of this container (as shown on the screen)
     *
     * @param posX      The x position (relative to the centre of the parent)
     * @param posY      The y position (relative to the centre of the parent)
     * @param touchSlop The amount of leeway a user has for exiting the bounds
     * @return True if inside (or nearly inside) the container, false otherwise
     */
    @Override
    public boolean withinBounds(float posX, float posY, float touchSlop) {
        if ((Math.abs(-posY + mParentOffsetY) - (mScreenHeight / 2.0f)) > touchSlop) {
            return false;
        }

        return (Math.abs(-posX + mParentOffsetX) - (mScreenWidth / 2.0f)) <= touchSlop;

    }

    @Override
    public void setAlpha(float alpha) {
        super.setAlpha(alpha);

        int len = mChildren.size();
        for (int i = 0; i < len; i++) {
            mChildren.get(i).setAlpha(alpha);
        }

        len = mChildContainers.size();
        for (int i = 0; i < len; i++) {
            mChildContainers.get(i).setAlpha(alpha);
        }
    }

    public List<Shape> getChildren() {
        return mChildren;
    }

    @Override
    public void destroy() {
        super.destroy();

        for (Shape c : mChildren) {
            c.destroy();
        }

        for (Container c : mChildContainers) {
            c.destroy();
        }
    }
}
