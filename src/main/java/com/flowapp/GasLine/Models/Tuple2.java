package com.flowapp.GasLine.Models;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class Tuple2<T, X> {
    private final T first;
    private final X second;

    public Tuple2(T first, X second) {
        this.first = first;
        this.second = second;
    }

    public static <T, X> Tuple2<T, X> of(T first, X second) {
        return new Tuple2<T, X>(first, second);
    }

    public T getFirst() {
        return first;
    }

    public X getSecond() {
        return second;
    }

    @Override public int hashCode() {
        final int h1 = first.hashCode();
        final int h2 = second.hashCode();
        return h1 ^ ((h2 >>> 16) | (h2 << 16));
    }

    @Override public boolean equals(Object obj) {
        if (!(obj instanceof Tuple2)) {
            return false;
        }
        Tuple2 other = (Tuple2)obj;
        return first==other.first && second==other.second;
    }

    @Override
    public String toString() {
        return "Tuple2{" +
                "First=" + first +
                ", Second=" + second +
                '}';
    }
}


