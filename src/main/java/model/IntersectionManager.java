package model;

import model.analytics.StatisticsCollector;
import model.config.ConfigurationManager;
import model.config.SimulationConfig;
import model.sensor.SensorSimulator;
import model.strategy.*;

import java.util.*;

/**
 * Core model class managing the intersection logic
 * Handles traffic lights, vehicles, and timing calculations
 * Integrates with strategy pattern, sensors, and analytics
 */
public class IntersectionManager {
    // Constants (can be overridden by config)
    public static final int TOTAL_CYCLE_TIME = 120; // seconds
    public static final int YELLOW_LIGHT_DURATION = 3; // seconds
    public static final int MIN_GREEN_TIME = 10; // seconds
    public static final int MAX_GREEN_TIME = 60; // seconds

    // Configuration and strategy
    private final ConfigurationManager configManager;
    private final SimulationConfig config;
    private final Map<String, TrafficControlStrategy> strategies;
    private TrafficControlStrategy activeStrategy;

    // Analytics and sensor simulation
    private final StatisticsCollector statisticsCollector;
    private final SensorSimulator sensorSimulator;

    // Traffic lights for each direction
    private final Map<Direction, TrafficLight> trafficLights;

    // Vehicles waiting at each direction
    private final Map<Direction, List<Vehicle>> vehicles;

    // Vehicle density (count) for each direction
    private final Map<Direction, Integer> vehicleDensity;

    // Direction wait times for adaptive strategies
    private final Map<Direction, Long> directionWaitTimes;
    private final Map<Direction, Long> lastGreenLightTime;

    // Current active direction
    private Direction currentActiveDirection;

    // Cycle state
    private boolean cycleActive;
    private double elapsedCycleTime;
    private Queue<Direction> directionQueue;

    // Vehicle ID counter
    private int nextVehicleId;

    // Accumulated time for tick (to update timers every second)
    private double accumulatedTime;

    /**
     * Default constructor with default configuration
     */
    public IntersectionManager() {
        this(new ConfigurationManager());
    }

    /**
     * Constructor with custom configuration manager
     */
    public IntersectionManager(ConfigurationManager configManager) {
        this.configManager = configManager;
        this.config = configManager.getConfig();

        // Initialize traffic infrastructure
        this.trafficLights = new EnumMap<>(Direction.class);
        this.vehicles = new EnumMap<>(Direction.class);
        this.vehicleDensity = new EnumMap<>(Direction.class);
        this.directionWaitTimes = new EnumMap<>(Direction.class);
        this.lastGreenLightTime = new EnumMap<>(Direction.class);

        long currentTime = System.currentTimeMillis();
        for (Direction dir : Direction.values()) {
            trafficLights.put(dir, new TrafficLight(dir));
            vehicles.put(dir, new ArrayList<>());
            vehicleDensity.put(dir, 0);
            directionWaitTimes.put(dir, 0L);
            lastGreenLightTime.put(dir, currentTime);
        }

        this.directionQueue = new LinkedList<>();
        this.cycleActive = false;
        this.elapsedCycleTime = 0.0;
        this.accumulatedTime = 0.0;
        this.nextVehicleId = 1;

        // Initialize strategies
        this.strategies = new HashMap<>();
        initializeStrategies();

        // Initialize analytics and sensors
        this.statisticsCollector = new StatisticsCollector();
        this.sensorSimulator = new SensorSimulator(config);
    }

    /**
     * Initialize all available control strategies
     */
    private void initializeStrategies() {
        strategies.put("FixedTime", new FixedTimeStrategy());
        strategies.put("DensityBased", new DensityBasedStrategy());
        strategies.put("Adaptive", new AdaptiveStrategy());
        strategies.put("Emergency", new EmergencyPriorityStrategy());

        // Set active strategy from config
        setActiveStrategy(config.getActiveStrategy());
    }

    /**
     * Set the active traffic control strategy
     */
    public void setActiveStrategy(String strategyName) {
        TrafficControlStrategy strategy = strategies.get(strategyName);
        if (strategy != null) {
            this.activeStrategy = strategy;
            config.setActiveStrategy(strategyName);
            System.out.println("Active strategy: " + strategy.getStrategyName());
        } else {
            System.err.println("Unknown strategy: " + strategyName + ", using DensityBased");
            this.activeStrategy = strategies.get("DensityBased");
        }
    }

    /**
     * Get available strategy names
     */
    public List<String> getAvailableStrategies() {
        return new ArrayList<>(strategies.keySet());
    }

