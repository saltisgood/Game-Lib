package com.nickstephen.gamelib.opengl.program;

import android.opengl.GLES20;

import com.nickstephen.gamelib.opengl.Utilities;
import com.nickstephen.gamelib.opengl.interfaces.IDisposable;
import com.nickstephen.gamelib.util.Pair;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A class used to abstract and help with the program concept in OpenGL.
 * @author Nick Stephen
 */
public class Program implements IDisposable {
    public static final String VARYING_PREF = "v_";

    private static final String uniform = "uniform ";
    private static final String attribute = "attribute ";
    private static final String varying = "varying ";

    private static final String pos = "gl_Position";
    private static final String colour = "gl_FragColor";

    private static final String vec2 = "vec2 ";
    private static final String vec4 = "vec4 ";
    private static final String mat4 = "mat4 ";
    private static final String flot = "float ";
    private static final String it = "int ";
    private static final String sampler2D = "sampler2D ";
    private static final String tex2D = "texture2D";
    private static final String dot = "dot";
    private static final String clamp = "clamp";

    private static final String func_sign = "void main() {\n";
    private static final String func_end = "}\n";
    private static final String line_end = ";\n";
    private static final String eq = " = ";
    private static final String mult = " * ";
    private static final String medPrec = "precision mediump float" + line_end;

    private static final String sampleTexStd = tex2D + "(" + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.TEXTURE
            + ", " + VARYING_PREF + AttrVariable.Constants.TEX_COORD + ")";

    public static class ProgramCreator {
        private ProgramCreator() {}

