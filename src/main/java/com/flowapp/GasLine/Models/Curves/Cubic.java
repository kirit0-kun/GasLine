package com.flowapp.GasLine.Models.Curves;

public class Cubic implements Curve {

    private final Float mXMinLimit;
    private final Float mXMaxLimit;

    private final float mA;
    private final float mB;
    private final float mC;
    private final float mD;

    public Cubic(float mA, float mB, float mC, float mD) {
        this(mA, mB, mC, mD, null, null);
    }

    public Cubic(float mA, float mB, float mC, float mD, Float mXMinLimit, Float mXMaxLimit) {
        this.mA = mA;
        this.mB = mB;
        this.mC = mC;
        this.mD = mD;
        this.mXMaxLimit = mXMaxLimit;
        this.mXMinLimit = mXMinLimit;
    }

    public boolean inRange(float x) {
        if (mXMinLimit != null && x < mXMinLimit) {
            return false;
        } else return mXMaxLimit == null || !(x > mXMaxLimit);
    }

    @Override
    public Float getX(float y) {
        return 0f;
    }

    @Override
    public Float getY(float x) {
        if (mXMinLimit != null && x < mXMinLimit) {
            x = mXMinLimit;
        }
        if (mXMaxLimit != null && x > mXMaxLimit) {
            x = mXMaxLimit;
        }
        return (float) (mA * Math.pow(x, 3) + mB * Math.pow(x, 2) + mC * x + mD);
    }
}
