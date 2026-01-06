package model.analytics;

import model.Direction;
import model.Vehicle;
import model.VehicleType;
import java.util.*;

/**
 * Collects and stores simulation statistics
 * Tracks performance metrics for analysis
 */
public class StatisticsCollector {

    // Current cycle statistics
    private int cycleNumber = 0;
    private long cycleStartTime = 0;
    private long cycleEndTime = 0;

    // Vehicle statistics
    private int totalVehiclesProcessed = 0;
    private Map<Direction, Integer> vehiclesPerDirection = new EnumMap<>(Direction.class);
    private Map<VehicleType, Integer> vehiclesPerType = new EnumMap<>(VehicleType.class);

    // Wait time statistics
    private List<Long> allWaitTimes = new ArrayList<>();
    private Map<Direction, List<Long>> waitTimesPerDirection = new EnumMap<>(Direction.class);

    // Queue length tracking
    private Map<Direction, List<Integer>> queueLengthHistory = new EnumMap<>(Direction.class);

    // Throughput tracking (vehicles per minute)
    private List<Double> throughputHistory = new ArrayList<>();

    // Cycle history
    private List<CycleStatistics> cycleHistory = new ArrayList<>();

    // Emergency vehicle statistics
    private int emergencyVehiclesProcessed = 0;
    private List<Long> emergencyWaitTimes = new ArrayList<>();

    public StatisticsCollector() {
        resetCycleStats();
    }

    private void resetCycleStats() {
        for (Direction dir : Direction.values()) {
            vehiclesPerDirection.put(dir, 0);
            waitTimesPerDirection.put(dir, new ArrayList<>());
            queueLengthHistory.put(dir, new ArrayList<>());
        }

        for (VehicleType type : VehicleType.values()) {
            vehiclesPerType.put(type, 0);
        }
    }

    /**
     * Start a new cycle
     */
    public void startCycle() {
        cycleNumber++;
        cycleStartTime = System.currentTimeMillis();
        resetCycleStats();
    }

    /**
     * Record cycle start with vehicle densities
     */
    public void recordCycleStart(Map<Direction, Integer> vehicleDensities) {
        startCycle();
    }

    /**
     * End current cycle and compute statistics
     */
    public void endCycle() {
        cycleEndTime = System.currentTimeMillis();
        long cycleDuration = cycleEndTime - cycleStartTime;

        // Calculate throughput (vehicles per minute)
        double throughput = 0;
        if (cycleDuration > 0) {
            int vehiclesInCycle = vehiclesPerDirection.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
            throughput = (vehiclesInCycle * 60000.0) / cycleDuration;
        }
        throughputHistory.add(throughput);

        // Store cycle statistics
        CycleStatistics cycleStats = new CycleStatistics(
            cycleNumber,
            cycleStartTime,
            cycleEndTime,
            cycleDuration,
            new EnumMap<>(vehiclesPerDirection),
            new EnumMap<>(vehiclesPerType),
            calculateAverageWaitTime(),
            throughput
        );

        cycleHistory.add(cycleStats);

        System.out.printf("Cycle %d completed: %d vehicles, avg wait %.1fs, throughput %.1f veh/min%n",
                cycleNumber, totalVehiclesProcessed, calculateAverageWaitTime() / 1000.0, throughput);
    }

    /**
     * Record cycle end
     */
    public void recordCycleEnd() {
        endCycle();
    }

    /**
     * Record direction activation
     */
    public void recordDirectionActivation(Direction direction, int greenDuration) {
        // For future analytics on timing decisions
    }

    /**
     * Record phase completion
     */
    public void recordPhaseCompletion(Direction direction, String phase) {
        // For future analytics on phase transitions
    }

    /**
     * Record vehicle arrival
     */
    public void recordVehicleArrival(Direction direction, int count) {
        // For future analytics on arrival patterns
    }

    /**
     * Record a vehicle crossing the intersection
     */
    public void recordVehicleCrossed(Vehicle vehicle) {
        totalVehiclesProcessed++;

        Direction dir = vehicle.getDirection();
        vehiclesPerDirection.merge(dir, 1, Integer::sum);

        VehicleType type = vehicle.getType();
        vehiclesPerType.merge(type, 1, Integer::sum);

        long waitTime = vehicle.getWaitTime();
        allWaitTimes.add(waitTime);
        waitTimesPerDirection.get(dir).add(waitTime);

        if (vehicle.isEmergency()) {
            emergencyVehiclesProcessed++;
            emergencyWaitTimes.add(waitTime);
        }
    }

    /**
     * Record a vehicle crossing the intersection (simplified version)
     */
    public void recordVehicleCrossed(Direction direction, VehicleType type) {
        totalVehiclesProcessed++;
        vehiclesPerDirection.merge(direction, 1, Integer::sum);
        vehiclesPerType.merge(type, 1, Integer::sum);
    }

    /**
     * Record queue length for a direction
     */
    public void recordQueueLength(Direction direction, int length) {
        queueLengthHistory.get(direction).add(length);
    }

