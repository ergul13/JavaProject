package model;

/**
 * Model class representing a vehicle at the intersection
 */
public class Vehicle {
    private final Direction direction;
    private double position; // Position along the lane (0.0 to 1.0)
    private boolean hasCrossed;
    private final int id;
    private final VehicleType type;
    private final long arrivalTime; // Timestamp when vehicle arrived (milliseconds)
    private long waitTime; // Total wait time in milliseconds
    private boolean hasHadGreenLight; // Track if vehicle has experienced green light

    public Vehicle(Direction direction, int id, double initialPosition) {
        this(direction, id, initialPosition, VehicleType.REGULAR);
    }

    public Vehicle(Direction direction, int id, double initialPosition, VehicleType type) {
        this.direction = direction;
        this.id = id;
        this.position = initialPosition;
        this.type = type;
        this.hasCrossed = false;
        this.arrivalTime = System.currentTimeMillis();
        this.waitTime = 0;
        this.hasHadGreenLight = false;
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

    public VehicleType getType() {
        return type;
    }

    public long getArrivalTime() {
        return arrivalTime;
    }

    public long getWaitTime() {
        return waitTime;
    }

    public void incrementWaitTime(long milliseconds) {
        this.waitTime += milliseconds;
    }

    public boolean hasHadGreenLight() {
        return hasHadGreenLight;
    }

    public void setHasHadGreenLight(boolean hasHadGreenLight) {
        this.hasHadGreenLight = hasHadGreenLight;
    }

    /**
     * Check if this is an emergency vehicle
     */
    public boolean isEmergency() {
        return type.isEmergency();
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

    /**
     * Get weighted priority score for this vehicle
     * Higher score = higher priority
     */
    public double getPriorityScore() {
        double waitFactor = Math.min(waitTime / 60000.0, 5.0); // Max 5x multiplier for 1 min wait
        return type.getPriorityWeight() * (1.0 + waitFactor);
    }
}

