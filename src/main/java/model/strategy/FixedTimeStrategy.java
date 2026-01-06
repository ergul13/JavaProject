package model.strategy;

import model.Direction;
import model.TrafficLight;
import java.util.*;

/**
 * Fixed-Time Strategy: Equal green time for all directions
 * Traditional traffic light system
 */
public class FixedTimeStrategy implements TrafficControlStrategy {

    private static final int MIN_GREEN_TIME = 15;
    private static final int MAX_GREEN_TIME = 30;

    @Override
    public String getStrategyName() {
        return "Fixed-Time Control";
    }

    @Override
    public String getDescription() {
        return "Traditional fixed timing - equal green time for all directions regardless of traffic";
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

        int numDirections = Direction.values().length;
        int totalYellowTime = numDirections * yellowLightDuration;
        int availableGreenTime = totalCycleTime - totalYellowTime;

        // Equal distribution
        int equalGreenTime = availableGreenTime / numDirections;
        equalGreenTime = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, equalGreenTime));

        for (Direction dir : Direction.values()) {
            durations.put(dir, equalGreenTime);
        }

        System.out.println("\n=== FIXED-TIME STRATEGY ===");
        System.out.println("Equal green time for all directions: " + equalGreenTime + "s");

        return durations;
    }
}

