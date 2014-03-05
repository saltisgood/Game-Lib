package com.nickstephen.gamelib.opengl;

import android.opengl.GLES20;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public abstract class Program {

    private int mProgramHandle;
    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private boolean mInitialized;

    public Program() {
        mInitialized = false;
    }

    public void init() {
        init(null, null, null);
    }

    public void init(String vertexShaderCode, String fragmentShaderCode, AttribVariable[] programVariables) {
        mVertexShaderHandle = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        mFragmentShaderHandle = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramHandle = Utilities.createProgram(
                mVertexShaderHandle, mFragmentShaderHandle, programVariables);

        mInitialized = true;
    }

    public int getHandle() {
        return mProgramHandle;
    }

    public void delete() {
        GLES20.glDeleteShader(mVertexShaderHandle);
        GLES20.glDeleteShader(mFragmentShaderHandle);
        GLES20.glDeleteProgram(mProgramHandle);
        mInitialized = false;
    }

    public boolean isInitialized() {
        return mInitialized;
    }
}