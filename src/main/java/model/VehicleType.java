package model;

/**
 * Enum representing different types of vehicles
 * Each type has different characteristics and priorities
 */
public enum VehicleType {
    REGULAR("Regular Car", 1.0, 1.0),
    BUS("Bus", 1.5, 1.2),
    MOTORCYCLE("Motorcycle", 0.5, 0.8),
    EMERGENCY("Emergency Vehicle", 2.0, 3.0);

    private final String displayName;
    private final double sizeMultiplier; // Size relative to regular car
    private final double priorityWeight; // Priority in traffic control

    VehicleType(String displayName, double sizeMultiplier, double priorityWeight) {
        this.displayName = displayName;
        this.sizeMultiplier = sizeMultiplier;
        this.priorityWeight = priorityWeight;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getSizeMultiplier() {
        return sizeMultiplier;
    }

    public double getPriorityWeight() {
        return priorityWeight;
    }

    /**
     * Check if this is an emergency vehicle
     */
    public boolean isEmergency() {
        return this == EMERGENCY;
    }
}

