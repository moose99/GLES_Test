package com.mustafathamer.gles_test;

/**
 * Created by moose-home on 11/9/2017.
 * <p>
 * (wavefront obj material files
 * http://paulbourke.net/dataformats/mtl/
 * Ex:
 * <p>
 * newmtl mat10.001
 * Ns 96.078431
 * Ka 0.433333 0.433333 0.433333
 * Kd 0.300000 0.690000 0.310000
 * Ks 0.500000 0.500000 0.500000
 * Ke 0.000000 0.000000 0.000000
 * Ni 1.000000
 * d 1.000000
 * illum 2
 */

public class ObjMaterial
{
    private String mName;
    private int sharpness;  //  sharpness of the reflections from the local reflection map.
    private float Ns;       // specular exponent (0-1000)
    private float[] Ka;     // ambient reflectivity
    private float[] Kd;     // diffuse reflectivity
    private float[] Ks;     // specular reflectivity
    private float[] Ke;
    private float[] Tf;     // transmission filter
    private float Ni;       // index of refraction, 0.001 to 10
    private float d;        // dissolve (0 is fully transparent, 1.0 is opaque)

    /*
    Illumination    Properties that are turned on in the
     model           Property Editor

     0		Color on and Ambient off
     1		Color on and Ambient on
     2		Highlight on
     3		Reflection on and Ray trace on
     4		Transparency: Glass on
            Reflection: Ray trace on
     5		Reflection: Fresnel on and Ray trace on
     6		Transparency: Refraction on
            Reflection: Fresnel off and Ray trace on
     7		Transparency: Refraction on
            Reflection: Fresnel on and Ray trace on
     8		Reflection on and Ray trace off
     9		Transparency: Glass on
            Reflection: Ray trace off
     10		Casts shadows onto invisible surfaces
     */
    private int illum;

    public String GetName()
    {
        return mName;
    }

    public int GetSharpness()
    {
        return sharpness;
    }

    public float GetNs()
    {
        return Ns;
    }

    public float[] GetKa()
    {
        return Ka;
    }

    public float[] GetKd()
    {
        return Kd;
    }

    public float[] GetKs()
    {
        return Ks;
    }

    public float[] GetKe()
    {
        return Ke;
    }

    public float[] GetTf()
    {
        return Tf;
    }

    public float GetNi()
    {
        return Ni;
    }

    public float GetD()
    {
        return d;
    }

    public int GetIllum()
    {
        return illum;
    }

    public void setName(String name)
    {
        mName = name;
    }

    public void setSharpness(int s)
    {
        sharpness = s;
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

    public void setTf(float[] tf)
    {
        Tf = tf;
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
        sharpness = 0;
        Ns = 0.0f;
        Ka = new float[3];
        Kd = new float[3];
        Ks = new float[3];
        Ke = new float[3];
        Tf = new float[3];
        Ni = 0.0f;
        d = 0.0f;
    }
}
