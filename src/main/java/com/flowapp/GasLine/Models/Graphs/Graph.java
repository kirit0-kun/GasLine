package com.flowapp.GasLine.Models.Graphs;

import com.flowapp.GasLine.Models.Curves.Curve;
import com.flowapp.GasLine.Models.Curves.PointsCurve;
import com.flowapp.GasLine.Models.Point;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

abstract class Graph {

    abstract List<Map.Entry<Float, Curve>> getCurves();

    public float getValue(float y, float x) {
        final List<Point> points = getCurves().stream()
                .map(integerCurveEntry ->
                        new Point(
                                integerCurveEntry.getValue().getX(y),
                                integerCurveEntry.getKey()
                        )
                )
                .collect(Collectors.toList());
        final Curve pointsCurve = new PointsCurve(points);
        return pointsCurve.getY(x);
    }

    public float getX(float value, float y) {
        final List<Point> points = getCurves().stream()
                .map(integerCurveEntry ->
                        new Point(
                                integerCurveEntry.getKey(),
                                integerCurveEntry.getValue().getX(y)
                        )
                )
                .collect(Collectors.toList());
        final Curve pointsCurve = new PointsCurve(points);
        return pointsCurve.getY(value);
    }

    public float getY(float value, float x) {
        final List<Point> points = getCurves().stream()
                .map(integerCurveEntry ->
                        new Point(
                                integerCurveEntry.getKey(),
                                integerCurveEntry.getValue().getY(x)
                        )
                )
                .collect(Collectors.toList());
        final Curve pointsCurve = new PointsCurve(points);
        return pointsCurve.getY(value);
    }
}
