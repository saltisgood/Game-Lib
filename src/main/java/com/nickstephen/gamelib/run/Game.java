package com.nickstephen.gamelib.run;

import android.content.Context;

import com.nickstephen.gamelib.opengl.OpenGLSurfaceView;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.layout.RootContainer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nick Stephen on 23/04/2014.
 */
public abstract class Game {
    protected static Game sInstance;

    public static Game getInstanceUnsafe() {
        if (sInstance == null) {
            throw new NullPointerException("Game singleton not initialised! Sub-class gamelib.run.Game and instantiate sInstance before use!");
        }
        return sInstance;
    }

    private OpenGLSurfaceView mSurface;
    protected RootContainer mActiveView;
    private int mWidth, mHeight;
    private final List<Runnable> mActions = new LinkedList<Runnable>();
    private final List<GestureEvent> mInputs = new LinkedList<GestureEvent>();
    protected Context mContext;

    protected Game(@NotNull Context context) {
        mContext = context;
    }

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

    public void addGLThreadAction(@NotNull Runnable r) {
        mActions.add(r);
    }

    public Runnable getGLThreadAction() {
        if (mActions.size() > 0) {
            return mActions.remove(0);
        }
        return null;
    }

    public void addInput(@NotNull GestureEvent e) {
        if (!consumeInputEvent(e)) {
            mInputs.add(e);
        }
    }

    public Context getContext() {
        return mContext;
    }

    protected void setContext(@NotNull Context context) {
        mContext = context;
    }

    public void releaseContext() {
        mContext = null;
    }

    /**
     * Sub-classes should override this method if there are certain inputs that should be immediately
     * used before the next game thread tick. Be careful! This will be called on the GUI thread.
     * @param e The event to consume
     * @return True to signal the event was consumed and should not be added to the input queue, false otherwise
     */
    protected boolean consumeInputEvent(@NotNull GestureEvent e) {
        return false;
    }

    public void clearInputs() {
        mInputs.clear();
    }

    @Nullable GestureEvent popInput() {
        if (mInputs.size() > 0) {
            return mInputs.remove(0);
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
