package com.traffic.model;

/**
 * Kavşaktaki 4 yönü temsil eden enum.
 */
public enum Direction {
    NORTH(0, "Kuzey"),
    EAST(1, "Dogu"),
    SOUTH(2, "Guney"),
    WEST(3, "Bati");

    private final int index;
    private final String displayName;

    Direction(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() { return index; }
    public String getDisplayName() { return displayName; }

    public static Direction fromIndex(int index) {
        for (Direction d : values()) {
            if (d.index == index) return d;
        }
        return NORTH;
    }

    public Direction getTargetDirection(TurnDirection turn) {
        switch (turn) {
            case STRAIGHT: return getOpposite();
            case LEFT: return getLeft();
            case RIGHT: return getRight();
            default: return getOpposite();
        }
    }

    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return this;
        }
    }

    public Direction getLeft() {
        switch (this) {
            case NORTH: return WEST;
            case SOUTH: return EAST;
            case EAST: return NORTH;
            case WEST: return SOUTH;
            default: return this;
        }
    }

    public Direction getRight() {
        switch (this) {
            case NORTH: return EAST;
            case SOUTH: return WEST;
            case EAST: return SOUTH;
            case WEST: return NORTH;
            default: return this;
        }
    }
}
