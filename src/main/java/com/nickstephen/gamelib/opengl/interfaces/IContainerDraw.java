package com.nickstephen.gamelib.opengl.interfaces;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 9/05/2014.
 */
public interface IContainerDraw {
    public void draw(@NotNull float[] projMatrix, @NotNull float[] viewMatrix);
}
