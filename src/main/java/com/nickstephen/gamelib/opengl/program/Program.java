package com.nickstephen.gamelib.opengl.program;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.AttribVariable;
import com.nickstephen.gamelib.opengl.UniformVariable;
import com.nickstephen.gamelib.opengl.Utilities;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public abstract class Program {

    private int mProgramHandle;
    private int mVertexShaderHandle;
    private int mFragmentShaderHandle;
    private boolean mInitialized;

    private AttribVariable[] mAttrVariables;
    private UniformVariable[] mUniformVariables;

    public Program() {
        mInitialized = false;
    }

    public void init() {
        init(null, null, null);
    }

    public void init(String vertexShaderCode, String fragmentShaderCode, AttribVariable[] programVariables) {
        mAttrVariables = programVariables;

        mVertexShaderHandle = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode);
        mFragmentShaderHandle = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode);

        mProgramHandle = Utilities.createProgram(
                mVertexShaderHandle, mFragmentShaderHandle, programVariables);

        mInitialized = true;
    }

    public void init(String vertexShaderCode, String fragmentShaderCode, AttribVariable[] attribVariables, UniformVariable[] uniformVariables) {
        init(vertexShaderCode, fragmentShaderCode, attribVariables);
        mUniformVariables = uniformVariables;
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

    public boolean usesVariable(AttribVariable attrib) {
        for (AttribVariable a : mAttrVariables) {
            if (attrib == a) {
                return true;
            }
        }
        return false;
    }

    public boolean usesVariable(UniformVariable uni) {
        for (UniformVariable u : mUniformVariables) {
            if (uni == u) {
                return true;
            }
        }
        return false;
    }

    public boolean isInitialized() {
        return mInitialized;
    }
}