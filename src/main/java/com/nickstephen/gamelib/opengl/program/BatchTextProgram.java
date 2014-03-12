package com.nickstephen.gamelib.opengl.program;

/**
 * A {@link Program} that is used for up to 24 vertices at once, supporting grayscale textures multiplied
 * by a 4-vector colour. Make sure to call to {@link #init()} after constructing and before use.
 * @author Nick Stephen
 */
public class BatchTextProgram extends Program {

    private static final AttrVariable[] programVariables = {
            AttrVariable.A_Position, AttrVariable.A_TexCoordinate, AttrVariable.A_MVPMatrixIndex,
    };

    private static final UniformVariable[] uniVariables = { UniformVariable.U_MVPMatrix, UniformVariable.U_Texture, UniformVariable.U_Colour,
            UniformVariable.U_Alpha };

    private static final String vertexShaderCode =
            "uniform mat4 u_MVPMatrix[24];      \n"     // An array representing the combined
                    // model/view/projection matrices for each sprite

                    + "attribute float a_MVPMatrixIndex; \n"	// The index of the MVPMatrix of the particular sprite
                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec2 a_TexCoordinate;\n"     // Per-vertex texture coordinate information we will pass in
                    + "varying vec2 v_TexCoordinate;  \n"   // This will be passed into the fragment shader.
                    + "void main()                    \n"     // The entry point for our vertex shader.
                    + "{                              \n"
                    + "   int mvpMatrixIndex = int(a_MVPMatrixIndex); \n"
                    + "   v_TexCoordinate = a_TexCoordinate; \n"
                    + "   gl_Position = u_MVPMatrix[mvpMatrixIndex]   \n"     // gl_Position is a special variable used to store the final position.
                    + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                    // normalized screen coordinates.
                    + "}                              \n";


    private static final String fragmentShaderCode =
            "uniform sampler2D u_Texture;       \n"    // The input texture.
                    +	"precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
                    // precision in the fragment shader.
                    + "uniform vec4 u_Color;          \n"
                    + "varying vec2 v_TexCoordinate;  \n" // Interpolated texture coordinate per fragment.
                    + "uniform float u_Alpha;"

                    + "void main()                    \n"     // The entry point for our fragment shader.
                    + "{                              \n"
                    + "   gl_FragColor = texture2D(u_Texture, v_TexCoordinate).w * u_Color * u_Alpha;\n" // texture is grayscale so take only grayscale value from
                    // it when computing color output (otherwise font is always black)
                    + "}                             \n";

    /**
     * Initialise the program. Creates the shaders, program and links them.
     */
    @Override
    public void init() {
        super.init(vertexShaderCode, fragmentShaderCode, programVariables, uniVariables);
    }

}

