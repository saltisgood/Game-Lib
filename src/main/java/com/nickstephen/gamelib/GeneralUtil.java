package com.nickstephen.gamelib;

import android.content.Context;
import android.os.Vibrator;

import org.jetbrains.annotations.NotNull;

/**
 * General utility functions not covered by other classes.
 * @author Nick Stephen
 */
public final class GeneralUtil {
    private GeneralUtil() {} // Don't call!

    private static Vibrator sVibrator;

    /**
     * Finds the average value in an array of type long.
     * @param input The input array to check.
     * @return The average value in the array.
     */
    public static long arrayAverage(@NotNull long[] input) {
        long ave = input[0];

        for (int i = 1; i < input.length; i++) {
            ave = ave / 2 + input[i] / 2;
        }

        return ave;
    }

    /**
     * Get a reference to the Vibrator system service
     * @param context A context
     */
    public static void setupVibrator(@NotNull Context context) {
        if (sVibrator == null) {
            sVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        }
    }

    /**
     * <p>Attempt to vibrate the phone for a given time. Make sure to call
     * {@link #setupVibrator(android.content.Context)} to get a reference to the Vibrator first. But
     * the method is null safe.</p>
     *
     * <p><strong>NOTE: The calling app must have VIBRATE permission to use this function!</strong></p>
     * @param time The length of time to vibrate for.
     */
    public static void vibrate(long time) {
        if (sVibrator != null) {
            sVibrator.vibrate(time);
        }
    }
}
