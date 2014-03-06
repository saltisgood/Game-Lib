package com.nickstephen.gamelib.opengl.widget;

import com.nickstephen.gamelib.opengl.Polygon;

/**
 * Created by Nick Stephen on 5/03/14.
 */
public class Square extends Polygon {
    private static float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Square(float posX, float posY, float radius) {
        super(posX, posY, radius, 0, 4, color);
    }

    public Square() {
        super(0, 0, 100.0f, 0, 4, color);
    }

    public Square(float posX, float posY, float radius, float angle) {
        super(posX, posY, radius, angle, 4, color);
    }

    public Square(float posX, float posY, float radius, float angle, float[] colour) {
        super(posX, posY, radius, angle, 4, colour);
    }
}
