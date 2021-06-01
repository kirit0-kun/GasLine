package com.flowapp.GasLine.Models;

import java.util.ArrayList;
import java.util.List;

public class GasPipe {
    private final float p1;
    private final float p2;
    private final float flowRateScfDay;
    private final float iDmm;
    private final float startMile;
    private final float lengthMile;

    public GasPipe(float p1, float p2, float flowRateScfDay, float iDmm, float startMile, float lengthMile) {
        this.p1 = p1;
        this.p2 = p2;
        this.flowRateScfDay = flowRateScfDay;
        this.iDmm = iDmm;
        this.startMile = startMile;
        this.lengthMile = lengthMile;
    }

    public float getP1() {
        return p1;
    }

    public float getP2() {
        return p2;
    }

    public float getFlowRateScfDay() {
        return flowRateScfDay;
    }

    public float getIDmm() {
        return iDmm;
    }

    public float getStartMile() {
        return startMile;
    }

    public float getEndMile() {
        return startMile + lengthMile;
    }

    public float getLengthMile() {
        return lengthMile;
    }

    public float calculatePx(float x) {
        final float px = (float) Math.pow(Math.pow(p1,2) - ((Math.pow(p1,2) - Math.pow(p2, 2)) * x), 0.5);
        return px;
    }

    public float calculateX(float px) {
        final float x = (float) ((Math.pow(p1,2) - Math.pow(px, 2))/(Math.pow(p1,2) - Math.pow(p2, 2)));
        return x;
    }

    public float calculatePxFromStart(float l) {
        final float actualL = Math.max(0, l - startMile);
        final float x = actualL / lengthMile;
        return calculatePx(x);
    }

    public List<Point> generateHG() {
        final List<Point> points = new ArrayList<>();
        points.add(Point.of(startMile, p2));
        for (int i = 0; i <= 10; i++) {
            final float x = i / 10.0f;
            points.add(Point.of(startMile + (x * lengthMile), calculatePx(x)));
        }
        return points;
    }
}
