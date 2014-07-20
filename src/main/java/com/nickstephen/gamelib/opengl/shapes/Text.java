package com.nickstephen.gamelib.opengl.shapes;

import android.content.Context;

import com.nickstephen.gamelib.opengl.AnimatedSprite;
import com.nickstephen.gamelib.opengl.SpriteHelper;
import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.gamelib.opengl.layout.Container;
import com.nickstephen.gamelib.opengl.textures.Texture;
import com.nickstephen.gamelib.util.Pair;
import com.nickstephen.gamelib.util.Triple;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nick Stephen on 17/07/2014.
 */
public class Text extends SpriteBatch {
    public static class Font {
        public final String[] textureNames;
        public final String fontName;

        public Font(String name, String[] texNames) {
            fontName = name;
            textureNames = texNames;
        }
    }

    public static class FontManager {
        private final static FontManager sInst = new FontManager();

        public static @NotNull Text getText(@NotNull Context context, @NotNull Container parent,
                                            @NotNull Font font) {
            synchronized (sInst.mFonts) {
                for (int i = sInst.mFonts.size() - 1; i >= 0; --i) {
                    Pair<Font, Integer> p = sInst.mFonts.get(i);

                    if (p.left.fontName.compareTo(font.fontName) == 0) {
                        ++p.right;

                        return new Text(context, parent, p.left);
                    }
                }

                sInst.mFonts.add(new Pair<Font, Integer>(font, 1));
                return new Text(context, parent, font);
            }
        }

        private final List<Pair<Font, Integer>> mFonts = new ArrayList<Pair<Font, Integer>>();

        private FontManager() {}
    }

    public static final float DEFAULT_FONT_SIZE = 40.f;

    protected final Font mFont;
    protected String mText;
    private boolean mTextInvalidated = false;
    protected float[] mChannelArr;
    protected float mCharSize = DEFAULT_FONT_SIZE;
    protected boolean mCentered = true;

    private Text(@NotNull Context context, @NotNull Container parent, @NotNull Font font) {
        super(context, parent);

        mFont = font;
    }

    public void setText(@Nullable String text) {
        if (text != null && text.length() > SpriteHelper.MAX_SPRITES) {
            throw new RuntimeException("Max text length currently 24 chars");
        }

        mText = text;
        mTextInvalidated = true;
    }

    public void setFontSize(float s) {
        if (s < 0.f) {
            s = DEFAULT_FONT_SIZE;
        }

        mCharSize = s;
    }

    public void setCentered(boolean centered) {
        mCentered = centered;
    }

    protected String containingText(char c) {
        //TODO: Implement for multiple texture fonts
        return mFont.textureNames[0];
    }

    protected int containingFrame(char c) {
        switch (c) {
            case 'A':
            case 'Q':
            case 'g':
                return 0;
            case 'B':
            case 'R':
            case 'h':
                return 1;
            case 'C':
            case 'S':
            case 'i':
                return 2;
            case 'D':
            case 'T':
            case 'j':
                return 3;
            case 'E':
            case 'U':
            case 'k':
                return 8;
            case 'F':
            case 'V':
            case 'l':
                return 9;
            case 'G':
            case 'W':
            case 'm':
                return 10;
            case 'H':
            case 'X':
            case 'n':
                return 11;
            case 'I':
            case 'Y':
            case 'o':
                return 16;
            case 'J':
            case 'Z':
            case 'p':
                return 17;
            case 'K':
            case 'a':
            case 'q':
                return 18;
            case 'L':
            case 'b':
            case 'r':
                return 19;
            case 'M':
            case 'c':
            case 's':
                return 24;
            case 'N':
            case 'd':
            case 't':
                return 25;
            case 'O':
            case 'e':
            case 'u':
                return 26;
            case 'P':
            case 'f':
            case 'v':
                return 27;
            case 'w':
            case '#':
            case '=':
                return 4;
            case 'x':
            case '$':
            case ';':
                return 5;
            case 'y':
            case '%':
            case ':':
                return 6;
            case 'z':
            case '^':
            case '{':
                 return 7;
            case '0':
            case '&':
            case '}':
                return 12;
            case '1':
            case '*':
            case '[':
                return 13;
            case '2':
            case '(':
            case ']':
                return 14;
            case '3':
            case ')':
            case '\'':
                return 15;
            case '4':
            case '-':
            case '<':
                return 20;
            case '5':
            case '_':
            case '>':
                return 21;
            case '6':
            case ',':
                return 22;
            case '7':
            case '.':
                return 23;
            case '8':
            case '\"':
                return 28;
            case '9':
            case '?':
                return 29;
            case '!':
            case '/':
                return 30;
            case '@':
            case '+':
                return 31;
        }

        throw new RuntimeException("unexpected char value: " + c);
    }

