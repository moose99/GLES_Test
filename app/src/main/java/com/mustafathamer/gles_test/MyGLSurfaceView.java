package com.mustafathamer.gles_test;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by moose-home on 10/25/2017.
 */

class MyGLSurfaceView extends GLSurfaceView
{
    private final MyGLRenderer mRenderer;

    public MyGLSurfaceView(Context context)
    {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data.
        // To allow the object to rotate automatically, this line is commented out.
        // Unless you have objects changing without any user interaction, it’s usually a good idea
        // have this flag turned on
        //setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }
}