package com.flowapp.GasLine.Models.Curves;

public class Quadratic implements Curve {

    private final boolean mPreferRight;
    private final float mA;
    private final float mB;
    private final float mC;

    public Quadratic(float mA, float mB, float mC) {
        this(mA, mB, mC, true);
    }


    public Quadratic(float mA, float mB, float mC, boolean preferRight) {
        this.mA = mA;
        this.mB = mB;
        this.mC = mC;
        this.mPreferRight = preferRight;
    }

    @Override
    public Float getX(float y) {
        if (mPreferRight) {
            return getXPreferRight(y);
        } else {
            return getXPreferLeft(y);
        }
    }

    public float getXPreferLeft(float y) {
        final float left = getLeftX(y);
        if (Float.isFinite(left)) {
            return left;
        } else {
            return getRightX(y);
        }
    }

    public float getXPreferRight(float y) {
        final float right = getRightX(y);
        if (Float.isFinite(right)) {
            return right;
        } else {
            return getLeftX(y);
        }
    }

    @Override
    public Float getY(float x) {
        return (float) (mA * Math.pow(x, 2) + mB * x + mC);
    }

    @Override
    public boolean inRange(float x) {
        return true;
    }

    public float getRightX(float y) {
        return (float) (( -mB + Math.sqrt(Math.pow(mB, 2) - 4 * mA * (mC - y)) ) / (2 * mA));
    }

    public float getLeftX(float y) {
        return (float) (( -mB - Math.sqrt(Math.pow(mB, 2) - 4 * mA * (mC - y)) ) / (2 * mA));
    }

}
