package com.mustafathamer.gles_test;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;


public class ObjModel
{
    private int mProgram;
    private ObjFileLoader objFileLoader;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    //
    // for drawing
    //
    private int mPositionHandle;
    private int mColorHandle;

    public ObjModel(Context context)
    {
        objFileLoader = new ObjFileLoader(context);
        try
        {
            objFileLoader.LoadObjFile("bowser");
        } catch (IOException e)
        {
            System.out.println("Failed loading OBJ file");
            e.printStackTrace();
        }

        // create empty OpenGL ES Program
        mProgram = GLES20.glCreateProgram();

        // add the vertex shader to program
        GLES20.glAttachShader(mProgram, MyGLRenderer.VertexShaderID);

        // add the fragment shader to program
        GLES20.glAttachShader(mProgram, MyGLRenderer.FragmentShaderID);

        // creates OpenGL ES program executables
        GLES20.glLinkProgram(mProgram);
    }

    public void draw(float[] mvpMatrix)
    { // pass in the calculated transformation matrix
        // Add program to OpenGL ES environment
        GLES20.glUseProgram(mProgram);

        // get handle to vertex shader's vPosition member
        mPositionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition");
        // Enable a handle to the triangle vertices
        GLES20.glEnableVertexAttribArray(mPositionHandle);
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(mPositionHandle, objFileLoader.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                objFileLoader.VertexStride, objFileLoader.GetVerticesBuffer());

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");
        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");
        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the object using the draw list buffer
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, objFileLoader.GetNumFaces() * 3,
                GLES20.GL_UNSIGNED_SHORT, objFileLoader.GetFacesBuffer());

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