    protected float[] containingChannel(char c) {
        if ((c >= 'A' && c <= 'P') || (c >= 'w' && c <= 'z') || (c >= '0' && c <= '9')) {
            return Utilities.red;
        } else if (c >= 'Q' && c <= 'f') {
            return Utilities.green;
        } else if (c >= 'g' && c <= 'v') {
            return Utilities.blue;
        }

        switch (c) {
            case '!':
            case '@':
                return Utilities.red;
            case '#':
            case '$':
            case '%':
            case '^':
            case '&':
            case '*':
            case '(':
            case ')':
            case '-':
            case '_':
            case ',':
            case '.':
            case '\"':
            case '?':
            case '/':
            case '+':
                return Utilities.green;
            case '=':
            case ';':
            case ':':
            case '{':
            case '}':
            case '[':
            case ']':
            case '\'':
            case '<':
            case '>':
                return Utilities.blue;
        }

        throw new RuntimeException("unexpected char value: " + c);
    }

    protected float getCharacterKerning(char c) {
        float adj = 0.4f;

        switch (c) {
            case 'l':
                adj = 0.5f;
                break;
            case '!':
                adj = 0.6f;
                break;
        }

        return mCharSize * adj;
    }

    @Override
    public void draw(@NotNull float[] vpMatrix) {
        if (mTextInvalidated) {
            clear(true);

            if (mText != null) {
                final int len = mText.length();
                float adjust = (len % 2 == 0) ? mCharSize / 2.f : 0.f;
                mChannelArr = new float[Utilities.QUAD_CHANNEL * len];

                for (int i = 0; i < len; ++i) {
                    char c = mText.charAt(i);

                    String texture = containingText(c);

                    AnimatedSprite s = new AnimatedSprite(mContext, getParent(), texture, mCharSize, mCharSize,
                            8, 4);
                    s.getTextureId(); // refreshes the texture stuff
                    s.gotoFrame(containingFrame(c));

                    if (i != 0) {
                        adjust -= getCharacterKerning(c);
                    }
                    float xmove = ((i - (len / 2)) * mCharSize) + adjust;
                    s.move(xmove, 0.f);

                    addSpriteToBatch(s);

                    System.arraycopy(containingChannel(c), 0, mChannelArr, Utilities.QUAD_CHANNEL * i, Utilities.QUAD_CHANNEL);
                }

                if (mCentered) {
                    moveTo(mBoundsChecker.getX() - );
                }
            } else {
                mChannelArr = null;
            }

            refresh();
            mTextInvalidated = false;
        }

        if (mText == null) {
            return;
        }

        super.draw(vpMatrix);
    }

    @Override
    public int getTextureId() {
        if (mSprites.size() != 0) {
            return mSprites.get(0).getTextureId();
        }
        return Texture.TEX_ID_UNASSIGNED;
    }

    @Nullable
    @Override
    public float[] getChannel() {
        return mChannelArr;
    }

    @Override
    public void moveTo(float newX, float newY) {
        if (mCentered) {

        }

        super.moveTo(newX, newY);
    }
}
