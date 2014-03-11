package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Polygon;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Convenience extension to {@link com.nickstephen.gamelib.opengl.Polygon} for drawing square primitives.
 * @see com.nickstephen.gamelib.opengl.widget.Circle
 * @see com.nickstephen.gamelib.opengl.widget.Triangle
 * @author Nick Stephen
 */
public class Square extends Polygon {
    private static float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    /**
     * Sets up the drawing object data for use in an OpenGL ES context.
     */
    public Square(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius) {
        super(context, parent, posX, posY, radius, 0, 4, color);
    }

    public Square(@NotNull Context context, @Nullable Container parent) {
        super(context, parent, 0, 0, 100.0f, 0, 4, color);
    }

    public Square(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle) {
        super(context, parent, posX, posY, radius, angle, 4, color);
    }

    public Square(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle, @NotNull float[] colour) {
        super(context, parent, posX, posY, radius, angle, 4, colour);
    }
}
