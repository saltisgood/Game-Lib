package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import com.nickstephen.gamelib.opengl.layout.RootContainer;

import org.jetbrains.annotations.NotNull;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * <p>An implementation of {@link android.opengl.GLSurfaceView.Renderer} that should be the base for any
 * calls to {@link com.nickstephen.gamelib.opengl.OpenGLSurfaceView#setRenderer(android.opengl.GLSurfaceView.Renderer)}.</p>
 *
 * <p>By default this renderer uses the raw pixel dimensions of the screen for its projection matrix.
 * If you wish to use relative dimensions (1.0 x 1.0) or density independent pixels (dip) you may
 * wish to provide an override for {@link #onSurfaceChanged(javax.microedition.khronos.opengles.GL10, int, int)}.</p>
 *
 * @author Nick Stephen
 */
public class Renderer implements GLSurfaceView.Renderer {
    protected final Context mContext;
    protected RootContainer mContentContainer;
    protected GLSurfaceView mSurface;
    private float[] mBaseViewMatrix = new float[16];
    private float[] mProjMatrix = new float[16];
    private float[] mVPMatrix = new float[16];
    private int mWidth, mHeight;

    /**
     * Constructor.
     * @param context A context
     * @param surface The surface to which the renderer is (going) to be attached
     */
    public Renderer(@NotNull Context context, @NotNull GLSurfaceView surface) {
        mContext = context;
        mSurface = surface;
    }

    /**
     * Callback when the frame is to be drawn to screen.
     * @param gl
     */
    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

        GLES20.glClear(clearMask);

        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mBaseViewMatrix, 0);

        onDraw(mProjMatrix, mBaseViewMatrix);
    }

    public void onDraw(float[] projMatrix, float[] viewMatrix) {
        if (mContentContainer != null) {
            mContentContainer.draw(projMatrix, viewMatrix);
        }
    }

    /**
     * Callback when the dimensions of the surface are changed.
     * @param gl
     * @param width
     * @param height
     */
    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);

        mWidth = width;
        mHeight = height;

        float ratio = (float) width / height;

        // Take into account device orientation
        if (width > height) {
            Matrix.frustumM(mProjMatrix, 0, -ratio, ratio, -1, 1, 1, 10);
        }
        else {
            Matrix.frustumM(mProjMatrix, 0, -1, 1, -1/ratio, 1/ratio, 1, 10);
        }

        int useForOrtho = Math.min(width, height);

        Matrix.orthoM(mBaseViewMatrix, 0,
                -useForOrtho/2,
                useForOrtho/2,
                -useForOrtho/2,
                useForOrtho/2, 0.1f, 100f);
    }

    /**
     * Callback when the surface is first created.
     * @param gl
     * @param config
     */
    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    /**
     * Method for passing the surface's touch event on to the GL content
     * @param e The motion event
     * @return True if the event was consumed, false otherwise
     */
    public boolean onTouchEvent(MotionEvent e) {
        return mContentContainer.onTouchEvent(e);
    }

    public void setContent(RootContainer root) {
        mContentContainer = root;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public void onDestroy() {
        if (mContentContainer != null) {
            final Shape shape = mContentContainer;
            mSurface.queueEvent(new Runnable() {
                @Override
                public void run() {
                    shape.destroy();
                }
            });
            mContentContainer = null;
        }
    }
}
