package com.flowapp.GasLine.Models;

public enum FlowType {
    LAMINAR("Laminar"),
    TRANSITIONAL("Transitional"),
    TURBULENT("Turbulent");

    private final String name;

    FlowType(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
