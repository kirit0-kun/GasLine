package com.flowapp.GasLine.Models.Figures;

import com.flowapp.GasLine.Models.Curves.PointsCurve;
import com.flowapp.GasLine.Models.Point;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class FigureLine extends PointsCurve {

    public FigureLine(FigurePoint[] mPoints) {
        super(mPoints);
    }

    public FigureLine(List<FigurePoint> points) {
        this(points.toArray(new FigurePoint[0]));
    }

    public FigurePoint[] getPoints() {
        return (FigurePoint[]) super.getPoints();
    }

    public Point getPoint(float value) {
        Arrays.sort(getPoints(), new FigurePoint.SortbyValue());
        FigurePoint lastFoundPoint = getPoints()[0];
        Point foundPoint = lastFoundPoint;
        for (FigurePoint curveEntry : getPoints()) {
            float foundValue = curveEntry.getValue();
            foundPoint = curveEntry;
            if (foundValue < value) {
                lastFoundPoint = curveEntry;
            } else if (foundValue == value) {
                break;
            } else {
                foundPoint = lastFoundPoint.add(curveEntry.subtract(lastFoundPoint).multiply((value - lastFoundPoint.getValue()) / (foundValue - lastFoundPoint.getValue())));
                break;
            }
        }
        return foundPoint;
    }


    public float getValue(@NotNull Point point) {
        Arrays.sort(getPoints(), new FigurePoint.SortbyXY());
        FigurePoint lastFoundPoint = getPoints()[0];
        float foundValue = lastFoundPoint.getValue();
        for (FigurePoint curveEntry : getPoints()) {
            foundValue = curveEntry.getValue();
            if (curveEntry.compareTo(point) < 0) {
                lastFoundPoint = curveEntry;
            } else if (curveEntry.compareTo(point) == 0) {
                break;
            } else {
                foundValue = lastFoundPoint.getValue() + point.subtract(lastFoundPoint).getMagnitude() / curveEntry.subtract(lastFoundPoint).getMagnitude() * (foundValue - lastFoundPoint.getValue());
                break;
            }
        }
        return foundValue;
    }
}
