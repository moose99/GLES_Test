package com.mustafathamer.gles_test;

/**
 * Created by moose-home on 11/9/2017.
 *
 * (wavefront obj material files
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

public class ObjMaterial
{
    private String mName;
    private float Ns;
    private float[] Ka;
    private float[] Kd;
    private float[] Ks;
    private float[] Ke;
    private float Ni;
    private float d;
    private int illum;

    public String GetName()     { return mName; }
    public float GetNs()        { return Ns;    }
    public float[] GetKa()      { return Ka;    }
    public float[] GetKd()      { return Kd;    }
    public float[] GetKs()      { return Ks;    }
    public float[] GetKe()      { return Ke;    }
    public float GetNi()        { return Ni;    }
    public float GetD()         { return d;     }
    public int GetIllum()       { return illum; }

    public void setName(String name)
    {
        mName = name;
    }

    public void setNs(float ns)
    {
        Ns = ns;
    }

    public void setKa(float[] ka)
    {
        Ka = ka;
    }

    public void setKd(float[] kd)
    {
        Kd = kd;
    }

    public void setKs(float[] ks)
    {
        Ks = ks;
    }

    public void setKe(float[] ke)
    {
        Ke = ke;
    }

    public void setNi(float ni)
    {
        Ni = ni;
    }

    public void setD(float d)
    {
        this.d = d;
    }

    public void setIllum(int illum)
    {
        this.illum = illum;
    }

    public ObjMaterial()
    {
        mName = "";
        Ns = 0.0f;
        Ka = new float[3];
        Kd = new float[3];
        Ks = new float[3];
        Ke = new float[3];
        Ni = 0.0f;
        d = 0.0f;
    }
}
