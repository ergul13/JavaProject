# Technical Documentation
## Traffic Light Control System - Architecture & Implementation

---

## Table of Contents
1. [System Architecture](#system-architecture)
2. [Model Layer Details](#model-layer-details)
3. [View Layer Details](#view-layer-details)
4. [Controller Layer Details](#controller-layer-details)
5. [Algorithm Specifications](#algorithm-specifications)
6. [Code Standards](#code-standards)

---

## System Architecture

### MVC Pattern Implementation

```
┌─────────────────────────────────────────────────────────┐
│                      Main.java                          │
│                  (Entry Point)                          │
└────────────┬──────────────────┬────────────────────────┘
             │                  │
             ▼                  ▼
    ┌────────────────┐  ┌──────────────────┐
    │ Model          │  │ View             │
    │ (Business      │  │ (Presentation)   │
    │  Logic)        │  │                  │
    └────────┬───────┘  └────────┬─────────┘
             │                   │
             └─────────┬─────────┘
                       │
                       ▼
              ┌────────────────┐
              │  Controller    │
              │  (Mediator)    │
              └────────────────┘
```

### Component Responsibilities

#### Model
- Manages intersection state
- Calculates green light durations
- Tracks vehicle positions
- Updates traffic light states
- Implements business rules

#### View
- Renders intersection visualization
- Displays input controls
- Shows countdown timers
- Animates vehicle movement
- Updates UI elements

#### Controller
- Handles user input events
- Synchronizes Model and View
- Manages simulation timeline
- Coordinates state updates

---

## Model Layer Details

### 1. Direction Enum
**Purpose:** Type-safe representation of intersection directions

**Properties:**
- `displayName`: Human-readable name
- `angle`: Rotation angle for visualization (0°, 90°, 180°, 270°)

**Methods:**
- `getOpposite()`: Returns opposite direction (N↔S, E↔W)

**Usage:**
```java
Direction north = Direction.NORTH;
Direction opposite = north.getOpposite(); // Returns SOUTH
String name = north.getDisplayName(); // Returns "North"
```

---

### 2. LightColor Enum
**Purpose:** Traffic light states

**Values:**
- `RED`: Stop
- `YELLOW`: Prepare to stop
- `GREEN`: Go

---

### 3. Vehicle Class
**Purpose:** Represents a vehicle in the system

**Attributes:**
```java
private final Direction direction;    // Which lane the vehicle is in
private double position;               // 0.0 = at stop line, 1.0 = crossed
private boolean hasCrossed;           // Whether vehicle has left intersection
private final int id;                 // Unique identifier
```

**Key Methods:**
- `move(double distance)`: Updates position
- `hasCrossed()`: Checks if vehicle completed crossing

**Position System:**
- Negative values: Vehicle waiting behind stop line
- 0.0: At stop line
- 0.0 - 1.0: Crossing intersection
- ≥ 1.0: Crossed (marked for removal)

---

### 4. TrafficLight Class
**Purpose:** Manages one traffic light

**Attributes:**
```java
private final Direction direction;    // Which direction this light controls
private LightColor currentColor;      // Current light color
private int remainingTime;            // Seconds remaining in current phase
private int greenDuration;            // Allocated green time for this cycle
```

**State Machine:**
```
RED (waiting) → GREEN (duration seconds) → YELLOW (3s) → RED (next cycle)
```

**Key Methods:**
- `tick()`: Decrements remaining time
- `reset()`: Returns light to RED state
- `setGreenDuration(int)`: Allocates green time from calculation

---

### 5. IntersectionManager Class
**Purpose:** Core business logic orchestrator

#### Constants
```java
TOTAL_CYCLE_TIME = 120 seconds
YELLOW_LIGHT_DURATION = 3 seconds
MIN_GREEN_TIME = 10 seconds
MAX_GREEN_TIME = 60 seconds
```

#### Data Structures
```java
Map<Direction, TrafficLight> trafficLights;     // One light per direction
Map<Direction, List<Vehicle>> vehicles;         // Vehicles per direction
Map<Direction, Integer> vehicleDensity;         // Input vehicle counts
Queue<Direction> directionQueue;                // Cycle order: N→E→S→W
```

#### Key Algorithms

##### A. Green Light Duration Calculation
```java
Algorithm: calculateGreenLightDurations()
Input: vehicleDensity map (vehicles per direction)
Output: greenDuration set for each TrafficLight

1. Calculate totalVehicles = sum of all densities
2. If totalVehicles == 0:
   - Assign MIN_GREEN_TIME to all directions
   - Return
3. Calculate available green time:
   availableGreenTime = TOTAL_CYCLE_TIME - (4 × YELLOW_LIGHT_DURATION)
   = 120 - 12 = 108 seconds
4. For each direction:
   a. proportion = vehicleDensity[dir] / totalVehicles
   b. rawGreenTime = proportion × availableGreenTime
   c. constrainedGreenTime = clamp(rawGreenTime, MIN_GREEN_TIME, MAX_GREEN_TIME)
   d. allocatedTime[dir] = constrainedGreenTime
5. Calculate totalAllocated = sum of all allocatedTime values
6. If totalAllocated != availableGreenTime:
   a. difference = availableGreenTime - totalAllocated
   b. Sort directions by density (descending)
   c. Distribute difference proportionally to top directions
   d. Respect min/max constraints during distribution
7. Set greenDuration for each TrafficLight
```

**Example Calculation:**
```
Input Densities:
- North: 20 vehicles
- South: 10 vehicles
- East: 5 vehicles
- West: 5 vehicles
Total: 40 vehicles

Calculations:
- North: (20/40) × 108 = 54s → GREEN for 54s
- South: (10/40) × 108 = 27s → GREEN for 27s
- East: (5/40) × 108 = 13.5s → 14s (rounded)
- West: (5/40) × 108 = 13.5s → 13s (adjusted)
Total: 54 + 27 + 14 + 13 = 108s ✓
```

##### B. Cycle Execution
```java
Algorithm: startCycle()
1. Calculate green light durations
2. Create vehicle objects based on densities
3. Initialize direction queue: [NORTH, EAST, SOUTH, WEST]
4. Activate first direction (NORTH)
5. Set cycleActive = true

Algorithm: update() - Called every second
1. If !cycleActive, return
2. Decrement active light's remaining time
3. Increment elapsed cycle time
4. If light is GREEN:
   - Move vehicles in active direction
5. If remaining time == 0:
   - If current color is GREEN:
     * Transition to YELLOW
     * Set remaining time = 3s
   - Else if current color is YELLOW:
     * Transition to RED
     * Activate next direction from queue
     * If queue is empty:
       > Complete cycle
       > Set cycleActive = false
```

##### C. Vehicle Movement
```java
Algorithm: moveVehicles(Direction dir)
1. Get vehicle list for direction
2. For each vehicle (front to back):
   a. If vehicle.hasCrossed(), skip
   b. Check if blocked by vehicle ahead:
      - If distance < 0.05, blocked = true
   c. If !blocked:
      - vehicle.move(0.02) // 2% of road per second
   d. If vehicle.position >= 1.0:
      - vehicle.setHasCrossed(true)
```

---

## View Layer Details

### TrafficSimulationView Class
**Extends:** `BorderPane` (JavaFX)

#### Layout Structure
```
┌────────────────────────────────────────────────┐
│                    STAGE                       │
│  ┌──────────┬──────────────────┬──────────┐   │
│  │  LEFT    │     CENTER       │          │   │
│  │  PANEL   │                  │          │   │
│  │          │  Canvas          │          │   │
│  │ Inputs   │  700×700px       │          │   │
│  │ Timers   │                  │          │   │
│  │          │  Intersection    │          │   │
│  │          │  Visualization   │          │   │
│  └──────────┴──────────────────┴──────────┘   │
│  ┌────────────────────────────────────────┐   │
│  │          BOTTOM PANEL                  │   │
│  │  [Start] [Pause] [Reset]               │   │
│  │  Status: Simulation Running - 45s      │   │
│  └────────────────────────────────────────┘   │
└────────────────────────────────────────────────┘
```

#### Canvas Coordinate System
```
(0,0) ──────────────────────► X (700)
  │
  │        Road Layout:
  │
  │           ║║║║
  │           ║║║║
  │      ═════╬╬╬╬═════
  │      ═════╬╬╬╬═════
  │           ║║║║
  │           ║║║║
  ▼
  Y (700)

Road Width: 150px
Lane Width: 75px (half of road)
Center: (350, 350)
```

#### Drawing Methods

##### 1. Intersection Rendering
```java
drawIntersection() {
  1. Clear canvas (light gray background)
  2. Draw grass (green) as background
  3. Draw roads:
     - Horizontal: (0, 275) to (700, 425)
     - Vertical: (275, 0) to (425, 700)
  4. Draw center intersection area (darker)
  5. Draw lane dividers (yellow dashed)
  6. Draw stop lines (white solid)
  7. Draw traffic lights for each direction
  8. Draw vehicles
  9. Draw direction labels
}
```

##### 2. Traffic Light Visualization
```java
Position Calculation:
- NORTH: (280, 205) - Above intersection
- SOUTH: (420, 495) - Below intersection
- EAST: (495, 280) - Right of intersection
- WEST: (205, 420) - Left of intersection

Light Colors:
- Active: Full brightness (RED: #FF0000, YELLOW: #FFFF00, GREEN: #00FF00)
- Inactive: Dim (RED: #330000, YELLOW: #333300, GREEN: #003300)
```

##### 3. Vehicle Animation
```java
Vehicle Positioning:
For each direction:
  - Calculate X, Y based on direction and position
  - NORTH: X = center - laneOffset, Y = center - roadWidth/2 - (position × distance)
  - SOUTH: X = center + laneOffset, Y = center + roadWidth/2 + (position × distance)
  - EAST: X = center + roadWidth/2 + (position × distance), Y = center - laneOffset
  - WEST: X = center - roadWidth/2 - (position × distance), Y = center + laneOffset

Vehicle Dimensions:
  - Vertical lanes: 20×35 pixels (width × height)
  - Horizontal lanes: 35×20 pixels (rotated)
  
Vehicle Colors:
  - Body: Blue (#0000FF)
  - Outline: Dark Blue (#00008B)
```

#### Input Validation
```java
getVehicleDensities() {
  For each text field:
    1. Parse integer value
    2. Clamp to range [0, 50]
    3. If invalid, default to 0
  Return Map<Direction, Integer>
}
```

---

## Controller Layer Details

### TrafficController Class

#### Initialization Flow
```java
Constructor(model, view) {
  1. Store model and view references
  2. Initialize Timeline (1 second intervals)
  3. Setup event handlers:
     - Start button → startSimulation()
     - Pause button → pauseOrResume()
     - Reset button → resetSimulation()
     - Random button → generateRandom()
  4. Call initial updateView()
}
```

#### Event Handlers

##### Start Simulation
```java
onStart() {
  1. If simulation already active, return
  2. Get vehicle densities from view input fields
  3. Update model with densities
  4. Call model.startCycle()
  5. Start timeline (1s ticks)
  6. Update view
}
```

##### Pause/Resume
```java
onPause() {
  If model.isCycleActive():
    - Call model.pause()
    - Pause timeline
  Else if model has active direction:
    - Call model.resume()
    - Resume timeline
}
```

##### Reset
```java
onReset() {
  1. Stop timeline
  2. Call model.reset()
  3. Update view (clears everything)
}
```

##### Generate Random
```java
onGenerateRandom() {
  1. Call model.generateRandomDensities()
  2. Get densities from model
  3. Update view input fields
}
```

#### Update Loop
```java
Timeline (every 1 second):
  1. Call model.update()
  2. Call updateView()
  3. If !model.isCycleActive():
     - Stop timeline
```

##### UpdateView Details
```java
updateView() {
  1. For each direction:
     a. Get TrafficLight from model
     b. Update view light color and timer
  2. For each direction:
     a. Get vehicle list from model
     b. Update view vehicle positions
  3. Update status label:
     - Show cycle active status
     - Show elapsed time
     - Show current active direction
}
```

---

## Algorithm Specifications

### Time Complexity Analysis

#### Green Light Calculation: O(n)
- n = number of directions (always 4)
- Single pass through directions for proportion calculation
- Single pass for constraint application
- Single pass for adjustment distribution
- **Total: O(4) = O(1) constant time**

#### Vehicle Movement: O(m)
- m = total number of vehicles
- For each direction: O(vehicles in direction)
- Collision check: O(1) per vehicle (only checks vehicle ahead)
- **Total: O(m) where m is sum of all vehicles**

#### Update Cycle: O(m)
- Traffic light state update: O(1)
- Vehicle movement: O(m)
- **Total: O(m)**

### Space Complexity: O(n + m)
- n = number of directions (4)
- m = number of vehicles
- EnumMaps: O(n)
- Vehicle lists: O(m)
- **Total: O(4 + m) = O(m)**

---

## Code Standards

### Naming Conventions
- **Classes:** PascalCase (e.g., `IntersectionManager`)
- **Methods:** camelCase (e.g., `calculateGreenLightDurations()`)
- **Constants:** UPPER_SNAKE_CASE (e.g., `TOTAL_CYCLE_TIME`)
- **Variables:** camelCase (e.g., `vehicleDensity`)

### Documentation
- All public classes have JavaDoc comments
- Complex algorithms have inline comments
- Method responsibilities clearly stated

### Design Principles Applied
1. **Single Responsibility:** Each class has one clear purpose
2. **Encapsulation:** Internal state protected with private access
3. **Immutability:** Direction and ID fields are final
4. **Type Safety:** Enums instead of strings/integers
5. **Fail-Safe:** Input validation and bounds checking
6. **Readability:** Clear variable names, logical structure

### Error Handling
- Input validation in view layer
- Boundary checks in model layer
- Graceful degradation (e.g., zero vehicles → minimum green time)
- No exceptions thrown to user

---

## Testing Recommendations

### Unit Tests
1. **Model Layer:**
   - Green light calculation with various inputs
   - Vehicle movement logic
   - Traffic light state transitions
   - Edge cases (zero vehicles, max vehicles)

2. **Controller Layer:**
   - Event handler invocation
   - Model-View synchronization
   - Timeline management

### Integration Tests
1. Complete simulation cycle
2. Pause and resume functionality
3. Reset and restart
4. Random generation consistency

### UI Tests
1. Input validation
2. Button state management
3. Visual rendering accuracy
4. Animation smoothness

---

## Performance Considerations

### Optimization Techniques
1. **EnumMap Usage:** Faster than HashMap for enum keys
2. **Canvas Rendering:** Direct drawing faster than node-based approach
3. **Single Timeline:** One timer for entire simulation
4. **Incremental Updates:** Only redraw when state changes
5. **Object Reuse:** Vehicles stay in lists until crossed

### Scalability Limits
- **Max Vehicles per Direction:** 50 (UI constraint)
- **Total Max Vehicles:** 200 (4 × 50)
- **Frame Rate:** 1 FPS (adequate for simulation)
- **Memory:** ~10KB per 100 vehicles (minimal)

### Future Optimization Possibilities
1. Multi-threading for vehicle calculations
2. Sprite-based vehicle rendering
3. Delta time for smoother animation
4. Spatial partitioning for collision detection

---

## Conclusion

This implementation demonstrates:
- ✅ Strict MVC architecture
- ✅ Clean separation of concerns
- ✅ Efficient algorithms
- ✅ Comprehensive visualization
- ✅ User-friendly interface
- ✅ Robust state management
- ✅ Proportional time allocation
- ✅ No third-party dependencies

**Total Lines of Code:** ~1,200
**Cyclomatic Complexity:** Low-Medium
**Maintainability Index:** High
**Test Coverage Potential:** >80%

---

**Document Version:** 1.0  
**Last Updated:** January 6, 2026  
**Author:** Senior Java Developer

