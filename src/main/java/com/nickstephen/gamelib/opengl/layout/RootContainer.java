package com.nickstephen.gamelib.opengl.layout;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.provider.SyncStateContract;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.text.FPSMeter;
import com.nickstephen.gamelib.opengl.text.Text;
import com.nickstephen.lib.VersionControl;

import org.jetbrains.annotations.NotNull;

/**
 * An extension to {@link com.nickstephen.gamelib.opengl.layout.Container} that's used as the root
 * container within the {@link com.nickstephen.gamelib.opengl.Renderer}.
 * @author Nick Stephen
 */
public class RootContainer extends Container {
    /**
     * Calls {@link #RootContainer(android.content.Context, android.opengl.GLSurfaceView, float, float)}.
     * Then adds a {@link com.nickstephen.gamelib.opengl.text.FPSMeter} to the container if the app
     * is in debug mode.
     * @param context A context
     * @param surface The surface which is being used to display the OpenGL
     * @param width The width of the container
     * @param height The height of the container
     * @param fontFile The filename of the font file to be used to initialise the
     *                 {@link com.nickstephen.gamelib.opengl.text.TextUtil} instance
     */
    public RootContainer(@NotNull Context context, @NotNull GLSurfaceView surface, float width, float height, @NotNull String fontFile) {
        this(context, surface, width, height);

        if (!VersionControl.IS_RELEASE) {
            Text.Font f = Text.FontManager.getDefaultFont();
            if (f != null) {
                FPSMeter fps = new FPSMeter(context, this, f);
                this.mChildren.add(fps);
            }
        }
    }

    /**
     * Default constructor. Sets the surface of this Container and then passes all arguments on to the
     * super class.
     * @param context A context
     * @param surface The surface which is being used to display the OpenGL
     * @param width The width of the container
     * @param height The height of the container
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
     * The start point for gesture inputs into the layout hierarchy. Use this method and only this
     * method to input the gesture events!
     *
     * It converts the gesture's position from the Top-Left of the screen to the more OpenGL correct
     * centre based position.
     *
     * @param e The input gesture
     * @return True if the gesture was consumed, false otherwise
     */
    public boolean onGestureEvent(@NotNull GestureEvent e) {
        float relX = e.originalX - (getScreenWidth() / 2.0f);
        float relY = -(e.originalY - (getScreenHeight() / 2.0f));

        return onGestureEvent(e, relX, relY);
    }

    @Override
    public void setScrollable(boolean val) {
        // RootContainer's can't scroll! Breaks glScissor for children.
    }
}
