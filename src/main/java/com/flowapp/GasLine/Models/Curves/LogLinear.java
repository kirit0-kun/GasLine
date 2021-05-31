package com.flowapp.GasLine.Models.Curves;

import com.flowapp.GasLine.Models.Point;

public class LogLinear implements Curve {
    private final float mSlope;
    private final int n;
    private final Point mFixed;

    public LogLinear(Point point1, Point point2) {
        this(10, point1, point2);
    }

    public LogLinear(int n, Point point1, Point point2) {
        this.mFixed = point1;
        this.n = n;
        if (point1.getX() == point2.getX()) {
            mSlope = Float.POSITIVE_INFINITY;
        } else {
            mSlope = (float) (loga(point2.getY() / point1.getY(), n) / (point2.getX() - point1.getX()));
        }
    }

    public float getSlope() {
        return mSlope;
    }

    @Override
    public Float getX(float y) {
        if (Float.isFinite(mSlope)) {
            return (float) (mFixed.getX() + (1 / mSlope) * ( Math.log10(y / mFixed.getY()) / Math.log10(n) ));
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public Float getY(float x) {
        if (Float.isFinite(mSlope)) {
            return (float) (mFixed.getY() * Math.pow(n, mSlope * (x - mFixed.getX())));
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public boolean inRange(float x) {
        return true;
    }

    static double loga(double b, double a) {
        return Math.log10(b) / Math.log10(a);
    }
}

