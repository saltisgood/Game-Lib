package com.nickstephen.gamelib.opengl.gestures;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 24/04/2014.
 */
public interface IGestureL {
    /**
     * Check whether a gesture event is inside the shape.
     * @param posX The x position of the gesture
     * @param posY The y position of the gesture
     * @return True if the gesture event is inside the shape, false otherwise
     */
    public boolean withinBounds(float posX, float posY);

    /**
     * Give a gesture event to this shape. Implementers can choose whether to call
     * {@link #withinBounds(float, float)} from the container and only then call this method, or
     * pass all calls through this method and only then check within bounds.
     * @param e The gesture event
     * @return True if this shape, or a child shape consumed the event, false otherwise
     */
    public boolean onGestureEvent(@NotNull GestureEvent e, float relativePosX, float relativePosY);
}
