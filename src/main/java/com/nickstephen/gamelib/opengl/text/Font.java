package com.nickstephen.gamelib.opengl.text;

/**
 * Created by Nick Stephen on 17/07/2014.
 */
public class Font {
    public final String[] textureNames;
    public final String fontName;

    public Font(String name, String[] texNames) {
        fontName = name;
        textureNames = texNames;
    }
}
