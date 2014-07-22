package com.nickstephen.gamelib.opengl.program;

/**
 * An enum used for keeping track of the uniform variables in use in a
 * {@link com.nickstephen.gamelib.opengl.program.Program}'s shaders.
 * @author Nick Stephen
 */
public enum UniformVariable {
    U_Colour(4, Constants.UNI_PREF + Constants.COLOR),
    U_Texture(5, Constants.UNI_PREF + Constants.TEXTURE),
    U_MVPMatrix(6, Constants.UNI_PREF + Constants.MVP_MAT),
    U_Alpha(7, Constants.UNI_PREF + Constants.ALPHA),
    U_ChannelBalance(8, Constants.UNI_PREF + Constants.CHANNEL);

    private int mHandle;
    private String mName;

    private UniformVariable(int handle, String name) {
        mHandle = handle;
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public static class Constants {
        public static final String UNI_PREF = "u_";
        public static final String COLOR = "Color";
        public static final String TEXTURE = "Texture";
        public static final String MVP_MAT = "MVPMatrix";
        public static final String ALPHA = "Alpha";
        public static final String CHANNEL = "Channel";

        private Constants() {}
    }
}