        public static Program create(boolean usesTexture, boolean usesChannels, boolean usesColour, boolean usesAlpha,
                                     int numShapes, boolean textureStencil) {
            StringBuilder sb = new StringBuilder();
            sb.append(uniform + mat4 + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.MVP_MAT);
            if (numShapes > 1) {
                sb.append("[").append(numShapes).append("]");
            }
            sb.append(line_end);

            if (usesChannels) {
                sb.append(uniform + vec4 + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.CHANNEL);

                if (numShapes > 1) {
                    sb.append("[").append(numShapes).append("]");
                }
                sb.append(line_end);
            }

            if (numShapes > 1) {
                sb.append(attribute + flot + AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.MVP_INDEX + line_end);
            }

            sb.append(attribute + vec4 + AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.POS + line_end);

            if (usesTexture) {
                sb.append(attribute + vec2 + AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.TEX_COORD + line_end);
                sb.append(varying + vec2 + VARYING_PREF + AttrVariable.Constants.TEX_COORD + line_end);
            }
            if (usesChannels) {
                sb.append(varying + vec4 + VARYING_PREF + UniformVariable.Constants.CHANNEL + line_end);
            }

            sb.append(func_sign);

            if (numShapes > 1) {
                sb.append(it + AttrVariable.Constants.MVP_INDEX + eq + it + "(" +
                        AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.MVP_INDEX + ")" + line_end);
            }

            if (usesTexture) {
                sb.append(VARYING_PREF + AttrVariable.Constants.TEX_COORD + eq +
                        AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.TEX_COORD + line_end);
            }

            if (usesChannels) {
                sb.append(VARYING_PREF + UniformVariable.Constants.CHANNEL + eq +
                    UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.CHANNEL);

                if (numShapes > 1) {
                    sb.append("[" + AttrVariable.Constants.MVP_INDEX + "]");
                }

                sb.append(line_end);
            }

            sb.append(pos + eq + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.MVP_MAT);
            if (numShapes > 1) {
                sb.append("[" + AttrVariable.Constants.MVP_INDEX + "]");
            }
            sb.append(mult + AttrVariable.Constants.ATTR_PREF + AttrVariable.Constants.POS + line_end + func_end);

            String vertexShader = sb.toString();

            sb.setLength(0);

            if (usesTexture) {
                sb.append(uniform + sampler2D + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.TEXTURE + line_end);
            }

            sb.append(medPrec);

            if (usesTexture) {
                sb.append(varying + vec2 + VARYING_PREF + AttrVariable.Constants.TEX_COORD + line_end);
            }

            if (usesChannels) {
                sb.append(varying + vec4 + VARYING_PREF + UniformVariable.Constants.CHANNEL + line_end);
            }

            if (usesColour) {
                sb.append(uniform + vec4 + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.COLOR + line_end);
            } else if (usesAlpha) {
                sb.append(uniform + flot + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.ALPHA + line_end);
            }

            sb.append(func_sign);

            if (usesColour) {
                if (usesTexture) {
                    if (textureStencil) {
                        sb.append(colour + eq + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.COLOR + line_end);

                        if (usesChannels) {
                            sb.append(colour + ".a" + eq + colour + ".a" + mult + clamp + "(" + dot + "(" + sampleTexStd
                                + ", " + VARYING_PREF + UniformVariable.Constants.CHANNEL + "), 0.0, 1.0)" + line_end);
                        } else {
                            sb.append(colour + ".a" + eq + colour + ".a" + mult + sampleTexStd + line_end);
                        }
                    } else {
                        sb.append(colour + eq + sampleTexStd + mult + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.COLOR + line_end);
                    }
                } else {
                    sb.append(colour + eq + UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.COLOR + line_end);
                }
            } else if (usesTexture) {
                if (textureStencil) {
                    sb.append(vec4 + "v " + eq + sampleTexStd + line_end);
                    sb.append(flot + "f " + eq + "v.r + v.g + v.b + v.a" + line_end);
                    sb.append(colour + eq + vec4 + "(f, f, f, f)" + line_end);
                } else {
                    sb.append(colour + eq + sampleTexStd + line_end);
                }

                if (usesAlpha) {
                    sb.append(colour + ".a" + eq + colour + ".a" + mult +
                            UniformVariable.Constants.UNI_PREF + UniformVariable.Constants.ALPHA + line_end);
                }
            } else {
                throw new RuntimeException("Invalid specifications!");
            }

            sb.append(func_end);

            String fragShader = sb.toString();

            int arrSize = 1;
            if (usesTexture) {
                ++arrSize;
            }
            if (numShapes > 1) {
                ++arrSize;
            }
            AttrVariable[] attrs = new AttrVariable[arrSize];
            arrSize = 0;
            attrs[arrSize++] = AttrVariable.A_Position;
            if (usesTexture) {
                attrs[arrSize++] = AttrVariable.A_TexCoordinate;
            }
            if (numShapes > 1) {
                attrs[arrSize] = AttrVariable.A_MVPMatrixIndex;
            }

            arrSize = 1;
            if (usesTexture) {
                ++arrSize;
            }
            if (usesChannels) {
                ++arrSize;
            }
            if (usesColour || usesAlpha) {
                ++arrSize;
            }

            UniformVariable[] unis = new UniformVariable[arrSize];
            unis[0] = UniformVariable.U_MVPMatrix;
            arrSize = 1;
            if (usesTexture) {
                unis[arrSize++] = UniformVariable.U_Texture;
            }
            if (usesChannels) {
                unis[arrSize++] = UniformVariable.U_ChannelBalance;
            }
            if (usesColour) {
                unis[arrSize] = UniformVariable.U_Colour;
            } else if (usesAlpha) {
                unis[arrSize] = UniformVariable.U_Alpha;
            }

            return Manager.get(vertexShader, fragShader, attrs, unis);
        }
    }

    public static class TestTextProgram {
        private static final AttrVariable[] programVariables = {
                AttrVariable.A_Position, AttrVariable.A_TexCoordinate, AttrVariable.A_MVPMatrixIndex
        };

        private static final UniformVariable[] uniVariables = {
                UniformVariable.U_MVPMatrix, UniformVariable.U_Texture,
                UniformVariable.U_ChannelBalance, UniformVariable.U_Colour
        };

        private static final String vertexShaderCode =
                "uniform mat4 u_MVPMatrix[24];      \n"     // An array representing the combined
                + "uniform vec4 u_Channel[24];"
                        // model/view/projection matrices for each sprite

                        + "attribute float a_MVPMatrixIndex; \n"	// The index of the MVPMatrix of the particular sprite
                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec2 a_TexCoordinate;\n"     // Per-vertex texture coordinate information we will pass in

                        + "varying vec2 v_TexCoordinate;  \n"   // This will be passed into the fragment shader.
                        + "varying vec4 v_Channel;"
                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   int mvpMIndex = int(a_MVPMatrixIndex); \n"
                        + "   v_TexCoordinate = a_TexCoordinate; \n"
                        + "v_Channel = u_Channel[mvpMIndex];"
                        + "   gl_Position = u_MVPMatrix[mvpMIndex]   \n"     // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        // normalized screen coordinates.
                        + "}                              \n";


