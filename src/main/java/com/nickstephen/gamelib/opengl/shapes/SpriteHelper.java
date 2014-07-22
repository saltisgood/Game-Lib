package com.nickstephen.gamelib.opengl.shapes;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nickstephen.gamelib.opengl.textures.TextureRegion;
import com.nickstephen.lib.Twig;

import org.jetbrains.annotations.NotNull;

/**
 * <p>An extension to {@link Vertices} that is used just for batch rendering
 * sprites.</p>
 *
 * <p>If using this class instead of manually using {@link Vertices},
 * you should only use methods inside this class.</p>
 *
 * @author Nick Stephen
 */
public class SpriteHelper extends Vertices {
    public static final int MAX_SPRITES = 24;

    private static final int INDICES_PER_SPRITE = 6;
    private static final int MAT4_SIZE = 16;
    private static final int VERTICES_PER_SPRITE = 4;

    private int mMaxSprites;
    private float[] mModelMatrices;
    private int mNumSprites;

    /**
     * Constructor that uses {@link #MAX_SPRITES} for the maxSprites argument of the other
     * constructor.
     * @param shape The shape to associate with these vertices
     */
    public SpriteHelper(@NotNull Shape shape) {
        this(shape, MAX_SPRITES);
    }

    /**
     * Default constructor. Note that if maxSprites is greater than {@link #MAX_SPRITES} it will just
     * get set to that instead.
     * @param shape The shape to associate with these vertices
     * @param maxSprites The maximum number of sprites to render at once
     */
    public SpriteHelper(@NotNull Shape shape, int maxSprites) {
        super(shape,
                VERTICES_PER_SPRITE * ((maxSprites > MAX_SPRITES) ? MAX_SPRITES : maxSprites),
                INDICES_PER_SPRITE * ((maxSprites > MAX_SPRITES) ? MAX_SPRITES : maxSprites),
                GLES20.GL_TRIANGLES);

        if (!mUsesMVPIndex || !mUsesTextureCoords || !mUsesTexture) {
            throw new IllegalArgumentException("Program must use MVP indices, texture coordinates and textures to use SpriteHelper!");
        }

        mMaxSprites = (maxSprites > MAX_SPRITES) ? MAX_SPRITES : maxSprites;

        mModelMatrices = new float[MAT4_SIZE * mMaxSprites];

        mVertexCoords = new float[mPositionCount * mNumVertices];
        mTexCoords = new float[mNumVertices * Vertices.TEXCOORD_CNT];
        mMVPIndices = new float[mNumVertices * VERTICES_PER_SPRITE];

        setupIndices();

        if ((mNumSprites * MAT4_SIZE) > mNumVertices * mVertexStride) {
            mScratch = new float[mNumSprites * MAT4_SIZE];
        }
    }

    /**
     * Add a sprite to be displayed.
     * @param x The centre x offset of this sprite
     * @param y The centre y offset of this sprite
     * @param width The width of the sprite
     * @param height The height of the sprite
     * @param region The region of the texture associated with this sprite
     * @param modelMatrix The model matrix to apply to this sprite
     */
    public void addSpriteToBatch(float x, float y, float width, float height, TextureRegion region,
                                 float[] modelMatrix) {
        if (mNumSprites == mMaxSprites) {
            Twig.debug("SpriteHelper", "Max sprites reached, ignoring this new one");
            return;
        }

        float halfWidth = width / 2.0f;                 // Calculate Half Width
        float halfHeight = height / 2.0f;               // Calculate Half Height
        float x1 = x - halfWidth;                       // Calculate Left X
        float y1 = y - halfHeight;                      // Calculate Bottom Y
        float x2 = x + halfWidth;                       // Calculate Right X
        float y2 = y + halfHeight;                      // Calculate Top Y

        int index = VERTICES_PER_SPRITE * mNumSprites * mPositionCount;

        mVertexCoords[index++] = x1;            // Add X for Vertex 0
        mVertexCoords[index++] = y1;            // Add Y for Vertex 0
        mVertexCoords[index++] = x2;            // Add X for Vertex 1
        mVertexCoords[index++] = y1;            // Add Y for Vertex 1
        mVertexCoords[index++] = x2;            // Add X for Vertex 2
        mVertexCoords[index++] = y2;            // Add Y for Vertex 2
        mVertexCoords[index++] = x1;            // Add X for Vertex 3
        mVertexCoords[index] = y2;              // Add Y for Vertex 3

        index = Vertices.TEXCOORD_CNT * mNumSprites * VERTICES_PER_SPRITE;

        mTexCoords[index++] = region.u1;        // Add U for Vertex 0
        mTexCoords[index++] = region.v2;        // Add V for Vertex 0
        mTexCoords[index++] = region.u2;        // Add U for Vertex 1
        mTexCoords[index++] = region.v2;        // Add V for Vertex 1
        mTexCoords[index++] = region.u2;        // Add U for Vertex 2
        mTexCoords[index++] = region.v1;        // Add V for Vertex 2
        mTexCoords[index++] = region.u1;        // Add U for Vertex 3
        mTexCoords[index] = region.v1;          // Add V for Vertex 3

        index = Vertices.MVP_MATRIX_INDEX_CNT * mNumSprites * VERTICES_PER_SPRITE;

        mMVPIndices[index++] = mNumSprites;
        mMVPIndices[index++] = mNumSprites;
        mMVPIndices[index++] = mNumSprites;
        mMVPIndices[index] = mNumSprites;

        index = mNumSprites * MAT4_SIZE;
        System.arraycopy(modelMatrix, 0, mModelMatrices, index, MAT4_SIZE);

        mNumSprites++;
    }

    /**
     * Overridden in order to generate all the MVP matrices associated with the sprites given a
     * view/projection matrix.
     * @param vpMatrix The view/projection matrix to use when drawing
     */
    @Override
    public synchronized void draw(@NotNull float[] vpMatrix) {
        for (int i = 0; i < mNumSprites; i++) {
            Matrix.multiplyMM(mScratch, i * MAT4_SIZE, vpMatrix, 0, mModelMatrices, i * MAT4_SIZE);
        }
        mNumMVPMatrices = mNumSprites;

        super.draw(mScratch);
    }

    /**
     * Call once you've finished adding sprites. This locks in the sprite information into the
     * float buffers in the super class.
     */
    public void finishAddingSprites() {
        resetFloatBuffer();
    }

    /**
     * Reset. This sets the number of sprites to 0, so you can restart adding sprites.
     * Note to self, this could also be a mechanism for drawing more than 24 sprites in a single
     * rendering pass if you wanted to...
     */
    public void reset() {
        mNumSprites = 0;
    }

    /**
     * Convenience method for setting up the indices. Should only be called from the constructor.
     */
    private void setupIndices() {
        int len = INDICES_PER_SPRITE * mMaxSprites;
        short[] indices = new short[len];

        short j = 0;                                    // Counter
        for ( int i = 0; i < len; i+= INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE )  {  // FOR Each Index Set (Per Sprite)
            indices[i + 0] = (short)( j + 0 );           // Calculate Index 0
            indices[i + 1] = (short)( j + 1 );           // Calculate Index 1
            indices[i + 2] = (short)( j + 2 );           // Calculate Index 2
            indices[i + 3] = (short)( j + 2 );           // Calculate Index 3
            indices[i + 4] = (short)( j + 3 );           // Calculate Index 4
            indices[i + 5] = (short)( j + 0 );           // Calculate Index 5
        }

        setIndices(indices, 0, len);
    }
}
