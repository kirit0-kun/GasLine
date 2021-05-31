package com.flowapp.GasLine.Models.Curves;

public interface Curve {
    public Float getX(float y);

    public Float getY(float x);

    public boolean inRange(float x);
}
