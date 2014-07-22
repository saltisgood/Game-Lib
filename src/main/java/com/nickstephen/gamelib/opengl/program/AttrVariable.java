package com.nickstephen.gamelib.opengl.program;

/**
 * This is used with GLES20.glBindAttribLocation(mProgram, attribVar.getHandle(), attribVar.getName())
 * to bind a particular name of an attribute in a shader with an index which can then be used later
 * with confidence as to which attribute it points to.
 */
public enum AttrVariable {
    A_Position(1, Constants.ATTR_PREF + Constants.POS),
    A_TexCoordinate(2, Constants.ATTR_PREF + Constants.TEX_COORD),
    A_MVPMatrixIndex(3, Constants.ATTR_PREF + Constants.MVP_INDEX);

    private int mHandle;
    private String mName;

    private AttrVariable(int handle, String name) {
        mHandle = handle;
        mName = name;
    }

    /**
     * Get the location of the attribute variable in the program
     * @return The location of the attribute variable in the program
     */
    public int getHandle() {
        return mHandle;
    }

    public String getName() {
        return mName;
    }

    public static class Constants {
        public static final String ATTR_PREF = "a_";
        public static final String POS = "Position";
        public static final String TEX_COORD = "TexCoordinate";
        public static final String MVP_INDEX = "MVPMatrixIndex";

        private Constants() {}
    }

}
