package com.mustafathamer.gles_test;

import android.content.Context;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Loads a wavefront OBJ file.
 *  Unlike OpenGL, the OpenGL ES API doesn’t allow rendering with quads (GL_QUADS), so you’ll have
 *  to export your model as a set of triangles.
 * In the file, each line that starts with a "v" represents a single vertex. Similarly, each line
 * starting with an "f" represents a single triangular face. While each vertex line contains the
 * X, Y, and Z coordinates of a vertex, each face line contains the indices of three vertices, which
 * together form a face.
 */

public class ObjFileLoader
{
    private Context mContext;
    private List<String> mVerticesList;
    private List<String> mNormalsList;
    private List<String> mFacesList;
    private List<String> mUVsList;

    private FloatBuffer mVerticesBuffer;
    private FloatBuffer mNormalsBuffer;
    private FloatBuffer mUVsBuffer;
    private ShortBuffer mFacesBuffer;
    private ShortBuffer mFaceNormalsIndicesBuffer;

    private final int bytesPerFloat = 4;
    private final int bytesPerShort = 2;

    public final int COORDS_PER_VERTEX = 3;
    public final int COORDS_PER_NORMAL = 3;
    public final int VertexStride = COORDS_PER_VERTEX * 4; // 4 bytes per vertex

    public FloatBuffer GetVerticesBuffer() { return mVerticesBuffer; }
    public FloatBuffer GetNormalsBuffer() { return mNormalsBuffer; }
    public FloatBuffer GetUVsBuffer() { return mUVsBuffer; }
    public ShortBuffer GetFacesBuffer()   { return mFacesBuffer;   }
    public ShortBuffer GetFaceNormalsIndicesBuffer()  { return mFaceNormalsIndicesBuffer;  }

    public int GetNumFaces() { return mFacesList.size(); }
    public int GetNumNormals() { return mNormalsList.size(); }
    public int GetNumVertices() { return mVerticesList.size(); }
    public int GetNumUVs() { return mUVsList.size(); }

    public ObjFileLoader(Context context)
    {
        mContext = context;
    }

    public void LoadObjFile(String fileName) throws IOException
    {
        mVerticesList = new ArrayList<>();
        mFacesList = new ArrayList<>();
        mNormalsList = new ArrayList<>();
        mUVsList = new ArrayList<>();

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
                mVerticesList.add(line);
            }
            else if (line.startsWith("f "))
            {
                // Add face line to faces list
                mFacesList.add(line);
            }
            else if (line.startsWith("vn "))
            {
                // Add normals line to normals list
                mNormalsList.add(line);
            }
            else if (line.startsWith("vt "))
            {
                // Add uvs line to uvs list
                mUVsList.add(line);
            }
        }

        // Close the scanner
        scanner.close();

        System.out.println("Found vertices:" + mVerticesList.size());
        System.out.println("Found faces:" + mFacesList.size());
        System.out.println("Found normals:" + mNormalsList.size());
        System.out.println("Found uvs:" + mUVsList.size());

        // Create buffer for vertices
        ByteBuffer buffer1 = ByteBuffer.allocateDirect(mVerticesList.size() * 3 * bytesPerFloat);
        buffer1.order(ByteOrder.nativeOrder());
        mVerticesBuffer = buffer1.asFloatBuffer();

        // Create buffer for faces
        ByteBuffer buffer2 = ByteBuffer.allocateDirect(mFacesList.size() * 3 * bytesPerShort);
        buffer2.order(ByteOrder.nativeOrder());
        mFacesBuffer = buffer2.asShortBuffer();

        // Create buffer for normals
        ByteBuffer buffer3 = ByteBuffer.allocateDirect(mNormalsList.size() * 3 * bytesPerFloat);
        buffer3.order(ByteOrder.nativeOrder());
        mNormalsBuffer = buffer3.asFloatBuffer();

        // Create buffer for faceNormalIndices
        ByteBuffer buffer4 = ByteBuffer.allocateDirect(mFacesList.size() * 3 * bytesPerShort);
        buffer4.order(ByteOrder.nativeOrder());
        mFaceNormalsIndicesBuffer = buffer4.asShortBuffer();

        // Populating the Vertices buffer involves looping through the contents of mVerticesList,
        // extracting the X, Y, and Z coordinates from each item, and calling the put() method to
        // put data inside the buffer.
        for (String vertex : mVerticesList)
        {
            String coords[] = vertex.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);      // string to float
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            mVerticesBuffer.put(x);
            mVerticesBuffer.put(y);
            mVerticesBuffer.put(z);
        }
        mVerticesBuffer.position(0);     // reset the position of the buffer

        // Populating the Normals buffer involves looping through the contents of mNormalsList,
        // extracting the X, Y, and Z coordinates from each item, and calling the put() method to
        // put data inside the buffer.
        for (String normal : mNormalsList)
        {
            String coords[] = normal.split(" "); // Split by space
            float x = Float.parseFloat(coords[1]);      // string to float
            float y = Float.parseFloat(coords[2]);
            float z = Float.parseFloat(coords[3]);
            mNormalsBuffer.put(x);
            mNormalsBuffer.put(y);
            mNormalsBuffer.put(z);
        }
        mNormalsBuffer.position(0);     // reset the position of the buffer

        //
        // populate faces buffer
        //
        for (String face : mFacesList)
        {
            String tmp[];
            String vertexIndices[] = face.split(" ");

            tmp = vertexIndices[1].split("//");
            short vertex1 = Short.parseShort(tmp[0]);     // convert each index to a short
            short normal1 = tmp.length > 1 ? Short.parseShort(tmp[1]) : -1;

            tmp = vertexIndices[2].split("//");
            short vertex2 = Short.parseShort(tmp[0]);
            short normal2 = tmp.length > 1 ? Short.parseShort(tmp[1]) : -1;

            tmp = vertexIndices[3].split("//");
            short vertex3 = Short.parseShort(tmp[0]);
            short normal3 = tmp.length > 1 ? Short.parseShort(tmp[1]) : -1;

            mFacesBuffer.put((short) (vertex1 - 1));                 // indices start from 1, not 0
            mFacesBuffer.put((short) (vertex2 - 1));
            mFacesBuffer.put((short) (vertex3 - 1));

            mFaceNormalsIndicesBuffer.put((short) (normal1 - 1));
            mFaceNormalsIndicesBuffer.put((short) (normal2 - 1));
            mFaceNormalsIndicesBuffer.put((short) (normal3 - 1));
        }
        mFacesBuffer.position(0);
        mFaceNormalsIndicesBuffer.position(0);
    }
}
