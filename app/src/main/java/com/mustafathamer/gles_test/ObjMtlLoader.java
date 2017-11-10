package com.mustafathamer.gles_test;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

/**
 * Loads .mtl files (wavefront obj material files
 * Ex:
 *
 * newmtl mat10.001
 * Ns 96.078431
 * Ka 0.433333 0.433333 0.433333
 * Kd 0.300000 0.690000 0.310000
 * Ks 0.500000 0.500000 0.500000
 * Ke 0.000000 0.000000 0.000000
 * Ni 1.000000
 * d 1.000000
 * illum 2
 *
 */

public class ObjMtlLoader
{
    private Context mContext;
    Map<String, ObjMaterial> mMtlMap;

    public ObjMtlLoader(Context context)
    {
        mContext = context;
    }

    public void LoadMtlFile(String fileName) throws IOException
    {
        // Open the MTL file with a Scanner
        Scanner scanner = new Scanner(mContext.getAssets().open(fileName + ".mtl"));
        Log.d("MOOSE", "scanning MTL file");

        ObjMaterial objMat = null;

        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.isEmpty())
            {
                // store mat
                mMtlMap.put(objMat.GetName(), objMat);
                objMat = null;
            }
            else
            if (line.startsWith("newmtl "))
            {
                assert(objMat==null);   // make sure last objMat was stored and nulled
                String tmp[] = line.split(" ");
                String matName = tmp[1];
                objMat = new ObjMaterial();
                objMat.setName(matName);
            }
        }

        // store last mtl
        if (objMat != null)
        {
            mMtlMap.put(objMat.GetName(), objMat);
            objMat = null;
        }
    }

}
