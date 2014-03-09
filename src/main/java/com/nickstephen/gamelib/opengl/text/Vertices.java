package com.nickstephen.gamelib.opengl.text;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.AttribVariable;

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
    private final static int POSITION_CNT_3D = 3;              // Number of Components in Vertex Position for 3D
    private final static int COLOR_CNT = 4;                    // Number of Components in Vertex Color
    private final static int TEXCOORD_CNT = 2;                 // Number of Components in Vertex Texture Coords
    private final static int NORMAL_CNT = 3;                   // Number of Components in Vertex Normal
    private static final int MVP_MATRIX_INDEX_CNT = 1; // Number of Components in MVP matrix index

    private final static int INDEX_SIZE = Short.SIZE / 8;      // Index Byte Size (Short.SIZE = bits)

    private final int mPositionCount;
    private final int mVertexStride;
    private final int mVertexSize;
    private final IntBuffer mVertices;
    private final ShortBuffer mIndices;
    private int mNumVertices;
    private int mNumIndices;
    private final int[] mTempBuffer;
    private final int mTextureCoordinateHandle;
    private final int mMVPIndexHandle;
    private final int mPositionHandle;
    private boolean mIsBound = false;

    public Vertices(int maxVertices, int maxIndices) {
        this.mPositionCount = POSITION_CNT_2D;  // Set Position Component Count
        this.mVertexStride = this.mPositionCount + TEXCOORD_CNT + MVP_MATRIX_INDEX_CNT;  // Calculate Vertex Stride
        this.mVertexSize = this.mVertexStride * 4;        // Calculate Vertex Byte Size

        ByteBuffer buffer = ByteBuffer.allocateDirect( maxVertices * mVertexSize);  // Allocate Buffer for Vertices (Max)
        buffer.order( ByteOrder.nativeOrder() );        // Set Native Byte Order
        this.mVertices = buffer.asIntBuffer();           // Save Vertex Buffer

        if ( maxIndices > 0 )  {                        // IF Indices Required
            buffer = ByteBuffer.allocateDirect( maxIndices * INDEX_SIZE );  // Allocate Buffer for Indices (MAX)
            buffer.order( ByteOrder.nativeOrder() );     // Set Native Byte Order
            this.mIndices = buffer.asShortBuffer();       // Save Index Buffer
        }
        else                                            // ELSE Indices Not Required
            mIndices = null;                              // No Index Buffer

        this.mTempBuffer = new int[maxVertices * mVertexSize / 4];  // Create Temp Buffer

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

    private void bind() {
        mIsBound = true;

        // bind vertex position pointer
        mVertices.position(0);                         // Set Vertex Buffer to Position
        GLES20.glVertexAttribPointer(mPositionHandle, mPositionCount,
                GLES20.GL_FLOAT, false, mVertexSize, mVertices);
        GLES20.glEnableVertexAttribArray(mPositionHandle);

        // bind texture position pointer
        mVertices.position(mPositionCount);  // Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
        GLES20.glVertexAttribPointer(mTextureCoordinateHandle, TEXCOORD_CNT,
                GLES20.GL_FLOAT, false, mVertexSize, mVertices);
        GLES20.glEnableVertexAttribArray(mTextureCoordinateHandle);

        // bind MVP Matrix index position handle
        mVertices.position(mPositionCount + TEXCOORD_CNT);
        GLES20.glVertexAttribPointer(mMVPIndexHandle, MVP_MATRIX_INDEX_CNT,
                GLES20.GL_FLOAT, false, mVertexSize, mVertices);
        GLES20.glEnableVertexAttribArray(mMVPIndexHandle);
    }

    /**
     * Draw the currently bound vertices in the vertex/ index buffers
     * @param primitiveType The type of primitive to draw
     * @param offset The offset in the vertex/ index buffer to start at
     * @param numVertices The number of vertices (indices) to draw
     */
    public void draw(int primitiveType, int offset, int numVertices) {
        bind();

        if (mIndices != null)  {                       // IF Indices Exist
            mIndices.position(offset);                  // Set Index Buffer to Specified Offset
            //draw indexed
            GLES20.glDrawElements(primitiveType, numVertices,
                    GLES20.GL_UNSIGNED_SHORT, mIndices);
        }
        else  {                                         // ELSE No Indices Exist
            //draw direct
            GLES20.glDrawArrays(primitiveType, offset, numVertices);
        }

        unbind();
    }

    private void unbind() {
        mIsBound = false;

        GLES20.glDisableVertexAttribArray(mTextureCoordinateHandle);
    }
}
