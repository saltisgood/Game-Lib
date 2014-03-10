package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.view.MotionEvent;
import com.nickstephen.gamelib.opengl.layout.RootContainer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Nick Stephen on 11/03/14.
 */
public class Renderer implements GLSurfaceView.Renderer {
    protected final Context mContext;
    protected RootContainer mContentContainer;
    private float[] mProjMatrix = new float[16];
    private float[] mBaseViewMatrix = new float[16];
    private float[] mVPMatrix = new float[16];
    protected GLSurfaceView mSurface;

    public Renderer(Context context, GLSurfaceView surface) {
        mContext = context;
        mSurface = surface;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
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

    @Override
    public void onDrawFrame(GL10 gl) {
        // Redraw background color
        int clearMask = GLES20.GL_COLOR_BUFFER_BIT;

        GLES20.glClear(clearMask);

        Matrix.multiplyMM(mVPMatrix, 0, mProjMatrix, 0, mBaseViewMatrix, 0);

        if (mContentContainer != null) {
            mContentContainer.draw(mProjMatrix, mBaseViewMatrix);
        }
    }

    public boolean onTouchEvent(MotionEvent e) {
        return mContentContainer.onTouchEvent(e);
    }
}
