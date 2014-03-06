package com.nickstephen.gamelib.opengl.widget;

import com.nickstephen.gamelib.opengl.Polygon;

/**
 * Created by Nick Stephen on 5/03/14.
 */
public class Triangle extends Polygon {
    private static float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Triangle() {
        super(0, 0, 100.0f, 0, 3, color);
    }

    public Triangle(float posX, float posY, float radius) {
        super(posX, posY, radius, 0, 3, color);
    }

    public Triangle(float posX, float posY, float radius, float angle) {
        super(posX, posY, radius, angle, 3, color);
    }

    public Triangle(float posX, float posY, float radius, float angle, float[] colour) {
        super(posX, posY, radius, angle, 3, colour);
    }
}