    /**
     * Get active strategy name
     */
    public String getActiveStrategyName() {
        return activeStrategy != null ? activeStrategy.getStrategyName() : "None";
    }

    /**
     * Set vehicle density for a specific direction
     */
    public void setVehicleDensity(Direction direction, int count) {
        vehicleDensity.put(direction, Math.max(0, count));
    }

    /**
     * Generate random vehicle densities for all directions
     */
    public void generateRandomDensities() {
        Random random = new Random();
        for (Direction dir : Direction.values()) {
            // Generate random density between 0 and 50
            int density = random.nextInt(51);
            vehicleDensity.put(dir, density);
        }
        System.out.println("Generated random densities: " + vehicleDensity);
    }

    /**
     * Calculate green light durations using active strategy
     */
    private void calculateGreenLightDurations() {
        System.out.println("\n=== CALCULATING GREEN LIGHT DURATIONS ===");
        System.out.println("Strategy: " + activeStrategy.getStrategyName());
        System.out.println("Vehicle densities: " + vehicleDensity);

        // Use strategy to calculate durations
        Map<Direction, Integer> durations = activeStrategy.calculateGreenLightDurations(
            vehicleDensity,
            trafficLights,
            config.getTotalCycleTime(),
            config.getYellowLightDuration(),
            null // Historical data can be added later
        );

        // Apply calculated durations to traffic lights
        for (Direction dir : Direction.values()) {
            int duration = durations.getOrDefault(dir, MIN_GREEN_TIME);
            trafficLights.get(dir).setGreenDuration(duration);
            System.out.println(dir + ": " + duration + "s");
        }

        System.out.println("=== END CALCULATION ===\n");
    }

    /**
     * Start the simulation cycle
     */
    public void startCycle() {
        if (cycleActive) return;

        // Calculate green light durations using strategy
        calculateGreenLightDurations();

        // Create vehicles based on density
        createVehicles();

        // Build direction queue (N -> E -> S -> W by default)
        directionQueue.clear();
        Direction nextDir = activeStrategy.determineNextDirection(
            null,
            vehicleDensity,
            directionWaitTimes
        );

        // Build sequential queue starting from determined direction
        directionQueue.add(Direction.NORTH);
        directionQueue.add(Direction.EAST);
        directionQueue.add(Direction.SOUTH);
        directionQueue.add(Direction.WEST);

        // Start with first direction
        currentActiveDirection = directionQueue.poll();
        activateDirection(currentActiveDirection);

        cycleActive = true;
        elapsedCycleTime = 0;

        // Record cycle start in statistics
        statisticsCollector.recordCycleStart(vehicleDensity);
    }

    /**
     * Create vehicles based on current density settings
     */
    private void createVehicles() {
        // Clear existing vehicles
        for (Direction dir : Direction.values()) {
            vehicles.get(dir).clear();
        }

        // Create new vehicles
        for (Direction dir : Direction.values()) {
            int count = vehicleDensity.get(dir);
            List<Vehicle> dirVehicles = vehicles.get(dir);

            for (int i = 0; i < count; i++) {
                // Position vehicles in a queue behind stop line
                // Stop line is at position 0, vehicles start behind (negative positions)
                double spacing = 0.05; // 5% spacing between vehicles
                double position = -spacing * (i + 1);

                // Determine vehicle type based on configuration
                VehicleType type = determineVehicleType();
                Vehicle vehicle = new Vehicle(dir, nextVehicleId++, position, type);
                dirVehicles.add(vehicle);
            }
        }
    }

    /**
     * Determine vehicle type based on configured percentages
     */
    private VehicleType determineVehicleType() {
        Random random = new Random();
        double rand = random.nextDouble() * 100;

        double cumulative = 0;
        cumulative += config.getRegularVehiclePercentage();
        if (rand < cumulative) return VehicleType.REGULAR;

        cumulative += config.getBusPercentage();
        if (rand < cumulative) return VehicleType.BUS;

        cumulative += config.getMotorcyclePercentage();
        if (rand < cumulative) return VehicleType.MOTORCYCLE;

        return VehicleType.EMERGENCY;
    }

    /**
     * Activate a specific direction (set to green)
     */
    private void activateDirection(Direction direction) {
        if (direction == null) return;

        TrafficLight light = trafficLights.get(direction);
        light.setCurrentColor(LightColor.GREEN);
        light.setRemainingTime(light.getGreenDuration());

        // Record activation time
        lastGreenLightTime.put(direction, System.currentTimeMillis());

        // Record in statistics
        statisticsCollector.recordDirectionActivation(direction, light.getGreenDuration());
    }

