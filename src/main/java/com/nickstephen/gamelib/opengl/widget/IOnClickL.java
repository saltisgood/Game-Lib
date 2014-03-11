package com.nickstephen.gamelib.opengl.widget;

import com.nickstephen.gamelib.opengl.Shape;

/**
 * On Click Listener interface. All click and long click listeners should implement this interface.
 * @author Nick Stephen
 */
public interface IOnClickL {
    /**
     * Method called upon clicking the shape.
     * @param shape The shape that was clicked.
     */
    public void onClick(Shape shape);
}
