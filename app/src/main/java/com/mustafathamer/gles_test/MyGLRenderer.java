package com.mustafathamer.gles_test;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.io.IOException;
import java.util.Scanner;


/**
 * Created by moose-home on 10/25/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer
{
    private Context mContext;
    private Triangle mTriangle;
    private Square mSquare;
    private ObjModel mObjModel;
    public static int VertexShaderID = -1;
    public static int FragmentShaderID = -1;

    //
    // for projection
    //
    private final float[] mMVPMatrix = new float[16];
    private final float[] mProjectionMatrix = new float[16];
    private final float[] mViewMatrix = new float[16];

    //
    // Since the renderer code is running on a separate thread from the main user interface thread
    // of your application, you must declare this public variable as volatile.
    //Instead of synchronized variable in Java, you can have java volatile variable, which will instruct
    // JVM threads to read value of volatile variable from main memory and don't cache it locally.
    //
    private volatile float mAngle;

    public MyGLRenderer()
    {

    }

    public void SetContext(Context context) { mContext = context; }

    public float GetAngle() { return mAngle; }
    public void SetAngle(float angle) { mAngle = angle; }

    private String LoadShaderCode(String fileName)
    {
        Scanner scanner = null;
        try
        {
            scanner = new Scanner(mContext.getAssets().open(fileName));
        } catch (IOException e)
        {
            System.out.println("Exception opening assets file:" + fileName);
            e.printStackTrace();
        }

        String shaderCode="";
        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("//"))      // skip comments
                continue;
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

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        System.out.println("Renderer: OnSurfaceCreated");
        // Set the background frame color to RED
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Create Shaders
        String shaderCode;
        shaderCode = LoadShaderCode("vertex_shader.txt");
        VertexShaderID = CreateShader(GLES20.GL_VERTEX_SHADER, shaderCode);

        shaderCode = LoadShaderCode("fragment_shader.txt");
        FragmentShaderID = CreateShader(GLES20.GL_FRAGMENT_SHADER, shaderCode);

        System.out.println("VertexShaderID=" + VertexShaderID + ", FragmentShaderID=" + FragmentShaderID);

        // initialize shapes
        mTriangle = new Triangle();
        mSquare = new Square();
        mObjModel = new ObjModel(mContext);
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 1, -8, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);

        float[] scratch = new float[16];
        float[] mRotationMatrix = new float[16];

        // Create a rotation transformation for the triangle
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 1.0f, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        //mTriangle.draw(scratch);
        //mSquare.draw(scratch);
        mObjModel.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        System.out.println("Renderer: OnSurfaceChanged");

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 20);
    }
}
