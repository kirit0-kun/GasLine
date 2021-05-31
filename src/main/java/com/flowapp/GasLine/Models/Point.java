package com.flowapp.GasLine.Models;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class Point implements Comparable<Point> {
    private final float mX;
    private final float mY;
    private final float mMagnitude;

    public Point(float mX, float mY) {
        this.mX = mX;
        this.mY = mY;
        this.mMagnitude = (float) Math.sqrt(Math.pow(mX, 2) + Math.pow(mY, 2));
    }

    public Point(double mX, double mY) {
        this((float) mX,(float) mY);
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public Point(@NotNull Point point) {
        this(point.mX, point.mY);
    }

    public static Point of(double x, double y) {
        return new Point(x, y);
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getMagnitude() {
        return mMagnitude;
    }

    public Point negate(){
        float x = -this.mX;
        float y = -this.mY;
        return of(x,y);
    }

    public Point add(@NotNull Point other){
        float x = this.mX + other.mX;
        float y = this.mY + other.mY;
        return of(x,y);
    }

    public Point subtract(@NotNull Point other){
        return add(other.negate());
    }

    public Point multiply(float multiplier){
        float x = this.mX * multiplier;
        float y = this.mY * multiplier;
        return of(x,y);
    }

    public Point divide(float multiplier){
        return multiply(1 / multiplier);
    }

    @Override public int hashCode() {
        final int h1 = Float.floatToIntBits(mX);
        final int h2 = Float.floatToIntBits(mY);
        return h1 ^ ((h2 >>> 16) | (h2 << 16));
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Point)) {
            return false;
        }
        Point other = (Point)obj;
        return mX==other.mX && mY==other.mY;
    }

    @Override
    public String toString() {
        return "Point{" +
                "X=" + mX +
                ", Y=" + mY +
                '}';
    }

    @Override
    public int compareTo(@NotNull Point o) {
        return Float.compare(mMagnitude, o.mMagnitude);
    }

    public static class SortbyX implements Comparator<Point>
    {
        public int compare(Point a, Point b)
        {
            return Float.compare(a.mX, b.mX);
        }
    }

    public static class SortbyY implements Comparator<Point>
    {
        public int compare(Point a, Point b)
        {
            return Float.compare(a.mY, b.mY);
        }
    }

    public static class SortbyYX implements Comparator<Point>
    {
        public int compare(Point a, Point b)
        {
            final int c1 = Float.compare(a.mY, b.mY);
            if (c1 == 0) {
                return Float.compare(a.mX, b.mX);
            }
            return c1;
        }
    }

    public static class SortbyXY implements Comparator<Point>
    {
        public int compare(Point a, Point b)
        {
            final int c1 = Float.compare(a.mX, b.mX);
            if (c1 == 0) {
                return Float.compare(a.mY, b.mY);
            }
            return c1;
        }
    }
}