    /**
     * Calculate average wait time across all vehicles (milliseconds)
     */
    public double calculateAverageWaitTime() {
        if (allWaitTimes.isEmpty()) return 0;
        return allWaitTimes.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    /**
     * Calculate average wait time for a specific direction
     */
    public double calculateAverageWaitTime(Direction direction) {
        List<Long> times = waitTimesPerDirection.get(direction);
        if (times.isEmpty()) return 0;
        return times.stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
    }

    /**
     * Calculate median wait time
     */
    public double calculateMedianWaitTime() {
        if (allWaitTimes.isEmpty()) return 0;
        List<Long> sorted = new ArrayList<>(allWaitTimes);
        Collections.sort(sorted);
        int middle = sorted.size() / 2;
        if (sorted.size() % 2 == 0) {
            return (sorted.get(middle - 1) + sorted.get(middle)) / 2.0;
        } else {
            return sorted.get(middle);
        }
    }

    /**
     * Calculate 95th percentile wait time
     */
    public double calculate95thPercentileWaitTime() {
        if (allWaitTimes.isEmpty()) return 0;
        List<Long> sorted = new ArrayList<>(allWaitTimes);
        Collections.sort(sorted);
        int index = (int) Math.ceil(0.95 * sorted.size()) - 1;
        return sorted.get(Math.max(0, Math.min(index, sorted.size() - 1)));
    }

    /**
     * Calculate average throughput (vehicles per minute)
     */
    public double calculateAverageThroughput() {
        if (throughputHistory.isEmpty()) return 0;
        return throughputHistory.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
    }

    /**
     * Calculate average queue length for a direction
     */
    public double calculateAverageQueueLength(Direction direction) {
        List<Integer> lengths = queueLengthHistory.get(direction);
        if (lengths.isEmpty()) return 0;
        return lengths.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
    }

    // Getters

    public int getCycleNumber() {
        return cycleNumber;
    }

    public int getTotalVehiclesProcessed() {
        return totalVehiclesProcessed;
    }

    public Map<Direction, Integer> getVehiclesPerDirection() {
        return new EnumMap<>(vehiclesPerDirection);
    }

    public Map<VehicleType, Integer> getVehiclesPerType() {
        return new EnumMap<>(vehiclesPerType);
    }

    public int getEmergencyVehiclesProcessed() {
        return emergencyVehiclesProcessed;
    }

    public List<CycleStatistics> getCycleHistory() {
        return new ArrayList<>(cycleHistory);
    }

    public List<CycleStatistics> getRecentCycleHistory(int count) {
        int start = Math.max(0, cycleHistory.size() - count);
        return new ArrayList<>(cycleHistory.subList(start, cycleHistory.size()));
    }

    /**
     * Clear all statistics
     */
    public void clear() {
        cycleNumber = 0;
        totalVehiclesProcessed = 0;
        emergencyVehiclesProcessed = 0;
        allWaitTimes.clear();
        throughputHistory.clear();
        cycleHistory.clear();
        emergencyWaitTimes.clear();
        resetCycleStats();
    }

    /**
     * Reset statistics (alias for clear)
     */
    public void reset() {
        clear();
    }

    /**
     * Get summary of current cycle
     */
    public String getCycleSummary() {
        return String.format("Cycle %d: %d vehicles processed", cycleNumber, totalVehiclesProcessed);
    }

    /**
     * Export statistics to file
     */
    public void exportToFile(String filename) {
        try (java.io.PrintWriter writer = new java.io.PrintWriter(filename)) {
            writer.println(generateSummaryReport());
            System.out.println("Statistics exported to: " + filename);
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Failed to export statistics: " + e.getMessage());
        }
    }

    /**
     * Generate summary report
     */
    public String generateSummaryReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== SIMULATION STATISTICS SUMMARY ===\n");
        sb.append(String.format("Total Cycles: %d%n", cycleNumber));
        sb.append(String.format("Total Vehicles Processed: %d%n", totalVehiclesProcessed));
        sb.append(String.format("Emergency Vehicles: %d%n", emergencyVehiclesProcessed));
        sb.append(String.format("Average Wait Time: %.2f seconds%n", calculateAverageWaitTime() / 1000.0));
        sb.append(String.format("Median Wait Time: %.2f seconds%n", calculateMedianWaitTime() / 1000.0));
        sb.append(String.format("95th Percentile Wait Time: %.2f seconds%n", calculate95thPercentileWaitTime() / 1000.0));
        sb.append(String.format("Average Throughput: %.2f vehicles/minute%n", calculateAverageThroughput()));
        sb.append("\nVehicles by Direction:\n");
        for (Direction dir : Direction.values()) {
            int count = vehiclesPerDirection.getOrDefault(dir, 0);
            double avgWait = calculateAverageWaitTime(dir) / 1000.0;
            sb.append(String.format("  %s: %d vehicles (avg wait %.2fs)%n", dir, count, avgWait));
        }
        sb.append("\nVehicles by Type:\n");
        for (VehicleType type : VehicleType.values()) {
            int count = vehiclesPerType.getOrDefault(type, 0);
            sb.append(String.format("  %s: %d%n", type.getDisplayName(), count));
        }
        return sb.toString();
    }

    /**
     * Inner class to store cycle-specific statistics
     */
    public static class CycleStatistics {
        public final int cycleNumber;
        public final long startTime;
        public final long endTime;
        public final long duration;
        public final Map<Direction, Integer> vehiclesPerDirection;
        public final Map<VehicleType, Integer> vehiclesPerType;
        public final double averageWaitTime;
        public final double throughput;

        public CycleStatistics(int cycleNumber, long startTime, long endTime, long duration,
                             Map<Direction, Integer> vehiclesPerDirection,
                             Map<VehicleType, Integer> vehiclesPerType,
                             double averageWaitTime, double throughput) {
            this.cycleNumber = cycleNumber;
            this.startTime = startTime;
            this.endTime = endTime;
            this.duration = duration;
            this.vehiclesPerDirection = vehiclesPerDirection;
            this.vehiclesPerType = vehiclesPerType;
            this.averageWaitTime = averageWaitTime;
            this.throughput = throughput;
        }

        public int getTotalVehicles() {
            return vehiclesPerDirection.values().stream()
                    .mapToInt(Integer::intValue)
                    .sum();
        }
    }
}

