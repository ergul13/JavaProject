package model.strategy;

import model.Direction;
import model.TrafficLight;

import java.util.Map;

/**
 * Strategy interface for different traffic control algorithms
 * Implements Strategy Design Pattern
 */
public interface TrafficControlStrategy {

    /**
     * Get the name of this strategy
     */
    String getStrategyName();

    /**
     * Get description of this strategy
     */
    String getDescription();

    /**
     * Calculate green light durations for all directions
     *
     * @param vehicleDensity Current vehicle count per direction
     * @param trafficLights Traffic light objects for each direction
     * @param totalCycleTime Total cycle time in seconds
     * @param yellowLightDuration Yellow light duration in seconds
     * @param historicalData Historical traffic data (optional, may be null)
     * @return Map of direction to green duration in seconds
     */
    Map<Direction, Integer> calculateGreenLightDurations(
        Map<Direction, Integer> vehicleDensity,
        Map<Direction, TrafficLight> trafficLights,
        int totalCycleTime,
        int yellowLightDuration,
        Map<Direction, Double> historicalData
    );

    /**
     * Determine the next direction to activate
     *
     * @param currentDirection Current active direction
     * @param vehicleDensity Current vehicle density
     * @param waitTimes Wait times for each direction
     * @return Next direction to activate
     */
    default Direction determineNextDirection(
        Direction currentDirection,
        Map<Direction, Integer> vehicleDensity,
        Map<Direction, Long> waitTimes
    ) {
        // Default: sequential order (N->E->S->W)
        if (currentDirection == null) return Direction.NORTH;

        return switch (currentDirection) {
            case NORTH -> Direction.EAST;
            case EAST -> Direction.SOUTH;
            case SOUTH -> Direction.WEST;
            case WEST -> Direction.NORTH;
        };
    }

    /**
     * Check if strategy supports dynamic re-calculation during cycle
     */
    default boolean supportsRealTimeAdjustment() {
        return false;
    }
}

