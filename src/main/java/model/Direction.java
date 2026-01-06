package model;

/**
 * Enum representing the four directions of the intersection
 */
public enum Direction {
    NORTH("North", 0),
    EAST("East", 90),
    SOUTH("South", 180),
    WEST("West", 270);

    private final String displayName;
    private final int angle;

    Direction(String displayName, int angle) {
        this.displayName = displayName;
        this.angle = angle;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getAngle() {
        return angle;
    }

    /**
     * Get the opposite direction
     */
    public Direction getOpposite() {
        return switch (this) {
            case NORTH -> SOUTH;
            case SOUTH -> NORTH;
            case EAST -> WEST;
            case WEST -> EAST;
        };
    }
}

