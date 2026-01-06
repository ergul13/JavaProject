package model.config;

import model.Direction;
import java.io.*;
import java.nio.file.*;
import java.util.Properties;

/**
 * Manages loading and saving of simulation configurations
 * Uses Java Properties for simple file persistence
 */
public class ConfigurationManager {

    private static final String DEFAULT_CONFIG_FILE = "traffic_simulation.properties";
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.trafficsim";

    private SimulationConfig currentConfig;

    public ConfigurationManager() {
        this.currentConfig = new SimulationConfig();
    }

    /**
     * Get current configuration
     */
    public SimulationConfig getConfig() {
        return currentConfig;
    }

    /**
     * Set configuration
     */
    public void setConfig(SimulationConfig config) {
        this.currentConfig = config;
    }

    /**
     * Load configuration from default file
     */
    public boolean loadConfig() {
        return loadConfig(getDefaultConfigPath());
    }

    /**
     * Load configuration from specified file
     */
    public boolean loadConfig(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                System.out.println("Config file not found: " + filePath);
                return false;
            }

            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
            }

            // Load properties into config
            currentConfig.setTotalCycleTime(
                Integer.parseInt(props.getProperty("totalCycleTime", "120")));
            currentConfig.setYellowLightDuration(
                Integer.parseInt(props.getProperty("yellowLightDuration", "3")));
            currentConfig.setMinGreenTime(
                Integer.parseInt(props.getProperty("minGreenTime", "10")));
            currentConfig.setMaxGreenTime(
                Integer.parseInt(props.getProperty("maxGreenTime", "60")));

            currentConfig.setContinuousGeneration(
                Boolean.parseBoolean(props.getProperty("continuousGeneration", "false")));
            currentConfig.setArrivalRateLambda(
                Double.parseDouble(props.getProperty("arrivalRateLambda", "5.0")));

            // Direction-specific arrival rates
            for (Direction dir : Direction.values()) {
                String key = "arrivalRate." + dir.name();
                if (props.containsKey(key)) {
                    currentConfig.setDirectionArrivalRate(dir,
                        Double.parseDouble(props.getProperty(key)));
                }
            }

            currentConfig.setRegularVehiclePercentage(
                Double.parseDouble(props.getProperty("regularVehiclePercentage", "85.0")));
            currentConfig.setBusPercentage(
                Double.parseDouble(props.getProperty("busPercentage", "10.0")));
            currentConfig.setMotorcyclePercentage(
                Double.parseDouble(props.getProperty("motorcyclePercentage", "5.0")));
            currentConfig.setEmergencyPercentage(
                Double.parseDouble(props.getProperty("emergencyPercentage", "0.5")));

            currentConfig.setActiveStrategy(
                props.getProperty("activeStrategy", "DensityBased"));

            currentConfig.setEmergencyProbabilityPerCycle(
                Double.parseDouble(props.getProperty("emergencyProbabilityPerCycle", "0.05")));
            currentConfig.setAutoEmergencyGeneration(
                Boolean.parseBoolean(props.getProperty("autoEmergencyGeneration", "false")));

            currentConfig.setUpdateFrequency(
                Integer.parseInt(props.getProperty("updateFrequency", "1")));
            currentConfig.setMaxVehiclesPerDirection(
                Integer.parseInt(props.getProperty("maxVehiclesPerDirection", "100")));
            currentConfig.setAnimationEnabled(
                Boolean.parseBoolean(props.getProperty("animationEnabled", "true")));

            currentConfig.setCollectStatistics(
                Boolean.parseBoolean(props.getProperty("collectStatistics", "true")));
            currentConfig.setStatisticsWindowSize(
                Integer.parseInt(props.getProperty("statisticsWindowSize", "50")));

            System.out.println("Configuration loaded from: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("Error loading configuration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Save current configuration to default file
     */
    public boolean saveConfig() {
        return saveConfig(getDefaultConfigPath());
    }

    /**
     * Save configuration to specified file
     */
    public boolean saveConfig(String filePath) {
        try {
            // Create config directory if it doesn't exist
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            Properties props = new Properties();

            // Save all configuration properties
            props.setProperty("totalCycleTime", String.valueOf(currentConfig.getTotalCycleTime()));
            props.setProperty("yellowLightDuration", String.valueOf(currentConfig.getYellowLightDuration()));
            props.setProperty("minGreenTime", String.valueOf(currentConfig.getMinGreenTime()));
            props.setProperty("maxGreenTime", String.valueOf(currentConfig.getMaxGreenTime()));

            props.setProperty("continuousGeneration", String.valueOf(currentConfig.isContinuousGeneration()));
            props.setProperty("arrivalRateLambda", String.valueOf(currentConfig.getArrivalRateLambda()));

            // Direction-specific arrival rates
            for (Direction dir : Direction.values()) {
                double rate = currentConfig.getDirectionArrivalRates().get(dir);
                props.setProperty("arrivalRate." + dir.name(), String.valueOf(rate));
            }

            props.setProperty("regularVehiclePercentage", String.valueOf(currentConfig.getRegularVehiclePercentage()));
            props.setProperty("busPercentage", String.valueOf(currentConfig.getBusPercentage()));
            props.setProperty("motorcyclePercentage", String.valueOf(currentConfig.getMotorcyclePercentage()));
            props.setProperty("emergencyPercentage", String.valueOf(currentConfig.getEmergencyPercentage()));

            props.setProperty("activeStrategy", currentConfig.getActiveStrategy());

            props.setProperty("emergencyProbabilityPerCycle", String.valueOf(currentConfig.getEmergencyProbabilityPerCycle()));
            props.setProperty("autoEmergencyGeneration", String.valueOf(currentConfig.isAutoEmergencyGeneration()));

            props.setProperty("updateFrequency", String.valueOf(currentConfig.getUpdateFrequency()));
            props.setProperty("maxVehiclesPerDirection", String.valueOf(currentConfig.getMaxVehiclesPerDirection()));
            props.setProperty("animationEnabled", String.valueOf(currentConfig.isAnimationEnabled()));

            props.setProperty("collectStatistics", String.valueOf(currentConfig.isCollectStatistics()));
            props.setProperty("statisticsWindowSize", String.valueOf(currentConfig.getStatisticsWindowSize()));

            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Traffic Simulation Configuration");
            }

            System.out.println("Configuration saved to: " + filePath);
            return true;

        } catch (Exception e) {
            System.err.println("Error saving configuration: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset to default configuration
     */
    public void resetToDefaults() {
        this.currentConfig = new SimulationConfig();
        System.out.println("Configuration reset to defaults");
    }

    /**
     * Get default configuration file path
     */
    private String getDefaultConfigPath() {
        return CONFIG_DIR + File.separator + DEFAULT_CONFIG_FILE;
    }

    /**
     * Export configuration to a specific file (for backup/sharing)
     */
    public boolean exportConfig(File file) {
        return saveConfig(file.getAbsolutePath());
    }

    /**
     * Import configuration from a specific file
     */
    public boolean importConfig(File file) {
        return loadConfig(file.getAbsolutePath());
    }
}