        private static final String fragmentShaderCode =
                "uniform sampler2D u_Texture;       \n"    // The input texture.
                        +	"precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec2 v_TexCoordinate;  \n" // Interpolated texture coordinate per fragment.
                        + "varying vec4 v_Channel;"
                        + "uniform vec4 u_Color;"

                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                            + "gl_FragColor = vec4(u_Color.r, u_Color.g, u_Color.b, clamp(dot(texture2D(u_Texture, v_TexCoordinate), v_Channel), 0.0, 1.0) * u_Color.a);"
                        + "}                             \n";


        private TestTextProgram() {}

        public static Program create() {
            return Manager.get(vertexShaderCode, fragmentShaderCode, programVariables, uniVariables);
        }
    }

    public static class BatchTextProgram {
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


        private BatchTextProgram() {}

        public static Program create() {
            return Manager.get(vertexShaderCode, fragmentShaderCode, programVariables, uniVariables);
        }
    }

    public static class GenericProgram {
        private static final AttrVariable[] attrVariables = { AttrVariable.A_Position };
        private static final UniformVariable[] uniVariables = { UniformVariable.U_Colour, UniformVariable.U_MVPMatrix };

        private static final String vertexShaderCode =
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

        private static final String fragmentShaderCode =
                "precision mediump float;" +
                        "uniform vec4 u_Color;" +
                        "void main() {" +
                        "gl_FragColor = u_Color;"
                        + "}";

        private GenericProgram() {}

        public static Program create() {
            return Manager.get(vertexShaderCode, fragmentShaderCode, attrVariables, uniVariables);
        }
    }

    public static class SpriteProgram {
        private static final AttrVariable[] attrVariables = {
                AttrVariable.A_Position, AttrVariable.A_TexCoordinate
        };

        private static final UniformVariable[] uniVariables = { UniformVariable.U_MVPMatrix, UniformVariable.U_Texture,
                UniformVariable.U_Alpha };

        private static final String vertexShaderCode =
                "uniform mat4 u_MVPMatrix;      \n"     // An array representing the combined
                        // model/view/projection matrices for each sprite

                        + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                        + "attribute vec2 a_TexCoordinate;\n"     // Per-vertex texture coordinate information we will pass in
                        + "varying vec2 v_TexCoordinate;  \n"   // This will be passed into the fragment shader.
                        + "void main()                    \n"     // The entry point for our vertex shader.
                        + "{                              \n"
                        + "   v_TexCoordinate = a_TexCoordinate; \n"
                        + "   gl_Position = u_MVPMatrix   \n"     // gl_Position is a special variable used to store the final position.
                        + "               * a_Position;   \n"     // Multiply the vertex by the matrix to get the final point in
                        // normalized screen coordinates.
                        + "}                              \n";


        private static final String fragmentShaderCode =
                "uniform sampler2D u_Texture;       \n"    // The input texture.
                        +	"precision mediump float;       \n"     // Set the default precision to medium. We don't need as high of a
                        // precision in the fragment shader.
                        + "varying vec2 v_TexCoordinate;  \n" // Interpolated texture coordinate per fragment.
                        + "uniform float u_Alpha;"

                        + "void main()                    \n"     // The entry point for our fragment shader.
                        + "{                              \n"
                        + "   gl_FragColor = texture2D(u_Texture, v_TexCoordinate);\n"
                        + "gl_FragColor.a = gl_FragColor.a * u_Alpha;\n"
                        + "}                             \n";

        private SpriteProgram() {}

        public static Program create() {
            return Manager.get(vertexShaderCode, fragmentShaderCode, attrVariables, uniVariables);
        }
    }

    public static class Manager {
        private final static Manager sInst = new Manager();

