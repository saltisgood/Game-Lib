package com.nickstephen.gamelib.util;

/**
 * Created by Nick Stephen on 17/07/2014.
 */
public class Triple<T, S, U> {
    public T first;
    public S second;
    public U third;

    public Triple(T f, S s, U t) {
        first = f;
        second = s;
        third = t;
    }
}
