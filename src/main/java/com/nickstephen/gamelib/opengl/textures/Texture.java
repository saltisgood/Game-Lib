package com.nickstephen.gamelib.opengl.textures;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.os.AsyncTask;

import com.nickstephen.gamelib.opengl.shapes.Shape;
import com.nickstephen.gamelib.opengl.interfaces.IDisposable;
import com.nickstephen.gamelib.run.GameLoop;
import com.nickstephen.gamelib.util.Pair;
import com.nickstephen.lib.Twig;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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

    protected class Loader extends AsyncTask<Void, Void, Bitmap> {
        protected final Context mContext;
        protected final String mFilename;

        public Loader(@NotNull Context loadContext, @NotNull String fileName) {
            mContext = loadContext;
            mFilename = fileName;
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {
            AssetManager assets = mContext.getAssets();
            InputStream is;
            try {
                is = assets.open(mFilename);
            } catch (IOException e) {
                Twig.printStackTrace(e);
                return null;
            }

            Bitmap raw = BitmapFactory.decodeStream(is);

            try {
                is.close();
            } catch (IOException e) {
                Twig.printStackTrace(e);
                return null;
            }

            return raw;
        }


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

                if (mTexId == TEX_ID_UNASSIGNED) {
                    return TEX_ID_UNASSIGNED;
                }
            }

            if (mRegions == null) {
                mRegions = setupTextureRegion(Texture.this.mRawWidth, Texture.this.mRawHeight,
                        mSpritesX, mSpritesY);

                if (mRegions.length > 0) {
                    mParent.setTextureCoords(mRegions[0]);
                } else {
                    Twig.warning("Texture", "Zero-length texture region array");
                }
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
    protected Loader mTexLoader;

    private Texture() {}

    private void delete() {
        if (mId != TEX_ID_UNASSIGNED) {
            GLES20.glDeleteTextures(1, new int[] { mId }, 0);

            mId = TEX_ID_UNASSIGNED;
        }
    }

    protected int getId(final @NotNull Context context) {
        if (mId == TEX_ID_UNASSIGNED) {
            if (mTexLoader == null) {
                GameLoop.getInstanceUnsafe().getMainThreadHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        synchronized (Texture.this) {
                            if (mTexLoader == null) {
                                mTexLoader = new Loader(context, mName);
                                mTexLoader.execute();
                            }
                        }
                    }
                });

                return TEX_ID_UNASSIGNED;
            } else {
                if (mTexLoader.getStatus() == AsyncTask.Status.FINISHED) {
                    Bitmap result = null;
                    try {
                        result = mTexLoader.get();
                    } catch (InterruptedException e) {
                        Twig.printStackTrace(e);
                    } catch(ExecutionException e) {
                        Twig.printStackTrace(e);
                    }

                    if (result != null) {
                        mId = TextureHelper.loadTexture(result);
                        mRawWidth = result.getWidth();
                        mRawHeight = result.getHeight();
                    } else {
                        Twig.debug("Texture", "Error loading texture!");
                        throw new RuntimeException("Error loading texture!");
                    }

                    mTexLoader = null;
                } else {
                    return TEX_ID_UNASSIGNED;
                }
            }
        }

        return mId;
    }
}
