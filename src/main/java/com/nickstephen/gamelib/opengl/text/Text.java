package com.nickstephen.gamelib.opengl.text;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 8/03/14.
 */
public class Text extends Shape {
    private static final float[] defColour = { 1.0f, 1.0f, 1.0f, 1.0f };

    public static final void destroyInstance() {
        TextUtil.destroyInstance();
    }

    private String mText;
    float mScaleX = 1.0f;
    float mScaleY = 1.0f;
    float mSpaceX;
    boolean mCentered = true;

    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile) {
        super(context, parent);

        this.setColour(defColour);

        TextUtil.init(context.getAssets(), fontFile);
    }

    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile, String text) {
        this(context, parent, fontFile);

        mText = text;
    }

    @Override
    public void draw(float[] VPMatrix) {
        TextUtil.getInstance().load(this);
        TextUtil.getInstance().draw(VPMatrix);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public void setCentered(boolean val) {
        mCentered = val;
    }

    public void setScaleX(float x) {
        mScaleX = x;
    }

    public void setScaleY(float y) {
        mScaleY = y;
    }

    public void setSpaceX(float x) {
        mScaleX = x;
    }
}
