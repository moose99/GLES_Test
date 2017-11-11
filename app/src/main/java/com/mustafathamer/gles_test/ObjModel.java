package com.mustafathamer.gles_test;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.Matrix;

import java.io.IOException;


public class ObjModel
{
   private ObjFileLoader objFileLoader;

    /**
     * Store the model matrix. This matrix is used to move models from object space (where each model can be thought
     * of being located at the center of the universe) to world space.
     */
    private float[] mModelMatrix = new float[16];

    //
    // CTOR
    //
    public ObjModel(Context context, String fileName)
    {
        objFileLoader = new ObjFileLoader(context);
        try
        {
            objFileLoader.LoadObjFile(fileName);
        } catch (IOException e)
        {
            System.out.println("Failed loading OBJ file");
            e.printStackTrace();
        }
    }

    public void draw(MyGLRenderer renderer)
    {
        //
        // POSITIONS
        //
        // Prepare the triangle coordinate data
        GLES20.glVertexAttribPointer(renderer.GetPositionHandle(), objFileLoader.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, objFileLoader.GetVerticesBuffer());
        GLES20.glEnableVertexAttribArray(renderer.GetPositionHandle());

        //
        // COLORS
        //
        // Set ambient and diffuse color for drawing the triangle
        GLES20.glVertexAttribPointer(renderer.GetDifColorHandle(), objFileLoader.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, objFileLoader.GetKdsBuffer());
        GLES20.glEnableVertexAttribArray(renderer.GetDifColorHandle());

        GLES20.glVertexAttribPointer(renderer.GetAmbColorHandle(), objFileLoader.COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                0, objFileLoader.GetKasBuffer());
        GLES20.glEnableVertexAttribArray(renderer.GetAmbColorHandle());

        //
        // NORMALS
        //
        // Pass in the normal information
        GLES20.glVertexAttribPointer(renderer.GetNormalHandle(), objFileLoader.COORDS_PER_NORMAL,
                GLES20.GL_FLOAT, false,
                0, objFileLoader.GetNormalsBuffer());

        GLES20.glEnableVertexAttribArray(renderer.GetNormalHandle());

        //
        // MATRIX
        //
        float[] mvpMatrix = new float[16];

        Matrix.setIdentityM(mModelMatrix, 0);

        Matrix.scaleM(mModelMatrix, 0, 2.0f, 2.0f, 2.0f);   // TEMP SCALE

        Matrix.translateM(mModelMatrix, 0, 0.0f, 0.0f, 0.0f);       // no translation for now
        Matrix.rotateM(mModelMatrix, 0, renderer.GetYAngle(), 0, 1.0f, 0);
        Matrix.rotateM(mModelMatrix, 0, renderer.GetXAngle(), 1.0f, 0, 0);

        Matrix.multiplyMM(mvpMatrix, 0, renderer.GetViewMatrix(), 0, mModelMatrix, 0);  // so far just model and view

        // Pass in the modelview matrix.
        GLES20.glUniformMatrix4fv(renderer.GetMVMatrixHandle(), 1, false, mvpMatrix, 0);

        // Combine the rotation matrix with the projection and camera view
        // Note that the mMVPMatrix factor *must be first* in order
        // for the matrix multiplication product to be correct.

        Matrix.multiplyMM(mvpMatrix, 0, renderer.GetProjectionMatrix(), 0, mvpMatrix, 0);

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(renderer.GetMVPMatrixHandle(), 1, false, mvpMatrix, 0);

        // Draw the object using the draw list buffer
//        GLES20.glDrawElements(
//                GLES20.GL_TRIANGLES, objFileLoader.GetNumFaces() * 3,
//                GLES20.GL_UNSIGNED_SHORT, objFileLoader.GetFacesBuffer());
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, objFileLoader.GetNumVerts());

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(renderer.GetPositionHandle());
        GLES20.glDisableVertexAttribArray(renderer.GetNormalHandle());
        GLES20.glDisableVertexAttribArray(renderer.GetDifColorHandle());
        GLES20.glDisableVertexAttribArray(renderer.GetAmbColorHandle());
    }
}
