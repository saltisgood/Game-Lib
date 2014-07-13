package com.nickstephen.gamelib.run.tasking;

import com.nickstephen.gamelib.run.tasking.ITask;

/**
 * Created by Nick Stephen on 13/07/2014.
 */
public abstract class SimpleTask implements ITask {
    @Override
    public boolean start() {
        return true;
    }

    @Override
    public void end() {

    }

    @Override
    public void kill() {

    }
}
