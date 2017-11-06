package com.mustafathamer.gles_test;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads a wavefront OBJ file.
 * Unlike OpenGL, the OpenGL ES API doesn’t allow rendering with quads (GL_QUADS), so you’ll have
 * to export your model as a set of triangles.
 * In the file, each line that starts with a "v" represents a single vertex. Similarly, each line
 * starting with an "f" represents a single triangular face. While each vertex line contains the
 * X, Y, and Z coordinates of a vertex, each face line contains the indices of three vertices, which
 * together form a face.
 */

public class ObjFileLoader
{
    private Context mContext;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mNormalsBuffer;
    private FloatBuffer mUVsBuffer;
    private int mNumVerts;

    private final int bytesPerFloat = 4;
    private final int bytesPerShort = 2;

    public final int COORDS_PER_VERTEX = 3;
    public final int COORDS_PER_NORMAL = 3;
    public final int VertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public FloatBuffer GetVerticesBuffer()
    {
        return mVerticesBuffer;
    }

    public FloatBuffer GetNormalsBuffer()
    {
        return mNormalsBuffer;
    }

    public FloatBuffer GetUVsBuffer()
    {
        return mUVsBuffer;
    }

    public int GetNumVerts()
    {
        return mNumVerts;
    }

    public ObjFileLoader(Context context)
    {
        mContext = context;
    }

    public void LoadObjFile(String fileName) throws IOException
    {
        List<String> verticesList = new ArrayList<>();
        List<String> normalsList = new ArrayList<>();
        List<String> facesList = new ArrayList<>();
        List<String> UVsList = new ArrayList<>();

        // Open the OBJ file with a Scanner
        Scanner scanner = new Scanner(mContext.getAssets().open(fileName + ".obj"));
        System.out.println("scanning OBJ file");

        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("v "))
            {
                // Add vertex line to list of vertices
                verticesList.add(line);     // ex: v 0.2332653 -0.1349314 -0.7298675
            } else if (line.startsWith("f "))
            {
                // Add face line to faces list
                facesList.add(line);    // ex: f 5164//7267 5037//7267 5035//7267 5163//7267
            } else if (line.startsWith("vn "))
            {
                // Add normals line to normals list
                normalsList.add(line);      // ex: vn 0.0543038 0.9978414 -0.03692874
            } else if (line.startsWith("vt "))
            {
                // Add uvs line to uvs list
                UVsList.add(line);
            }
        }

        // Close the scanner
        scanner.close();

        System.out.println("Found vertices:" + verticesList.size());
        System.out.println("Found faces:" + facesList.size());
        System.out.println("Found normals:" + normalsList.size());
        System.out.println("Found uvs:" + UVsList.size());

        // loop through all faces and check how many verts we have
        mNumVerts = 0;
        for (String face : facesList)
        {
            String tmp[];
            String vertexIndices[] = face.split(" ");   // create a list of verts, each looks like a//b//c
            mNumVerts += vertexIndices.length;
        }

        Log.d("MOOSE", "NumVerts:" + mNumVerts);


        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(mNumVerts * 3 * bytesPerFloat);
        buffer1.order(ByteOrder.nativeOrder());
        mVerticesBuffer = buffer1.asFloatBuffer();
        List<Float> origVerts = new ArrayList<>();

        // Create buffer for normals
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(mNumVerts * 3 * bytesPerFloat);
        buffer2.order(ByteOrder.nativeOrder());
        mNormalsBuffer = buffer2.asFloatBuffer();
        List<Float> origNormals = new ArrayList<>();

        // Create buffer for UVs
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(mNumVerts * 2 * bytesPerFloat);
        buffer3.order(ByteOrder.nativeOrder());
        mUVsBuffer = buffer3.asFloatBuffer();
        List<Float> origUVs = new ArrayList<>();

        // parse and save original verts
        for (String vertex : verticesList)
        {
            String coords[] = vertex.split(" "); // Split by space
            for (int i = 1; i < 4; i++)
            {
                float f = Float.parseFloat(coords[i]);      // string to float
                origVerts.add(f);
            }
        }

        // parse and save original normals
        for (String normal : normalsList)
        {
            String coords[] = normal.split(" "); // Split by space
            for (int i = 1; i < 4; i++)
            {
                float f = Float.parseFloat(coords[i]);      // string to float
                origNormals.add(f);
            }
        }

        // parse and save original UVs
        for (String uv : UVsList)
        {
            String coords[] = uv.split(" "); // Split by space
            for (int i = 1; i < 3; i++)
            {
                float f = Float.parseFloat(coords[i]);      // string to float
                origUVs.add(f);
            }
        }

        Log.d("MOOSE", "OrigVerts:" + origVerts.size());
        Log.d("MOOSE", "OrigNormals:" + origNormals.size());
        Log.d("MOOSE", "OrigUVs:" + origUVs.size());

        //
        // populate faces buffer
        //
        for (String face : facesList)
        {
            String tmp[];
            String vertexIndices[] = face.split(" ");   // each one looks like: a or a//b or a//b//c
            int idx;
            for (int i = 1; i < vertexIndices.length; i++)
            {
                tmp = vertexIndices[i].split("//");

                // VERTS
                idx = Short.parseShort(tmp[0]) - 1;     // convert each index to a short
                mVerticesBuffer.put(origVerts.get(idx * 3));
                mVerticesBuffer.put(origVerts.get(idx * 3 + 1));
                mVerticesBuffer.put(origVerts.get(idx * 3 + 2));

                if (tmp.length == 2)
                {
                    // NORMALS
                    idx = Short.parseShort(tmp[1]) - 1;     // convert each index to a short
                    mNormalsBuffer.put(origNormals.get(idx * 3));
                    mNormalsBuffer.put(origNormals.get(idx * 3 + 1));
                    mNormalsBuffer.put(origNormals.get(idx * 3 + 2));
                }

                if (tmp.length == 3)
                {
                    // UVs
                    idx = Short.parseShort(tmp[1]) - 1;     // convert each index to a short
                    mUVsBuffer.put(origUVs.get(idx * 2));
                    mUVsBuffer.put(origUVs.get(idx * 2 + 1));

                    // NORMALS
                    idx = Short.parseShort(tmp[2]) - 1;     // convert each index to a short
                    mNormalsBuffer.put(origNormals.get(idx * 3));
                    mNormalsBuffer.put(origNormals.get(idx * 3 + 1));
                    mNormalsBuffer.put(origNormals.get(idx * 3 + 2));
                }
            }
        }

        mVerticesBuffer.position(0);
        mNormalsBuffer.position(0);
        mUVsBuffer.position(0);
    }
}
