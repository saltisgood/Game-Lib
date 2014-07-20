package com.nickstephen.gamelib.opengl;

/**
 * Simple abstraction of the U,V coordinates of textures used with a vertex.
 * @author Nick Stephen
 */
public class TextureRegion {

    //--Members--//
    public final float u1, v1;                               // Top/Left U,V Coordinates
    public final float u2, v2;                               // Bottom/Right U,V Coordinates

    /**
     * Calculate U,V coordinates from specified texture coordinates
     * @param texWidth The width of the texture the region is for
     * @param texHeight The height of the texture the region is for
     * @param x The top left of the region on the texture (x-axis, pixels)
     * @param y The top left of the region on the texture (y-axis, pixels)
     * @param width The width of the region on the texture (in pixels)
     * @param height The height of the region on the texture (in pixels)
     */
    public TextureRegion(float texWidth, float texHeight, float x, float y, float width, float height)  {
        u1 = x / texWidth;                         // Calculate U1
        v1 = y / texHeight;                        // Calculate V1
        u2 = u1 + ( width / texWidth );       // Calculate U2
        v2 = v1 + ( height / texHeight );     // Calculate V2
    }

    public TextureRegion(float _u1, float _u2, float _v1, float _v2) {
        u1 = _u1;
        u2 = _u2;
        v1 = _v1;
        v2 = _v2;
    }
}
