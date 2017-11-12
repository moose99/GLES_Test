package com.mustafathamer.gles_test;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;

import java.io.IOException;
import java.util.Scanner;


/**
 * Created by moose-home on 10/25/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer
{
    private Context mContext;
    private String mObjFileName = "fidget2";

    // handles to programs
    private int mObjectProgramHandle;
    private int mPointProgramHandle;

    /** This will be used to pass in the transformation matrix. */
    private int mMVPMatrixHandle;

    /** This will be used to pass in the modelview matrix. */
    private int mMVMatrixHandle;

    /** This will be used to pass in the light position. */
    private int mLightPosHandle;

    /** This will be used to pass in model position information. */
    private int mPositionHandle;

    /** This will be used to pass in model color information. */
    private int mDifColorHandle;
    private int mAmbColorHandle;

    /** This will be used to pass in model normal information. */
    private int mNormalHandle;

    public int GetMVPMatrixHandle() { return mMVPMatrixHandle; }
    public int GetMVMatrixHandle() { return mMVMatrixHandle; }
    public int GetLightPosHandle() { return mLightPosHandle; }
    public int GetPositionHandle() { return mPositionHandle; }
    public int GetDifColorHandle() { return mDifColorHandle; }
    public int GetAmbColorHandle() { return mAmbColorHandle; }
    public int GetNormalHandle() { return mNormalHandle; }

    //
    // objects to draw
    //
    private Triangle mTriangle;
    private Square mSquare;
    private ObjModel mObjModel;
    private Cube mCube;

    //
    // for projection
    //
    private float[] mProjectionMatrix = new float[16];    // project onto 2D viewport
    // This can be thought of as our camera. This matrix transforms world space to eye space;
    private float[] mViewMatrix = new float[16];

    public float[] GetProjectionMatrix()    { return mProjectionMatrix;   }
    public float[] GetViewMatrix()          {return mViewMatrix;    }

    /**
     * Stores a copy of the model matrix specifically for the light position.
     */
    private float[] mLightModelMatrix = new float[16];

    /** Used to hold a light centered on the origin in model space. We need a 4th coordinate so we can get translations to work when
     *  we multiply this by our transformation matrices. */
    private final float[] mLightPosInModelSpace = new float[] {0.0f, 0.0f, 0.0f, 1.0f};

    /** Used to hold the current position of the light in world space (after transformation via model matrix). */
    private final float[] mLightPosInWorldSpace = new float[4];

    /** Used to hold the transformed position of the light in eye space (after transformation via modelview matrix) */
    private final float[] mLightPosInEyeSpace = new float[4];


    //
    // Since the renderer code is running on a separate thread from the main user interface thread
    // of your application, you must declare this public variable as volatile.
    //Instead of synchronized variable in Java, you can have java volatile variable, which will instruct
    // JVM threads to read value of volatile variable from main memory and don't cache it locally.
    //
    private volatile float mYAngle;
    private volatile float mXAngle;

    public void SetContext(Context context)    { mContext = context; }
    public float GetYAngle()              { return mYAngle; }
    public float GetXAngle()              { return mXAngle; }
    public void SetYAngle(float angle)    { mYAngle = angle; }
    public void SetXAngle(float angle)    { mXAngle = angle; }

    //
    // CTOR
    //
    public MyGLRenderer()
    {

    }

    //
    // Load the given text file (containing shader code), and return as a string
    //
    private String LoadShaderCode(String fileName)
    {
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(mContext.getAssets().open(fileName));
        } catch (IOException e)
        {
            Log.e("MOOSE", "Exception opening assets file:" + fileName);
            e.printStackTrace();
        }

        String shaderCode = "";
        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            shaderCode += line;
        }
        scanner.close();

        return shaderCode;
    }

    //
    // utility func for loading shader code from string buffer
    //
    private int CreateShader(int type, String shaderCode)
    {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    /**
     * Helper function to compile and link a program.
     *
     * @param vertexShaderHandle   An OpenGL handle to an already-compiled vertex shader.
     * @param fragmentShaderHandle An OpenGL handle to an already-compiled fragment shader.
     * @param attributes           Attributes that need to be bound to the program.
     * @return An OpenGL handle to the program.
     */
    private int CreateAndLinkProgram(final int vertexShaderHandle, final int fragmentShaderHandle, final String[] attributes)
    {
        int programHandle = GLES20.glCreateProgram();
        if (programHandle != 0)
        {
            // Bind the vertex shader to the program.
            GLES20.glAttachShader(programHandle, vertexShaderHandle);

            // Bind the fragment shader to the program.
            GLES20.glAttachShader(programHandle, fragmentShaderHandle);

            // Bind attributes
            if (attributes != null)
            {
                final int size = attributes.length;
                for (int i = 0; i < size; i++)
                {
                    GLES20.glBindAttribLocation(programHandle, i, attributes[i]);
                }
            }

            // Link the two shaders together into a program.
            GLES20.glLinkProgram(programHandle);

            // Get the link status.
            final int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0);

            // If the link failed, delete the program.
            if (linkStatus[0] == GLES20.GL_FALSE)
            {
                Log.e("MOOSE", "Error compiling program: ");
                String result = GLES20.glGetProgramInfoLog(programHandle);
                Log.e("MOOSE", result);
                GLES20.glDeleteProgram(programHandle);
                programHandle = 0;
            }
        }

        if (programHandle == 0)
        {
            throw new RuntimeException("Error creating program.");
        }

        return programHandle;
    }

    //
    // set up renderer clear color, depth test, and backface cull
    // set view matrix
    // set up shaders
    //
    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        Log.d("MOOSE", "Renderer: OnSurfaceCreated");

        // Set the background frame color
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Use culling to remove back faces.
        GLES20.glEnable(GLES20.GL_CULL_FACE);

        // Enable depth testing
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0,
                0, 4, -8,
                0f, 0f, 0f,
                0f, 1.0f, 0.0f);

        String shaderCode;

        //
        // Create shaders for objects
        //
        shaderCode = LoadShaderCode("object_vertex_shader.txt");
        final int objectVertexShaderHandle = CreateShader(GLES20.GL_VERTEX_SHADER, shaderCode);

        shaderCode = LoadShaderCode("object_fragment_shader.txt");
        final int objectFragmentShaderHandle = CreateShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);

        mObjectProgramHandle = CreateAndLinkProgram(objectVertexShaderHandle, objectFragmentShaderHandle,
                new String[] {"a_Position",  "a_AmbColor", "a_DifColor", "a_Normal"});       // attributes from object vertex shader
        Log.d("MOOSE", "Loaded object shaders");

        //
        // create shaders for point light
        //
        shaderCode = LoadShaderCode("point_vertex_shader.txt");
        int pointVertexShaderID = CreateShader(GLES20.GL_VERTEX_SHADER, shaderCode);

        shaderCode = LoadShaderCode("point_fragment_shader.txt");
        int pointFragmentShaderID = CreateShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);

        mPointProgramHandle = CreateAndLinkProgram(pointVertexShaderID, pointFragmentShaderID,
                new String[] {"a_Position"});       // attributes from point vertex shader

        Log.d("MOOSE", "Loaded point shaders");

        // initialize shapes
        mTriangle = new Triangle();
        mSquare = new Square();
        mObjModel = new ObjModel(mContext, mObjFileName);
        mCube = new Cube();
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glUseProgram(mObjectProgramHandle);

        // Do a complete rotation every 10 seconds.
        long time = SystemClock.uptimeMillis() % 10000L;
        float timeAngleInDegrees = (360.0f / 10000.0f) * ((int) time);

        // Set program shader var handles for drawing.
        mMVPMatrixHandle = GLES20.glGetUniformLocation(mObjectProgramHandle, "u_MVPMatrix");
        mMVMatrixHandle = GLES20.glGetUniformLocation(mObjectProgramHandle, "u_MVMatrix");
        mLightPosHandle = GLES20.glGetUniformLocation(mObjectProgramHandle, "u_LightPos");
        mPositionHandle = GLES20.glGetAttribLocation(mObjectProgramHandle, "a_Position");
        mDifColorHandle = GLES20.glGetAttribLocation(mObjectProgramHandle, "a_DifColor");
        mAmbColorHandle = GLES20.glGetAttribLocation(mObjectProgramHandle, "a_AmbColor");
        mNormalHandle = GLES20.glGetAttribLocation(mObjectProgramHandle, "a_Normal");

        // Calculate position of the light. Rotate and then push into the distance.
        Matrix.setIdentityM(mLightModelMatrix, 0);
        Matrix.rotateM(mLightModelMatrix, 0, timeAngleInDegrees, 0.0f, 1.0f, 0.0f);
        Matrix.translateM(mLightModelMatrix, 0, 0.0f, 0.0f, -2.0f);

        Matrix.multiplyMV(mLightPosInWorldSpace, 0, mLightModelMatrix, 0, mLightPosInModelSpace, 0);
        Matrix.multiplyMV(mLightPosInEyeSpace, 0, mViewMatrix, 0, mLightPosInWorldSpace, 0);

        // Pass in the light position in eye space.
        GLES20.glUniform3f(mLightPosHandle, mLightPosInEyeSpace[0], mLightPosInEyeSpace[1], mLightPosInEyeSpace[2]);

        //mTriangle.draw(this);
        //mSquare.draw(this);
        mObjModel.draw(this);
        //mCube.Draw(this);

        // Draw a point to indicate the light.
        GLES20.glUseProgram(mPointProgramHandle);
        DrawLight();
    }

    /**
     * Draws a point representing the position of the light.
     */
    private void DrawLight()
    {
        final int pointMVPMatrixHandle = GLES20.glGetUniformLocation(mPointProgramHandle, "u_MVPMatrix");
        final int pointPositionHandle = GLES20.glGetAttribLocation(mPointProgramHandle, "a_Position");

        // Pass in the position.
        GLES20.glVertexAttrib3f(pointPositionHandle, mLightPosInModelSpace[0], mLightPosInModelSpace[1], mLightPosInModelSpace[2]);

        // Since we are not using a buffer object, disable vertex arrays for this attribute.
        GLES20.glDisableVertexAttribArray(pointPositionHandle);

        float[] mvpMatrix = new float[16];

        // Pass in the transformation matrix.
        Matrix.multiplyMM(mvpMatrix, 0, mViewMatrix, 0, mLightModelMatrix, 0);
        Matrix.multiplyMM(mvpMatrix, 0, mProjectionMatrix, 0, mvpMatrix, 0);
        GLES20.glUniformMatrix4fv(pointMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the point.
        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, 1);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        Log.d("MOOSE", "Renderer: OnSurfaceChanged");

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
    }

}
