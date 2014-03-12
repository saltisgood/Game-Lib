package com.nickstephen.gamelib.run;

import com.nickstephen.gamelib.anim.Animation;
import com.nickstephen.lib.Twig;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class GameLoop implements Runnable {
    public static final long[] HZ_60 = new long[] { 16, 17, 17 };
    public static final long[] HZ_80 = new long[] { 12, 13 };

    protected static GameLoop sInstance;

    public static GameLoop getInstanceUnsafe() {
        return sInstance;
    }

    private long[] mUpdateHzArray;
    private int mUpdateHzIndex;
    private boolean mStop = false;
    private int mTicks;
    private List<Animation> mAnimations;
    private boolean mPause = true;

    protected GameLoop(@NotNull long[] updateHz) {
        mUpdateHzArray = updateHz;
        mAnimations = new ArrayList<Animation>();
    }

    @Override
    public void run() {
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

    protected void tick(long now) {
        updateGameLogic();
        updateAnimations(now);
    }

    protected void updateGameLogic() {

    }

    protected void updateAnimations(long now) {
        for (int i = 0; i < mAnimations.size(); i++) {
            Animation a = mAnimations.get(i);

            a.onUpdate(now);
            if (a.shouldFinish(now)) {
                mAnimations.remove(a);
                i--;
            }
        }
    }

    protected void onExit() {

    }

    public final void addAnimation(Animation anim) {
        mAnimations.add(anim);
    }

    private long updateInterval() {
        return mUpdateHzArray[mUpdateHzIndex++ % mUpdateHzArray.length];
    }

    public void pause() {
        mPause = true;
    }

    public void resume() {
        mPause = false;
    }

    public void stop() {
        mStop = true;
    }
}
