package com.nickstephen.gamelib.opengl.shapes;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.gamelib.opengl.program.AttrVariable;
import com.nickstephen.gamelib.opengl.program.Program;
import com.nickstephen.gamelib.opengl.program.UniformVariable;
import com.nickstephen.gamelib.opengl.shapes.Shape;
import com.nickstephen.gamelib.opengl.textures.Texture;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Class for containing all the low level OpenGL stuff about a shape. Also handles the raw drawing.
 * @author Nick Stephen
 */
public class Vertices {
    public static final int MVP_MATRIX_INDEX_CNT = 1; // Number of Components in MVP matrix index
    public final static int TEXCOORD_CNT = 2;                 // Number of Components in Vertex Texture Coords
    public final static int POSITION_CNT_2D = 2;              // Number of Components in Vertex Position for 2D

    private final static int INDEX_SIZE = Short.SIZE / 8;      // Index Byte Size (Short.SIZE = bits)

    private final static int HANDLE_UNSET = -1;

    protected final int mNumVertices;
    /**
     * Number of position components (2=2D, 3=3D)
     */
    protected final int mPositionCount;
    protected final Program mProgram;
    protected final boolean mUsesAlpha;
    protected final boolean mUsesMVPIndex;
    protected final boolean mUsesTexture;
    protected final boolean mUsesTextureCoords;
    protected final boolean mUsesChannelBalance;
    protected final int mVertexStride;

    private final ShortBuffer mIndices;
    private final int mMVPIndexHandle;
    private final int mNumIndices;
    private final int mPositionHandle;
    private final int mPrimitiveType;
    private final Shape mShape;
    private final int mTextureCoordinateHandle;
    private final boolean mUsesColour;
    /**
     * Bytesize of a single vertex (stride * bytes in float)
     */
    private final int mVertexSize;

    private final FloatBuffer mVertices;

    protected float[] mMVPIndices;
    protected int mNumMVPMatrices = 1;
    protected float[] mTexCoords;
    protected float[] mVertexCoords;
    protected float[] mScratch;

    private int mMVPMatrixHandle = HANDLE_UNSET;
    private int mColourHandle = HANDLE_UNSET;
    private int mTextureUniformHandle = HANDLE_UNSET;
    private int mAlphaHandle = HANDLE_UNSET;
    private int mChannelHandle = HANDLE_UNSET;

    /**
     * Constructor.
     * @param shape The shape associated with these vertices
     * @param numVertices The number of vertices to use
     * @param numIndices The number of indices to use
     * @param glPrimitive The OpenGL primitive type to use when drawing
     */
    public Vertices(@NotNull Shape shape, int numVertices, int numIndices, int glPrimitive) {
        mShape = shape;
        mProgram = shape.getProgram();
        mPositionCount = POSITION_CNT_2D;  // Set Position Component Count

        mNumVertices = numVertices;
        mNumIndices = numIndices;

        mUsesColour = mProgram.usesVariable(UniformVariable.U_Colour);
        mUsesTexture = mProgram.usesVariable(UniformVariable.U_Texture);
        mUsesTextureCoords = mProgram.usesVariable(AttrVariable.A_TexCoordinate);
        mUsesMVPIndex = mProgram.usesVariable(AttrVariable.A_MVPMatrixIndex);
        mUsesAlpha = mProgram.usesVariable(UniformVariable.U_Alpha);
        mUsesChannelBalance = mProgram.usesVariable(UniformVariable.U_ChannelBalance);

        if (mUsesChannelBalance && !mUsesMVPIndex) {
            throw new RuntimeException("Not yet supported!"); //TODO: Add this
        }

        mVertexStride = mPositionCount +
                (mUsesMVPIndex ? MVP_MATRIX_INDEX_CNT : 0) +
                (mUsesTextureCoords ? TEXCOORD_CNT : 0);
                // Calculate Vertex Stride
        mVertexSize = mVertexStride * 4;        // Calculate Vertex Byte Size
        mPrimitiveType = glPrimitive;

        ByteBuffer buffer = ByteBuffer.allocateDirect( numVertices * mVertexSize);  // Allocate Buffer for Vertices (Max)
        buffer.order( ByteOrder.nativeOrder() );        // Set Native Byte Order
        mVertices = buffer.asFloatBuffer();           // Save Vertex Buffer

        if ( numIndices > 0 )  {                        // IF Indices Required
            buffer = ByteBuffer.allocateDirect( numIndices * INDEX_SIZE );  // Allocate Buffer for Indices (MAX)
            buffer.order( ByteOrder.nativeOrder() );     // Set Native Byte Order
            mIndices = buffer.asShortBuffer();       // Save Index Buffer
        }
        else                                            // ELSE Indices Not Required
            mIndices = null;                              // No Index Buffer



        // initialize the shader attribute handles
        mTextureCoordinateHandle = AttrVariable.A_TexCoordinate.getHandle();
        mMVPIndexHandle = AttrVariable.A_MVPMatrixIndex.getHandle();
        mPositionHandle = AttrVariable.A_Position.getHandle();

        if (mUsesTextureCoords) {
            mTexCoords = new float[TEXCOORD_CNT * mNumVertices];
        }

        mScratch = new float[mNumVertices * mVertexStride];
    }

