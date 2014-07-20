package com.nickstephen.gamelib.opengl.text;

import android.content.Context;
import android.opengl.Matrix;

import com.nickstephen.gamelib.opengl.Shape;
import com.nickstephen.gamelib.opengl.SpriteHelper;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.program.Program;

import org.jetbrains.annotations.NotNull;

/**
 * <p>An extension to Shape meant for drawing text to screen (the equivalent of {@link android.widget.TextView}).
 * Note that the bulk of the code is in {@link com.nickstephen.gamelib.opengl.text.TextUtil}, this is
 * just the programmer visible API side of things.</p>
 *
 * Doesn't currently support text with over 24 characters. //TODO: Increase this limit
 * @author Nick Stephen
 */
public class Text extends Shape {
    private static final float[] defColour = { 1.0f, 1.0f, 1.0f, 1.0f };

    /**
     * Destroys the singleton instance of {@link com.nickstephen.gamelib.opengl.text.TextUtil}. Should
     * always be called on pausing the GLThread and never before!
     */
    public static void destroyInstance() {
        TextUtil.destroyInstance();
    }

    protected String mText = "";
    boolean mCentered = true;
    private float[] mIdentityMatrix;
    float mScaleX = 1.0f;
    float mScaleY = 1.0f;
    float mSpaceX;


    /**
     * Default constructor.
     * @param context A context
     * @param parent The container for this shape
     * @param fontFile The filename of the font file to be loaded (must be located in the /assets/
     *                 folder)
     * @param text The initial text to display to screen
     */
    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile, @NotNull String text) {
        this(context, parent, fontFile);

        setText(text);
    }

    /**
     * Constructor 1. Doesn't automatically set the text to display, which should be done before any
     * rendering passes.
     * @param context A context
     * @param parent The container for this shape
     * @param fontFile The filename of the font file to be loaded (must be located in the /assets/
     *                 folder)
     */
    public Text(@NotNull Context context, @NotNull Container parent, @NotNull String fontFile) {
        super(context, parent, Program.BatchTextProgram.create());

        this.setColour(defColour);

        TextUtil.init(context.getAssets(), fontFile);
        TextUtil.getInstance().load(this);

        mVertices = new SpriteHelper(this);

        mIdentityMatrix = new float[16];
        Matrix.setIdentityM(mIdentityMatrix, 0);
    }

    /**
     * Get the text which will be displayed to screen
     * @return The string version of the text
     */
    public @NotNull String getText() {
        return mText;
    }

    /**
     * Sets the text to be displayed to screen. NOTE: Will only display the first 24 characters!
     * @see com.nickstephen.gamelib.opengl.text.Text
     * @param text The new text to display
     */
    public void setText(@NotNull String text) {
        mText = text;

        reloadVertices();
    }

    /**
     * Reload the vertex information with any changes that have previously been performed on this
     * object.
     */
    private void reloadVertices() {
        ((SpriteHelper) mVertices).reset();
        TextUtil.getInstance().load(this).addTextToBatch((SpriteHelper) mVertices);
        ((SpriteHelper) mVertices).finishAddingSprites();
    }

    /**
     * Move a relative distance inside the container. Just overriden here to make sure there's a call
     * to {@link #reloadVertices()}.
     * @param dx The distance between the old and new x offsets (pixels)
     * @param dy The distance between the old and new y offsets (pixels)
     */
    @Override
    public void move(float dx, float dy) {
        super.move(dx, dy);

        reloadVertices();
    }

    /**
     * Move to a new container offset. Just override here to make sure there's a call to
     * {@link #reloadVertices()}.
     * @param newX The new x offset (pixels)
     * @param newY The new y offset (pixels)
     */
    @Override
    public void moveTo(float newX, float newY) {
        super.moveTo(newX, newY);

        reloadVertices();
    }

    /**
     * Since the model matrices are already taken care of in
     * {@link com.nickstephen.gamelib.opengl.SpriteHelper}, simply return the identity matrix so that
     * the multiplication in {@link #draw(float[])} is ignored.
     * @return The identity matrix
     */
    @NotNull
    @Override
    public float[] getModelMatrix() {
        Matrix.setIdentityM(mIdentityMatrix, 0);
        return mIdentityMatrix;
    }

    /**
     * Sets whether the text should be centered around its given position
     * @param val True to centre, false otherwise
     */
    public void setCentered(boolean val) {
        mCentered = val;

        reloadVertices();
    }

    /**
     * Set both of the multipliers for the text's display scale. Should normally be 0 < x < 1 so as
     * not to decrease the quality of the text texture. If you need a bigger texture then increase
     * the value of {@link com.nickstephen.gamelib.opengl.text.TextUtil#FONT_SIZE}.
     * @param x The new scale for the x-axis
     * @param y The new scale for the y-axis
     */
    public void setScale(float x, float y) {
        mScaleX = x;
        mScaleY = y;
        reloadVertices();
    }

    /**
     * Sets the scale to multiply the text's size by in the x-axis. De-coupling this from the y scale
     * will result in warped text.
     * @see #setScale(float, float)
     * @param x The new scale for the x-axis
     */
    public void setScaleX(float x) {
        mScaleX = x;

        reloadVertices();
    }

    /**
     * Sets the scale to multiply the text's size by in the y-axis. De-coupling this from the x scale
     * will result in warped text.
     * @see #setScale(float, float)
     * @param y The new scale for the y-axis
     */
    public void setScaleY(float y) {
        mScaleY = y;

        reloadVertices();
    }

    /**
     * Set the space to put between characters when they're displayed on screen, i.e. controls
     * keming.
     * @param x Distance in pixels
     */
    public void setSpaceX(float x) {
        mScaleX = x;

        reloadVertices();
    }
}