        /**
         * Gets a program instance based on the vertex and fragment shader codes. The variable
         * arguments are non-negotiable. If you don't want to pass anything, just pass an empty array.
         * But you should pass the variables that are in use in the shaders to this function to make the
         * best use of the in built methods.
         * @param vertexShaderCode The GLSL code to be compiled as the vertex shader
         * @param fragmentShaderCode The GLSL code to be compiled as the fragment shader
         * @param attrVariables The attribute variables in use in the shaders
         * @param uniformVariables The uniform variables in use in the shaders
         */
        public static Program get(@NotNull String vertexShaderCode, @NotNull String fragmentShaderCode, @NotNull AttrVariable[] attrVariables,
                                  @NotNull UniformVariable[] uniformVariables) {
            synchronized (sInst.mProgs) {
                for (int i = sInst.mProgs.size() - 1; i >= 0; --i) {
                    Pair<Program, Integer> p = sInst.mProgs.get(i);

                    if (p.left.mFragmentShaderCode.compareTo(fragmentShaderCode) == 0 &&
                            p.left.mVertexShaderCode.compareTo(vertexShaderCode) == 0) {
                        ++p.right;
                        return p.left;
                    }
                }

                Program p = new Program();
                p.mVertexShaderCode = vertexShaderCode;
                p.mFragmentShaderCode = fragmentShaderCode;
                p.mAttrVariables = attrVariables;
                p.mUniformVariables = uniformVariables;

                sInst.mProgs.add(new Pair<Program, Integer>(p, 1));
                return p;
            }
        }

        private static void release(@NotNull Program program) {
            synchronized (sInst.mProgs) {
                for (int i = sInst.mProgs.size() - 1; i >= 0; --i) {
                    Pair<Program, Integer> p = sInst.mProgs.get(i);

                    if (p.left.equals(program)) {
                        --p.right;

                        if (p.right <= 0) {
                            program.delete();

                            sInst.mProgs.remove(i);
                            break;
                        }
                    }
                }
            }
        }

        @SuppressWarnings("SpellCheckingInspection")
        private final List<Pair<Program, Integer>> mProgs = new ArrayList<Pair<Program, Integer>>();

        private Manager() {}
    }

    private AttrVariable[] mAttrVariables = null;

    private int mFragmentShaderHandle = 0;
    private String mFragmentShaderCode = null;

    private int mVertexShaderHandle = 0;
    private String mVertexShaderCode = null;

    private boolean mInitialized = false;
    private int mProgramHandle = 0;
    private UniformVariable[] mUniformVariables = null;

    private Program() { }

    public final void dispose() {
        Manager.release(this);
    }

    /**
     * Delete the shaders and program from the OpenGL context
     */
    private void delete() {
        GLES20.glDeleteShader(mVertexShaderHandle);
        GLES20.glDeleteShader(mFragmentShaderHandle);
        GLES20.glDeleteProgram(mProgramHandle);
        Utilities.checkGlError("glDeleteShader/Program");
        mInitialized = false;
    }

    /**
     * Get the handle to the program that's lazily created
     * @return The handle to the OpenGL program
     */
    public int getHandle() {
        if (!mInitialized) {
            setup();
        }

        return mProgramHandle;
    }

    private void setup() {
        mVertexShaderHandle = Utilities.loadShader(GLES20.GL_VERTEX_SHADER, mVertexShaderCode);
        mFragmentShaderHandle = Utilities.loadShader(GLES20.GL_FRAGMENT_SHADER, mFragmentShaderCode);

        mProgramHandle = Utilities.createProgram(mVertexShaderHandle, mFragmentShaderHandle, mAttrVariables);

        Utilities.checkGlError("Create program");

        mInitialized = true;
    }

    /**
     * Check whether the program has been initialised.
     * @return True if the program has been initialised, false otherwise.
     */
    public boolean isInitialized() {
        return mInitialized;
    }

    /**
     * Check whether the program uses one the uniform variable enums
     * @param uni The uniform variable to check
     * @return True if the variable is in use, false otherwise
     */
    public boolean usesVariable(UniformVariable uni) {
        for (int i = 0; i < mUniformVariables.length; ++i) {
            if (mUniformVariables[i] == uni) {
                return true;
            }
        }

        return false;
    }

    /**
     * Check whether the program uses one of the attribute variable enums
     * @param attrib The attribute variable to check
     * @return True if the variable is in use, false otherwise
     */
    public boolean usesVariable(AttrVariable attrib) {
        for (int i = 0; i < mAttrVariables.length; ++i) {
            if (mAttrVariables[i] == attrib) {
                return true;
            }
        }

        return false;
    }
}