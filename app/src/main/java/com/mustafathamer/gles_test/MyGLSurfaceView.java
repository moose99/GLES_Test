package com.mustafathamer.gles_test;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

/**
 * Created by moose-home on 10/25/2017.
 */

class MyGLSurfaceView extends GLSurfaceView
{
    private final MyGLRenderer mRenderer;

    //
    // for touch events
    //
    private final float TOUCH_SCALE_FACTOR = 180.0f / 320;
    private float mPreviousX;
    private float mPreviousY;

    public MyGLSurfaceView(Context context)
    {
        super(context);

        // Create an OpenGL ES 2.0 context
        setEGLContextClientVersion(2);

        mRenderer = new MyGLRenderer();
        mRenderer.SetContext(context);

        // Set the Renderer for drawing on the GLSurfaceView
        setRenderer(mRenderer);

        // Render the view only when there is a change in the drawing data.
        // To allow the object to rotate automatically, this line is commented out.
        // Unless you have objects changing without any user interaction, it’s usually a good idea
        // have this flag turned on
//        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e)
    {
        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.
        float x = e.getX();
        float y = e.getY();

        switch (e.getAction())
        {
            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                // reverse direction of rotation above the mid-line
                if (y > getHeight() / 2)
                {
                    dx = dx * -1;
                }

                // reverse direction of rotation to left of the mid-line
                if (x < getWidth() / 2)
                {
                    dy = dy * -1;
                }

                mRenderer.SetAngle(
                        mRenderer.GetAngle() +
                                ((dx + dy) * TOUCH_SCALE_FACTOR));
                requestRender();
        }

        mPreviousX = x;
        mPreviousY = y;
        return true;
    }
}