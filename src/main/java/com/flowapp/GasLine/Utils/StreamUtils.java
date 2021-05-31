package com.flowapp.GasLine.Utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.stream.Stream;

public class StreamUtils {
    public static <T> Stream<T> reverse(Stream<T> stream) {
        Deque<T> deque = new ArrayDeque<>();
        stream.forEach(deque::push);
        return deque.stream();
    }
}
