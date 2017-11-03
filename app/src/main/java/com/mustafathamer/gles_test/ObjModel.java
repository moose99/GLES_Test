package com.mustafathamer.gles_test;

import android.content.Context;
import android.opengl.GLES20;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loaded from wavefront OBJ file.
 * In the file, each line that starts with a "v" represents a single vertex. Similarly, each line
 * starting with an "f" represents a single triangular face. While each vertex line contains the
 * X, Y, and Z coordinates of a vertex, each face line contains the indices of three vertices, which
 * together form a face.
 */

public class ObjModel
{
    private List<String> verticesList;
    private List<String> normalsList;
    private List<String> facesList;

    private FloatBuffer verticesBuffer;
    private FloatBuffer normalsBuffer;
    private ShortBuffer facesBuffer;
    private ShortBuffer faceNormalsIndicesBuffer;

    private int mProgram;
    static final int COORDS_PER_VERTEX = 3;

    // Set color with red, green, blue and alpha (opacity) values
    float color[] = {0.63671875f, 0.76953125f, 0.22265625f, 1.0f};

    //
    // for drawing
    //
    private int mPositionHandle;
    private int mColorHandle;

    private final int vertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public ObjModel(Context context)
    {
        try
        {
            LoadObjFile(context);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void LoadObjFile(Context context) throws IOException
    {
        verticesList = new ArrayList<>();
        facesList = new ArrayList<>();
        normalsList = new ArrayList<>();

        // Open the OBJ file with a Scanner
        Scanner scanner = new Scanner(context.getAssets().open("model.obj"));

        System.out.println("scanning model");
        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("v "))
            {
                // Add vertex line to list of vertices
                verticesList.add(line);
            }
            else if (line.startsWith("f "))
            {
                // Add face line to faces list
                facesList.add(line);
            }
            else if (line.startsWith("vn "))
            {
                // Add face line to faces list
                normalsList.add(line);
            }
        }

        // Close the scanner
        scanner.close();

        System.out.println("Found vertices:" + verticesList.size());
        System.out.println("Found faces:" + facesList.size());
        System.out.println("Found normals:" + normalsList.size());

        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(verticesList.size() * 3 * 4 /* sizeof float */);
        buffer1.order(ByteOrder.nativeOrder());
        verticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2 /* sizeof short */);
        buffer2.order(ByteOrder.nativeOrder());
        facesBuffer = buffer2.asShortBuffer();

        // Create buffer for normals
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(normalsList.size() * 3 * 4 /* sizeof float */);
        buffer3.order(ByteOrder.nativeOrder());
        normalsBuffer = buffer3.asFloatBuffer();

        // Create buffer for faceNormalIndices
        ByteBuffer buffer4 = ByteBuffer.allocateDirect(facesList.size() * 3 * 2 /* sizeof short */);
        buffer4.order(ByteOrder.nativeOrder());
        faceNormalsIndicesBuffer = buffer4.asShortBuffer();


        // Populating the Vertices buffer involves looping through the contents of verticesList,
        // extracting the X, Y, and Z coordinates from each item, and calling the put() method to
        // put data inside the buffer.
        for (String vertex : verticesList)
        {
            String coords[] = vertex.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);      // string to float
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            verticesBuffer.put(x);
            verticesBuffer.put(y);
            verticesBuffer.put(z);
        }
        verticesBuffer.position(0);     // reset the position of the buffer

        // Populating the Normals buffer involves looping through the contents of normalsList,
        // extracting the X, Y, and Z coordinates from each item, and calling the put() method to
        // put data inside the buffer.
        for (String normal : normalsList)
        {
            String coords[] = normal.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);      // string to float
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            normalsBuffer.put(x);
            normalsBuffer.put(y);
            normalsBuffer.put(z);
        }
        normalsBuffer.position(0);     // reset the position of the buffer

        //
        // populate faces buffer
        //
        for (String face : facesList)
        {
            String tmp[];
            String vertexIndices[] = face.split(" ");

            tmp = vertexIndices[1].split("//");
            short vertex1 = Short.parseShort(tmp[0]);     // convert each index to a short
            short normal1 = tmp.length>1 ? Short.parseShort(tmp[1]) : -1;

            tmp = vertexIndices[2].split("//");
            short vertex2 = Short.parseShort(tmp[0]);
            short normal2 = tmp.length>1 ? Short.parseShort(tmp[1]) : -1;

            tmp = vertexIndices[3].split("//");
            short vertex3 = Short.parseShort(tmp[0]);
            short normal3 = tmp.length>1 ? Short.parseShort(tmp[1]) : -1;

            facesBuffer.put((short) (vertex1 - 1));                 // indices start from 1, not 0
            facesBuffer.put((short) (vertex2 - 1));
            facesBuffer.put((short) (vertex3 - 1));

            faceNormalsIndicesBuffer.put((short) (normal1 - 1));
            faceNormalsIndicesBuffer.put((short) (normal2 - 1));
            faceNormalsIndicesBuffer.put((short) (normal3 - 1));
        }
        facesBuffer.position(0);
        faceNormalsIndicesBuffer.position(0);

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
        GLES20.glVertexAttribPointer(mPositionHandle, COORDS_PER_VERTEX,
                GLES20.GL_FLOAT, false,
                vertexStride, verticesBuffer);

        // get handle to fragment shader's vColor member
        mColorHandle = GLES20.glGetUniformLocation(mProgram, "vColor");

        // Set color for drawing the triangle
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);

        // get handle to shape's transformation matrix
        int mMVPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix");

        // Pass the projection and view transformation to the shader
        GLES20.glUniformMatrix4fv(mMVPMatrixHandle, 1, false, mvpMatrix, 0);

        // Draw the torus using the draw list buffer
        GLES20.glDrawElements(
                GLES20.GL_TRIANGLES, facesList.size() * 3,
                GLES20.GL_UNSIGNED_SHORT, facesBuffer);

        // Disable vertex array
        GLES20.glDisableVertexAttribArray(mPositionHandle);
    }
}
