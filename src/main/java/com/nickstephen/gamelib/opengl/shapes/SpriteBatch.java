package com.nickstephen.gamelib.opengl.shapes;

import android.content.Context;

import com.nickstephen.gamelib.opengl.bounds.Multiple;
import com.nickstephen.gamelib.opengl.bounds.Quadrilateral;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Stephen on 16/07/2014.
 */
public class SpriteBatch extends Shape {
    protected final List<Sprite> mSprites;

    public SpriteBatch(@NotNull Context context, @NotNull Container parent) {
        super(context, parent, Program.TestTextProgram.create());

        mVertices = new SpriteHelper(this);
        mBoundsChecker = new Multiple<Quadrilateral>(this);
        mSprites = new ArrayList<Sprite>();
    }

    public void addSpriteToBatch(@NotNull Sprite sprite) {
        Quadrilateral quadBounds = (Quadrilateral) sprite.mBoundsChecker;

        ((Multiple<Quadrilateral>) mBoundsChecker).addBound(quadBounds);

        ((SpriteHelper) mVertices).addSpriteToBatch(0, 0, quadBounds.getWidth(), quadBounds.getHeight(),
                sprite.getCurrentTextureRegion(), sprite.getModelMatrix());

        mSprites.add(sprite);
    }

    public void clear(boolean alsoDispose) {
        if (alsoDispose) {
            for (int i = mSprites.size() - 1; i >= 0; --i) {
                mSprites.get(i).dispose();
            }
        }

        mSprites.clear();
        ((SpriteHelper) mVertices).reset();
    }

    public void refresh() {
        ((SpriteHelper) mVertices).finishAddingSprites();
    }

    @Override
    public void dispose() {
        super.dispose();

        clear(true);
    }
}
