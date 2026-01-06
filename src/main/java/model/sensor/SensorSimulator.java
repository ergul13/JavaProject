package model.sensor;

import model.Direction;
import model.Vehicle;
import model.VehicleType;
import model.config.SimulationConfig;

import java.util.*;

/**
 * Simulates traffic sensors that detect and generate vehicles
 * Uses Poisson distribution for realistic arrival patterns
 */
public class SensorSimulator {

    private final SimulationConfig config;
    private final Random random;
    private int nextVehicleId = 1000; // Start at 1000 to distinguish from manual vehicles

    // Time tracking for continuous generation
    private double accumulatedTime = 0.0; // seconds

    // Statistics
    private Map<Direction, Integer> generatedVehiclesCount = new EnumMap<>(Direction.class);

    public SensorSimulator(SimulationConfig config) {
        this.config = config;
        this.random = new Random();

        for (Direction dir : Direction.values()) {
            generatedVehiclesCount.put(dir, 0);
        }
    }

    /**
     * Generate vehicles for a time step using Poisson distribution
     *
     * @param deltaTime Time step in seconds
     * @param direction Direction to generate vehicles for
     * @return List of newly generated vehicles
     */
    public List<Vehicle> generateVehicles(double deltaTime, Direction direction) {
        List<Vehicle> newVehicles = new ArrayList<>();

        if (!config.isContinuousGeneration()) {
            return newVehicles;
        }

        // Get arrival rate for this direction (vehicles per minute)
        double lambda = config.getDirectionArrivalRates().get(direction);

        // Convert to vehicles per second
        double ratePerSecond = lambda / 60.0;

        // Expected number of arrivals in this time step
        double expectedArrivals = ratePerSecond * deltaTime;

        // Use Poisson distribution to determine actual arrivals
        int arrivals = poissonRandom(expectedArrivals);

        // Generate vehicles
        for (int i = 0; i < arrivals; i++) {
            VehicleType type = selectVehicleType();
            Vehicle vehicle = new Vehicle(direction, nextVehicleId++, -0.05, type);
            newVehicles.add(vehicle);
            generatedVehiclesCount.merge(direction, 1, Integer::sum);
        }

        return newVehicles;
    }

    /**
     * Generate vehicles for all directions
     */
    public Map<Direction, List<Vehicle>> generateVehiclesAllDirections(double deltaTime) {
        Map<Direction, List<Vehicle>> allVehicles = new EnumMap<>(Direction.class);

        for (Direction dir : Direction.values()) {
            allVehicles.put(dir, generateVehicles(deltaTime, dir));
        }

        return allVehicles;
    }

    /**
     * Generate a random number following Poisson distribution
     * Using Knuth's algorithm for small lambda values
     */
    private int poissonRandom(double lambda) {
        if (lambda <= 0) return 0;

        // For very small lambda, use simple probability
        if (lambda < 0.1) {
            return random.nextDouble() < lambda ? 1 : 0;
        }

        // Knuth's algorithm
        double L = Math.exp(-lambda);
        double p = 1.0;
        int k = 0;

        do {
            k++;
            p *= random.nextDouble();
        } while (p > L && k < 100); // Limit to prevent infinite loop

        return k - 1;
    }

    /**
     * Select vehicle type based on configured percentages
     */
    private VehicleType selectVehicleType() {
        double rand = random.nextDouble() * 100.0;
        double cumulative = 0.0;

        // Check emergency first (highest priority)
        cumulative += config.getEmergencyPercentage();
        if (rand < cumulative) {
            return VehicleType.EMERGENCY;
        }

        // Check motorcycle
        cumulative += config.getMotorcyclePercentage();
        if (rand < cumulative) {
            return VehicleType.MOTORCYCLE;
        }

        // Check bus
        cumulative += config.getBusPercentage();
        if (rand < cumulative) {
            return VehicleType.BUS;
        }

        // Default to regular
        return VehicleType.REGULAR;
    }

    /**
     * Generate an emergency vehicle at a specific direction
     */
    public Vehicle generateEmergencyVehicle(Direction direction) {
        Vehicle vehicle = new Vehicle(direction, nextVehicleId++, -0.05, VehicleType.EMERGENCY);
        generatedVehiclesCount.merge(direction, 1, Integer::sum);
        System.out.println("EMERGENCY VEHICLE GENERATED: " + direction);
        return vehicle;
    }

    /**
     * Check if emergency vehicle should be generated this cycle (random probability)
     */
    public boolean shouldGenerateEmergency() {
        if (!config.isAutoEmergencyGeneration()) {
            return false;
        }
        return random.nextDouble() < config.getEmergencyProbabilityPerCycle();
    }

    /**
     * Generate random initial vehicles for a direction
     */
    public List<Vehicle> generateInitialVehicles(Direction direction, int count) {
        List<Vehicle> vehicles = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            double spacing = 0.05;
            double position = -spacing * (i + 1);
            VehicleType type = selectVehicleType();
            Vehicle vehicle = new Vehicle(direction, nextVehicleId++, position, type);
            vehicles.add(vehicle);
            generatedVehiclesCount.merge(direction, 1, Integer::sum);
        }

        return vehicles;
    }

    /**
     * Reset statistics
     */
    public void reset() {
        for (Direction dir : Direction.values()) {
            generatedVehiclesCount.put(dir, 0);
        }
        accumulatedTime = 0.0;
    }

    // Getters

    public Map<Direction, Integer> getGeneratedVehiclesCount() {
        return new EnumMap<>(generatedVehiclesCount);
    }

    public int getTotalGeneratedVehicles() {
        return generatedVehiclesCount.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
    }
}

