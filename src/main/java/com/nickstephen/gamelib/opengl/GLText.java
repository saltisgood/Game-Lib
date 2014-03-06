package com.nickstephen.gamelib.opengl;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nickstephen.gamelib.opengl.program.BatchTextProgram;
import com.nickstephen.gamelib.opengl.program.Program;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class GLText {

    //--Constants--//
    /**
     * First Character (ASCII Code)
     */
    public final static int CHAR_START = 32;
    /**
     * Last Character (ASCII Code)
     */
    public final static int CHAR_END = 126;
    /**
     * Character Count (Including Character to use for Unknown)
     */
    public final static int CHAR_CNT = ( ( ( CHAR_END - CHAR_START ) + 1 ) + 1 );

    /**
     * Character to Use for Unknown (ASCII Code)
     */
    public final static int CHAR_NONE = 32;
    /**
     * Index of the Unknown Character
     */
    public final static int CHAR_UNKNOWN = ( CHAR_CNT - 1 );

    /**
     * Minimum Font Size (Pixels)
     */
    public final static int FONT_SIZE_MIN = 6;
    /**
     * Maximum Font Size (Pixels)
     */
    public final static int FONT_SIZE_MAX = 180;

    /**
     * Number of Characters to Render Per Batch.
     * Must be the same as the size of u_MVPMatrix in BatchTextProgram
     */
    public final static int CHAR_BATCH_SIZE = 24;

    private static final String TAG = "GLText";

    //--Members--//
    /**
     * Asset Manager
     */
    private AssetManager mAssets;
    /**
     * Batch Renderer
     */
    private SpriteBatch mBatch;

    /**
     * Font Padding (Pixels; On each side, i.e. Doubled on both x + y axis)
     */
    private int mFontPaddingX, mFontPaddingY;

    /**
     * Font Height (actual; pixels)
     */
    private float mFontHeight;
    /**
     * Font Ascent (Above baseline; pixels)
     */
    private float mFontAscent;
    /**
     * Font Descent (Below baseline; pixels)
     */
    private float mFontDescent;

    /**
     * Font Texture ID
     */
    private int mTextureId;
    /**
     * Texture Size for Font
     */
    private int mTextureSize;
    /**
     * Full Texture Region
     */
    private TextureRegion mTextureRegion;

    /**
     * Character Width (Maximum; Pixels)
     */
    private float mCharWidthMax;
    /**
     * Character Height (Maximum; Pixels)
     */
    private float mCharHeight;
    /**
     * Width of Each Character (Actual; Pixels)
     */
    private final float[] mCharWidths;
    /**
     * Region of Each Character (Texture Coordinates)
     */
    private TextureRegion[] mCharRegion;
    /**
     * Character Cell Width/Height
     */
    private int mCellWidth, mCellHeight;
    /**
     * Number of Rows/Columns
     */
    private int mRowCount, mColCount;

    /**
     * Font Scale (X,Y Axis)
     */
    private float mScaleX, mScaleY;
    /**
     * Additional (X,Y Axis) Spacing (Unscaled)
     */
    private float mSpaceX;

    /**
     * OpenGL Program object
     */
    private Program mProgram;
    /**
     * Shader color handle
     */
    private int mColorHandle;
    /**
     * Shader texture handle
     */
    private int mTextureUniformHandle;

    /**
     * Save program and asset manager, create arrays and initialise the members
     * @param program The OpenGL Program Object
     * @param assets The Asset Manager instance to retrieve the typefaces
     */
    public GLText(Program program, AssetManager assets) {
        if (program == null) {
            program = new BatchTextProgram();
            program.init();
        }

        mAssets = assets;                           // Save the Asset Manager Instance

        mBatch = new SpriteBatch(CHAR_BATCH_SIZE, program );  // Create Sprite Batch (with Defined Size)

        mCharWidths = new float[CHAR_CNT];               // Create the Array of Character Widths
        mCharRegion = new TextureRegion[CHAR_CNT];          // Create the Array of Character Regions

        // initialize remaining members
        mFontPaddingX = 0;
        mFontPaddingY = 0;

        mFontHeight = 0.0f;
        mFontAscent = 0.0f;
        mFontDescent = 0.0f;

        mTextureId = -1;
        mTextureSize = 0;

        mCharWidthMax = 0;
        mCharHeight = 0;

        mCellWidth = 0;
        mCellHeight = 0;
        mRowCount = 0;
        mColCount = 0;

        mScaleX = 1.0f;                                  // Default Scale = 1 (Unscaled)
        mScaleY = 1.0f;                                  // Default Scale = 1 (Unscaled)
        mSpaceX = 0.0f;

        // Initialize the color and texture handles
        mProgram = program;
        mColorHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_Color");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_Texture");
    }

    /**
     * Convenience constructor using the default program (BatchTextProgram)
     * @param assets The Asset Manager instance to retrieve the typefaces
     */
    public GLText(AssetManager assets) {
        this(null, assets);
    }

    //--Load Font--//
    // description
    //    this will load the specified font file, create a texture for the defined
    //    character range, and setup all required values used to render with it.
    // arguments:
    //    file - Filename of the font (.ttf, .otf) to use. In 'Assets' folder.
    //    size - Requested pixel size of font (height)
    //    padX, padY - Extra padding per character (X+Y Axis); to prevent overlapping characters.

    /**
     * This will load the specified font file, create a texture for the defined character range,
     * and setup all required values used to render with it
     * @param file Filename of the font (.ttf, .otf) to use. In 'assets' folder.
     * @param size Requested pixel size of font (height)
     * @param padX Extra padding per character (kerning) to prevent overlapping characters
     * @param padY Extra padding per character (kerning) to prevent overlapping characters
     * @return True on success, false on an error
     */
    public boolean load(String file, int size, int padX, int padY) {

        // setup requested values
        mFontPaddingX = padX;                                // Set Requested X Axis Padding
        mFontPaddingY = padY;                                // Set Requested Y Axis Padding

        // load the font and setup paint instance for drawing
        Typeface tf = Typeface.createFromAsset(mAssets, file);  // Create the Typeface from Font File
        Paint paint = new Paint();                      // Create Android Paint Instance
        paint.setAntiAlias( true );                     // Enable Anti Alias
        paint.setTextSize( size );                      // Set Text Size
        paint.setColor( 0xffffffff );                   // Set ARGB (White, Opaque)
        paint.setTypeface( tf );                        // Set Typeface

        // get font metrics
        Paint.FontMetrics fm = paint.getFontMetrics();  // Get Font Metrics
        mFontHeight = (float)Math.ceil( Math.abs( fm.bottom ) + Math.abs( fm.top ) );  // Calculate Font Height
        mFontAscent = (float)Math.ceil( Math.abs( fm.ascent ) );  // Save Font Ascent
        mFontDescent = (float)Math.ceil( Math.abs( fm.descent ) );  // Save Font Descent

        // determine the width of each character (including unknown character)
        // also determine the maximum character width
        char[] s = new char[2];                         // Create Character Array
        mCharWidthMax = mCharHeight = 0;                  // Reset Character Width/Height Maximums
        float[] w = new float[2];                       // Working Width Value
        int cnt = 0;                                    // Array Counter
        for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
            s[0] = c;                                    // Set Character
            paint.getTextWidths( s, 0, 1, w );           // Get Character Bounds
            mCharWidths[cnt] = w[0];                      // Get Width
            if ( mCharWidths[cnt] > mCharWidthMax)        // IF Width Larger Than Max Width
                mCharWidthMax = mCharWidths[cnt];           // Save New Max Width
            cnt++;                                       // Advance Array Counter
        }
        s[0] = CHAR_NONE;                               // Set Unknown Character
        paint.getTextWidths( s, 0, 1, w );              // Get Character Bounds
        mCharWidths[cnt] = w[0];                         // Get Width
        if ( mCharWidths[cnt] > mCharWidthMax)           // IF Width Larger Than Max Width
            mCharWidthMax = mCharWidths[cnt];              // Save New Max Width
        cnt++;                                          // Advance Array Counter

        // set character height to font height
        mCharHeight = mFontHeight;                        // Set Character Height

        // find the maximum size, validate, and setup cell sizes
        mCellWidth = (int) mCharWidthMax + ( 2 * mFontPaddingX);  // Set Cell Width
        mCellHeight = (int) mCharHeight + ( 2 * mFontPaddingY);  // Set Cell Height
        int maxSize = mCellWidth > mCellHeight ? mCellWidth : mCellHeight;  // Save Max Size (Width/Height)
        if ( maxSize < FONT_SIZE_MIN || maxSize > FONT_SIZE_MAX )  // IF Maximum Size Outside Valid Bounds
            return false;                                // Return Error

        // set texture size based on max font size (width or height)
        // NOTE: these values are fixed, based on the defined characters. when
        // changing start/end characters (CHAR_START/CHAR_END) this will need adjustment too!
        if ( maxSize <= 24 )                            // IF Max Size is 18 or Less
            mTextureSize = 256;                           // Set 256 Texture Size
        else if ( maxSize <= 40 )                       // ELSE IF Max Size is 40 or Less
            mTextureSize = 512;                           // Set 512 Texture Size
        else if ( maxSize <= 80 )                       // ELSE IF Max Size is 80 or Less
            mTextureSize = 1024;                          // Set 1024 Texture Size
        else                                            // ELSE IF Max Size is Larger Than 80 (and Less than FONT_SIZE_MAX)
            mTextureSize = 2048;                          // Set 2048 Texture Size

        // create an empty bitmap (alpha only)
        Bitmap bitmap = Bitmap.createBitmap(mTextureSize, mTextureSize, Bitmap.Config.ALPHA_8 );  // Create Bitmap
        Canvas canvas = new Canvas( bitmap );           // Create Canvas for Rendering to Bitmap
        bitmap.eraseColor( 0x00000000 );                // Set Transparent Background (ARGB)

        // calculate rows/columns
        // NOTE: while not required for anything, these may be useful to have :)
        mColCount = mTextureSize / mCellWidth;               // Calculate Number of Columns
        mRowCount = (int)Math.ceil( (float)CHAR_CNT / (float) mColCount);  // Calculate Number of Rows

        // render each of the characters to the canvas (ie. build the font map)
        float x = mFontPaddingX;                             // Set Start Position (X)
        float y = ( mCellHeight - 1 ) - mFontDescent - mFontPaddingY;  // Set Start Position (Y)
        for ( char c = CHAR_START; c <= CHAR_END; c++ )  {  // FOR Each Character
            s[0] = c;                                    // Set Character to Draw
            canvas.drawText( s, 0, 1, x, y, paint );     // Draw Character
            x += mCellWidth;                              // Move to Next Character
            if ( ( x + mCellWidth - mFontPaddingX) > mTextureSize)  {  // IF End of Line Reached
                x = mFontPaddingX;                             // Set X for New Row
                y += mCellHeight;                          // Move Down a Row
            }
        }
        s[0] = CHAR_NONE;                               // Set Character to Use for NONE
        canvas.drawText( s, 0, 1, x, y, paint );        // Draw Character

        // save the bitmap in a texture
        mTextureId = TextureHelper.loadTexture(bitmap);

        // setup the array of character texture regions
        x = 0;                                          // Initialize X
        y = 0;                                          // Initialize Y
        for ( int c = 0; c < CHAR_CNT; c++ )  {         // FOR Each Character (On Texture)
            mCharRegion[c] = new TextureRegion(mTextureSize, mTextureSize, x, y, mCellWidth -1, mCellHeight -1 );  // Create Region for Character
            x += mCellWidth;                              // Move to Next Char (Cell)
            if ( x + mCellWidth > mTextureSize)  {
                x = 0;                                    // Reset X Position to Start
                y += mCellHeight;                          // Move to Next Row (Cell)
            }
        }

        // create full texture region
        mTextureRegion = new TextureRegion(mTextureSize, mTextureSize, 0, 0, mTextureSize, mTextureSize);  // Create Full Texture Region

        // return success
        return true;                                    // Return Success
    }

    /**
     * Begin text drawing. Call this method before all draw() calls using a text instance.
     * This convenience method is for opaque white text.
     * NOTE: color is set on a per-batch basis, and fonts should be 8-bit alpha only.
     * @param vpMatrix View and projection matrix to use
     */
    public void begin(float[] vpMatrix)  {
        begin( 1.0f, 1.0f, 1.0f, 1.0f, vpMatrix );
    }

    /**
     * Begin text drawing. Call this method before all draw() calls using a text instance.
     * This convenience method is for white text with an explicit alpha.
     * NOTE: color is set on a per-batch basis, and fonts should be 8-bit alpha only.
     * @param alpha The transparency percentage (0.0f - 1.0f)
     * @param vpMatrix View and projection matrix to use
     */
    public void begin(float alpha, float[] vpMatrix)  {
        begin( 1.0f, 1.0f, 1.0f, alpha, vpMatrix );
    }

    /**
     * Begin text drawing. Call this method before all draw() calls using a text instance.
     * NOTE: color is set on a per-batch basis, and fonts should be 8-bit alpha only.
     * @param red Red value for font colour
     * @param green Green value for font colour
     * @param blue Blue value for font colour
     * @param alpha The transparency percentage (0.0f - 1.0f)
     * @param vpMatrix View and projection matrix to use
     */
    public void begin(float red, float green, float blue, float alpha, float[] vpMatrix)  {
        initDraw(red, green, blue, alpha);
        mBatch.beginBatch(vpMatrix);
    }

    private void initDraw(float red, float green, float blue, float alpha) {
        GLES20.glUseProgram(mProgram.getHandle()); // specify the program to use

        // set color TODO: only alpha component works, text is always black #BUG
        float[] color = {red, green, blue, alpha};
        GLES20.glUniform4fv(mColorHandle, 1, color , 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // Set the active texture unit to texture unit 0

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId); // Bind the texture to this unit

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0
        GLES20.glUniform1i(mTextureUniformHandle, 0);
    }

    public void end()  {
        mBatch.endBatch();                               // End Batch
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }

    /**
     * Draw text at the specified x,y position
     * @param text The string to draw
     * @param x The x position to draw text at (bottom left of text, including descent)
     * @param y The y position to draw text at (bottom left of text, including descent)
     * @param z The z position to draw text at (bottom left of text, including descent)
     * @param angleDegX angle to rotate the text
     * @param angleDegY angle to rotate the text
     * @param angleDegZ angle to rotate the text
     */
    public void draw(String text, float x, float y, float z, float angleDegX, float angleDegY, float angleDegZ)  {
        float chrHeight = mCellHeight * mScaleY;          // Calculate Scaled Character Height
        float chrWidth = mCellWidth * mScaleX;            // Calculate Scaled Character Width
        int len = text.length();                        // Get String Length
        x += ( chrWidth / 2.0f ) - ( mFontPaddingX * mScaleX);  // Adjust Start X
        y += ( chrHeight / 2.0f ) - ( mFontPaddingY * mScaleY);  // Adjust Start Y

        // create a model matrix based on x, y and angleDeg
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, z);
        Matrix.rotateM(modelMatrix, 0, angleDegZ, 0, 0, 1);
        Matrix.rotateM(modelMatrix, 0, angleDegX, 1, 0, 0);
        Matrix.rotateM(modelMatrix, 0, angleDegY, 0, 1, 0);

        float letterX, letterY;
        letterX = letterY = 0;

        for (int i = 0; i < len; i++)  {              // FOR Each Character in String
            int c = (int)text.charAt(i) - CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (c < 0 || c >= CHAR_CNT)                // IF Character Not In Font
                c = CHAR_UNKNOWN;                         // Set to Unknown Character Index
            //TODO: optimize - applying the same model matrix to all the characters in the string
            mBatch.drawSprite(letterX, letterY, chrWidth, chrHeight, mCharRegion[c], modelMatrix);  // Draw the Character
            letterX += (mCharWidths[c] + mSpaceX) * mScaleX;    // Advance X Position by Scaled Character Width
        }
    }
    public void draw(String text, float x, float y, float z, float angleDegZ) {
        draw(text, x, y, z, 0, 0, angleDegZ);
    }
    public void draw(String text, float x, float y, float angleDeg) {
        draw(text, x, y, 0, angleDeg);
    }

    public void draw(String text, float x, float y) {
        draw(text, x, y, 0, 0);
    }

    /**
     * Draw text centered at the specified x,y position
     * @param text The string to draw
     * @param x The x position to draw text at (bottom left of text)
     * @param y The y position to draw text at (bottom left of text)
     * @param z The z position to draw text at (bottom left of text)
     * @param angleDegX angle to rotate the text
     * @param angleDegY angle to rotate the text
     * @param angleDegZ angle to rotate the text
     * @return The total width of the text that was drawn
     */
    public float drawC(String text, float x, float y, float z, float angleDegX, float angleDegY, float angleDegZ)  {
        float len = getLength( text );                  // Get Text Length
        draw( text, x - ( len / 2.0f ), y - ( getCharHeight() / 2.0f ), z, angleDegX, angleDegY, angleDegZ );  // Draw Text Centered
        return len;                                     // Return Length
    }
    public float drawC(String text, float x, float y, float z, float angleDegZ) {
        return drawC(text, x, y, z, 0, 0, angleDegZ);
    }
    public float drawC(String text, float x, float y, float angleDeg) {
        return drawC(text, x, y, 0, angleDeg);
    }
    public float drawC(String text, float x, float y) {
        float len = getLength( text );                  // Get Text Length
        return drawC(text, x - (len / 2.0f), y - ( getCharHeight() / 2.0f ), 0);

    }
    public float drawCX(String text, float x, float y)  {
        float len = getLength( text );                  // Get Text Length
        draw( text, x - ( len / 2.0f ), y );            // Draw Text Centered (X-Axis Only)
        return len;                                     // Return Length
    }
    public void drawCY(String text, float x, float y)  {
        draw( text, x, y - ( getCharHeight() / 2.0f ) );  // Draw Text Centered (Y-Axis Only)
    }

    //--Set Scale--//
    // D: set the scaling to use for the font
    // A: scale - uniform scale for both x and y axis scaling
    //    sx, sy - separate x and y axis scaling factors
    // R: [none]
    public void setScale(float scale)  {
        mScaleX = mScaleY = scale;                        // Set Uniform Scale
    }
    public void setScale(float sx, float sy)  {
        mScaleX = sx;                                    // Set X Scale
        mScaleY = sy;                                    // Set Y Scale
    }

    //--Get Scale--//
    // D: get the current scaling used for the font
    // A: [none]
    // R: the x/y scale currently used for scale
    public float getScaleX()  {
        return mScaleX;                                  // Return X Scale
    }
    public float getScaleY()  {
        return mScaleY;                                  // Return Y Scale
    }

    //--Set Space--//
    // D: set the spacing (unscaled; ie. pixel size) to use for the font
    // A: space - space for x axis spacing
    // R: [none]
    public void setSpace(float space)  {
        mSpaceX = space;                                 // Set Space
    }

    //--Get Space--//
    // D: get the current spacing used for the font
    // A: [none]
    // R: the x/y space currently used for scale
    public float getSpace()  {
        return mSpaceX;                                  // Return X Space
    }

    //--Get Length of a String--//
    // D: return the length of the specified string if rendered using current settings
    // A: text - the string to get length for
    // R: the length of the specified string (pixels)
    public float getLength(String text) {
        float len = 0.0f;                               // Working Length
        int strLen = text.length();                     // Get String Length (Characters)
        for ( int i = 0; i < strLen; i++ )  {           // For Each Character in String (Except Last
            int c = (int)text.charAt( i ) - CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            len += ( mCharWidths[c] * mScaleX);           // Add Scaled Character Width to Total Length
        }
        len += ( strLen > 1 ? ( ( strLen - 1 ) * mSpaceX) * mScaleX : 0 );  // Add Space Length
        return len;                                     // Return Total Length
    }

    //--Get Width/Height of Character--//
    // D: return the scaled width/height of a character, or max character width
    //    NOTE: since all characters are the same height, no character index is required!
    //    NOTE: excludes spacing!!
    // A: chr - the character to get width for
    // R: the requested character size (scaled)
    public float getCharWidth(char chr)  {
        int c = chr - CHAR_START;                       // Calculate Character Index (Offset by First Char in Font)
        return ( mCharWidths[c] * mScaleX);              // Return Scaled Character Width
    }
    public float getCharWidthMax()  {
        return ( mCharWidthMax * mScaleX);               // Return Scaled Max Character Width
    }
    public float getCharHeight() {
        return ( mCharHeight * mScaleY);                 // Return Scaled Character Height
    }

    //--Get Font Metrics--//
    // D: return the specified (scaled) font metric
    // A: [none]
    // R: the requested font metric (scaled)
    public float getAscent()  {
        return ( mFontAscent * mScaleY);                 // Return Font Ascent
    }
    public float getDescent()  {
        return ( mFontDescent * mScaleY);                // Return Font Descent
    }
    public float getHeight()  {
        return ( mFontHeight * mScaleY);                 // Return Font Height (Actual)
    }

    //--Draw Font Texture--//
    // D: draw the entire font texture (NOTE: for testing purposes only)
    // A: width, height - the width and height of the area to draw to. this is used
    //    to draw the texture to the top-left corner.
    //    vpMatrix - View and projection matrix to use
    public void drawTexture(int width, int height, float[] vpMatrix)  {
        initDraw(1.0f, 1.0f, 1.0f, 1.0f);

        mBatch.beginBatch(vpMatrix);                  // Begin Batch (Bind Texture)
        float[] idMatrix = new float[16];
        Matrix.setIdentityM(idMatrix, 0);
        mBatch.drawSprite(width - (mTextureSize / 2), height - (mTextureSize / 2),
                mTextureSize, mTextureSize, mTextureRegion, idMatrix);  // Draw
        mBatch.endBatch();                               // End Batch
    }
}