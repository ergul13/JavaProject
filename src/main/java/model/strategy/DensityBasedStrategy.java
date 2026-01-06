package model.strategy;

import model.Direction;
import model.TrafficLight;
import java.util.*;

/**
 * Density-Based Strategy: Proportional green time based on vehicle count
 * This is the original algorithm from the base system
 */
public class DensityBasedStrategy implements TrafficControlStrategy {

    private static final int MIN_GREEN_TIME = 10;
    private static final int MAX_GREEN_TIME = 60;

    @Override
    public String getStrategyName() {
        return "Density-Based Control";
    }

    @Override
    public String getDescription() {
        return "Proportional allocation - green time based on current vehicle density";
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

        int totalVehicles = vehicleDensity.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        System.out.println("\n=== DENSITY-BASED STRATEGY ===");
        System.out.println("Total vehicles: " + totalVehicles);

        if (totalVehicles == 0) {
            // No vehicles, equal distribution
            for (Direction dir : Direction.values()) {
                durations.put(dir, MIN_GREEN_TIME);
            }
            return durations;
        }

        int numDirections = Direction.values().length;
        int availableGreenTime = totalCycleTime - (numDirections * yellowLightDuration);

        // First pass: proportional allocation
        Map<Direction, Integer> allocatedTime = new EnumMap<>(Direction.class);
        int totalAllocated = 0;

        for (Direction dir : Direction.values()) {
            int dirVehicles = vehicleDensity.get(dir);
            double proportion = (double) dirVehicles / totalVehicles;
            int rawGreenTime = (int) Math.round(proportion * availableGreenTime);

            int greenTime = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, rawGreenTime));
            allocatedTime.put(dir, greenTime);
            totalAllocated += greenTime;

            System.out.printf("%s: %d vehicles (%.1f%%) -> %d sec%n",
                    dir, dirVehicles, proportion * 100, greenTime);
        }

        // Adjust if total doesn't match available time
        int difference = availableGreenTime - totalAllocated;
        if (difference != 0) {
            adjustAllocations(allocatedTime, vehicleDensity, difference);
        }

        return allocatedTime;
    }

    private void adjustAllocations(Map<Direction, Integer> allocatedTime,
                                   Map<Direction, Integer> vehicleDensity,
                                   int difference) {
        List<Direction> sortedDirs = new ArrayList<>(Arrays.asList(Direction.values()));
        sortedDirs.sort((d1, d2) -> vehicleDensity.get(d2).compareTo(vehicleDensity.get(d1)));

        int adjustment = difference > 0 ? 1 : -1;
        int remaining = Math.abs(difference);

        for (Direction dir : sortedDirs) {
            if (remaining == 0) break;

            int current = allocatedTime.get(dir);
            int newTime = current + adjustment;

            if (newTime >= MIN_GREEN_TIME && newTime <= MAX_GREEN_TIME) {
                allocatedTime.put(dir, newTime);
                remaining--;
            }
        }
    }
}

