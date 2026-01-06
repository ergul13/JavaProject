package model;

/**
 * Model class representing a vehicle at the intersection
 */
public class Vehicle {
    private final Direction direction;
    private double position; // Position along the lane (0.0 to 1.0)
    private boolean hasCrossed;
    private final int id;

    public Vehicle(Direction direction, int id, double initialPosition) {
        this.direction = direction;
        this.id = id;
        this.position = initialPosition;
        this.hasCrossed = false;
    }

    public Direction getDirection() {
        return direction;
    }

    public double getPosition() {
        return position;
    }

    public void setPosition(double position) {
        this.position = position;
    }

    public boolean hasCrossed() {
        return hasCrossed;
    }

    public void setHasCrossed(boolean hasCrossed) {
        this.hasCrossed = hasCrossed;
    }

    public int getId() {
        return id;
    }

    /**
     * Move vehicle forward
     * @param distance Distance to move (normalized 0.0 to 1.0)
     */
    public void move(double distance) {
        this.position += distance;
        if (this.position >= 1.0) {
            this.hasCrossed = true;
        }
    }
}

