package com.nickstephen.gamelib.opengl.program;

import com.nickstephen.gamelib.opengl.program.Program;

/**
 * Created by Nick Stephen on 6/03/14.
 */
public class GenericProgram extends Program {
    private static final String mVertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix * vPosition;" +
                    "}";

    private static final String mFragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

    @Override
    public void init() {
        super.init(mVertexShaderCode, mFragmentShaderCode, null);
    }
}
