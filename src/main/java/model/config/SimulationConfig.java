package model.config;

import model.Direction;
import java.util.*;

/**
 * Configuration holder for simulation parameters
 * Encapsulates all configurable settings
 */
public class SimulationConfig {

    // Timing parameters
    private int totalCycleTime = 120; // seconds
    private int yellowLightDuration = 3; // seconds
    private int minGreenTime = 10; // seconds
    private int maxGreenTime = 60; // seconds

    // Vehicle generation parameters
    private boolean continuousGeneration = false;
    private double arrivalRateLambda = 5.0; // vehicles per minute (Poisson distribution)
    private Map<Direction, Double> directionArrivalRates = new EnumMap<>(Direction.class);

    // Vehicle type distribution (percentages, must sum to 100)
    private double regularVehiclePercentage = 85.0;
    private double busPercentage = 10.0;
    private double motorcyclePercentage = 5.0;
    private double emergencyPercentage = 0.5;

    // Strategy selection
    private String activeStrategy = "DensityBased"; // FixedTime, DensityBased, Adaptive, Emergency

    // Emergency vehicle settings
    private double emergencyProbabilityPerCycle = 0.05; // 5% chance per cycle
    private boolean autoEmergencyGeneration = false;

    // Performance settings
    private int updateFrequency = 1; // Update UI every N seconds
    private int maxVehiclesPerDirection = 100;
    private boolean animationEnabled = true;

    // Statistics settings
    private boolean collectStatistics = true;
    private int statisticsWindowSize = 50; // Keep last N cycles

    public SimulationConfig() {
        // Initialize default arrival rates per direction
        for (Direction dir : Direction.values()) {
            directionArrivalRates.put(dir, arrivalRateLambda);
        }
    }

    // Getters and Setters

    public int getTotalCycleTime() {
        return totalCycleTime;
    }

    public void setTotalCycleTime(int totalCycleTime) {
        this.totalCycleTime = Math.max(60, Math.min(300, totalCycleTime));
    }

    public int getYellowLightDuration() {
        return yellowLightDuration;
    }

    public void setYellowLightDuration(int yellowLightDuration) {
        this.yellowLightDuration = Math.max(2, Math.min(5, yellowLightDuration));
    }

    public int getMinGreenTime() {
        return minGreenTime;
    }

    public void setMinGreenTime(int minGreenTime) {
        this.minGreenTime = Math.max(5, Math.min(30, minGreenTime));
    }

    public int getMaxGreenTime() {
        return maxGreenTime;
    }

    public void setMaxGreenTime(int maxGreenTime) {
        this.maxGreenTime = Math.max(30, Math.min(120, maxGreenTime));
    }

    public boolean isContinuousGeneration() {
        return continuousGeneration;
    }

    public void setContinuousGeneration(boolean continuousGeneration) {
        this.continuousGeneration = continuousGeneration;
    }

    public double getArrivalRateLambda() {
        return arrivalRateLambda;
    }

    public void setArrivalRateLambda(double arrivalRateLambda) {
        this.arrivalRateLambda = Math.max(0.1, Math.min(20.0, arrivalRateLambda));
    }

    public Map<Direction, Double> getDirectionArrivalRates() {
        return new EnumMap<>(directionArrivalRates);
    }

    public void setDirectionArrivalRate(Direction direction, double rate) {
        directionArrivalRates.put(direction, Math.max(0.1, Math.min(20.0, rate)));
    }

    public double getRegularVehiclePercentage() {
        return regularVehiclePercentage;
    }

    public void setRegularVehiclePercentage(double regularVehiclePercentage) {
        this.regularVehiclePercentage = Math.max(0, Math.min(100, regularVehiclePercentage));
    }

    public double getBusPercentage() {
        return busPercentage;
    }

    public void setBusPercentage(double busPercentage) {
        this.busPercentage = Math.max(0, Math.min(100, busPercentage));
    }

    public double getMotorcyclePercentage() {
        return motorcyclePercentage;
    }

