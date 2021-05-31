package com.flowapp.GasLine.Models.Curves;

import com.flowapp.GasLine.Models.Point;

public class Linear implements Curve {
    private final float mSlope;
    private final float mYIntercept;

    public Linear(Point point1, Point point2) {
        if (point1.getX() == point2.getX()) {
            mSlope = Float.POSITIVE_INFINITY;
            mYIntercept = Float.NaN;
        } else {
            mSlope = (point2.getY() - point1.getY()) / (point2.getX() - point1.getX());
            mYIntercept = point1.getY() - mSlope * point1.getX();
        }
    }

    public Linear(float slope, Point point) {
        this.mSlope = slope;
        if (Float.isFinite(mSlope)) {
            mYIntercept = point.getY() - mSlope * point.getX();
        } else {
            mYIntercept = Float.NaN;
        }
    }

    public Linear(float slope, float yIntercept) {
        this.mSlope = slope;
        this.mYIntercept = yIntercept;
    }

    public float getSlope() {
        return mSlope;
    }

    public float getYIntercept() {
        return mYIntercept;
    }

    @Override
    public Float getX(float y) {
        if (Float.isFinite(mSlope)) {
            return (y - mYIntercept) / mSlope;
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public Float getY(float x) {
        if (Float.isFinite(mSlope)) {
            return mSlope * x + mYIntercept;
        } else {
            return Float.POSITIVE_INFINITY;
        }
    }

    @Override
    public boolean inRange(float x) {
        return true;
    }
}
