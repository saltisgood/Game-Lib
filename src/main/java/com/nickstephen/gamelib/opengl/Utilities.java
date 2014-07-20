package com.nickstephen.gamelib.opengl;

import android.opengl.GLES20;
import android.util.Log;

import com.nickstephen.gamelib.opengl.program.AttrVariable;

import org.jetbrains.annotations.Nullable;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

/**
 * Various utility methods.
 * @author Nick Stephen
 */
public class Utilities {
    public static final int QUAD_CHANNEL = 4;

    public static final int BYTES_PER_FLOAT = 4;
    public static final int BYTES_PER_SHORT = 2;
    private static final String TAG = "Utilities";

    public static final float[] red = new float[] { 1.0f, 0.f, 0.f, 0.f };
    public static final float[] green = new float[] { 0.f, 1.f, 0.f, 0.f };
    public static final float[] blue = new float[] { 0.f, 0.f, 1.f, 0.f };

    /**
     * Utility method for debugging OpenGL calls. Provide the name of the call
     * just after making it:
     *
     * <pre>
     * mColorHandle = GLES20.glGetUniformLocation(mProgram, "u_Color");
     * MyGLRenderer.checkGlError("glGetUniformLocation");</pre>
     *
     * If the operation is not successful, the check throws an error.
     *
     * @param glOperation - Name of the OpenGL call to check.
     */
    public static void checkGlError(String glOperation) {
        int error;
        while ((error = GLES20.glGetError()) != GLES20.GL_NO_ERROR) {
            Log.e(TAG, glOperation + ": glError " + error);
            throw new RuntimeException(glOperation + ": glError " + error);
        }
    }

    /**
     * Create an OpenGL program
     * @param vertexShaderHandle A handle to a vertex shader to attach to the program
     * @param fragmentShaderHandle A handle to a fragment shader to attach to the program
     * @param variables A set of attribute variables to bind the locations of in the program
     * @return
     */
    public static int createProgram(int vertexShaderHandle, int fragmentShaderHandle, @Nullable AttrVariable[] variables) {
        int  mProgram = GLES20.glCreateProgram();

        if (mProgram != 0) {
            GLES20.glAttachShader(mProgram, vertexShaderHandle);
            GLES20.glAttachShader(mProgram, fragmentShaderHandle);

            if (variables != null) {
                for (AttrVariable var: variables) {
                    GLES20.glBindAttribLocation(mProgram, var.getHandle(), var.getName());
                }
            }

            GLES20.glLinkProgram(mProgram);

            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(mProgram, GLES20.GL_LINK_STATUS, linkStatus, 0);

            if (linkStatus[0] == 0)
            {
                Log.v(TAG, GLES20.glGetProgramInfoLog(mProgram));
                GLES20.glDeleteProgram(mProgram);
                mProgram = 0;
            }
        }

        if (mProgram == 0)
        {
            throw new RuntimeException("Error creating program.");
        }
        return mProgram;
    }

    /**
     * Utility method for compiling a OpenGL shader.
     *
     * <p><strong>Note:</strong> When developing shaders, use the checkGlError()
     * method to debug shader coding errors.</p>
     *
     * @param type - Vertex or fragment shader type.
     * @param shaderCode - String containing the shader code.
     * @return - Returns an id for the shader.
     */
    public static int loadShader(int type, String shaderCode){
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shaderHandle = GLES20.glCreateShader(type);

        if (shaderHandle != 0)
        {
            // add the source code to the shader and compile it
            GLES20.glShaderSource(shaderHandle, shaderCode);
            GLES20.glCompileShader(shaderHandle);
            checkGlError("glCompileShader");

            // Get the compilation status.
            final int[] compileStatus = new int[1];
            GLES20.glGetShaderiv(shaderHandle, GLES20.GL_COMPILE_STATUS, compileStatus, 0);
            checkGlError("glGetShaderiv");

            // If the compilation failed, delete the shader.
            if (compileStatus[0] == 0)
            {
                Log.v(TAG, "Shader fail info: " + GLES20.glGetShaderInfoLog(shaderHandle));
                GLES20.glDeleteShader(shaderHandle);
                shaderHandle = 0;
            }
        }


        if (shaderHandle == 0)
        {
            throw new RuntimeException("Error creating shader " + type);
        }
        return shaderHandle;
    }

    /**
     * Generate a new float buffer for storing vertex data
     * @param verticesData The array of data to store
     * @return A new float buffer with the information copied from the argument
     */
    public static FloatBuffer newFloatBuffer(float[] verticesData) {
        FloatBuffer floatBuffer;
        floatBuffer = ByteBuffer.allocateDirect(verticesData.length * BYTES_PER_FLOAT)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        floatBuffer.put(verticesData).position(0);
        return floatBuffer;
    }
}
