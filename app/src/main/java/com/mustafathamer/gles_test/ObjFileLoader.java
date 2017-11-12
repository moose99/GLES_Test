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
    // holds face info and current material name
    private class FaceInfo
    {
        public String mVertIndices;
        public String mMatName;
        FaceInfo(String v, String matName)
        {
            mVertIndices = v;
            mMatName = matName;
        }
    };

    private Context mContext;

    private ObjMtlLoader mObjMtlLoader;
    private FloatBuffer mVerticesBuffer;    // vert positions
    private FloatBuffer mNormalsBuffer;     // vert  normals
    private FloatBuffer mUVsBuffer;         // vert UVs
    private FloatBuffer mKdsBuffer;         // vert diffuse colors
    private FloatBuffer mKasBuffer;         // vert ambient colors
    private int mNumVerts;

    private final int bytesPerFloat = 4;
    private final int bytesPerShort = 2;

    public final int COORDS_PER_VERTEX = 3;
    public final int COORDS_PER_NORMAL = 3;

    private FloatBuffer CreateFloatBuffer(int size, int numFloats)
    {
        ByteBuffer buffer = ByteBuffer.allocateDirect(size * numFloats * bytesPerFloat);
        buffer.order(ByteOrder.nativeOrder());
        return buffer.asFloatBuffer();
    }

    public ObjMtlLoader GetObjMtlLoader() { return mObjMtlLoader; }
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
    public FloatBuffer GetKdsBuffer()
    {
        return mKdsBuffer;
    }
    public FloatBuffer GetKasBuffer()
    {
        return mKasBuffer;
    }

    public int GetNumVerts()
    {
        return mNumVerts;
    }

    //
    // CTOR
    //
    public ObjFileLoader(Context context)
    {
        mContext = context;
        mObjMtlLoader = new ObjMtlLoader(mContext);
    }

    public void LoadObjFile(String fileName) throws IOException
    {
        List<String> verticesList = new ArrayList<>();
        List<String> normalsList = new ArrayList<>();
        List<FaceInfo> facesList = new ArrayList<>();
        List<String> UVsList = new ArrayList<>();

        // Open the OBJ file with a Scanner
        Scanner scanner = new Scanner(mContext.getAssets().open(fileName + ".obj"));
        System.out.println("scanning OBJ file");

        String curMatName = "None";
        int numMats=0;
        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("mtllib "))
            {
                String tmp[] = line.split(" ");
                // First parse the MTL file
                try
                {
                    mObjMtlLoader.LoadMtlFile(tmp[1]);
                }
                catch (IOException e)
                {
                    Log.i("MOOSE", "Failed loading: " + tmp[1]);
                }
            }
            else
            if (line.startsWith("usemtl "))
            {
                // set current material
                String tmp[] = line.split(" ");
                curMatName = tmp[1];
                Log.d("MOOSE", "Set current material=" + curMatName);
                numMats++;
            }
            else
            if (line.startsWith("v "))
            {
                // Add vertex line to list of vertices
                verticesList.add(line);     // ex: v 0.2332653 -0.1349314 -0.7298675
            } else if (line.startsWith("f "))
            {
                // Add face line to faces list
                facesList.add(new FaceInfo(line, curMatName));    // ex: f 5164//7267 5037//7267 5035//7267 5163//7267
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
        System.out.println("Found mats:" + numMats);

        // loop through all faces and check how many verts we have
        mNumVerts = 0;
        for (FaceInfo faceInfo : facesList)
        {
            String face = faceInfo.mVertIndices;
            String tmp[];
            String vertexIndices[] = face.split(" ");   // create a list of verts, each looks like a//b//c
            mNumVerts += vertexIndices.length - 1;      // ignore the 'f' at the start of each line
        }

        Log.d("MOOSE", "NumVerts:" + mNumVerts);


        // Create buffer for vertices
        mVerticesBuffer = CreateFloatBuffer(mNumVerts, 3);
        List<Float> origVerts = new ArrayList<>();

        // Create buffer for normals
        mNormalsBuffer = CreateFloatBuffer(mNumVerts, 3);
        List<Float> origNormals = new ArrayList<>();

        // Create buffer for UVs
        mUVsBuffer = CreateFloatBuffer(mNumVerts, 2);
        List<Float> origUVs = new ArrayList<>();

        if (mObjMtlLoader.GetMtlMap() != null && !mObjMtlLoader.GetMtlMap().isEmpty())
        {
            // Create buffer for colors
            mKdsBuffer = CreateFloatBuffer(mNumVerts, 3);
            mKasBuffer = CreateFloatBuffer(mNumVerts, 3);
        }
        else
        {
            mKdsBuffer = null;
            mKasBuffer = null;
        }

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
        for (FaceInfo faceInfo: facesList)
        {
            String face = faceInfo.mVertIndices;
            ObjMaterial material = mObjMtlLoader.GetMtlMap()!=null ? mObjMtlLoader.GetMtlMap().get(faceInfo.mMatName) : null;

            String tmp[];
            String vertexIndices[] = face.split(" ");   // each one looks like: a or a//b or a//b//c
            int idx;
            for (int i = 1; i < vertexIndices.length; i++)  // start at index of 1 to skip the 'f' at the start
            {
                tmp = vertexIndices[i].split("//");
                if (tmp.length == 1)    // if no split occurred
                    tmp = vertexIndices[i].split("/");

                // VERTS
                idx = Short.parseShort(tmp[0]) - 1;     // convert each index to a short
                mVerticesBuffer.put(origVerts.get(idx * 3));
                mVerticesBuffer.put(origVerts.get(idx * 3 + 1));
                mVerticesBuffer.put(origVerts.get(idx * 3 + 2));

                if (material != null)
                {
                    // add the colors at each vertex from the face material
                    mKdsBuffer.put(material.GetKd()[0]);
                    mKdsBuffer.put(material.GetKd()[1]);
                    mKdsBuffer.put(material.GetKd()[2]);

                    mKasBuffer.put(material.GetKa()[0]);
                    mKasBuffer.put(material.GetKa()[1]);
                    mKasBuffer.put(material.GetKa()[2]);
                }

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
        if (mKdsBuffer != null)
            mKdsBuffer.position(0);
        if (mKasBuffer != null)
                mKasBuffer.position(0);
    }
}
