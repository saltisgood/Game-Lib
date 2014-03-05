package com.nickstephen.gamelib.opengl;

/**
 * As I understand it, this is used with GLES20.glBindAttribLocation(mProgram, attribVar.getHandle(), attribVar.getName())
 * to bind a particular name of an attribute in a shader with an index which can then be used later
 * with confidence as to which attribute it points to.
 */
public enum AttribVariable {
    A_Position(1, "a_Position"),
    A_TexCoordinate(2, "a_TexCoordinate"),
    A_MVPMatrixIndex(3, "a_MVPMatrixIndex");

    private int mHandle;
    private String mName;

    private AttribVariable(int handle, String name) {
        mHandle = handle;
        mName = name;
    }

    public int getHandle() {
        return mHandle;
    }

    public String getName() {
        return mName;
    }
}
