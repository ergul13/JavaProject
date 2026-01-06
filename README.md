# Traffic Light Control System Based on Vehicle Density

## Project Overview
A smart traffic light simulation system for a 4-way intersection (North, South, East, West) that adjusts green light durations based on vehicle density.

**Tech Stack:** Java SE 21, JavaFX, MVC Design Pattern  
**Developer:** Senior Java Developer  
**Constraints:** NO third-party libraries, Strict MVC architecture

## Architecture

### Model-View-Controller (MVC) Pattern

#### Model Layer (`model` package)
- **Direction.java** - Enum representing the four directions (NORTH, SOUTH, EAST, WEST)
- **LightColor.java** - Enum for traffic light colors (RED, YELLOW, GREEN)
- **Vehicle.java** - Represents a vehicle waiting at the intersection
- **TrafficLight.java** - Represents a traffic light for one direction
- **IntersectionManager.java** - Core logic managing the intersection, vehicles, and timing calculations

#### View Layer (`view` package)
- **TrafficSimulationView.java** - JavaFX-based GUI with:
  - 4-way intersection visualization
  - Vehicle density input fields
  - Control buttons (Start, Pause, Reset, Random)
  - Digital countdown timers
  - Animated vehicle movement

#### Controller Layer (`controller` package)
- **TrafficController.java** - Bridges Model and View, handles events and updates

## Features

### 1. Smart Green Light Allocation
- **Total Cycle Time:** 120 seconds (fixed)
- **Yellow Light Duration:** 3 seconds (constant)
- **Green Light Duration:** Proportional to vehicle density
  - Minimum: 10 seconds
  - Maximum: 60 seconds
  - Algorithm: Calculates percentage of total traffic per lane

### 2. GUI Components
- **Central Visualization:** Graphical 4-way intersection with roads, lanes, and stop lines
- **Input Panel:** 
  - Manual vehicle count input (0-50 per direction)
  - "Generate Random Density" button
- **Control Panel:**
  - Start Simulation
  - Pause/Resume
  - Reset
- **Live Display:**
  - Digital countdown timers for each direction
  - Color-coded timer based on current light (Red/Yellow/Green)
  - Status indicator showing elapsed time and active direction

### 3. Vehicle Animation
- Cars appear in queue at each direction based on density
- Cars move during green light
- Cars disappear after crossing the intersection
- No collision detection - cars maintain safe distance
- Smooth animation at 1 second intervals

### 4. Traffic Logic Rules
1. Cycle processes initial vehicle set (no new cars during cycle)
2. Direction order: North → East → South → West
3. Each direction gets: Green → Yellow → Red
4. Next direction activates after yellow completes
5. Cycle ends when all directions complete their phases

## Algorithm: Green Light Duration Calculation

```
1. Calculate total vehicles across all directions
2. Available green time = 120s - (4 directions × 3s yellow) = 108s
3. For each direction:
   - Calculate proportion = vehicles_in_direction / total_vehicles
   - Initial green time = proportion × available_green_time
   - Apply constraints: MIN(10s, MAX(60s, green_time))
4. Adjust if total doesn't match available time:
   - Distribute difference to directions with most vehicles
   - Respect min/max constraints
```

## How to Run

### Prerequisites
- Java 21 or higher
- Maven 3.x
- JavaFX 21

### Build and Run
```bash
# Clean and compile
./mvnw clean compile

# Run application
./mvnw javafx:run
```

### Using the Application
1. **Set Vehicle Density:**
   - Enter vehicle counts (0-50) for each direction, OR
   - Click "Generate Random Density" for automatic values

2. **Start Simulation:**
   - Click "Start Simulation"
   - Watch the traffic lights cycle through colors
   - Observe vehicles moving during green lights
   - Monitor countdown timers

3. **Control Simulation:**
   - **Pause:** Pause the running simulation
   - **Resume:** Continue from paused state
   - **Reset:** Clear all data and start fresh

## Project Structure
```
JavaProject/
├── src/main/java/
│   ├── model/
│   │   ├── Direction.java
│   │   ├── LightColor.java
│   │   ├── Vehicle.java
│   │   ├── TrafficLight.java
│   │   └── IntersectionManager.java
│   ├── view/
│   │   └── TrafficSimulationView.java
│   ├── controller/
│   │   └── TrafficController.java
│   ├── odev/odev/
│   │   └── Main.java
│   └── module-info.java
├── pom.xml
└── README.md
```

## Key Design Decisions

### 1. Strict MVC Separation
- **Model** contains all business logic, no GUI dependencies
- **View** handles all UI rendering, no business logic
- **Controller** acts as mediator, coordinates Model and View

### 2. Standard Java Collections
- `EnumMap` for direction-based storage (optimal for enums)
- `ArrayList` for vehicle lists
- `LinkedList` for direction queue (FIFO)

### 3. JavaFX Canvas for Animation
- Direct graphics rendering using GraphicsContext
- Efficient for real-time vehicle movement
- Full control over visual representation

### 4. Proportional Allocation Algorithm
- Fair distribution based on traffic load
- Respects safety constraints (min/max times)
- Handles edge cases (zero vehicles, unequal distribution)

### 5. Time Management
- `Timeline` with 1-second intervals
- Countdown timers for user feedback
- State machine for light transitions

## Future Enhancements (Not Implemented)
- Emergency vehicle priority
- Pedestrian crossings
- Multiple intersections
- Historical traffic data analysis
- Optimization using machine learning

## Testing Scenarios

### Scenario 1: Equal Distribution
- Input: 10 vehicles per direction
- Expected: Each direction gets ~27s green time (108s / 4)

### Scenario 2: Unequal Distribution
- Input: North=20, South=10, East=5, West=5
- Expected: North gets more green time proportionally

### Scenario 3: Edge Case - Zero Vehicles
- Input: 0 vehicles in one or more directions
- Expected: Minimum 10s green time still allocated

### Scenario 4: Maximum Constraint
- Input: North=50, others=1
- Expected: North gets 60s (max), others get minimum time

## License
Educational project - Free to use and modify

## Author
Senior Java Developer
Version 1.0 - January 2026

