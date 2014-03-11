package com.nickstephen.gamelib.opengl.widget;

import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

/**
 * An interface implemented by {@link com.nickstephen.gamelib.opengl.Shape} to deal with touch events.
 * Classes must determine whether the touch at a certain position is within their bounds and then
 * what to do if it is.
 * @author Nick Stephen
 */
public interface ITouchL {
    /**
     * Check whether a touch event is inside the shape.
     * @param posX The x position of the touch
     * @param posY The y position of the touch
     * @return True if the touch event is inside the shape, false otherwise
     */
    public boolean withinBounds(float posX, float posY);

    /**
     * Give a touch event to this shape. Implementers can choose whether to call
     * {@link #withinBounds(float, float)} from the container and only then call this method, or
     * pass all calls through this method and only then check within bounds.
     * @param e The original motion event
     * @param relativePosX The position of the touch event relative to the parent container's offset
     *                     (x-axis, pixels)
     * @param relativePosY The position of the touch event relative to the parent container's offset
     *                     (y-axis, pixels)
     * @return True if this shape, or a child shape consumed the event, false otherwise
     */
    public boolean onTouchEvent(@NotNull MotionEvent e, float relativePosX, float relativePosY);
}
