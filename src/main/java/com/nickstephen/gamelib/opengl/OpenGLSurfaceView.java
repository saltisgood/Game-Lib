package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.support.v4.view.GestureDetectorCompat;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.gestures.GestureControl;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.text.Text;
import com.nickstephen.gamelib.run.Game;

import org.jetbrains.annotations.NotNull;

/**
 * A simple extension over {@link android.opengl.GLSurfaceView} that enforce compatability with the
 * rest of the library, so it should be used instead of GLSurfaceView inside of
 * {@link android.app.Activity}s and {@link android.support.v4.app.Fragment}s.
 *
 * For instance, makes sure to use OpenGL ES 2.0 and calls
 * {@link com.nickstephen.gamelib.opengl.text.Text#destroyInstance()} on pausing the GL Thread so
 * as not to crash on restart.
 *
 * Make sure to call the {@link #init(com.nickstephen.gamelib.opengl.Renderer)} or {@link #init()}
 * methods after construction and before use to set the renderer.
 * @author Nick Stephen
 */
public class OpenGLSurfaceView extends GLSurfaceView {
    protected final Context mContext;
    protected com.nickstephen.gamelib.opengl.Renderer mRenderer;

    private final GestureDetectorCompat mGestureDetector;

    /**
     * Initialise the surface view and set the OpenGL version.
     * @param context A context
     */
    public OpenGLSurfaceView(@NotNull Context context) {
        super(context);

        mContext = context;

        GestureControl controller = new GestureControl();
        mGestureDetector = new GestureDetectorCompat(context, controller);
        mGestureDetector.setOnDoubleTapListener(controller);

        this.setEGLContextClientVersion(2);
    }

    /**
     * Default initialisation. Use this if you don't have a new renderer to use.
     */
    public void init() {
        this.setRenderer(mRenderer = new com.nickstephen.gamelib.opengl.Renderer(mContext, this));
    }

    /**
     * Proper initialisation. Use this to set the renderer for the surface. The new renderer can just
     * be a very simple extension to {@link com.nickstephen.gamelib.opengl.Renderer} that creates new
     * shapes at surface creation.
     * @param renderer The renderer to set
     */
    public void init(@NotNull com.nickstephen.gamelib.opengl.Renderer renderer) {
        this.setRenderer(mRenderer = renderer);
    }

    /**
     * Simple override to onPause that destroys the instance of {@link com.nickstephen.gamelib.opengl.text.TextUtil}
     * so as not to crash on restart. Note that this means you will have to reinitialise it on resume,
     * but this should be taken care of if you are recreating any {@link com.nickstephen.gamelib.opengl.text.Text}
     * shapes.
     */
    @Override
    public void onPause() {
        if (mRenderer != null) {
            mRenderer.onDestroy();

            while (mRenderer.hasView()) {
                Thread.yield();
            }
        }

        Text.destroyInstance();

        super.onPause();
    }

    /**
     * Passes the touch event to the gesture detector, which may then pass it to child containers.
     * @param event The motion event
     * @return True always
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);

        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            Game.getInstanceUnsafe().addInput(GestureEvent.construct(event, null, GestureEvent.Type.FINISH, 0f, 0f));
        }
        return true;
    }
}
