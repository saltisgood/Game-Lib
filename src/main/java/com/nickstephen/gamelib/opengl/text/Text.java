package com.nickstephen.gamelib.opengl.text;

import android.content.Context;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.SpriteHelper;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.BatchTextProgram;

import org.jetbrains.annotations.NotNull;

/**
 * Created by Nick Stephen on 8/03/14.
 */
public class Text extends Shape {
    private static final float[] defColour = { 1.0f, 1.0f, 1.0f, 1.0f };

    public static void destroyInstance() {
        TextUtil.destroyInstance();
    }

    protected String mText;
    float mScaleX = 1.0f;
    float mScaleY = 1.0f;
    float mSpaceX;
    boolean mCentered = true;

    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile) {
        super(context, parent, new BatchTextProgram());

        this.setColour(defColour);

        TextUtil.init(context.getAssets(), fontFile);
        TextUtil.getInstance().load(this);
        setTextureId(TextUtil.getInstance().getTextureId());

        mVertices = new SpriteHelper(this);
    }

    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile, String text) {
        this(context, parent, fontFile);

        setText(text);
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;

        reloadVertices();
    }

    public void setCentered(boolean val) {
        mCentered = val;

        reloadVertices();
    }

    public void setScaleX(float x) {
        mScaleX = x;

        reloadVertices();
    }

    public void setScaleY(float y) {
        mScaleY = y;

        reloadVertices();
    }

    public void setSpaceX(float x) {
        mScaleX = x;

        reloadVertices();
    }

    public void setScale(float x, float y) {
        mScaleX = x;
        mScaleY = y;
        reloadVertices();
    }

    private void reloadVertices() {
        ((SpriteHelper) mVertices).reset();
        TextUtil.getInstance().addTextToBatch((SpriteHelper) mVertices);
        ((SpriteHelper) mVertices).finishAddingSprites();
    }

    @Override
    public void moveTo(float newX, float newY) {
        super.moveTo(newX, newY);

        reloadVertices();
    }

    @Override
    public void move(float dx, float dy) {
        super.move(dx, dy);

        reloadVertices();
    }

    @Override
    public void draw(float[] vpMatrix) {
        mVertices.draw(vpMatrix);
    }
}
