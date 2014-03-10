package com.nickstephen.gamelib.opengl.program;

/**
 * Created by Nick Stephen on 10/03/14.
 */
public enum UniformVariable {
    U_Colour(4, "u_Color"),
    U_Texture(5, "u_Texture"),
    U_MVPMatrix(6, "u_MVPMatrix");

    private int mHandle;
    private String mName;

    private UniformVariable(int handle, String name) {
        mHandle = handle;
        mName = name;
    }

    public String getName() {
        return mName;
    }
}
