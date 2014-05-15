package com.nickstephen.gamelib.util;

/**
 * Created by Ben on 22/04/2014.
 */
public enum Direction {
    UP, RIGHT, LEFT, DOWN;

    private Direction clockwise;
    private Direction antiClockwise;
    private Direction opposite;

    static {
        UP.clockwise = RIGHT;
        RIGHT.clockwise = DOWN;
        DOWN.clockwise = LEFT;
        LEFT.clockwise = UP;

        UP.antiClockwise = LEFT;
        LEFT.antiClockwise = DOWN;
        DOWN.antiClockwise = RIGHT;
        RIGHT.antiClockwise = UP;

        UP.opposite = DOWN;
        DOWN.opposite = UP;
        LEFT.opposite = RIGHT;
        RIGHT.opposite = LEFT;
    }

    public Direction rotateClockwise(){
        // TODO: Make sure this works in the intended fashion.
        return clockwise;
    }

    public Direction rotateAntiClockwise(){
        // TODO: Make sure this works in the intended fashion.
        return antiClockwise;
    }

    public Direction flip(){
        // TODO: Make sure this works in the intended fashion.
        return opposite;
    }
}