    public void setMotorcyclePercentage(double motorcyclePercentage) {
        this.motorcyclePercentage = Math.max(0, Math.min(100, motorcyclePercentage));
    }

    public double getEmergencyPercentage() {
        return emergencyPercentage;
    }

    public void setEmergencyPercentage(double emergencyPercentage) {
        this.emergencyPercentage = Math.max(0, Math.min(10, emergencyPercentage));
    }

    public String getActiveStrategy() {
        return activeStrategy;
    }

    public void setActiveStrategy(String activeStrategy) {
        this.activeStrategy = activeStrategy;
    }

    public double getEmergencyProbabilityPerCycle() {
        return emergencyProbabilityPerCycle;
    }

    public void setEmergencyProbabilityPerCycle(double emergencyProbabilityPerCycle) {
        this.emergencyProbabilityPerCycle = Math.max(0, Math.min(1.0, emergencyProbabilityPerCycle));
    }

    public boolean isAutoEmergencyGeneration() {
        return autoEmergencyGeneration;
    }

    public void setAutoEmergencyGeneration(boolean autoEmergencyGeneration) {
        this.autoEmergencyGeneration = autoEmergencyGeneration;
    }

    public int getUpdateFrequency() {
        return updateFrequency;
    }

    public void setUpdateFrequency(int updateFrequency) {
        this.updateFrequency = Math.max(1, Math.min(10, updateFrequency));
    }

    public int getMaxVehiclesPerDirection() {
        return maxVehiclesPerDirection;
    }

    public void setMaxVehiclesPerDirection(int maxVehiclesPerDirection) {
        this.maxVehiclesPerDirection = Math.max(10, Math.min(500, maxVehiclesPerDirection));
    }

    public boolean isAnimationEnabled() {
        return animationEnabled;
    }

    public void setAnimationEnabled(boolean animationEnabled) {
        this.animationEnabled = animationEnabled;
    }

    public boolean isCollectStatistics() {
        return collectStatistics;
    }

    public void setCollectStatistics(boolean collectStatistics) {
        this.collectStatistics = collectStatistics;
    }

    public int getStatisticsWindowSize() {
        return statisticsWindowSize;
    }

    public void setStatisticsWindowSize(int statisticsWindowSize) {
        this.statisticsWindowSize = Math.max(10, Math.min(200, statisticsWindowSize));
    }

    /**
     * Create a copy of this configuration
     */
    public SimulationConfig copy() {
        SimulationConfig copy = new SimulationConfig();
        copy.totalCycleTime = this.totalCycleTime;
        copy.yellowLightDuration = this.yellowLightDuration;
        copy.minGreenTime = this.minGreenTime;
        copy.maxGreenTime = this.maxGreenTime;
        copy.continuousGeneration = this.continuousGeneration;
        copy.arrivalRateLambda = this.arrivalRateLambda;
        copy.directionArrivalRates = new EnumMap<>(this.directionArrivalRates);
        copy.regularVehiclePercentage = this.regularVehiclePercentage;
        copy.busPercentage = this.busPercentage;
        copy.motorcyclePercentage = this.motorcyclePercentage;
        copy.emergencyPercentage = this.emergencyPercentage;
        copy.activeStrategy = this.activeStrategy;
        copy.emergencyProbabilityPerCycle = this.emergencyProbabilityPerCycle;
        copy.autoEmergencyGeneration = this.autoEmergencyGeneration;
        copy.updateFrequency = this.updateFrequency;
        copy.maxVehiclesPerDirection = this.maxVehiclesPerDirection;
        copy.animationEnabled = this.animationEnabled;
        copy.collectStatistics = this.collectStatistics;
        copy.statisticsWindowSize = this.statisticsWindowSize;
        return copy;
    }

    @Override
    public String toString() {
        return String.format("SimulationConfig[cycleTime=%ds, strategy=%s, continuousGen=%b]",
                totalCycleTime, activeStrategy, continuousGeneration);
    }
}

