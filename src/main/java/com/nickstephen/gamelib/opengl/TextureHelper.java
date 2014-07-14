package com.nickstephen.gamelib.opengl;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * A helper class for loading textures.
 * @author Nick Stephen
 */
public class TextureHelper {
    public static class Manager {

    }

    private TextureHelper() {} // Don't use

    /**
     * Load a texture from a context's resources
     * @param context The context to use
     * @param resourceId The id of the image resource
     * @return The id of the texture as loaded into OpenGL
     */
    public static int loadTexture(final Context context, final int resourceId) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false; // No pre-scaling
        final Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);

        return loadTexture(bitmap);
    }

    /**
     * Load a texture into OpenGL from a Bitmap
     * @param bitmap The bitmap to convert to a texture
     * @return The id of the texture as loaded into OpenGL
     */
    public static int loadTexture(Bitmap bitmap)
    {
        final int[] textureHandle = new int[1];

        GLES20.glGenTextures(1, textureHandle, 0);
        Utilities.checkGlError("glGenTextures");

        if (textureHandle[0] != 0)
        {
            // Bind to the texture in OpenGL
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE );  // Set U Wrapping
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE );  // Set V Wrapping

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

            // Recycle the bitmap, since its data has been loaded into OpenGL.
            bitmap.recycle();
        }

        if (textureHandle[0] == 0)
        {
            throw new RuntimeException("Error loading texture.");
        }

        return textureHandle[0];
    }
}
