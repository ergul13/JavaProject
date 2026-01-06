package model.strategy;

import model.Direction;
import model.TrafficLight;
import java.util.*;

/**
 * Adaptive Strategy: Considers vehicle density + historical patterns + wait times
 * Advanced algorithm with weighted factors
 */
public class AdaptiveStrategy implements TrafficControlStrategy {

    private static final int MIN_GREEN_TIME = 8;
    private static final int MAX_GREEN_TIME = 70;

    // Weighting factors
    private static final double DENSITY_WEIGHT = 0.6;
    private static final double HISTORICAL_WEIGHT = 0.25;
    private static final double WAIT_TIME_WEIGHT = 0.15;

    private final Map<Direction, Long> directionWaitTimes = new EnumMap<>(Direction.class);

    public AdaptiveStrategy() {
        for (Direction dir : Direction.values()) {
            directionWaitTimes.put(dir, 0L);
        }
    }

    @Override
    public String getStrategyName() {
        return "Adaptive Control";
    }

    @Override
    public String getDescription() {
        return "Intelligent adaptation - considers density, historical patterns, and wait times";
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
        System.out.println("\n=== ADAPTIVE STRATEGY ===");

        // Calculate weighted scores for each direction
        Map<Direction, Double> scores = new EnumMap<>(Direction.class);
        double totalScore = 0.0;

        int totalVehicles = vehicleDensity.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalVehicles == 0) {
            Map<Direction, Integer> durations = new EnumMap<>(Direction.class);
            for (Direction dir : Direction.values()) {
                durations.put(dir, MIN_GREEN_TIME);
            }
            return durations;
        }

        // Find max wait time for normalization
        long maxWaitTime = Math.max(1, directionWaitTimes.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(1));

        for (Direction dir : Direction.values()) {
            double densityScore = (double) vehicleDensity.get(dir) / totalVehicles;

            double historicalScore = 0.0;
            if (historicalData != null && historicalData.containsKey(dir)) {
                historicalScore = historicalData.get(dir);
            }

            double waitScore = (double) directionWaitTimes.get(dir) / maxWaitTime;

            double weightedScore = (densityScore * DENSITY_WEIGHT) +
                                  (historicalScore * HISTORICAL_WEIGHT) +
                                  (waitScore * WAIT_TIME_WEIGHT);

            scores.put(dir, weightedScore);
            totalScore += weightedScore;

            System.out.printf("%s: density=%.2f, historical=%.2f, wait=%.2f -> score=%.3f%n",
                    dir, densityScore, historicalScore, waitScore, weightedScore);
        }

        // Allocate green time based on scores
        int numDirections = Direction.values().length;
        int availableGreenTime = totalCycleTime - (numDirections * yellowLightDuration);

        Map<Direction, Integer> durations = new EnumMap<>(Direction.class);
        int totalAllocated = 0;

        for (Direction dir : Direction.values()) {
            double proportion = scores.get(dir) / totalScore;
            int greenTime = (int) Math.round(proportion * availableGreenTime);
            greenTime = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, greenTime));

            durations.put(dir, greenTime);
            totalAllocated += greenTime;

            System.out.printf("%s: %.1f%% -> %d sec%n", dir, proportion * 100, greenTime);
        }

        // Fine-tune to match available time
        balanceAllocations(durations, scores, availableGreenTime - totalAllocated);

        return durations;
    }

    @Override
    public Direction determineNextDirection(
        Direction currentDirection,
        Map<Direction, Integer> vehicleDensity,
        Map<Direction, Long> waitTimes
    ) {
        // Update wait times
        if (waitTimes != null) {
            directionWaitTimes.putAll(waitTimes);
        }

        // Find direction with highest priority (density * wait time factor)
        Direction nextDir = null;
        double maxPriority = -1;

        for (Direction dir : Direction.values()) {
            if (dir == currentDirection) continue;

            int density = vehicleDensity.getOrDefault(dir, 0);
            long waitTime = directionWaitTimes.getOrDefault(dir, 0L);

            // Priority = density * (1 + waitTime/60)
            double priority = density * (1.0 + waitTime / 60.0);

            if (priority > maxPriority) {
                maxPriority = priority;
                nextDir = dir;
            }
        }

        return nextDir != null ? nextDir : currentDirection.getOpposite();
    }

    private void balanceAllocations(Map<Direction, Integer> durations,
                                    Map<Direction, Double> scores,
                                    int difference) {
        if (difference == 0) return;

        List<Direction> sortedDirs = new ArrayList<>(Arrays.asList(Direction.values()));
        sortedDirs.sort((d1, d2) -> Double.compare(scores.get(d2), scores.get(d1)));

        int adjustment = difference > 0 ? 1 : -1;
        int remaining = Math.abs(difference);

        for (Direction dir : sortedDirs) {
            if (remaining == 0) break;

            int current = durations.get(dir);
            int newTime = current + adjustment;

            if (newTime >= MIN_GREEN_TIME && newTime <= MAX_GREEN_TIME) {
                durations.put(dir, newTime);
                remaining--;
            }
        }
    }

    public void updateWaitTime(Direction direction, long additionalTime) {
        directionWaitTimes.merge(direction, additionalTime, Long::sum);
    }

    public void resetWaitTime(Direction direction) {
        directionWaitTimes.put(direction, 0L);
    }
}

