package com.mustafathamer.gles_test;

import javax.microedition.khronos.egl.EGLConfig;
//import android.opengl.EGLConfig;
import android.opengl.GLES20;
//import android.opengl.GLES10;
import android.opengl.GLSurfaceView;

import javax.microedition.khronos.opengles.GL10;

/**
 * Created by moose-home on 10/25/2017.
 */

public class MyGLRenderer implements GLSurfaceView.Renderer
{
    private Triangle mTriangle;
    private Square   mSquare;

    // basic shaders
    public static final String vertexShaderCode =
            "attribute vec4 vPosition;" +
                    "void main() {" +
                    "  gl_Position = vPosition;" +
                    "}";

    public static final String fragmentShaderCode =
            "precision mediump float;" +
                    "uniform vec4 vColor;" +
                    "void main() {" +
                    "  gl_FragColor = vColor;" +
                    "}";


    //
    // utility func for loading shader code from string buffer
    //
    public static int loadShader(int type, String shaderCode){

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

        //mTriangle.draw();
        mSquare.draw();
    }

    @Override
    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);
    }
}
