package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Polygon;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Convenience extension to {@link com.nickstephen.gamelib.opengl.Polygon} for easy drawing of
 * triangle primitives.
 * @see com.nickstephen.gamelib.opengl.widget.Square
 * @see com.nickstephen.gamelib.opengl.widget.Circle
 * @author Nick Stephen
 */
public class Triangle extends Polygon {
    private static float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 0.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Triangle(@NotNull Context context, @Nullable Container parent) {
        super(context, parent, 0, 0, 100.0f, 0, 3, color);
    }

    public Triangle(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius) {
        super(context, parent, posX, posY, radius, 0, 3, color);
    }

    public Triangle(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle) {
        super(context, parent, posX, posY, radius, angle, 3, color);
    }

    public Triangle(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle, float[] colour) {
        super(context, parent, posX, posY, radius, angle, 3, colour);
    }
}
