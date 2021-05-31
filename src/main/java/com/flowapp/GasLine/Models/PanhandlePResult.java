package com.flowapp.GasLine.Models;

public class PanhandlePResult {
    private final float p1;
    private final float p2;
    private final float pAvg;
    private final float pr;
    private final float z;
    private final float pc;
    private final float tc;
    private final float tr;

    public PanhandlePResult(float p1, float p2, float pAvg, float pr, float z, float pc, float tc, float tr) {
        this.p1 = p1;
        this.p2 = p2;
        this.pAvg = pAvg;
        this.pr = pr;
        this.z = z;
        this.pc = pc;
        this.tc = tc;
        this.tr = tr;
    }

    public float getP1() {
        return p1;
    }

    public float getP2() {
        return p2;
    }

    public float getPAvg() {
        return pAvg;
    }

    public float getPr() {
        return pr;
    }

    public float getZ() {
        return z;
    }

    public float getPc() {
        return pc;
    }

    public float getTc() {
        return tc;
    }

    public float getTr() {
        return tr;
    }
}
