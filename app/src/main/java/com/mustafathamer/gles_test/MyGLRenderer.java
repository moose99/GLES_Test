package com.mustafathamer.gles_test;

import javax.microedition.khronos.egl.EGLConfig;
//import android.opengl.EGLConfig;
import android.opengl.GLES20;
//import android.opengl.GLES10;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by moose-home on 10/25/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer
{
    private Triangle mTriangle;
    private Square mSquare;

    // basic shaders
    public static final String vertexShaderCode =
            // This matrix member variable provides a hook to manipulate
            // the coordinates of the objects that use this vertex shader
            "uniform mat4 uMVPMatrix;" +
                    "attribute vec4 vPosition;" +
                    "void main() {" +
                    // the matrix must be included as a modifier of gl_Position
                    // Note that the uMVPMatrix factor *must be first* in order
                    // for the matrix multiplication product to be correct.
                    "  gl_Position = uMVPMatrix  * vPosition;" +
                    "}";

    public static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";

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

    //
    // utility func for loading shader code from string buffer
    //
    public static int loadShader(int type, String shaderCode)
    {
        // create a vertex shader type (GLES20.GL_VERTEX_SHADER)
        // or a fragment shader type (GLES20.GL_FRAGMENT_SHADER)
        int shader = GLES20.glCreateShader(type);

        // add the source code to the shader and compile it
        GLES20.glShaderSource(shader, shaderCode);
        GLES20.glCompileShader(shader);

        return shader;
    }

    public float getAngle()
    {
        return mAngle;
    }

    public void setAngle(float angle)
    {
        mAngle = angle;
    }

    @Override
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        System.out.println("Renderer: OnSurfaceCreated");
        // Set the background frame color to RED
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);

        // Create the shapes I want to draw

        // initialize a triangle
        mTriangle = new Triangle();
        // initialize a square
        mSquare = new Square();
    }

    @Override
    public void onDrawFrame(GL10 unused)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        // Set the camera position (View matrix)
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);


        float[] scratch = new float[16];
        float[] mRotationMatrix = new float[16];

        // Create a rotation transformation for the triangle
 //       long time = SystemClock.uptimeMillis() % 4000L;
 //       float angle = 0.090f * ((int) time);
        Matrix.setRotateM(mRotationMatrix, 0, mAngle, 0, 0, -1.0f);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.
        Matrix.multiplyMM(scratch, 0, mMVPMatrix, 0, mRotationMatrix, 0);

        mTriangle.draw(scratch);
        //mSquare.draw(scratch);
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        System.out.println("Renderer: OnSurfaceChanged");

        GLES20.glViewport(0, 0, width, height);

        float ratio = (float) width / height;

        // this projection matrix is applied to object coordinates
        // in the onDrawFrame() method
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
    }
}
