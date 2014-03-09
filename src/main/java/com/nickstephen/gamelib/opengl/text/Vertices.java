package com.nickstephen.gamelib.opengl.text;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.AttribVariable;
import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.UniformVariable;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

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
    private final IntBuffer mVertices;
    private final ShortBuffer mIndices;
    private int mNumVertices;
    private int mNumIndices;
    private final int[] mTempBuffer;
    private final int mTextureCoordinateHandle;
    private final int mMVPIndexHandle;
    private final int mPositionHandle;
    private float[] mPositions;
    private float[] mTextureCoordinates;
    private float[] mColours;
    private final Program mProgram;
    private final Shape mShape;
    private final int mPrimitiveType;

    public Vertices(@NotNull Shape shape, int numVertices, int numIndices, int glPrimitive) {
        mShape = shape;
        mProgram = shape.getProgram();
        mPositionCount = POSITION_CNT_2D;  // Set Position Component Count
        mVertexStride = mPositionCount +
                (mProgram.usesVariable(AttribVariable.A_MVPMatrixIndex) ? MVP_MATRIX_INDEX_CNT : 0) +
                (mProgram.usesVariable(AttribVariable.A_TexCoordinate) ? TEXCOORD_CNT : 0);
                // Calculate Vertex Stride
        mVertexSize = mVertexStride * 4;        // Calculate Vertex Byte Size
        mPrimitiveType = glPrimitive;

        ByteBuffer buffer = ByteBuffer.allocateDirect( numVertices * mVertexSize);  // Allocate Buffer for Vertices (Max)
        buffer.order( ByteOrder.nativeOrder() );        // Set Native Byte Order
        mVertices = buffer.asIntBuffer();           // Save Vertex Buffer

        if ( numIndices > 0 )  {                        // IF Indices Required
            buffer = ByteBuffer.allocateDirect( numIndices * INDEX_SIZE );  // Allocate Buffer for Indices (MAX)
            buffer.order( ByteOrder.nativeOrder() );     // Set Native Byte Order
            mIndices = buffer.asShortBuffer();       // Save Index Buffer
        }
        else                                            // ELSE Indices Not Required
            mIndices = null;                              // No Index Buffer

        mTempBuffer = new int[numVertices * mVertexSize / 4];  // Create Temp Buffer

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
        mNumIndices = length;
    }

    /**
     * Set the specified vertices in the vertex buffer. Optimised to use int buffer.
     * @param vertices Array of vertices (floats) to set
     * @param offset Offset to first vertex in the array
     * @param length Number of floats in the vertex array. For easy setting use vertexCount * (vertexSize / 4)
     */
    public void setVertices(float[] vertices, int offset, int length) {
        mVertices.clear();
        int last = offset + length;
        for (int i = offset, j = 0; i < last; i++, j++) {
            mTempBuffer[j] = Float.floatToRawIntBits(vertices[j]);
        }
        mVertices.put(mTempBuffer, 0, length);
        mVertices.flip();
        mNumVertices = length / mVertexStride;
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

        if (mProgram.usesVariable(UniformVariable.U_Colour)) {
            int colourHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Colour.getName());
            GLES20.glUniform4fv(colourHandle, 1, mShape.getColour(), 0);
            GLES20.glEnableVertexAttribArray(colourHandle);
        }

        if (mProgram.usesVariable(UniformVariable.U_Texture)) {
            int textureUniformHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), UniformVariable.U_Texture.getName());
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mShape.getTextureId());
            GLES20.glUniform1i(textureUniformHandle, 0);
        }

        if (mProgram.usesVariable(AttribVariable.A_TexCoordinate)) {
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
        if (mProgram.usesVariable(AttribVariable.A_MVPMatrixIndex)) {
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
