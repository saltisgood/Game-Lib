package com.nickstephen.gamelib;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public final class GeneralUtil {
    private GeneralUtil() {} // Don't call!

    public static @NotNull float[] arrayCopy(@NotNull float[] input) {
        float[] array = new float[input.length];

        for (int i = 0; i < input.length; i++) {
            array[i] = input[i];
        }

        return array;
    }

    public static long arrayAverage(@NotNull long[] input) {
        long ave = input[0];

        for (int i = 1; i < input.length; i++) {
            ave = ave / 2 + input[i] / 2;
        }

        return ave;
    }
}
