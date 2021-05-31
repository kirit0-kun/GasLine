package com.flowapp.GasLine.Models.Curves;

public class Relation implements Curve {
    private final float mXPower;
    private final float mXCoefficient;

    public Relation(float mXPower, float mXCoefficient) {
        this.mXPower = mXPower;
        this.mXCoefficient = mXCoefficient;
    }

    @Override
    public Float getX(float y) {
        return (float) Math.pow(y / mXCoefficient, 1/mXPower);
    }

    @Override
    public Float getY(float x) {
        return (float) (mXCoefficient * Math.pow(x, mXPower));
    }

    @Override
    public boolean inRange(float x) {
        return true;
    }
}
