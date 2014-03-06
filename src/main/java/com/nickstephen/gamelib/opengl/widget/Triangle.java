package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Polygon;

/**
 * Created by Nick Stephen on 5/03/14.
 */
public class Triangle extends Polygon {
    private static float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Triangle(Context context) {
        super(context, 0, 0, 100.0f, 0, 3, color);
    }

    public Triangle(Context context, float posX, float posY, float radius) {
        super(context, posX, posY, radius, 0, 3, color);
    }

    public Triangle(Context context, float posX, float posY, float radius, float angle) {
        super(context, posX, posY, radius, angle, 3, color);
    }

    public Triangle(Context context, float posX, float posY, float radius, float angle, float[] colour) {
        super(context, posX, posY, radius, angle, 3, colour);
    }
}
