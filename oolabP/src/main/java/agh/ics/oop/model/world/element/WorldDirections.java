package agh.ics.oop.model.world.element;

import agh.ics.oop.util.Vector2d;

public enum WorldDirections {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    SOUTH_WEST,
    WEST,
    NORTH_WEST;

    public static WorldDirections next(WorldDirections direction) {
        return switch (direction) {
            case NORTH -> NORTH_EAST;
            case NORTH_EAST -> EAST;
            case EAST -> SOUTH_EAST;
            case SOUTH_EAST -> SOUTH;
            case SOUTH -> SOUTH_WEST;
            case SOUTH_WEST -> WEST;
            case WEST -> NORTH_WEST;
            case NORTH_WEST -> NORTH;
        };
    }
    public static WorldDirections previous(WorldDirections direction) {
        return switch (direction){
            case NORTH -> NORTH_WEST;
            case NORTH_WEST -> WEST;
            case WEST -> SOUTH_WEST;
            case SOUTH_WEST -> SOUTH;
            case SOUTH -> SOUTH_EAST;
            case SOUTH_EAST -> EAST;
            case EAST -> NORTH_EAST;
            case NORTH_EAST -> NORTH;
        };
    }
    public static WorldDirections opposite(WorldDirections direction) {
        return switch (direction){
            case NORTH -> SOUTH;
            case NORTH_EAST -> SOUTH_WEST;
            case EAST -> WEST;
            case SOUTH_EAST ->  NORTH_WEST;
            case SOUTH -> NORTH;
            case SOUTH_WEST -> NORTH_EAST;
            case WEST -> EAST;
            case NORTH_WEST -> SOUTH_EAST;
        };
    }
    public static Vector2d toUnitVector(WorldDirections direction) {
        return switch (direction){
            case NORTH -> new Vector2d(0,1);
            case NORTH_EAST ->  new Vector2d(1,1);
            case EAST -> new Vector2d(1,0);
            case SOUTH_EAST ->  new Vector2d(1,-1);
            case SOUTH -> new Vector2d(0,-1);
            case SOUTH_WEST ->  new Vector2d(-1,-1);
            case WEST -> new Vector2d(-1,0);
            case NORTH_WEST ->  new Vector2d(-1,1);
        };
    }
}
