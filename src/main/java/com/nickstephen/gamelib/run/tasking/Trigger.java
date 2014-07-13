package com.nickstephen.gamelib.run.tasking;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 13/07/2014.
 */
public class Trigger extends SimpleTask {
    private ITriggerTest mTest;
    private boolean mTriggered = false;
    private Runnable mTask;

    public Trigger(@NotNull Runnable task, @NotNull ITriggerTest test) {
        mTest = test;
        mTask = task;
    }

    @Override
    public boolean update() {
        if (mTriggered) {
            return true;
        }

        if (mTest.check()) {
            mTriggered = true;
            mTask.run();
        }

        return mTriggered;
    }
}
