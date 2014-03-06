package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Polygon;

/**
 * Created by Nick Stephen on 7/03/14.
 */
public class Circle extends Polygon {
    private static final float[] defColour = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };

    public Circle(Context context, float posX, float posY, float radius) {
        super(context, posX, posY, radius, 0, 100, defColour);
    }

    public Circle(Context context, float radius) {
        super(context, 0, 0, radius, 0, 100, defColour);
    }

    public Circle(Context context, float radius, float[] colour) {
        super(context, 0, 0, radius, 0, 100, colour);
    }
}
