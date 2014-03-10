package com.nickstephen.gamelib.opengl.widget;

import android.content.Context;

import com.nickstephen.gamelib.opengl.GLText;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.BatchTextProgram;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by Nick Stephen on 7/03/14.
 */
public class Text extends Shape {
    private final GLText mTextInstance;
    private String mText;
    private boolean mCentered = true;

    public Text(@NotNull Context context, @Nullable Container parent, @NotNull String text, @NotNull String fontPath) {
        super(context, parent, new BatchTextProgram());

        mTextInstance = new GLText(context.getAssets());
        mTextInstance.load(fontPath, 40, 0, 0);
        //mTextInstance.setScale(2.0f);
        mText = text;
    }

    public boolean isTextCentered() {
        return mCentered;
    }

    public void setTextCentered(boolean val) {
        if (mCentered == val) {
            return;
        }
        mCentered = val;

        if (val) {
            move(-mTextInstance.getLength(mText) / 2.0f, -mTextInstance.getCharHeight() / 2.0f);
        } else {
            move(mTextInstance.getLength(mText) / 2.0f, mTextInstance.getCharHeight() / 2.0f);
        }
    }

    @Override
    public void moveTo(float newX, float newY) {
        if (mCentered) {
            newX -= mTextInstance.getLength(mText) / 2.0f;
            newY -= mTextInstance.getCharHeight() / 2.0f;
        }

        super.moveTo(newX, newY);
    }

    @Override
    public void draw(float[] mvpMatrix) {
        mTextInstance.begin(mvpMatrix);

        if (mCentered) {
            mTextInstance.draw(mText, this.getX(), this.getY());
        } else {
            mTextInstance.draw(mText, this.getX(), this.getY());
        }
        mTextInstance.end();
    }
}
