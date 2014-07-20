package com.nickstephen.gamelib.opengl.textures;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.TextureHelper;
import com.nickstephen.gamelib.opengl.TextureRegion;
import com.nickstephen.gamelib.opengl.interfaces.IDisposable;
import com.nickstephen.gamelib.util.Pair;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Stephen on 16/07/2014.
 */
public class Texture {
    public static final int TEX_ID_UNASSIGNED = 0;

    public static class Manager {
        private final static Manager sInst = new Manager();

        public static Client get(@NotNull String name, @NotNull Shape shape, @NotNull Context texLoadContext) {
            synchronized (sInst.mNamedTexts) {
                for (int i = sInst.mNamedTexts.size() - 1; i >= 0; --i) {
                    Pair<Texture, Integer> p = sInst.mNamedTexts.get(i);

                    if (p.left.mName.compareTo(name) == 0) {
                        ++p.right;
                        return p.left.new Client(shape, texLoadContext);
                    }
                }

                Texture t = new Texture();
                t.mName = name;
                sInst.mNamedTexts.add(new Pair<Texture, Integer>(t, 1));
                return t.new Client(shape, texLoadContext);
            }
        }

        private static void release(@NotNull Texture t) {
            synchronized (sInst.mNamedTexts) {
                for (int i = sInst.mNamedTexts.size() - 1; i >= 0; --i) {
                    Pair<Texture, Integer> p = sInst.mNamedTexts.get(i);

                    if (p.left.equals(t)) {
                        --p.right;

                        if (p.right <= 0) {
                            p.left.delete();
                        }

                        return;
                    }
                }
            }
        }

        private final List<Pair<Texture, Integer>> mNamedTexts = new ArrayList<Pair<Texture, Integer>>();

        private Manager() {}

    }

    protected static @NotNull TextureRegion[] setupTextureRegion(float rawW, float rawH, int spritesX, int spritesY) {
        TextureRegion[] regions = new TextureRegion[spritesX * spritesY];

        float cellWidth = rawW / (float) spritesX, cellHeight = rawH / (float) spritesY;

        for (int j = 0, c = 0; j < spritesY; j++) {
            for (int i = 0; i < spritesX; i++) {
                regions[c++] = new TextureRegion(rawW, rawH, i * cellWidth, j * cellHeight,
                        cellWidth, cellHeight);
            }
        }

        return regions;
    }

    public class Client implements IDisposable {
        protected final Shape mParent;
        protected Context mTexLoadContext;
        protected TextureRegion[] mRegions;
        protected int mTexId;
        protected int mSpritesX;
        protected int mSpritesY;

        private Client(@NotNull Shape shape, @NotNull Context context) {
            mParent = shape;
            mTexLoadContext = context;
            mTexId = Texture.this.mId;
        }

        @Override
        public void dispose() {
            Manager.release(Texture.this);
        }

        public Client setTextureDimensions(int numX, int numY) {
            mSpritesX = numX;
            mSpritesY = numY;
            return this;
        }

        public int getId() {
            if (mTexId == TEX_ID_UNASSIGNED) {
                mTexId = Texture.this.getId(mTexLoadContext);
            }

            if (mRegions == null) {
                mRegions = setupTextureRegion(Texture.this.mRawWidth, Texture.this.mRawHeight,
                        mSpritesX, mSpritesY);

                mParent.setTextureCoords(mRegions[0]);
            }

            return mTexId;
        }

        public TextureRegion[] getTexRegions() {
            return mRegions;
        }
    }

    private String mName;
    protected int mId;
    protected float mRawWidth;
    protected float mRawHeight;

    private Texture() {}

    private void delete() {
        if (mId != TEX_ID_UNASSIGNED) {
            GLES20.glDeleteTextures(1, new int[] { mId }, 0);

            mId = TEX_ID_UNASSIGNED;
        }
    }

    protected int getId(@NotNull Context context) {
        if (mId == TEX_ID_UNASSIGNED) {
            AssetManager assets = context.getAssets();
            InputStream is;
            try {
                is = assets.open(mName);
            } catch (IOException e) {
                return 0;
            }

            Bitmap raw = BitmapFactory.decodeStream(is);

            try {
                is.close();
            } catch (IOException e) {
                return 0;
            }

            mId = TextureHelper.loadTexture(raw);

            mRawWidth = raw.getWidth();
            mRawHeight = raw.getHeight();

            //mTextureRegions = setupTextureRegion(raw, mSpritesX, mSpritesY);
            //setTextureCoords(mTextureRegions[0]);
        }

        return mId;
    }
}
