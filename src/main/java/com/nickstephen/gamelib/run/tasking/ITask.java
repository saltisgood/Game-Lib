package com.nickstephen.gamelib.run.tasking;

/**
 * Created by Nick Stephen on 13/07/2014.
 */
public interface ITask {
    public abstract boolean start();
    public abstract boolean update();
    public abstract void end();
    public abstract void kill();
}
