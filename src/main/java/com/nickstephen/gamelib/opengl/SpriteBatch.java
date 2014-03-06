package com.nickstephen.gamelib.opengl;

import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nickstephen.gamelib.opengl.program.Program;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class SpriteBatch {

    //--Constants--//
    final static int VERTEX_SIZE = 5;                  // Vertex Size (in Components) ie. (X,Y,U,V,M), M is MVP matrix index
    final static int VERTICES_PER_SPRITE = 4;          // Vertices Per Sprite
    final static int INDICES_PER_SPRITE = 6;           // Indices Per Sprite
    private static final String TAG = "SpriteBatch";

    //--Members--//
    private Vertices mVertices;                                 // Vertices Instance Used for Rendering
    private float[] mVertexBuffer;                              // Vertex Buffer
    private int mBufferIndex;                                   // Vertex Buffer Start Index
    private int mMaxSprites;                                    // Maximum Sprites Allowed in Buffer
    private int mNumSprites;                                    // Number of Sprites Currently in Buffer
    private float[] mVPMatrix;							// View and projection matrix specified at begin
    private float[] uMVPMatrices = new float[GLText.CHAR_BATCH_SIZE*16]; // MVP matrix array to pass to shader
    private int mMVPMatricesHandle;							// shader handle of the MVP matrix array
    private float[] mMVPMatrix = new float[16];				// used to calculate MVP matrix of each sprite

    //--Constructor--//
    // D: prepare the sprite batcher for specified maximum number of sprites
    // A: maxSprites - the maximum allowed sprites per batch
    //    program - program to use when drawing
    public SpriteBatch(int maxSprites, Program program)  {
        this.mVertexBuffer = new float[maxSprites * VERTICES_PER_SPRITE * VERTEX_SIZE];  // Create Vertex Buffer
        this.mVertices = new Vertices(maxSprites * VERTICES_PER_SPRITE, maxSprites * INDICES_PER_SPRITE);  // Create Rendering Vertices
        this.mBufferIndex = 0;                           // Reset Buffer Index
        this.mMaxSprites = maxSprites;                   // Save Maximum Sprites
        this.mNumSprites = 0;                            // Clear Sprite Counter

        short[] indices = new short[maxSprites * INDICES_PER_SPRITE];  // Create Temp Index Buffer
        int len = indices.length;                       // Get Index Buffer Length
        short j = 0;                                    // Counter
        for ( int i = 0; i < len; i+= INDICES_PER_SPRITE, j += VERTICES_PER_SPRITE )  {  // FOR Each Index Set (Per Sprite)
            indices[i + 0] = (short)( j + 0 );           // Calculate Index 0
            indices[i + 1] = (short)( j + 1 );           // Calculate Index 1
            indices[i + 2] = (short)( j + 2 );           // Calculate Index 2
            indices[i + 3] = (short)( j + 2 );           // Calculate Index 3
            indices[i + 4] = (short)( j + 3 );           // Calculate Index 4
            indices[i + 5] = (short)( j + 0 );           // Calculate Index 5
        }
        mVertices.setIndices(indices, 0, len);         // Set Index Buffer for Rendering
        mMVPMatricesHandle = GLES20.glGetUniformLocation(program.getHandle(), "u_MVPMatrix");
    }

    public void beginBatch(float[] vpMatrix)  {
        mNumSprites = 0;                                 // Empty Sprite Counter
        mBufferIndex = 0;                                // Reset Buffer Index (Empty)
        mVPMatrix = vpMatrix;
    }

    //--End Batch--//
    // D: signal the end of a batch. render the batched sprites
    // A: [none]
    // R: [none]
    public void endBatch()  {
        if ( mNumSprites > 0 )  {                        // IF Any Sprites to Render
            // bind MVP matrices array to shader
            GLES20.glUniformMatrix4fv(mMVPMatricesHandle, mNumSprites, false, uMVPMatrices, 0);
            GLES20.glEnableVertexAttribArray(mMVPMatricesHandle);

            mVertices.setVertices(mVertexBuffer, 0, mBufferIndex);  // Set Vertices from Buffer
            mVertices.bind();                             // Bind Vertices
            mVertices.draw(GLES20.GL_TRIANGLES, 0, mNumSprites * INDICES_PER_SPRITE);  // Render Batched Sprites
            mVertices.unbind();                           // Unbind Vertices
        }
    }

    //--Draw Sprite to Batch--//
    // D: batch specified sprite to batch. adds vertices for sprite to vertex buffer
    //    NOTE: MUST be called after beginBatch(), and before endBatch()!
    //    NOTE: if the batch overflows, this will render the current batch, restart it,
    //          and then batch this sprite.
    // A: x, y - the x,y position of the sprite (center)
    //    width, height - the width and height of the sprite
    //    region - the texture region to use for sprite
    //    modelMatrix - the model matrix to assign to the sprite
    // R: [none]
    public void drawSprite(float x, float y, float width, float height, TextureRegion region, float[] modelMatrix)  {
        if ( mNumSprites == mMaxSprites)  {              // IF Sprite Buffer is Full
            endBatch();                                  // End Batch
            // NOTE: leave current texture bound!!
            mNumSprites = 0;                              // Empty Sprite Counter
            mBufferIndex = 0;                             // Reset Buffer Index (Empty)
        }

        float halfWidth = width / 2.0f;                 // Calculate Half Width
        float halfHeight = height / 2.0f;               // Calculate Half Height
        float x1 = x - halfWidth;                       // Calculate Left X
        float y1 = y - halfHeight;                      // Calculate Bottom Y
        float x2 = x + halfWidth;                       // Calculate Right X
        float y2 = y + halfHeight;                      // Calculate Top Y

        mVertexBuffer[mBufferIndex++] = x1;               // Add X for Vertex 0
        mVertexBuffer[mBufferIndex++] = y1;               // Add Y for Vertex 0
        mVertexBuffer[mBufferIndex++] = region.u1;        // Add U for Vertex 0
        mVertexBuffer[mBufferIndex++] = region.v2;        // Add V for Vertex 0
        mVertexBuffer[mBufferIndex++] = mNumSprites;

        mVertexBuffer[mBufferIndex++] = x2;               // Add X for Vertex 1
        mVertexBuffer[mBufferIndex++] = y1;               // Add Y for Vertex 1
        mVertexBuffer[mBufferIndex++] = region.u2;        // Add U for Vertex 1
        mVertexBuffer[mBufferIndex++] = region.v2;        // Add V for Vertex 1
        mVertexBuffer[mBufferIndex++] = mNumSprites;

        mVertexBuffer[mBufferIndex++] = x2;               // Add X for Vertex 2
        mVertexBuffer[mBufferIndex++] = y2;               // Add Y for Vertex 2
        mVertexBuffer[mBufferIndex++] = region.u2;        // Add U for Vertex 2
        mVertexBuffer[mBufferIndex++] = region.v1;        // Add V for Vertex 2
        mVertexBuffer[mBufferIndex++] = mNumSprites;

        mVertexBuffer[mBufferIndex++] = x1;               // Add X for Vertex 3
        mVertexBuffer[mBufferIndex++] = y2;               // Add Y for Vertex 3
        mVertexBuffer[mBufferIndex++] = region.u1;        // Add U for Vertex 3
        mVertexBuffer[mBufferIndex++] = region.v1;        // Add V for Vertex 3
        mVertexBuffer[mBufferIndex++] = mNumSprites;

        // add the sprite mvp matrix to uMVPMatrices array

        Matrix.multiplyMM(mMVPMatrix, 0, mVPMatrix, 0, modelMatrix, 0);

        //TODO: make sure numSprites < 24
        for (int i = 0; i < 16; ++i) {
            uMVPMatrices[mNumSprites *16+i] = mMVPMatrix[i];
        }

        mNumSprites++;                                   // Increment Sprite Count
    }
}
