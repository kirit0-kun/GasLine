package com.flowapp.GasLine.Models.Curves;

import com.flowapp.GasLine.Models.Point;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompositeCurve implements Curve {

    private final Stop[] curves;

    public CompositeCurve(Stop[] curves) {
        this.curves = curves;
    }

    public CompositeCurve(List<Stop> curves) {
        this(curves.toArray(new Stop[0]));
    }

    @Override
    public Float getY(float x) {
        Float value = null;
        for (var point: curves) {
            if (point.start == null || point.start.getX() <= x) {
                if (point.end == null || point.end.getX() >= x) {
                    value = point.curve.getY(x);
                    break;
                }
            }
        }
        return value;
    }

    @Override
    public Float getX(float y) {
        Float value = null;
        for (var point: curves) {
            if (point.start == null || point.start.getY() <= y) {
                if (point.end == null || point.end.getY() >= y) {
                    value = point.curve.getX(y);
                    break;
                }
            }
        }
        return value;
    }

    @Override
    public boolean inRange(float x) {
        return false;
    }

    public static class Stop {
        private final Point start;
        private final Point end;
        private final Curve curve;

        public Stop(Point start, Point end, @NotNull Curve curve) {
            this.start = start;
            this.end = end;
            this.curve = curve;
        }

        @NotNull
        public static Stop of(Point start, Point end, @NotNull Curve curve) {
            return new Stop(start, end, curve);
        }
    }
}
