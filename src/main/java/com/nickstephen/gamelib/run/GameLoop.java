package com.nickstephen.gamelib.run;

import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

/**
 * Created by Nick Stephen on 12/03/14.
 */
public class GameLoop extends HandlerThread implements Handler.Callback {
    public static final int MSG_START = 0x10;
    public static final int MSG_QUITSAFE = 0x100;

    private static final String TAG = "GameLoop";

    protected static GameLoop sInstance;

    public static GameLoop getInstanceUnsafe() {
        return sInstance;
    }

    private Handler mGuiHandler;
    private Handler mGameHandler;

    public GameLoop(Handler guiHandler) {
        super(TAG);

        mGuiHandler = guiHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mGameHandler = new Handler(this);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_START:
                break;
            case MSG_QUITSAFE:
                quitSafely();
                break;
        }
        return false;
    }

    @Override
    public boolean quit() {
        sInstance = null;

        return super.quit();
    }

    @Override
    public boolean quitSafely() {
        if (Build.VERSION.SDK_INT >= 18) {
            sInstance = null;

            return super.quitSafely();
        } else {
            return quit();
        }
    }

    public Handler getGuiHandler() {
        return mGuiHandler;
    }

    public Handler getGameHandler() {
        return mGameHandler;
    }

    public boolean postGameRunnable(Runnable action) {
        return mGameHandler.post(action);
    }

    public boolean sendGameMessage(int what) {
        return mGameHandler.sendEmptyMessage(what);
    }
}
