package com.nickstephen.gamelib.util;

import android.widget.Toast;

import com.nickstephen.gamelib.run.Game;
import com.nickstephen.gamelib.run.GameLoop;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 13/07/2014.
 */
public class Utilities {
    public static void toastMessageSafe(@NotNull final CharSequence mesg, final boolean shortTime) {
        GameLoop.getInstanceUnsafe().getMainThreadHandler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Game.getInstanceUnsafe().getContext(), mesg,
                        (shortTime ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG)).show();
            }
        });
    }
}
