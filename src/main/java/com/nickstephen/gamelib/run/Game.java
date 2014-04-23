package com.nickstephen.gamelib.run;

import com.nickstephen.gamelib.opengl.OpenGLSurfaceView;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.layout.RootContainer;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nick Stephen on 23/04/2014.
 */
public abstract class Game {
    protected static Game sInstance;

    public static Game getInstanceUnsafe() {
        return sInstance;
    }

    private OpenGLSurfaceView mSurface;
    private RootContainer mActiveView;
    private int mWidth, mHeight;
    private final List<Runnable> mActions = new LinkedList<Runnable>();

    public void setSurface(@NotNull OpenGLSurfaceView surface) {
        mSurface = surface;
    }

    public void setup(int width, int height) {
        mWidth = width;
        mHeight = height;
    }

    public RootContainer getActiveView() {
        return mActiveView;
    }

    public Runnable getGLThreadAction() {
        if (mActions.size() > 0) {
            return mActions.remove(0);
        }
        return null;
    }

    public void destroy() {
        GameLoop.getInstanceUnsafe().cancelAnimations(false);

        if (mActiveView != null) {
            synchronized (this) {
                final Shape shape = mActiveView;
                mSurface.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        shape.destroy();
                    }
                });
                mActiveView = null;
            }
        }

        mSurface = null;
    }

    protected final OpenGLSurfaceView getSurface() {
        return mSurface;
    }

    protected final int getWidth() {
        return mWidth;
    }

    protected final int getHeight() {
        return mHeight;
    }
}