    /**
     * Update simulation state (called every frame/second)
     */
    public void update() {
        update(1.0); // Default 1 second update
    }

    /**
     * Update simulation state with delta time
     * @param deltaTime Time elapsed in seconds
     */
    public void update(double deltaTime) {
        if (!cycleActive || currentActiveDirection == null) return;

        accumulatedTime += deltaTime;

        // Update every second
        if (accumulatedTime >= 1.0) {
            accumulatedTime -= 1.0;

            TrafficLight activeLight = trafficLights.get(currentActiveDirection);
            activeLight.tick();
            elapsedCycleTime++;

            // Update wait times for inactive directions
            updateWaitTimes();
        }

        // Move vehicles (continuous, not per second)
        if (trafficLights.get(currentActiveDirection).getCurrentColor() == LightColor.GREEN) {
            moveVehicles(currentActiveDirection);
        }

        // Also move vehicles that are already crossing (position >= 0) regardless of light
        for (Direction dir : Direction.values()) {
            if (dir != currentActiveDirection) {
                moveVehiclesInIntersection(dir);
            }
        }

        // Generate new vehicles if continuous generation is enabled
        if (config.isContinuousGeneration()) {
            generateContinuousVehicles(deltaTime);
        }

        // Check if current phase is complete
        TrafficLight activeLight = trafficLights.get(currentActiveDirection);
        if (activeLight.getRemainingTime() <= 0) {
            // Check current color and transition
            if (activeLight.getCurrentColor() == LightColor.GREEN) {
                // Green -> Yellow
                activeLight.setCurrentColor(LightColor.YELLOW);
                activeLight.setRemainingTime(config.getYellowLightDuration());

                // Record green phase completion
                statisticsCollector.recordPhaseCompletion(currentActiveDirection, "GREEN");
            } else if (activeLight.getCurrentColor() == LightColor.YELLOW) {
                // Yellow -> Red, move to next direction
                activeLight.setCurrentColor(LightColor.RED);

                // Record yellow phase completion
                statisticsCollector.recordPhaseCompletion(currentActiveDirection, "YELLOW");

                if (!directionQueue.isEmpty()) {
                    // Determine next direction (can use strategy)
                    currentActiveDirection = directionQueue.poll();
                    activateDirection(currentActiveDirection);
                } else {
                    // Cycle complete
                    completeCycle();
                }
            }
        }
    }

    /**
     * Update wait times for all directions
     */
    private void updateWaitTimes() {
        long currentTime = System.currentTimeMillis();
        for (Direction dir : Direction.values()) {
            if (dir != currentActiveDirection) {
                long lastGreen = lastGreenLightTime.get(dir);
                directionWaitTimes.put(dir, currentTime - lastGreen);
            } else {
                directionWaitTimes.put(dir, 0L);
            }
        }
    }

    /**
     * Generate vehicles continuously based on arrival rates
     */
    private void generateContinuousVehicles(double deltaTime) {
        if (sensorSimulator == null) return;

        Map<Direction, List<Vehicle>> newVehicles =
            sensorSimulator.generateVehiclesAllDirections(deltaTime);

        for (Direction dir : Direction.values()) {
            List<Vehicle> dirNewVehicles = newVehicles.get(dir);
            if (dirNewVehicles != null && !dirNewVehicles.isEmpty()) {
                vehicles.get(dir).addAll(dirNewVehicles);
                vehicleDensity.put(dir, vehicles.get(dir).size());

                // Record new vehicle arrival
                statisticsCollector.recordVehicleArrival(dir, dirNewVehicles.size());
            }
        }
    }

    /**
     * Move vehicles that are already in the intersection (position >= 0)
     */
    private void moveVehiclesInIntersection(Direction direction) {
        List<Vehicle> dirVehicles = vehicles.get(direction);
        double speed = 0.015;

        for (Vehicle vehicle : dirVehicles) {
            if (vehicle.hasCrossed()) continue;

            // Only move vehicles that are already in/past the intersection
            if (vehicle.getPosition() >= 0) {
                vehicle.move(speed);

                // Record if vehicle just crossed
                if (vehicle.hasCrossed()) {
                    statisticsCollector.recordVehicleCrossed(direction, vehicle.getType());
                }
            }
        }
    }

