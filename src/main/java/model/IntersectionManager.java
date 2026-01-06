package model;

import java.util.*;

/**
 * Core model class managing the intersection logic
 * Handles traffic lights, vehicles, and timing calculations
 */
public class IntersectionManager {
    // Constants
    public static final int TOTAL_CYCLE_TIME = 120; // seconds
    public static final int YELLOW_LIGHT_DURATION = 3; // seconds
    public static final int MIN_GREEN_TIME = 10; // seconds
    public static final int MAX_GREEN_TIME = 60; // seconds

    // Traffic lights for each direction
    private final Map<Direction, TrafficLight> trafficLights;

    // Vehicles waiting at each direction
    private final Map<Direction, List<Vehicle>> vehicles;

    // Vehicle density (count) for each direction
    private final Map<Direction, Integer> vehicleDensity;

    // Current active direction
    private Direction currentActiveDirection;

    // Cycle state
    private boolean cycleActive;
    private int elapsedCycleTime;
    private Queue<Direction> directionQueue;

    // Vehicle ID counter
    private int nextVehicleId;

    public IntersectionManager() {
        this.trafficLights = new EnumMap<>(Direction.class);
        this.vehicles = new EnumMap<>(Direction.class);
        this.vehicleDensity = new EnumMap<>(Direction.class);
        this.directionQueue = new LinkedList<>();
        this.nextVehicleId = 0;

        // Initialize traffic lights and collections for each direction
        for (Direction dir : Direction.values()) {
            trafficLights.put(dir, new TrafficLight(dir));
            vehicles.put(dir, new ArrayList<>());
            vehicleDensity.put(dir, 0);
        }

        this.cycleActive = false;
        this.elapsedCycleTime = 0;
        this.currentActiveDirection = null;
    }

    /**
     * Set vehicle density for a specific direction
     */
    public void setVehicleDensity(Direction direction, int count) {
        vehicleDensity.put(direction, Math.max(0, count));
    }

    /**
     * Get vehicle density for a specific direction
     */
    public int getVehicleDensity(Direction direction) {
        return vehicleDensity.get(direction);
    }

    /**
     * Generate random vehicle densities for all directions
     */
    public void generateRandomDensities() {
        Random random = new Random();
        for (Direction dir : Direction.values()) {
            int count = random.nextInt(20) + 5; // 5 to 24 vehicles
            vehicleDensity.put(dir, count);
        }
    }

    /**
     * Calculate green light durations based on vehicle density
     * Algorithm: Proportional allocation with min/max constraints
     */
    public void calculateGreenLightDurations() {
        // Calculate total vehicles
        int totalVehicles = vehicleDensity.values().stream()
                .mapToInt(Integer::intValue)
                .sum();

        if (totalVehicles == 0) {
            // No vehicles, equal distribution
            for (Direction dir : Direction.values()) {
                trafficLights.get(dir).setGreenDuration(MIN_GREEN_TIME);
            }
            return;
        }

        // Available time for green lights (total - yellows)
        int availableGreenTime = TOTAL_CYCLE_TIME - (Direction.values().length * YELLOW_LIGHT_DURATION);

        // First pass: proportional allocation
        Map<Direction, Integer> allocatedTime = new EnumMap<>(Direction.class);
        int totalAllocated = 0;

        for (Direction dir : Direction.values()) {
            double proportion = (double) vehicleDensity.get(dir) / totalVehicles;
            int greenTime = (int) Math.round(proportion * availableGreenTime);

            // Apply constraints
            greenTime = Math.max(MIN_GREEN_TIME, Math.min(MAX_GREEN_TIME, greenTime));

            allocatedTime.put(dir, greenTime);
            totalAllocated += greenTime;
        }

        // Adjust if total doesn't match available time
        int difference = availableGreenTime - totalAllocated;
        if (difference != 0) {
            // Distribute difference proportionally to directions with most vehicles
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

        // Set calculated durations
        for (Direction dir : Direction.values()) {
            trafficLights.get(dir).setGreenDuration(allocatedTime.get(dir));
        }
    }

    /**
     * Start the simulation cycle
     */
    public void startCycle() {
        if (cycleActive) return;

        // Calculate green light durations
        calculateGreenLightDurations();

        // Create vehicles based on density
        createVehicles();

        // Build direction queue (N -> E -> S -> W)
        directionQueue.clear();
        directionQueue.add(Direction.NORTH);
        directionQueue.add(Direction.EAST);
        directionQueue.add(Direction.SOUTH);
        directionQueue.add(Direction.WEST);

        // Start with first direction
        currentActiveDirection = directionQueue.poll();
        activateDirection(currentActiveDirection);

        cycleActive = true;
        elapsedCycleTime = 0;
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
                // Position vehicles in a queue (0.0 = at stop line, negative = behind)
                double position = -0.05 * (count - i - 1);
                Vehicle vehicle = new Vehicle(dir, nextVehicleId++, position);
                dirVehicles.add(vehicle);
            }
        }
    }

    /**
     * Activate a specific direction (set to green)
     */
    private void activateDirection(Direction direction) {
        if (direction == null) return;

        TrafficLight light = trafficLights.get(direction);
        light.setCurrentColor(LightColor.GREEN);
        light.setRemainingTime(light.getGreenDuration());
    }

    /**
     * Update simulation state (called every second)
     */
    public void update() {
        if (!cycleActive || currentActiveDirection == null) return;

        TrafficLight activeLight = trafficLights.get(currentActiveDirection);
        activeLight.tick();
        elapsedCycleTime++;

        // Move vehicles if green light
        if (activeLight.getCurrentColor() == LightColor.GREEN) {
            moveVehicles(currentActiveDirection);
        }

        // Check if current phase is complete
        if (activeLight.getRemainingTime() <= 0) {
            // Check current color and transition
            if (activeLight.getCurrentColor() == LightColor.GREEN) {
                // Green -> Yellow
                activeLight.setCurrentColor(LightColor.YELLOW);
                activeLight.setRemainingTime(YELLOW_LIGHT_DURATION);
            } else if (activeLight.getCurrentColor() == LightColor.YELLOW) {
                // Yellow -> Red, move to next direction
                activeLight.setCurrentColor(LightColor.RED);

                if (!directionQueue.isEmpty()) {
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
     * Move vehicles in the specified direction
     */
    private void moveVehicles(Direction direction) {
        List<Vehicle> dirVehicles = vehicles.get(direction);

        // Move vehicles forward
        for (int i = 0; i < dirVehicles.size(); i++) {
            Vehicle vehicle = dirVehicles.get(i);
            if (vehicle.hasCrossed()) continue;

            // Calculate movement speed
            double speed = 0.02; // Base speed

            // Check if blocked by vehicle ahead
            boolean blocked = false;
            if (i > 0) {
                Vehicle ahead = dirVehicles.get(i - 1);
                if (!ahead.hasCrossed() && ahead.getPosition() - vehicle.getPosition() < 0.05) {
                    blocked = true;
                }
            }

            if (!blocked) {
                vehicle.move(speed);
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
        return elapsedCycleTime;
    }

    public Direction getCurrentActiveDirection() {
        return currentActiveDirection;
    }

    public Map<Direction, Integer> getAllVehicleDensities() {
        return new EnumMap<>(vehicleDensity);
    }
}

