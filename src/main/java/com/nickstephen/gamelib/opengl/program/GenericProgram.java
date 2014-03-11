package com.nickstephen.gamelib.opengl.program;

/**
 * A {@link Program} that is used for any number of vertices with a single MVP matrix and a
 * single vector-4 colour.
 * @author Nick Stephen
 */
public class GenericProgram extends Program {
    private static final AttrVariable[] attrVariables = { AttrVariable.A_Position };
    private static final UniformVariable[] uniVariables = { UniformVariable.U_Colour, UniformVariable.U_MVPMatrix };

    private static final String mVertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 u_MVPMatrix;" +
                    "attribute vec4 a_Position;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = u_MVPMatrix * a_Position;" +
                    "}";

    private static final String mFragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 u_Color;" +
                    "void main() {" +
                    "  gl_FragColor = u_Color;" +
                    "}";

    @Override
    public void init() {
        super.init(mVertexShaderCode, mFragmentShaderCode, attrVariables, uniVariables);
    }
}
