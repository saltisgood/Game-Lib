package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

import com.nickstephen.gamelib.opengl.text.Text;

/**
 * Created by Nick Stephen on 11/03/14.
 */
public class OpenGLSurfaceView extends GLSurfaceView {
    protected com.nickstephen.gamelib.opengl.Renderer mRenderer;
    protected final Context mContext;

    public OpenGLSurfaceView(Context context) {
        super(context);

        mContext = context;

        this.setEGLContextClientVersion(2);
    }

    public void defaultInit() {
        this.setRenderer(mRenderer = new com.nickstephen.gamelib.opengl.Renderer(mContext, this));
    }

    public void init(com.nickstephen.gamelib.opengl.Renderer renderer) {
        this.setRenderer(mRenderer = renderer);
    }

    @Override
    public void onPause() {
        super.onPause();

        Text.destroyInstance();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mRenderer != null) {
            return mRenderer.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }
}
