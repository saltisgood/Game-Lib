package com.nickstephen.gamelib;

import android.content.Context;
import android.os.Vibrator;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public final class GeneralUtil {
    private GeneralUtil() {} // Don't call!

    private static Vibrator sVibrator;

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

    public static void setupVibrator(@NotNull Context context) {
        sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }

    public static void vibrate(long time) {
        if (sVibrator != null) {
            sVibrator.vibrate(time);
        }
    }
}
