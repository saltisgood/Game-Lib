package com.nickstephen.gamelib.opengl.text;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.AttribVariable;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.UniformVariable;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;

/**
 * Created by Nick Stephen on 9/03/14.
 */
public class Vertices {
    //--Constants--//
    private final static int POSITION_CNT_2D = 2;              // Number of Components in Vertex Position for 2D
    private final static int TEXCOORD_CNT = 2;                 // Number of Components in Vertex Texture Coords
    private static final int MVP_MATRIX_INDEX_CNT = 1; // Number of Components in MVP matrix index

    private final static int INDEX_SIZE = Short.SIZE / 8;      // Index Byte Size (Short.SIZE = bits)

    /**
     * Number of position components (2=2D, 3=3D)
     */
    private final int mPositionCount;
    private final int mVertexStride;
    /**
     * Bytesize of a single vertex (stride * bytes in float)
     */
    private final int mVertexSize;
    private final FloatBuffer mVertices;
    private final ShortBuffer mIndices;
    private final int mNumVertices;
    private final int mNumIndices;
    private final int mTextureCoordinateHandle;
    private final int mMVPIndexHandle;
    private final int mPositionHandle;
    private final Program mProgram;
    private final Shape mShape;
    private final int mPrimitiveType;
    private final boolean mUsesColour;
    private final boolean mUsesTexture;
    private final boolean mUsesTextureCoords;
    private final boolean mUsesMVPIndex;
    private float[] mVertexCoords;
    private float[] mTexCoords;
    private float[] mMVPIndices;

    public Vertices(@NotNull Shape shape, int numVertices, int numIndices, int glPrimitive) {
        mShape = shape;
        mProgram = shape.getProgram();
        mPositionCount = POSITION_CNT_2D;  // Set Position Component Count

        mNumVertices = numVertices;
        mNumIndices = numIndices;

        mUsesColour = mProgram.usesVariable(UniformVariable.U_Colour);
        mUsesTexture = mProgram.usesVariable(UniformVariable.U_Texture);
        mUsesTextureCoords = mProgram.usesVariable(AttribVariable.A_TexCoordinate);
        mUsesMVPIndex = mProgram.usesVariable(AttribVariable.A_MVPMatrixIndex);

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
        mTextureCoordinateHandle = AttribVariable.A_TexCoordinate.getHandle();
        mMVPIndexHandle = AttribVariable.A_MVPMatrixIndex.getHandle();
        mPositionHandle = AttribVariable.A_Position.getHandle();
    }

    /**
     * Set the specified indices in the index buffer
     * @param indices Array of indices (shorts) to set
     * @param offset Offset to first index in the array
     * @param length Number of indices in array
     */
    public void setIndices(short[] indices, int offset, int length) {
        mIndices.clear();
        mIndices.put(indices, offset, length);
        mIndices.flip();
    }

    /**
     * Set the specified vertices in the vertex buffer.
     * @param vertices Array of vertices (floats) to set
     */
    public void setVertices(float[] vertices) {
        if (vertices.length != mPositionCount * mNumVertices) {
            throw new IllegalArgumentException("Invalid vertex array size");
        }
        mVertexCoords = vertices;
        resetFloatBuffer();
    }

    public void setTextureCoords(float[] coords) {
        if (coords.length != TEXCOORD_CNT * mNumVertices) {
            throw new IllegalArgumentException("Invalid tex-coord array size");
        }
        mTexCoords = coords;
        resetFloatBuffer();
    }

    public void setMVPIndices(float[] indices) {
        if (indices.length != MVP_MATRIX_INDEX_CNT * mNumVertices) {
            throw new IllegalArgumentException("Invalid mvp-index array size");
        }
        mMVPIndices = indices;
        resetFloatBuffer();
    }

    private void resetFloatBuffer() {
        int len = mNumVertices * mVertexStride;
        float[] buff = new float[len];

        for (int c = 0, i = 0, j = 0, k = 0; i < len; c++) {
            for (; j < (c + 1) * mPositionCount;) {
                buff[i++] = mVertexCoords[j++];
            }

            if (mUsesTextureCoords) {
                for (; k < (c + 1) * TEXCOORD_CNT ;) {
                    buff[i++] = mTexCoords[k++];
                }
            }

            if (mUsesMVPIndex) {
                buff[i++] = mMVPIndices[c];
            }
        }

        mVertices.clear();
        mVertices.put(buff, 0, len);
        mVertices.flip();
    }

    private void bind(float[] mvpMatrix) {
        GLES20.glUseProgram(mProgram.getHandle());

        int mvpMatricesHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_MVPMatrix.getName());
        GLES20.glUniformMatrix4fv(mvpMatricesHandle, 1, false, mvpMatrix, 0);
        GLES20.glEnableVertexAttribArray(mvpMatricesHandle);

        // bind vertex position pointer
        mVertices.position(0);                         // Set Vertex Buffer to Position
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionCount,
                GLES20.GL_FLOAT, false, mVertexSize, mVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        if (mUsesColour) {
            int colourHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Colour.getName());
            GLES20.glUniform4fv(colourHandle, 1, mShape.getColour(), 0);
            GLES20.glEnableVertexAttribArray(colourHandle);
        }

        if (mUsesTexture) {
            int textureUniformHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Texture.getName());
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mShape.getTextureId());
            GLES20.glUniform1i(textureUniformHandle, 0);
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

        // bind MVP Matrix index position handle
        if (mUsesMVPIndex) {
            GLES20.glVertexAttribPointer(mMVPIndexHandle, MVP_MATRIX_INDEX_CNT,
                    GLES20.GL_FLOAT, false, mVertexSize, mVertices);
            GLES20.glEnableVertexAttribArray(mMVPIndexHandle);
        }
    }

    /**
     * Draw the currently bound vertices in the vertex/ index buffers
     */
    public void draw(float[] mvpMatrix) {
        bind(mvpMatrix);

        if (mIndices != null)  {                       // IF Indices Exist
            mIndices.position(0);                  // Set Index Buffer to Specified Offset
            //draw indexed
            GLES20.glDrawElements(mPrimitiveType, mNumIndices,
                    GLES20.GL_UNSIGNED_SHORT, mIndices);
        }
        else  {                                         // ELSE No Indices Exist
            //draw direct
            GLES20.glDrawArrays(mPrimitiveType, 0, mNumVertices);
        }

        unbind();
    }

    private void unbind() {
        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
