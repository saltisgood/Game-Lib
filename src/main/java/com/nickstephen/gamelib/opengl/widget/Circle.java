package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Polygon;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 7/03/14.
 */
public class Circle extends Polygon {
    private static final float[] defColour = new float[] { 1.0f, 1.0f, 0.0f, 1.0f };

    public Circle(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius) {
        super(context, parent, posX, posY, radius, 0, 100, defColour);
    }

    public Circle(@NotNull Context context, @Nullable Container parent, float radius) {
        super(context, parent, 0, 0, radius, 0, 100, defColour);
    }

    public Circle(@NotNull Context context, @Nullable Container parent, float radius, float[] colour) {
        super(context, parent, 0, 0, radius, 0, 100, colour);
    }
}
