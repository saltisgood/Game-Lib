package com.nickstephen.gamelib.run;

import android.os.Handler;

import com.nickstephen.gamelib.anim.Animation;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.gestures.GestureEvent;
import com.nickstephen.gamelib.opengl.layout.RootContainer;
import com.nickstephen.lib.Twig;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The main brains of the game. This is effectively a custom Looper class that runs at a certain
 * number of ticks per second on a separate thread from both the rendering and the main thread.</p>
 *
 * <p>The constructor is protected so you need to extend this class to use it, which is just as well
 * because the default implementation only handles animations. To actually implement game logic
 * implement the {@link #updateGameLogic()} method.</p>
 * @author Nick Stephen
 */
public class GameLoop implements Runnable {
    public static final long[] HZ_60 = new long[] { 16, 17, 17 };
    public static final long[] HZ_80 = new long[] { 12, 13 };

    protected static GameLoop sInstance;

    /**
     * Get a reference to the GameLoop object. There are no checks for null values so you should
     * make sure the instance is intialised before using.
     * @return A GameLoop instance
     */
    public static GameLoop getInstanceUnsafe() {
        return sInstance;
    }

    private long[] mUpdateHzArray;
    private int mUpdateHzIndex;
    private boolean mStop = false;
    private int mTicks;
    private List<Animation> mAnimations;
    private boolean mPause = true;
    private boolean mIsAlive = false;
    private Handler mHandler;

    /**
     * Construct the GameLoop (doesn't start it).
     * @param updateHz An array of times (millis) between each tick.
     */
    protected GameLoop(@NotNull long[] updateHz) {
        mUpdateHzArray = updateHz;
        mAnimations = new ArrayList<Animation>();
        mHandler = new Handler();
    }

    @Override
    public void run() {
        mIsAlive = true;
        long nextUpdate = System.currentTimeMillis();

        while (!mStop) {
            if (mPause) {
                try {
                    Thread.sleep(125);
                } catch (InterruptedException e) {
                    Twig.printStackTrace(e);
                }
                continue;
            }

            long now = System.currentTimeMillis();

            if (now < nextUpdate) {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Twig.printStackTrace(e);
                }
                continue;
            }

            tick(now);

            long post = System.currentTimeMillis();
            long update = updateInterval();

            if ((post - now) >= update) {
                Twig.debug("GameLoop", "Tick " + mTicks + " took longer than update interval");
            }

            nextUpdate += update;
            mTicks++;
        }

        onExit();
    }

    /**
     * Perform the required actions at every tick of the game. Updates the game logic and any
     * animations.
     * @param now The current system time (millis)
     */
    private void tick(long now) {
        {
            GestureEvent e;
            //noinspection StatementWithEmptyBody
            while ((e = Game.getInstanceUnsafe().popInput()) != null && !handleUserInput(e)) {}
        }
        updateGameLogic();
        updateAnimations(now);
    }

    /**
     * Write your own override method to handle user gesture events before the game logic is performed.
     * The return value determines whether to continue feeding gesture events before continuing on to
     * the rest of the tick method. i.e. If you ignored the gesture and want another one, return false.
     * The default method just passes all events onto the RootContainer with no filtering.
     *
     * The method will never be called with a null input (if there are no more gestures in the queue).
     *
     * NOTE: This is called from the Game thread, not the main thread!
     * @param e The event to consume
     * @return True to signal that the loop should continue without consuming any more gesture events
     */
    protected boolean handleUserInput(@NotNull GestureEvent e) {
        RootContainer root = Game.getInstanceUnsafe().getActiveView();
        if (root != null) {

        }

        return true;
    }

    /**
     * Write your own game logic in an implementation of this method. The default is a no-op.
     */
    protected void updateGameLogic() {

    }

    /**
     * Update any running animations
     * @param now The current system time (millis)
     */
    private void updateAnimations(long now) {
        for (int i = 0; i < mAnimations.size(); i++) {
            Animation a = mAnimations.get(i);

            a.onUpdate(now);
            if (a.shouldFinish(now)) {
                mAnimations.remove(a);
                i--;
            }
        }
    }

    /**
     * Override this method to perform any implementation specific actions upon exiting the loop
     * (because {@link #stop()} was called). Still call through to the super class because it
     * keeps track of whether the loop is running.
     */
    protected void onExit() {
        mIsAlive = false;
    }

    /**
     * Add an animation to be run
     * @param anim The animation
     */
    public final void addAnimation(Animation anim) {
        mAnimations.add(anim);
    }

    /**
     * Get the interval before the next tick
     * @return The time in millis
     */
    private long updateInterval() {
        return mUpdateHzArray[mUpdateHzIndex++ % mUpdateHzArray.length];
    }

    /**
     * Pause the loop. Will sleep the thread until {@link #resume()} is called.
     */
    public void pause() {
        mPause = true;
    }

    /**
     * Resume the loop. Only necessary if {@link #pause()} is called.
     */
    public void resume() {
        mPause = false;
    }

    /**
     * Permanently stop the loop. The loop will exit at the next iteration and {@link #onExit()} will
     * be called before the thread exits.
     */
    public void stop() {
        mStop = true;
    }

    /**
     * Check whether the loop is still alive. Alive is defined as having started running and not reached
     * the exit of the loop. This method will return true if the loop is paused but not stopped.
     * @return Whether the loop is alive
     */
    public boolean isAlive() {
        return mIsAlive;
    }

    /**
     * Check whether the loop is currently running. This is defined as both being alive and not
     * paused.
     * @return Whether the loop is running
     */
    public boolean isRunning() {
        return !mPause;
    }

    public void cancelAnimations(boolean graceful) {
        long now = System.currentTimeMillis();

        if (graceful) {
            int len = mAnimations.size();
            for (int i = 0; i < len; i++) {
                mAnimations.get(i).onFinish(now);
            }
        }
        mAnimations.clear();
    }

    public Handler getMainThreadHandler() {
        return mHandler;
    }

    public void removeAnimationsOfShape(Shape shape) {
        int len = mAnimations.size();
        for (int i = 0; i < len; i++) {
            if (mAnimations.get(i).getShape() == shape) {
                mAnimations.remove(i);
                len = mAnimations.size();
                i--;
            }
        }
    }
}