    /**
     * Move vehicles in the specified direction
     */
    private void moveVehicles(Direction direction) {
        List<Vehicle> dirVehicles = vehicles.get(direction);
        double speed = 0.015; // Base speed (1.5% of road per second)
        double minDistance = 0.04; // Minimum distance between vehicles

        // Move vehicles forward (iterate from front to back)
        for (int i = 0; i < dirVehicles.size(); i++) {
            Vehicle vehicle = dirVehicles.get(i);
            if (vehicle.hasCrossed()) continue;

            // Check if vehicle is at stop line and light is not green
            TrafficLight light = trafficLights.get(direction);
            if (vehicle.getPosition() < 0 && light.getCurrentColor() != LightColor.GREEN) {
                continue; // Don't move if waiting at red/yellow
            }

            // Check if blocked by vehicle ahead
            boolean blocked = false;
            if (i > 0) {
                Vehicle ahead = dirVehicles.get(i - 1);
                if (!ahead.hasCrossed()) {
                    double gap = ahead.getPosition() - vehicle.getPosition();
                    if (gap < minDistance) {
                        blocked = true;
                    }
                }
            }

            if (!blocked) {
                vehicle.move(speed);

                // Record if vehicle just crossed
                if (vehicle.hasCrossed() && vehicle.getPosition() >= 1.0) {
                    statisticsCollector.recordVehicleCrossed(direction, vehicle.getType());
                }
            }
        }
    }

    /**
     * Complete the current cycle
     */
    private void completeCycle() {
        cycleActive = false;
        currentActiveDirection = null;

        // Reset all lights to red
        for (TrafficLight light : trafficLights.values()) {
            light.reset();
        }

        // Record cycle completion
        statisticsCollector.recordCycleEnd();

        System.out.println("\n=== CYCLE COMPLETED ===");
        System.out.println("Total time: " + (int)elapsedCycleTime + "s");
        System.out.println("Statistics: " + statisticsCollector.getCycleSummary());
    }

    /**
     * Pause the simulation
     */
    public void pause() {
        cycleActive = false;
    }

    /**
     * Resume the simulation
     */
    public void resume() {
        if (currentActiveDirection != null) {
            cycleActive = true;
        }
    }

    /**
     * Reset the entire simulation
     */
    public void reset() {
        cycleActive = false;
        elapsedCycleTime = 0;
        currentActiveDirection = null;
        directionQueue.clear();
        accumulatedTime = 0;
        nextVehicleId = 1;

        // Reset all traffic lights
        for (TrafficLight light : trafficLights.values()) {
            light.reset();
        }

        // Clear all vehicles
        for (Direction dir : Direction.values()) {
            vehicles.get(dir).clear();
        }

        // Clear densities
        for (Direction dir : Direction.values()) {
            vehicleDensity.put(dir, 0);
        }

        // Reset wait times
        long currentTime = System.currentTimeMillis();
        for (Direction dir : Direction.values()) {
            directionWaitTimes.put(dir, 0L);
            lastGreenLightTime.put(dir, currentTime);
        }

        // Reset statistics
        statisticsCollector.reset();
    }

    // Getters
    public TrafficLight getTrafficLight(Direction direction) {
        return trafficLights.get(direction);
    }

    public List<Vehicle> getVehicles(Direction direction) {
        return new ArrayList<>(vehicles.get(direction));
    }

    public boolean isCycleActive() {
        return cycleActive;
    }

    public int getElapsedCycleTime() {
        return (int) elapsedCycleTime;
    }

    public Direction getCurrentActiveDirection() {
        return currentActiveDirection;
    }

    public Map<Direction, Integer> getAllVehicleDensities() {
        return new EnumMap<>(vehicleDensity);
    }

    public int getVehicleDensity(Direction direction) {
        return vehicleDensity.getOrDefault(direction, 0);
    }

    public ConfigurationManager getConfigManager() {
        return configManager;
    }

    public SimulationConfig getConfig() {
        return config;
    }

    public StatisticsCollector getStatisticsCollector() {
        return statisticsCollector;
    }

    public TrafficControlStrategy getActiveStrategy() {
        return activeStrategy;
    }

    /**
     * Get waiting vehicle count for a direction
     */
    public int getWaitingVehicleCount(Direction direction) {
        List<Vehicle> dirVehicles = vehicles.get(direction);
        return (int) dirVehicles.stream()
            .filter(v -> !v.hasCrossed())
            .count();
    }

    /**
     * Get crossed vehicle count for a direction
     */
    public int getCrossedVehicleCount(Direction direction) {
        List<Vehicle> dirVehicles = vehicles.get(direction);
        return (int) dirVehicles.stream()
            .filter(Vehicle::hasCrossed)
            .count();
    }

    /**
     * Export current statistics to file
     */
    public void exportStatistics(String filename) {
        statisticsCollector.exportToFile(filename);
    }
}

