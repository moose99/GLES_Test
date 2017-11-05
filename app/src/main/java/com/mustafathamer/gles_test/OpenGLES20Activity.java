package com.mustafathamer.gles_test;

import android.opengl.GLSurfaceView;
import android.os.Bundle;

/**
 * Created by moose-home on 10/25/2017.
 */

public class OpenGLES20Activity extends MainActivity
{
    private GLSurfaceView mGLView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Create a GLSurfaceView instance and set it
        // as the ContentView for this Activity.
        mGLView = new MyGLSurfaceView(this);
        setContentView(mGLView);
    }
    @Override
    protected void onResume()
    {
        // The activity must call the GL surface view's onResume() on activity onResume().
        super.onResume();
        mGLView.onResume();
    }

    @Override
    protected void onPause()
    {
        // The activity must call the GL surface view's onPause() on activity onPause().
        super.onPause();
        mGLView.onPause();
    }
}
