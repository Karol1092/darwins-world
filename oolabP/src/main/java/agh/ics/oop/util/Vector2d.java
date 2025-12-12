package agh.ics.oop.util;

import java.util.Objects;

import static java.lang.Math.max;
import static java.lang.Math.min;

public class Vector2d {
    private final int x;
    private final int y;

    public Vector2d(int x, int y){
        this.y = y;
        this.x = x;
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
    public boolean precedes(Vector2d other) {
        return (x <= other.x && y <= other.y);
    }
    public boolean follows(Vector2d other) {
        return   (x >= other.x && y >= other.y);
    }
    public Vector2d add(Vector2d other) {
        return new Vector2d(x + other.x, y + other.y);
    }
    public Vector2d subtract(Vector2d other) {
        return new Vector2d(x - other.x, y - other.y);
    }
    public Vector2d upperRight(Vector2d other) {
        return new Vector2d(max(x,other.x), max(y, other.y));
    }
    public Vector2d lowerLeft(Vector2d other) {
        return new Vector2d(min(x,other.x), min(y, other.y));
    }
    public Vector2d opposite() {
        return new Vector2d(-x, -y);
    }
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Vector2d)) return false;
        Vector2d that = (Vector2d) other;
        return x == that.x && y == that.y;
    }
    @Override
    public int hashCode() {
        return Objects.hash(x,y);
    }
}
