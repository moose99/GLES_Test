package com.mustafathamer.gles_test;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Loads .mtl files (wavefront obj material files
 * See: http://paulbourke.net/dataformats/mtl/
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
    private HashMap<String, ObjMaterial> mMtlMap;

    //
    // assumes line is in the form of something like:
    // Ka 1.000000 1.000000 1.000000
    //
    private float[] Read3Floats(String line)
    {
        String tmp[] = line.split(" ");
        float vals[] = new float[3];
        for (int i = 0; i < 3; i++)
        {
            if (i+1 >= tmp.length)
            {
                // the last 2 vals are optional
                vals[i] = vals[0];
            }
            else
            {
                vals[i] = Float.parseFloat(tmp[i + 1]);
            }
        }
        return vals;
    }

    public HashMap<String, ObjMaterial> GetMtlMap() { return mMtlMap; }

    //
    // CTOR
    //
    public ObjMtlLoader(Context context)
    {
        mContext = context;
    }

    //
    // Main entry point, starts fresh when called on a file
    //
    public void LoadMtlFile(String fileName) throws IOException
    {
        // Open the MTL file with a Scanner
        Scanner scanner = new Scanner(mContext.getAssets().open(fileName + ".mtl"));
        Log.d("MOOSE", "scanning MTL file");

        mMtlMap = new HashMap<String, ObjMaterial>();
        ObjMaterial objMat = null;

        // Loop through all its lines
        while (scanner.hasNextLine())
        {
            String line = scanner.nextLine();
            if (line.startsWith("#"))
                continue;               // skip comments
            if (line.isEmpty() && objMat != null)
            {
                // store mat
                Log.d("MOOSE", "Saving material:" + objMat.GetName());
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
                Log.d("MOOSE", "Creating new material:" + matName);
            }
            else
            if (line.startsWith("illum "))
            {
                String tmp[] = line.split(" ");
                objMat.setIllum(Integer.parseInt(tmp[1]));
            }
            else
            if (line.contains(" spectral ") || line.contains(" xyz "))
            {
                Log.i("MOOSE", "Unsupported MTL format, line: " + line);
            }
            else
            if (line.startsWith("Ns "))
            {
                String tmp[] = line.split(" ");
                objMat.setNs(Float.parseFloat(tmp[1]));
            }
            else
            if (line.startsWith("Ni "))
            {
                String tmp[] = line.split(" ");
                objMat.setNi(Float.parseFloat(tmp[1]));
            }
            else
            if (line.startsWith("d "))
            {
                String tmp[] = line.split(" ");
                objMat.setD(Float.parseFloat(tmp[tmp.length-1]));   // skip optional -halo param
            }
            else
            if (line.startsWith("Kd "))
            {
                objMat.setKd(Read3Floats(line));
            }
            else
            if (line.startsWith("Ka "))
            {
                objMat.setKa(Read3Floats(line));
            }
            else
            if (line.startsWith("Ks "))
            {
                objMat.setKs(Read3Floats(line));
            }
            else
            if (line.startsWith("Ke "))
            {
                objMat.setKe(Read3Floats(line));
            }
        }

        // store last mtl
        if (objMat != null)
        {
            Log.d("MOOSE", "Saving material:" + objMat.GetName());
            mMtlMap.put(objMat.GetName(), objMat);
            objMat = null;
        }
    }

}
