package com.nickstephen.gamelib.opengl.text;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.nickstephen.gamelib.opengl.GLText;
import com.nickstephen.gamelib.opengl.SpriteBatch;
import com.nickstephen.gamelib.opengl.TextureHelper;
import com.nickstephen.gamelib.opengl.TextureRegion;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.gamelib.opengl.program.BatchTextProgram;
import com.nickstephen.gamelib.opengl.program.Program;

/**
 * Created by Nick Stephen on 8/03/14.
 */
class TextUtil {
    private static final int FONT_SIZE = 20;
    private static final int FONT_COLOUR = 0xFFFFFFFF;

    private static TextUtil sInstance;

    public static TextUtil getInstance() {
        return sInstance;
    }

    /**
     * MUST BE CALLED ON GLThread.Pause()! Otherwise errors will occur on restart with cached programs
     * and whatnot!
     */
    public static void destroyInstance() {
        sInstance = null;
    }

    public static TextUtil init(AssetManager assets, String file) {
        if (sInstance == null) {
            sInstance = new TextUtil(assets, file);
        }
        return sInstance;
    }

    private Program mProgram;
    private SpriteBatch mBatch;
    private float[] mCharWidths = new float[GLText.CHAR_CNT];
    private TextureRegion[] mCharRegion = new TextureRegion[GLText.CHAR_CNT];
    private int mColorHandle;
    private int mTextureUniformHandle;
    private float mFontHeight;
    private float mFontAscent;
    private float mFontDescent;
    private float mCharWidthMax;
    private float mCharHeight;
    private int mCellWidth;
    private int mCellHeight;
    private int mTextureSize;
    private int mColCount;
    private int mRowCount;
    private int mTextureId;
    private Text mTextObj;

    private TextUtil(AssetManager assets, String file) {
        mProgram = new BatchTextProgram();
        mProgram.init();

        mBatch = new SpriteBatch(GLText.CHAR_BATCH_SIZE, mProgram);

        mColorHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_Color");
        Utilities.checkGlError("glGetUniformLocation");
        mTextureUniformHandle = GLES20.glGetUniformLocation(mProgram.getHandle(), "u_Texture");
        Utilities.checkGlError("glGetUniformLocation");

        Typeface tf = Typeface.createFromAsset(assets, file);
        Paint paint = new Paint();                      // Create Android Paint Instance
        paint.setAntiAlias(true);                     // Enable Anti Alias
        paint.setTextSize(FONT_SIZE);                      // Set Text Size
        paint.setColor(FONT_COLOUR);                   // Set ARGB (White, Opaque)
        paint.setTypeface(tf);                        // Set Typeface

        // get font metrics
        Paint.FontMetrics fm = paint.getFontMetrics();  // Get Font Metrics
        mFontHeight = (float)Math.ceil( Math.abs( fm.bottom ) + Math.abs( fm.top ) );  // Calculate Font Height
        mFontAscent = (float)Math.ceil( Math.abs( fm.ascent ) );  // Save Font Ascent
        mFontDescent = (float)Math.ceil( Math.abs( fm.descent ) );  // Save Font Descent

        // determine the width of each character (including unknown character)
        // also determine the maximum character width
        char[] chars = new char[GLText.CHAR_CNT];
        int cnt = 0;
        for (char i = GLText.CHAR_START; i <= GLText.CHAR_END; i++) {
            chars[cnt++] = i;
        }
        paint.getTextWidths(chars, 0, chars.length, mCharWidths);

        chars[0] = GLText.CHAR_NONE;
        float[] w = new float[2];
        paint.getTextWidths(chars, 0, 1, w);
        mCharWidths[cnt] = w[0];

        for (int i = 0; i < GLText.CHAR_CNT; i++) {
            if (mCharWidths[i] > mCharWidthMax) {
                mCharWidthMax = mCharWidths[i];
            }
        }

        mCharHeight = mFontHeight;

        // find the maximum size, validate, and setup cell sizes
        mCellWidth = (int) mCharWidthMax; // + ( 2 * mFontPaddingX);  // Set Cell Width
        mCellHeight = (int) mCharHeight; // + ( 2 * mFontPaddingY);  // Set Cell Height
        int maxSize = Math.max(mCellWidth, mCellHeight);
        if ( maxSize < GLText.FONT_SIZE_MIN || maxSize > GLText.FONT_SIZE_MAX )  // IF Maximum Size Outside Valid Bounds
            throw new RuntimeException("Max char size outside bounds");                                // Throw Error

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
        Canvas canvas = new Canvas(bitmap);           // Create Canvas for Rendering to Bitmap
        bitmap.eraseColor(Color.TRANSPARENT);                // Set Transparent Background (ARGB)

        // calculate rows/columns
        // NOTE: while not required for anything, these may be useful to have :)
        mColCount = mTextureSize / mCellWidth;               // Calculate Number of Columns
        mRowCount = (int)Math.ceil((float)GLText.CHAR_CNT / (float) mColCount);  // Calculate Number of Rows

        // render each of the characters to the canvas (ie. build the font map)
        float x = 0; //mFontPaddingX;                             // Set Start Position (X)
        float y = (mCellHeight - 1) - mFontDescent; // - mFontPaddingY;  // Set Start Position (Y)
        char[] s = new char[2];
        for (char c = GLText.CHAR_START; c <= GLText.CHAR_END; c++)  {  // FOR Each Character
            s[0] = c;                                    // Set Character to Draw
            canvas.drawText(s, 0, 1, x, y, paint);     // Draw Character
            x += mCellWidth;                              // Move to Next Character
            //if ( ( x + mCellWidth - mFontPaddingX) > mTextureSize)  {  // IF End of Line Reached
            if ((x + mCellWidth) > mTextureSize) {
                //x = mFontPaddingX;                             // Set X for New Row
                x = 0;
                y += mCellHeight;                          // Move Down a Row
            }
        }
        s[0] = GLText.CHAR_NONE;                               // Set Character to Use for NONE
        canvas.drawText(s, 0, 1, x, y, paint);        // Draw Character

        // save the bitmap in a texture
        mTextureId = TextureHelper.loadTexture(bitmap);

        // setup the array of character texture regions
        x = 0;                                          // Initialize X
        y = 0;                                          // Initialize Y
        for ( int c = 0; c < GLText.CHAR_CNT; c++ )  {         // FOR Each Character (On Texture)
            mCharRegion[c] = new TextureRegion(mTextureSize, mTextureSize, x, y, mCellWidth -1, mCellHeight -1 );  // Create Region for Character
            x += mCellWidth;                              // Move to Next Char (Cell)
            if ( x + mCellWidth > mTextureSize)  {
                x = 0;                                    // Reset X Position to Start
                y += mCellHeight;                          // Move to Next Row (Cell)
            }
        }
    }

