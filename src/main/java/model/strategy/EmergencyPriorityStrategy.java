package model.strategy;

import model.Direction;
import model.TrafficLight;
import java.util.*;

/**
 * Emergency Priority Strategy: Immediate priority for emergency vehicles
 * Overrides normal scheduling when emergency vehicle detected
 */
public class EmergencyPriorityStrategy implements TrafficControlStrategy {

    private static final int MIN_GREEN_TIME = 5;
    private static final int MAX_GREEN_TIME = 80;
    private static final int EMERGENCY_GREEN_TIME = 45; // Extended time for emergency

    private Direction emergencyDirection = null;
    private boolean emergencyActive = false;

    @Override
    public String getStrategyName() {
        return "Emergency Priority Control";
    }

    @Override
    public String getDescription() {
        return "Emergency vehicle detection - immediate priority and extended green time";
    }

    @Override
    public boolean supportsRealTimeAdjustment() {
        return true;
    }

    @Override
    public Map<Direction, Integer> calculateGreenLightDurations(
        Map<Direction, Integer> vehicleDensity,
        Map<Direction, TrafficLight> trafficLights,
        int totalCycleTime,
        int yellowLightDuration,
        Map<Direction, Double> historicalData
    ) {
        Map<Direction, Integer> durations = new EnumMap<>(Direction.class);

        System.out.println("\n=== EMERGENCY PRIORITY STRATEGY ===");

        if (emergencyActive && emergencyDirection != null) {
            // Emergency mode: give maximum time to emergency direction
            System.out.println("EMERGENCY MODE: Priority to " + emergencyDirection);

            durations.put(emergencyDirection, EMERGENCY_GREEN_TIME);

            // Minimal time for other directions
            int numDirections = Direction.values().length;
            int remainingTime = totalCycleTime - (numDirections * yellowLightDuration) - EMERGENCY_GREEN_TIME;
            int minTime = Math.max(MIN_GREEN_TIME, remainingTime / (numDirections - 1));

            for (Direction dir : Direction.values()) {
                if (dir != emergencyDirection) {
                    durations.put(dir, minTime);
                }
            }
        } else {
            // Normal mode: density-based with emergency awareness
            int totalVehicles = vehicleDensity.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();

            if (totalVehicles == 0) {
                for (Direction dir : Direction.values()) {
                    durations.put(dir, MIN_GREEN_TIME);
                }
                return durations;
            }

            int numDirections = Direction.values().length;
            int availableGreenTime = totalCycleTime - (numDirections * yellowLightDuration);

            for (Direction dir : Direction.values()) {
                int dirVehicles = vehicleDensity.get(dir);
                double proportion = (double) dirVehicles / totalVehicles;
                int greenTime = (int) Math.round(proportion * availableGreenTime);
                greenTime = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, greenTime));

                durations.put(dir, greenTime);
            }
        }

        return durations;
    }

    @Override
    public Direction determineNextDirection(
        Direction currentDirection,
        Map<Direction, Integer> vehicleDensity,
        Map<Direction, Long> waitTimes
    ) {
        // If emergency active, prioritize emergency direction
        if (emergencyActive && emergencyDirection != null) {
            return emergencyDirection;
        }

        // Otherwise, sequential order
        return switch (currentDirection) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
    }

    /**
     * Activate emergency mode for a specific direction
     */
    public void activateEmergency(Direction direction) {
        this.emergencyDirection = direction;
        this.emergencyActive = true;
        System.out.println("EMERGENCY ACTIVATED: " + direction);
    }

    /**
     * Deactivate emergency mode
     */
    public void deactivateEmergency() {
        System.out.println("EMERGENCY CLEARED: " + emergencyDirection);
        this.emergencyActive = false;
        this.emergencyDirection = null;
    }

    /**
     * Check if emergency is currently active
     */
    public boolean isEmergencyActive() {
        return emergencyActive;
    }

    /**
     * Get current emergency direction
     */
    public Direction getEmergencyDirection() {
        return emergencyDirection;
    }
}

