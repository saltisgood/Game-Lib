package com.nickstephen.gamelib.opengl.program;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.Utilities;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A class used to abstract and help with the program concept in OpenGL. Make sure to call
 * {@link #init()} before use.
 * @author Nick Stephen
 */
public abstract class Program {

    private AttrVariable[] mAttrVariables;
    private int mFragmentShaderHandle;
    private boolean mInitialized;
    private int mProgramHandle;
    private UniformVariable[] mUniformVariables;
    private int mVertexShaderHandle;

    public Program() {
        mInitialized = false;
    }

    /**
     * Delete the shaders and program from the OpenGL context
     */
    public void delete() {
        GLES20.glDeleteShader(mVertexShaderHandle);
        GLES20.glDeleteShader(mFragmentShaderHandle);
        GLES20.glDeleteProgram(mProgramHandle);
        Utilities.checkGlError("glDeleteShader/Program");
        mInitialized = false;
    }

    /**
     * Get the handle to the program that's created on {@link #init()}
     * @return The handle to the OpenGL program
     */
    public int getHandle() {
        return mProgramHandle;
    }

    public abstract void init();

    /**
     * Initialises the program with the given shader codes and variable enum arrays. The variable
     * arguments are non-negotiable. If you don't want to pass anything, just pass an empty array.
     * But you should pass the variables that are in use in the shaders to this function to make the
     * best use of the in built methods.
     * @param vertexShaderCode The GLSL code to be compiled as the vertex shader
     * @param fragmentShaderCode The GLSL code to be compiled as the fragment shader
     * @param attrVariables The attribute variables in use in the shaders
     * @param uniformVariables The uniform variables in use in the shaders
     */
    protected void init(@NotNull String vertexShaderCode, @NotNull String fragmentShaderCode, @NotNull AttrVariable[] attrVariables,
                     @NotNull UniformVariable[] uniformVariables) {
        mUniformVariables = uniformVariables;
        mAttrVariables = attrVariables;

        mVertexShaderHandle = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        mFragmentShaderHandle = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramHandle = Utilities.createProgram(mVertexShaderHandle, mFragmentShaderHandle, attrVariables);

        mInitialized = true;
    }

    /**
     * Check whether the program has been initialised.
     * @return True if the program has been initialised, false otherwise.
     */
    public boolean isInitialized() {
        return mInitialized;
    }

    /**
     * Check whether the program uses one the uniform variable enums
     * @param uni The uniform variable to check
     * @return True if the variable is in use, false otherwise
     */
    public boolean usesVariable(UniformVariable uni) {
        for (UniformVariable u : mUniformVariables) {
            if (uni == u) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check whether the program uses one of the attribute variable enums
     * @param attrib The attribute variable to check
     * @return True if the variable is in use, false otherwise
     */
    public boolean usesVariable(AttrVariable attrib) {
        for (AttrVariable a : mAttrVariables) {
            if (attrib == a) {
                return true;
            }
        }
        return false;
    }
}