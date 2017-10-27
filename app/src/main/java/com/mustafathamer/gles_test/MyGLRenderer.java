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
    public void onSurfaceCreated(GL10 unused, EGLConfig config)
    {
        // Set the background frame color to RED
        GLES20.glClearColor(1.0f, 0.0f, 0.0f, 1.0f);
    }

    public void onDrawFrame(GL10 unused)
    {
        // Redraw background color
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
    }

    public void onSurfaceChanged(GL10 unused, int width, int height)
    {
        GLES20.glViewport(0, 0, width, height);
    }
}
