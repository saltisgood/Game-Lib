package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.text.FPSMeter;
import com.nickstephen.lib.VersionControl;

import org.jetbrains.annotations.NotNull;

/**
 * An extension to {@link com.nickstephen.gamelib.opengl.layout.Container} that's used as the root
 * container within the {@link com.nickstephen.gamelib.opengl.Renderer}.
 * @author Nick Stephen
 */
public class RootContainer extends Container {
    /**
     * Calls {@link #RootContainer(android.content.Context, android.opengl.GLSurfaceView, float, float, float, float)}.
     * Then adds a {@link com.nickstephen.gamelib.opengl.text.FPSMeter} to the container if the app
     * is in debug mode.
     * @param context A context
     * @param surface The surface which is being used to display the OpenGL
     * @param width The width of the container
     * @param height The height of the container
     * @param parentOffsetX The offset of the container from its parent (x-axis)
     * @param parentOffsetY The offset of the container from its parent (y-axis)
     * @param fontFile The filename of the font file to be used to initialise the
     *                 {@link com.nickstephen.gamelib.opengl.text.TextUtil} instance
     */
    public RootContainer(@NotNull Context context, @NotNull GLSurfaceView surface, float width, float height, String fontFile) {
        this(context, surface, width, height);

        if (!VersionControl.IS_RELEASE) {
            FPSMeter fps = new FPSMeter(context, this, fontFile);
            this.mChildren.add(fps);
        }
    }

    /**
     * Default constructor. Sets the surface of this Container and then passes all arguments on to the
     * super class.
     * @param context A context
     * @param surface The surface which is being used to display the OpenGL
     * @param width The width of the container
     * @param height The height of the container
     * @param parentOffsetX The offset of the container from its parent (x-axis)
     * @param parentOffsetY The offset of the container from its parent (y-axis)
     */
    public RootContainer(@NotNull Context context, @NotNull GLSurfaceView surface, float width, float height) {
        super(context, null, width, height, 0, 0);

        this.setSurface(surface);
    }

    /**
     * Simple override that always returns 0. See base method for description.
     * @return 0
     */
    @Override
    public int getAbsoluteBLCornerX() {
        return 0;
    }

    /**
     * Simple override that always returns 0. See base method for description.
     * @return 0
     */
    @Override
    public int getAbsoluteBLCornerY() {
        return 0;
    }

    /**
     * Takes the touch event from the surface, converts the coordinates to be relative to the centre
     * of the container and passes it to {@link #onTouchEvent(android.view.MotionEvent, float, float)}
     * for further processing.
     * @param e The touch event from the surface
     * @return True if a screen component consumed the event, false otherwise
     */
    public boolean onTouchEvent(MotionEvent e) {
        float relX = e.getRawX() - (getScreenWidth() / 2.0f);
        float relY = -(e.getRawY() - (getScreenHeight() / 2.0f));

        return onTouchEvent(e, relX, relY);
    }
}
