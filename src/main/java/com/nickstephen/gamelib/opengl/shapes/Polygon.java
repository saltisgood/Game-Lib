package com.nickstephen.gamelib.opengl.shapes;

import android.content.Context;
import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.bounds.Radial;
import com.nickstephen.gamelib.opengl.layout.Container;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * <p>A class for drawing generic regular concave polygons (triangle, square, circle). Does fill
 * itself in, unlike {@link com.nickstephen.gamelib.opengl.Quadrilateral}.</p>
 *
 * <p>Uses the GL_TRIANGLE_FAN primitive type to draw itself, so all shapes are essentially just
 * the same as each other but with more sides.</p>
 * @author Nick Stephen
 */
public class Polygon extends Shape {
    private static final int CIRCLE_SIDE_NO = 100;

    public static Polygon createCircle(@NotNull Context context, @Nullable Container parent, float radius) {
        return new Polygon(context, parent, 0, 0, radius, 0, CIRCLE_SIDE_NO, color);
    }

    public static Polygon createTriangle(@NotNull Context context, @Nullable Container parent, float radius) {
        return new Polygon(context, parent, 0, 0, radius, 0, 3, color);
    }

    public static Polygon createSquare(@NotNull Context context, @Nullable Container parent, float radius) {
        return new Polygon(context, parent, 0, 0, radius, 0, 4, color);
    }

    // number of coordinates per vertex in this array
    private static final int COORDS_PER_VERTEX = 2;

    private static final float color[] = { 0.63671875f, 0.76953125f, 0.22265625f, 1.0f };
    private final int mSideCount;

    public Polygon(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, int numberOfSides) {
        this(context, parent, posX, posY, radius, 0, numberOfSides, color);
    }

    /**
     * Default constructor.
     * @param context A context
     * @param parent The parent of this shape
     * @param posX The x offset of this shape from its parent's centre
     * @param posY The y offset of this shape from its parent's centre
     * @param radius The radius of this shape. For non-circles, think about it as if you drew a circle
     *               around the edge of the shape. The vertices of the shape would just be touching
     *               the edge of the shape.
     * @param angle The starting angle of the shape. If you're unsure about the default orientation
     *              of your shape just set it to 0, see what it looks like and go from there.
     * @param numberOfSides The number of sides for the shape. For circles I recommend 100 or more.
     * @param colour The starting colour of the shape's vertices.
     */
    public Polygon(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle, int numberOfSides, float[] colour) {
        super(context, parent);

        mBoundsChecker = new Radial(this).setWidth(radius);
        mSideCount = numberOfSides;

        if (colour.length != 4) {
            throw new RuntimeException("Colour vector must be 4 long");
        }
        mColour = colour;

        mVertices = new Vertices(this, mSideCount + 1, mSideCount * 3, GLES20.GL_TRIANGLE_FAN);

        moveTo(posX, posY);
        setAngle(angle);

        short[] order = new short[3 * mSideCount];
        for (int i = 0; i < mSideCount; i++) {
            order[3 * i] = 0;
            order[3 * i + 1] = (short)(i + 1);
            order[3 * i + 2] = (i + 2 <= mSideCount) ? (short)(i + 2) : 1;
        }
        mVertices.setIndices(order, 0, 3 * mSideCount);

        setVertices();
    }

    public Polygon(@NotNull Context context, @Nullable Container parent, float posX, float posY, float radius, float angle, int numberOfSides) {
        this(context, parent, posX, posY, radius, angle, numberOfSides, color);
    }

    private void setVertices() {
        // Calculate the vertex positions
        //float x = (float)Math.cos(mAngle) * mRadius;
        //float y = (float)Math.sin(mAngle) * mRadius;
        float x = mBoundsChecker.getWidth(), y = 0;
        float[] buff = new float[COORDS_PER_VERTEX * (mSideCount + 1)];

        float theta = 2.0f * 3.14159f / (float) mSideCount;
        float tanFactor = (float) Math.tan(theta);
        float radialFactor = (float) Math.cos(theta);

        buff[0] = 0.f;
        buff[1] = 0.f;
        for (int i = 1; i < mSideCount + 1; i++) {
            buff[COORDS_PER_VERTEX * i] = x;
            buff[COORDS_PER_VERTEX * i + 1] = y;

            float tx = -y;
            float ty = x;

            x += tx * tanFactor;
            y += ty * tanFactor;

            x *= radialFactor;
            y *= radialFactor;
        }
        mVertices.setVertices(buff);
    }
}
