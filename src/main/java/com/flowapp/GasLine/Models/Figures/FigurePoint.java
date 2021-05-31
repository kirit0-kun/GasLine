package com.flowapp.GasLine.Models.Figures;

import com.flowapp.GasLine.Models.Point;

import java.util.Comparator;

public final class FigurePoint extends Point {

    private final float mValue;

    public FigurePoint(float mValue, Point mPoint) {
        super(mPoint);
        this.mValue = mValue;
    }

    public FigurePoint(double mValue, Point mPoint) {
        this((float) mValue,mPoint);
    }

    public static FigurePoint of(float mValue, Point mPoint) {
        return new FigurePoint(mValue, mPoint);
    }

    public float getValue() {
        return mValue;
    }

    @Override public int hashCode() {
        final int h1 = Float.floatToIntBits(mValue);
        final int h2 = super.hashCode();
        return h1 ^ ((h2 >>> 16) | (h2 << 16));
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof FigurePoint)) {
            return false;
        }
        FigurePoint other = (FigurePoint)obj;
        return super.equals(obj) && mValue==other.mValue;
    }

    @Override
    public String toString() {
        return "FigurePoint{" +
                "X=" + getX() +
                ", Y=" + getY() +
                ", Value=" + mValue +
                '}';
    }

    static class SortbyValue implements Comparator<FigurePoint>
    {
        public int compare(FigurePoint a, FigurePoint b)
        {
            return Float.compare(a.mValue, b.mValue);
        }
    }
}
