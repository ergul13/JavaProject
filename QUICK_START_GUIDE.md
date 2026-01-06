# 🚀 Quick Start Guide - Enhanced Traffic Light Control System

## 📋 Table of Contents
1. [Quick Start](#quick-start)
2. [Strategy Selection](#strategy-selection)
3. [Configuration](#configuration)
4. [Statistics & Analytics](#statistics--analytics)
5. [Advanced Features](#advanced-features)

---

## ⚡ Quick Start

### Run the Application
```bash
# Using Maven
mvn clean javafx:run

# Or run Main.java directly
# JavaFX must be configured in your IDE
```

### Basic Simulation (Using GUI)
1. **Launch the application**
2. **Enter vehicle counts** for each direction (0-50)
3. **Click "Start"** to begin simulation
4. **Watch vehicles move** through intersection
5. **Click "Reset"** to try again

### Basic Simulation (Programmatically)
```java
IntersectionManager manager = new IntersectionManager();

// Set densities
manager.setVehicleDensity(Direction.NORTH, 20);
manager.setVehicleDensity(Direction.SOUTH, 15);
manager.setVehicleDensity(Direction.EAST, 10);
manager.setVehicleDensity(Direction.WEST, 25);

// Start cycle
manager.startCycle();

// Update loop (in your JavaFX Timeline)
manager.update();  // Call every second
```

---

## 🎯 Strategy Selection

### Available Strategies

#### 1. Density-Based (Default) ⭐
**When to use:** Balanced traffic, static conditions

```java
manager.setActiveStrategy("DensityBased");
```

**How it works:**
- Allocates time proportional to vehicle count
- Min 10s, Max 60s per direction
- Original algorithm from base system

#### 2. Fixed-Time
**When to use:** Predictable patterns, testing baseline

```java
manager.setActiveStrategy("FixedTime");
```

**How it works:**
- Equal time for all directions
- Simple round-robin
- Ignores vehicle density

#### 3. Adaptive
**When to use:** Dynamic traffic, unbalanced loads

```java
manager.setActiveStrategy("Adaptive");
```

**How it works:**
- Considers both density AND wait time
- Formula: (density × 0.7) + (waitTime × 0.3)
- Prevents direction starvation

#### 4. Emergency Priority
**When to use:** Emergency vehicle scenarios

```java
manager.setActiveStrategy("Emergency");
```

**How it works:**
- Detects emergency vehicles
- Immediate priority switching
- Extended green time (45s)

### List Available Strategies
```java
List<String> strategies = manager.getAvailableStrategies();
System.out.println("Available: " + strategies);
// Output: [FixedTime, DensityBased, Adaptive, Emergency]
```

### Get Active Strategy
```java
String current = manager.getActiveStrategyName();
System.out.println("Current: " + current);
```

---

## ⚙️ Configuration

### Using Default Configuration
```java
// Uses built-in defaults
IntersectionManager manager = new IntersectionManager();
```

### Load Custom Configuration
```java
ConfigurationManager configMgr = new ConfigurationManager();
configMgr.loadConfig("my_config.properties");

IntersectionManager manager = new IntersectionManager(configMgr);
```

### Create Configuration File
Create `traffic_config.properties`:

```properties
# Basic Timing
totalCycleTime=120
yellowLightDuration=3
minGreenTime=10
maxGreenTime=60

# Strategy
activeStrategy=DensityBased

# Vehicle Distribution (must sum to 100%)
regularVehiclePercentage=85.0
busPercentage=10.0
motorcyclePercentage=5.0
emergencyPercentage=0.5

# Continuous Generation
continuousGeneration=false
arrivalRateLambda=5.0

# Per-Direction Arrival Rates (vehicles per minute)
arrivalRate.NORTH=5.0
arrivalRate.EAST=4.0
arrivalRate.SOUTH=6.0
arrivalRate.WEST=3.0

# Emergency Vehicles
autoEmergencyGeneration=false
emergencyProbabilityPerCycle=0.05

# Performance
updateFrequency=60
```

### Modify Configuration at Runtime
```java
SimulationConfig config = manager.getConfig();
config.setTotalCycleTime(150);
config.setMinGreenTime(15);
config.setActiveStrategy("Adaptive");
```

### Save Configuration
```java
ConfigurationManager configMgr = manager.getConfigManager();
configMgr.saveConfig("saved_config.properties");
```

---

## 📊 Statistics & Analytics

### Basic Statistics
```java
StatisticsCollector stats = manager.getStatisticsCollector();

// Get totals
int totalVehicles = stats.getTotalVehiclesProcessed();
int emergencyCount = stats.getEmergencyVehiclesProcessed();

System.out.println("Processed: " + totalVehicles + " vehicles");
System.out.println("Emergency: " + emergencyCount + " vehicles");
```

### Wait Time Analysis
```java
StatisticsCollector stats = manager.getStatisticsCollector();

// Average wait time (all vehicles)
double avgWait = stats.calculateAverageWaitTime() / 1000.0; // Convert to seconds
System.out.println("Avg Wait: " + avgWait + "s");

// Median wait time
double medianWait = stats.calculateMedianWaitTime() / 1000.0;
System.out.println("Median Wait: " + medianWait + "s");

// 95th percentile
double p95 = stats.calculate95thPercentileWaitTime() / 1000.0;
System.out.println("95th Percentile: " + p95 + "s");

// Per direction
double northWait = stats.calculateAverageWaitTime(Direction.NORTH) / 1000.0;
System.out.println("North Avg Wait: " + northWait + "s");
```

### Throughput Analysis
```java
double throughput = stats.calculateAverageThroughput();
System.out.println("Throughput: " + throughput + " vehicles/minute");
```

### Direction-Specific Stats
```java
Map<Direction, Integer> perDirection = stats.getVehiclesPerDirection();
for (Direction dir : Direction.values()) {
    int count = perDirection.get(dir);
    System.out.println(dir + ": " + count + " vehicles");
}
```

### Vehicle Type Stats
```java
Map<VehicleType, Integer> perType = stats.getVehiclesPerType();
for (VehicleType type : VehicleType.values()) {
    int count = perType.get(type);
    System.out.println(type.getDisplayName() + ": " + count);
}
```

### Generate Report
```java
String report = stats.generateSummaryReport();
System.out.println(report);
```

**Output Example:**
```
=== SIMULATION STATISTICS SUMMARY ===
Total Cycles: 5
Total Vehicles Processed: 180
Emergency Vehicles: 2
Average Wait Time: 12.45 seconds
Median Wait Time: 10.20 seconds
95th Percentile Wait Time: 28.50 seconds
Average Throughput: 36.00 vehicles/minute

Vehicles by Direction:
  NORTH: 45 vehicles (avg wait 11.2s)
  EAST: 38 vehicles (avg wait 13.1s)
  SOUTH: 52 vehicles (avg wait 12.8s)
  WEST: 45 vehicles (avg wait 12.5s)

Vehicles by Type:
  Regular Car: 153
  Bus: 18
  Motorcycle: 7
  Emergency Vehicle: 2
```

### Export Statistics to File
```java
manager.exportStatistics("simulation_results.txt");
System.out.println("Statistics exported!");
```

### Cycle History
```java
List<CycleStatistics> history = stats.getCycleHistory();
for (CycleStatistics cycle : history) {
    System.out.printf("Cycle %d: %d vehicles, %.1f veh/min%n",
        cycle.cycleNumber,
        cycle.getTotalVehicles(),
        cycle.throughput);
}

// Get recent cycles only
List<CycleStatistics> recent = stats.getRecentCycleHistory(3);
```

---

## 🎨 Advanced Features

### 1. Random Density Generation
```java
manager.generateRandomDensities();
Map<Direction, Integer> densities = manager.getAllVehicleDensities();
System.out.println("Generated: " + densities);
```

### 2. Query Vehicle Counts
```java
// Waiting vehicles (not yet crossed)
int waiting = manager.getWaitingVehicleCount(Direction.NORTH);

// Crossed vehicles
int crossed = manager.getCrossedVehicleCount(Direction.NORTH);

// Total for direction
int total = manager.getVehicleDensity(Direction.NORTH);

System.out.println("North: " + waiting + " waiting, " + crossed + " crossed");
```

### 3. Cycle Control
```java
// Start cycle
manager.startCycle();

// Pause
manager.pause();

// Resume
manager.resume();

// Reset everything
manager.reset();
```

### 4. Check Cycle Status
```java
boolean active = manager.isCycleActive();
int elapsed = manager.getElapsedCycleTime();
Direction current = manager.getCurrentActiveDirection();

System.out.println("Active: " + active);
System.out.println("Elapsed: " + elapsed + "s");
System.out.println("Current: " + current);
```

### 5. Access Traffic Lights
```java
TrafficLight northLight = manager.getTrafficLight(Direction.NORTH);
LightColor color = northLight.getCurrentColor();
int remaining = northLight.getRemainingTime();
int greenDuration = northLight.getGreenDuration();

System.out.println("North Light: " + color + " (" + remaining + "s remaining)");
```

### 6. Access Vehicles
```java
List<Vehicle> northVehicles = manager.getVehicles(Direction.NORTH);
for (Vehicle v : northVehicles) {
    System.out.printf("Vehicle %d: %.2f position, %s type%n",
        v.getId(), v.getPosition(), v.getType().getDisplayName());
}
```

### 7. Continuous Vehicle Generation
```java
SimulationConfig config = manager.getConfig();
config.setContinuousGeneration(true);
config.setArrivalRateLambda(5.0);  // 5 vehicles per minute average

// Set per-direction rates
config.setDirectionArrivalRate(Direction.NORTH, 6.0);
config.setDirectionArrivalRate(Direction.SOUTH, 4.0);

// Vehicles will now be generated continuously during simulation
manager.startCycle();
```

### 8. Emergency Vehicle Scenarios
```java
// Enable auto emergency generation
SimulationConfig config = manager.getConfig();
config.setAutoEmergencyGeneration(true);
config.setEmergencyProbabilityPerCycle(0.1);  // 10% chance per cycle

// Use emergency strategy
manager.setActiveStrategy("Emergency");
```

---

## 🎓 Complete Example

```java
import model.*;
import model.config.*;
import model.analytics.*;

public class TrafficSimulationExample {
    public static void main(String[] args) {
        // 1. Create manager with custom config
        ConfigurationManager configMgr = new ConfigurationManager();
        configMgr.loadConfig("my_config.properties");
        IntersectionManager manager = new IntersectionManager(configMgr);
        
        // 2. Select strategy
        manager.setActiveStrategy("Adaptive");
        System.out.println("Using: " + manager.getActiveStrategyName());
        
        // 3. Set vehicle densities
        manager.setVehicleDensity(Direction.NORTH, 30);
        manager.setVehicleDensity(Direction.SOUTH, 20);
        manager.setVehicleDensity(Direction.EAST, 15);
        manager.setVehicleDensity(Direction.WEST, 25);
        
        // 4. Start simulation
        manager.startCycle();
        
        // 5. Simulation loop (would be in JavaFX Timeline)
        while (manager.isCycleActive()) {
            manager.update();
            
            // Print status
            Direction current = manager.getCurrentActiveDirection();
            int elapsed = manager.getElapsedCycleTime();
            System.out.printf("Time: %ds, Active: %s%n", elapsed, current);
            
            // Sleep to simulate real-time
            try { Thread.sleep(1000); } catch (InterruptedException e) {}
        }
        
        // 6. Get statistics
        StatisticsCollector stats = manager.getStatisticsCollector();
        System.out.println("\n" + stats.generateSummaryReport());
        
        // 7. Export results
        manager.exportStatistics("results.txt");
        
        System.out.println("\n✅ Simulation complete!");
    }
}
```

---

## 📞 Need Help?

1. **Check documentation:** `ENHANCED_ARCHITECTURE.md`
2. **Review examples:** This guide
3. **Read JavaDoc:** All classes fully documented
4. **Examine strategies:** `model/strategy/` package

---

## 🎯 Tips & Best Practices

### For Testing
- Use `FixedTime` strategy for baseline comparison
- Start with small vehicle counts (5-10 per direction)
- Export statistics for each test run
- Compare different strategies with same inputs

### For Demonstration
- Use `generateRandomDensities()` for variety
- Try `Adaptive` strategy for realistic behavior
- Show emergency vehicle priority
- Display statistics report at end

### For Development
- Extend `TrafficControlStrategy` for new algorithms
- Add custom metrics to `StatisticsCollector`
- Create configuration profiles for scenarios
- Use getters for monitoring state

---

**Ready to run!** 🚀 Start with the GUI or use the programmatic API.