    /**
     * Perform the setups prior to drawing.
     * @param mvpMatrix The full MVP matrix to use
     */
    private synchronized boolean bind(@NotNull float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram.getHandle());

        if (mMVPMatrixHandle == HANDLE_UNSET) {
            mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_MVPMatrix.getName());
        }
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, mNumMVPMatrices, false, mvpMatrix, 0);

        // bind vertex position pointer
        mVertices.position(0);                         // Set Vertex Buffer to Position
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionCount,
                GLES20.GL_FLOAT, false, mVertexSize, mVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        if (mUsesColour) {
            if (mColourHandle == HANDLE_UNSET) {
                mColourHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Colour.getName());
            }
            GLES20.glUniform4fv(mColourHandle, 1, mShape.getColour(), 0);
        }

        if (mUsesTexture) {
            if (mTextureUniformHandle == HANDLE_UNSET) {
                mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Texture.getName());
            }
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);

            int texId = mShape.getTextureId();
            if (texId != Texture.TEX_ID_UNASSIGNED) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texId);
                GLES20.glUniform1i(mTextureUniformHandle, 0);
            } else {
                return false;
            }
        }

        if (mUsesTextureCoords) {
            // bind texture position pointer
            mVertices.position(mPositionCount);  // Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
            GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXCOORD_CNT,
                    GLES20.GL_FLOAT, false, mVertexSize, mVertices);
            GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);
            mVertices.position(mPositionCount + TEXCOORD_CNT);
        } else {
            mVertices.position(mPositionCount);
        }

        if (mUsesAlpha) {
            if (mAlphaHandle == HANDLE_UNSET) {
                mAlphaHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Alpha.getName());
            }
            GLES20.glUniform1f(mAlphaHandle, mShape.getAlpha());
        }

        if (mUsesChannelBalance) {
            int channelHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_ChannelBalance.getName());
            GLES20.glUniform4fv(channelHandle, mNumMVPMatrices, mShape.getChannel(), 0);
        }

        // bind MVP Matrix index position handle
        if (mUsesMVPIndex) {
            GLES20.glVertexAttribPointer(mMVPIndexHandle, MVP_MATRIX_INDEX_CNT,
                    GLES20.GL_FLOAT, false, mVertexSize, mVertices);
            GLES20.glEnableVertexAttribArray(mMVPIndexHandle);
        }

        return true;
    }

    /**
     * Draw the currently bound vertices in the vertex/ index buffers
     * @param mvpMatrix The full MVP matrix to use
     */
    public void draw(@NotNull float[] mvpMatrix) {
        if (!bind(mvpMatrix)) {
            unbind();
            return;
        }

        if (mIndices != null)  {                       // IF Indices Exist
            synchronized (mIndices) {
                mIndices.position(0);                  // Set Index Buffer to Specified Offset
                //draw indexed
                GLES20.glDrawElements(mPrimitiveType, mNumIndices,
                        GLES20.GL_UNSIGNED_SHORT, mIndices);
            }
        }
        else  {                                         // ELSE No Indices Exist
            //draw direct
            GLES20.glDrawArrays(mPrimitiveType, 0, mNumVertices);
        }

        unbind();
    }

    /**
     * Set the specified indices in the index buffer
     * @param indices Array of indices (shorts) to set
     * @param offset Offset to first index in the array
     * @param length Number of indices in array
     */
    public void setIndices(@NotNull short[] indices, int offset, int length) {
        synchronized (mIndices) {
            mIndices.clear();
            mIndices.put(indices, offset, length);
            mIndices.flip();
        }
    }

    /**
     * Set the indices to use in the MVP index array
     * @param indices The index array
     */
    public void setMVPIndices(@NotNull float[] indices) {
        if (indices.length != MVP_MATRIX_INDEX_CNT * mNumVertices) {
            throw new IllegalArgumentException("Invalid mvp-index array size!");
        } else if (!mUsesMVPIndex) {
            throw new UnsupportedOperationException("Program doesn't use MVP indices!");
        }
        mMVPIndices = indices;
        //resetFloatBuffer();
    }

    /**
     * Use the currently stored values for vertex coords, tex coords, etc, to populate the
     * float buffer actually used for OpenGL calls. Make sure to call before drawing when the
     * shape's properties change.
     */
    public synchronized void resetFloatBuffer() {
        for (int c = 0, i = 0, j = 0, k = 0; i < mNumVertices * mVertexStride; c++) {
            for (; j < (c + 1) * mPositionCount;) {
                mScratch[i++] = mVertexCoords[j++];
            }

            if (mUsesTextureCoords) {
                for (; k < (c + 1) * TEXCOORD_CNT ;) {
                    mScratch[i++] = mTexCoords[k++];
                }
            }

            if (mUsesMVPIndex) {
                mScratch[i++] = mMVPIndices[c];
            }
        }

        mVertices.clear();
        mVertices.put(mScratch, 0, mNumVertices * mVertexStride);
        mVertices.flip();
    }

    public @NotNull float[] getTextureCoords() {
        return mTexCoords;
    }

    /**
     * Set the texture coordinates of the shape.
     * @param coords The float array of texture coordinates
     */
    public void setTextureCoords(@NotNull float[] coords) {
        if (coords.length != TEXCOORD_CNT * mNumVertices) {
            throw new IllegalArgumentException("Invalid tex-coord array size!");
        } else if (!mUsesTextureCoords) {
            throw new UnsupportedOperationException("Program doesn't use texture coords!");
        }
        mTexCoords = coords;
        resetFloatBuffer();
    }

    /**
     * Perform any necessary disabling settings here
     */
    private void unbind() {
        if (mUsesTextureCoords) {
            GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
            Utilities.checkGlError("glDisableVertexAttribArray/TexCoord");
        }
        if (mUsesMVPIndex) {
            GLES20.glDisableVertexAttribArray(mMVPIndexHandle);
            Utilities.checkGlError("glDisableVertexAttribArray/MVPIndex");
        }
    }

    /**
     * Set the specified vertices in the vertex buffer.
     * @param vertices Array of vertices (floats) to set
     */
    public void setVertices(float[] vertices) {
        if (vertices.length != mPositionCount * mNumVertices) {
            throw new IllegalArgumentException("Invalid vertex array size!");
        }
        mVertexCoords = vertices;
        resetFloatBuffer();
    }
}