    public void load(Text text) {
        mTextObj = text;
    }

    public void draw(float[] vpMatrix, Text text) {
        load(text);
        draw(vpMatrix);
    }

    public void draw(float[] vpMatrix) {
        GLES20.glUseProgram(mProgram.getHandle());

        GLES20.glUniform4fv(mColorHandle, 1, mTextObj.getColour(), 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextureId);
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        mBatch.beginBatch(vpMatrix);

        drawText();

        mBatch.endBatch();
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }

    private void drawText() {
        float chrHeight = mCellHeight * mTextObj.mScaleY;          // Calculate Scaled Character Height
        float chrWidth = mCellWidth * mTextObj.mScaleX;            // Calculate Scaled Character Width
        int len = mTextObj.getText().length();                        // Get String Length
        float x = mTextObj.getX(), y = mTextObj.getY();
        x += (chrWidth / 2.0f); // - ( mFontPaddingX * mScaleX);  // Adjust Start X
        y += (chrHeight / 2.0f); // - ( mFontPaddingY * mScaleY);  // Adjust Start Y

        // create a model matrix based on x, y and angleDeg
        float[] modelMatrix = new float[16];
        Matrix.setIdentityM(modelMatrix, 0);
        Matrix.translateM(modelMatrix, 0, x, y, 0);
        //Matrix.rotateM(modelMatrix, 0, angleDegZ, 0, 0, 1);
        //Matrix.rotateM(modelMatrix, 0, angleDegX, 1, 0, 0);
        //Matrix.rotateM(modelMatrix, 0, angleDegY, 0, 1, 0);

        float letterX, letterY;
        letterX = letterY = 0;

        for (int i = 0; i < len; i++)  {              // FOR Each Character in String
            int c = (int)mTextObj.getText().charAt(i) - GLText.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (c < 0 || c >= GLText.CHAR_CNT)                // IF Character Not In Font
                c = GLText.CHAR_UNKNOWN;                         // Set to Unknown Character Index
            mBatch.drawSprite(letterX, letterY, chrWidth, chrHeight, mCharRegion[c], modelMatrix);  // Draw the Character
            letterX += (mCharWidths[c] + mTextObj.mSpaceX) * mTextObj.mScaleX;    // Advance X Position by Scaled Character Width
        }
    }

    private float getLength(String text) {
        float len = 0.0f;                               // Working Length
        int strLen = text.length();                     // Get String Length (Characters)
        for (int i = 0; i < strLen; i++)  {           // For Each Character in String (Except Last
            int c = (int)text.charAt(i) - GLText.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            len += (mCharWidths[c] * mTextObj.mScaleX);           // Add Scaled Character Width to Total Length
        }
        len += (strLen > 1 ? ((strLen - 1) * mTextObj.mSpaceX) * mTextObj.mScaleX : 0);  // Add Space Length
        return len;                                     // Return Total Length
    }
}
