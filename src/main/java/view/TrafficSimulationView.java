package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import model.Direction;
import model.LightColor;
import model.Vehicle;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * View class for the Traffic Simulation System
 * Implements the visualization using JavaFX components
 */
public class TrafficSimulationView extends BorderPane {

    // Canvas for intersection visualization
    private Canvas intersectionCanvas;
    private GraphicsContext gc;

    // Input fields for vehicle density
    private Map<Direction, TextField> densityInputs;

    // Labels for countdown timers
    private Map<Direction, Label> timerLabels;

    // Labels for vehicle counts
    private Map<Direction, Label> vehicleCountLabels;

    // Labels for green time durations
    private Map<Direction, Label> greenTimeLabels;

    // Control buttons
    private Button startButton;
    private Button pauseButton;
    private Button resetButton;
    private Button randomButton;

    // Status label
    private Label statusLabel;

    // Current state for visualization
    private Map<Direction, LightColor> currentLights;
    private Map<Direction, List<Vehicle>> currentVehicles;

    // Event handlers
    private Runnable onStart;
    private Runnable onPause;
    private Runnable onReset;
    private Runnable onGenerateRandom;

    public TrafficSimulationView() {
        this.densityInputs = new EnumMap<>(Direction.class);
        this.timerLabels = new EnumMap<>(Direction.class);
        this.vehicleCountLabels = new EnumMap<>(Direction.class);
        this.greenTimeLabels = new EnumMap<>(Direction.class);
        this.currentLights = new EnumMap<>(Direction.class);
        this.currentVehicles = new EnumMap<>(Direction.class);

        // Initialize light states
        for (Direction dir : Direction.values()) {
            currentLights.put(dir, LightColor.RED);
        }

        initializeComponents();
        layoutComponents();
    }

    /**
     * Initialize all UI components
     */
    private void initializeComponents() {
        // Canvas for intersection
        intersectionCanvas = new Canvas(700, 700);
        gc = intersectionCanvas.getGraphicsContext2D();

        // Control buttons
        startButton = new Button("Start Simulation");
        startButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        startButton.setMinWidth(150);

        pauseButton = new Button("Pause");
        pauseButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        pauseButton.setMinWidth(150);

        resetButton = new Button("Reset");
        resetButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");
        resetButton.setMinWidth(150);

        randomButton = new Button("Generate Random Density");
        randomButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 20px;");

        // Status label
        statusLabel = new Label("Ready to start simulation");
        statusLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        // Setup button actions
        startButton.setOnAction(e -> {
            if (onStart != null) onStart.run();
        });

        pauseButton.setOnAction(e -> {
            if (onPause != null) onPause.run();
        });

        resetButton.setOnAction(e -> {
            if (onReset != null) onReset.run();
        });

        randomButton.setOnAction(e -> {
            if (onGenerateRandom != null) onGenerateRandom.run();
        });
    }

    /**
     * Layout all components
     */
    private void layoutComponents() {
        // Left panel - Input controls
        VBox leftPanel = createInputPanel();
        leftPanel.setPadding(new Insets(20));
        leftPanel.setSpacing(15);
        leftPanel.setMinWidth(250);
        leftPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");

        // Center panel - Intersection visualization
        VBox centerPanel = new VBox(20);
        centerPanel.setAlignment(Pos.CENTER);
        centerPanel.setPadding(new Insets(20));
        centerPanel.getChildren().add(intersectionCanvas);

        // Bottom panel - Control buttons and status
        VBox bottomPanel = createBottomPanel();
        bottomPanel.setPadding(new Insets(15));
        bottomPanel.setSpacing(10);
        bottomPanel.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 1 0 0 0;");

        // Add to BorderPane
        this.setLeft(leftPanel);
        this.setCenter(centerPanel);
        this.setBottom(bottomPanel);

        // Initial draw
        drawIntersection();
    }

    /**
     * Create input panel for vehicle densities
     */
    private VBox createInputPanel() {
        VBox panel = new VBox(15);

        Label title = new Label("Vehicle Density Input");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        panel.getChildren().add(title);
        panel.getChildren().add(randomButton);
        panel.getChildren().add(new Label("Enter vehicle count (0-50):"));

        // Create input fields for each direction
        for (Direction dir : Direction.values()) {
            VBox dirBox = new VBox(5);

            Label dirLabel = new Label(dir.getDisplayName() + ":");
            dirLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

            TextField textField = new TextField("10");
            textField.setPrefWidth(150);
            textField.setPromptText("0-50");

            // Timer label
            Label timerLabel = new Label("--:--");
            timerLabel.setFont(Font.font("Monospace", 12));
            timerLabel.setStyle("-fx-text-fill: #666666;");

            // Vehicle count label
            Label countLabel = new Label("Waiting: 0");
            countLabel.setFont(Font.font("Arial", 11));
            countLabel.setStyle("-fx-text-fill: #555555;");

            // Green time allocation label
            Label greenLabel = new Label("Green: -- sec");
            greenLabel.setFont(Font.font("Arial", 10));
            greenLabel.setStyle("-fx-text-fill: #008800;");

            densityInputs.put(dir, textField);
            timerLabels.put(dir, timerLabel);
            vehicleCountLabels.put(dir, countLabel);
            greenTimeLabels.put(dir, greenLabel);

            HBox inputRow = new HBox(10);
            inputRow.setAlignment(Pos.CENTER_LEFT);
            inputRow.getChildren().addAll(textField, timerLabel);

            dirBox.getChildren().addAll(dirLabel, inputRow, countLabel, greenLabel);
            panel.getChildren().add(dirBox);
        }

        return panel;
    }

    /**
     * Create bottom control panel
     */
    private VBox createBottomPanel() {
        VBox panel = new VBox(10);
        panel.setAlignment(Pos.CENTER);

        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, pauseButton, resetButton);

        panel.getChildren().addAll(buttonBox, statusLabel);

        return panel;
    }

    /**
     * Draw the intersection on canvas
     */
    private void drawIntersection() {
        double width = intersectionCanvas.getWidth();
        double height = intersectionCanvas.getHeight();

        // Clear canvas
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, width, height);

        // Draw roads
        double roadWidth = 150;
        double centerX = width / 2;
        double centerY = height / 2;

        // Draw background grass
        gc.setFill(Color.web("#90EE90"));
        gc.fillRect(0, 0, width, height);

        // Draw horizontal road
        gc.setFill(Color.web("#404040"));
        gc.fillRect(0, centerY - roadWidth / 2, width, roadWidth);

        // Draw vertical road
        gc.fillRect(centerX - roadWidth / 2, 0, roadWidth, height);

        // Draw center intersection area
        gc.setFill(Color.web("#505050"));
        gc.fillRect(centerX - roadWidth / 2, centerY - roadWidth / 2, roadWidth, roadWidth);

        // Draw lane dividers
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2);
        gc.setLineDashes(10, 10);

        // Horizontal lane divider
        gc.strokeLine(0, centerY, centerX - roadWidth / 2, centerY);
        gc.strokeLine(centerX + roadWidth / 2, centerY, width, centerY);

        // Vertical lane divider
        gc.strokeLine(centerX, 0, centerX, centerY - roadWidth / 2);
        gc.strokeLine(centerX, centerY + roadWidth / 2, centerX, height);

        gc.setLineDashes(null);

        // Draw stop lines
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);

        // North stop line
        gc.strokeLine(centerX - roadWidth / 4 - 10, centerY - roadWidth / 2,
                     centerX + roadWidth / 4 - 10, centerY - roadWidth / 2);

        // South stop line
        gc.strokeLine(centerX - roadWidth / 4 + 10, centerY + roadWidth / 2,
                     centerX + roadWidth / 4 + 10, centerY + roadWidth / 2);

        // East stop line
        gc.strokeLine(centerX + roadWidth / 2, centerY - roadWidth / 4 + 10,
                     centerX + roadWidth / 2, centerY + roadWidth / 4 + 10);

        // West stop line
        gc.strokeLine(centerX - roadWidth / 2, centerY - roadWidth / 4 - 10,
                     centerX - roadWidth / 2, centerY + roadWidth / 4 - 10);

        // Draw traffic lights
        drawTrafficLights();

        // Draw vehicles
        drawVehicles();

        // Draw direction labels
        drawDirectionLabels();
    }

    /**
     * Draw traffic lights for all directions
     */
    private void drawTrafficLights() {
        double width = intersectionCanvas.getWidth();
        double height = intersectionCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double roadWidth = 150;

        for (Direction dir : Direction.values()) {
            LightColor color = currentLights.getOrDefault(dir, LightColor.RED);
            drawTrafficLight(dir, color, centerX, centerY, roadWidth);
        }
    }

    /**
     * Draw a single traffic light
     */
    private void drawTrafficLight(Direction dir, LightColor activeColor,
                                   double centerX, double centerY, double roadWidth) {
        double lightSize = 15;
        double spacing = 20;
        double x = 0, y = 0;

        // Position based on direction
        switch (dir) {
            case NORTH:
                x = centerX - roadWidth / 4 - 30;
                y = centerY - roadWidth / 2 - 70;
                break;
            case SOUTH:
                x = centerX + roadWidth / 4 + 30;
                y = centerY + roadWidth / 2 + 70;
                break;
            case EAST:
                x = centerX + roadWidth / 2 + 70;
                y = centerY - roadWidth / 4 + 30;
                break;
            case WEST:
                x = centerX - roadWidth / 2 - 70;
                y = centerY + roadWidth / 4 - 30;
                break;
        }

        // Draw light box
        gc.setFill(Color.BLACK);
        if (dir == Direction.EAST || dir == Direction.WEST) {
            gc.fillRoundRect(x - spacing, y - lightSize / 2, spacing * 3 - 5, lightSize + 10, 5, 5);
        } else {
            gc.fillRoundRect(x - lightSize / 2, y - spacing, lightSize + 10, spacing * 3 - 5, 5, 5);
        }

        // Draw lights
        Color redColor = activeColor == LightColor.RED ? Color.RED : Color.web("#330000");
        Color yellowColor = activeColor == LightColor.YELLOW ? Color.YELLOW : Color.web("#333300");
        Color greenColor = activeColor == LightColor.GREEN ? Color.LIME : Color.web("#003300");

        if (dir == Direction.EAST || dir == Direction.WEST) {
            // Horizontal arrangement
            gc.setFill(redColor);
            gc.fillOval(x - spacing + 5, y - lightSize / 2 + 2, lightSize, lightSize);

            gc.setFill(yellowColor);
            gc.fillOval(x + 2, y - lightSize / 2 + 2, lightSize, lightSize);

            gc.setFill(greenColor);
            gc.fillOval(x + spacing - 10, y - lightSize / 2 + 2, lightSize, lightSize);
        } else {
            // Vertical arrangement
            gc.setFill(redColor);
            gc.fillOval(x - lightSize / 2 + 2, y - spacing + 5, lightSize, lightSize);

            gc.setFill(yellowColor);
            gc.fillOval(x - lightSize / 2 + 2, y + 2, lightSize, lightSize);

            gc.setFill(greenColor);
            gc.fillOval(x - lightSize / 2 + 2, y + spacing - 10, lightSize, lightSize);
        }
    }

    /**
     * Draw vehicles
     */
    private void drawVehicles() {
        double width = intersectionCanvas.getWidth();
        double height = intersectionCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;
        double roadWidth = 150;

        for (Direction dir : Direction.values()) {
            List<Vehicle> vehicles = currentVehicles.get(dir);
            if (vehicles == null) continue;

            for (Vehicle vehicle : vehicles) {
                if (vehicle.hasCrossed()) continue;

                drawVehicle(vehicle, dir, centerX, centerY, roadWidth);
            }
        }
    }

    /**
     * Draw a single vehicle
     */
    private void drawVehicle(Vehicle vehicle, Direction dir,
                            double centerX, double centerY, double roadWidth) {
        double position = vehicle.getPosition();
        double carWidth = 20;
        double carHeight = 35;
        double laneOffset = roadWidth / 4;
        double maxDistance = 250; // Maximum distance to render vehicles

        double x = 0, y = 0;

        // Calculate position based on direction
        switch (dir) {
            case NORTH:
                x = centerX - laneOffset;
                // Negative position = behind stop line, positive = crossing/crossed
                if (position < 0) {
                    y = centerY - roadWidth / 2 + position * maxDistance;
                } else {
                    y = centerY - roadWidth / 2 - position * (centerY - roadWidth / 2);
                }
                break;
            case SOUTH:
                x = centerX + laneOffset;
                if (position < 0) {
                    y = centerY + roadWidth / 2 - position * maxDistance;
                } else {
                    y = centerY + roadWidth / 2 + position * (centerY - roadWidth / 2);
                }
                break;
            case EAST:
                y = centerY - laneOffset;
                if (position < 0) {
                    x = centerX + roadWidth / 2 - position * maxDistance;
                } else {
                    x = centerX + roadWidth / 2 + position * (centerX - roadWidth / 2);
                }
                // Swap dimensions for horizontal
                double temp = carWidth;
                carWidth = carHeight;
                carHeight = temp;
                break;
            case WEST:
                y = centerY + laneOffset;
                if (position < 0) {
                    x = centerX - roadWidth / 2 + position * maxDistance;
                } else {
                    x = centerX - roadWidth / 2 - position * (centerX - roadWidth / 2);
                }
                // Swap dimensions for horizontal
                temp = carWidth;
                carWidth = carHeight;
                carHeight = temp;
                break;
        }

        // Only draw if within canvas bounds
        if (x < -50 || x > intersectionCanvas.getWidth() + 50 ||
            y < -50 || y > intersectionCanvas.getHeight() + 50) {
            return;
        }

        // Draw car shadow
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRoundRect(x - carWidth / 2 + 2, y - carHeight / 2 + 2, carWidth, carHeight, 3, 3);

        // Draw car body
        gc.setFill(Color.BLUE);
        gc.fillRoundRect(x - carWidth / 2, y - carHeight / 2, carWidth, carHeight, 3, 3);

        // Draw car outline
        gc.setStroke(Color.DARKBLUE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(x - carWidth / 2, y - carHeight / 2, carWidth, carHeight, 3, 3);

        // Draw car windows
        gc.setFill(Color.LIGHTBLUE);
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            gc.fillRoundRect(x - carWidth / 2 + 3, y - carHeight / 2 + 3, carWidth - 6, carHeight / 3, 2, 2);
        } else {
            gc.fillRoundRect(x - carWidth / 2 + 3, y - carHeight / 2 + 3, carWidth / 3, carHeight - 6, 2, 2);
        }
    }

    /**
     * Draw direction labels
     */
    private void drawDirectionLabels() {
        double width = intersectionCanvas.getWidth();
        double height = intersectionCanvas.getHeight();
        double centerX = width / 2;
        double centerY = height / 2;

        gc.setFill(Color.BLACK);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        gc.fillText("NORTH", centerX - 30, 30);
        gc.fillText("SOUTH", centerX - 30, height - 15);
        gc.fillText("EAST", width - 60, centerY + 5);
        gc.fillText("WEST", 10, centerY + 5);
    }

    // Public methods for controller interaction

    /**
     * Update traffic light display
     */
    public void updateTrafficLight(Direction direction, LightColor color, int remainingTime) {
        currentLights.put(direction, color);

        // Update timer label
        Label timerLabel = timerLabels.get(direction);
        if (remainingTime > 0) {
            int minutes = remainingTime / 60;
            int seconds = remainingTime % 60;
            timerLabel.setText(String.format("%02d:%02d", minutes, seconds));

            // Color based on light
            String textColor = switch (color) {
                case RED -> "#CC0000";
                case YELLOW -> "#CCAA00";
                case GREEN -> "#00AA00";
            };
            timerLabel.setStyle("-fx-text-fill: " + textColor + "; -fx-font-weight: bold;");
        } else {
            timerLabel.setText("--:--");
            timerLabel.setStyle("-fx-text-fill: #666666;");
        }

        drawIntersection();
    }

    /**
     * Update green time allocation display
     */
    public void updateGreenTimeAllocation(Direction direction, int greenDuration) {
        Label greenLabel = greenTimeLabels.get(direction);
        greenLabel.setText("Green: " + greenDuration + " sec");
    }

    /**
     * Update vehicles display
     */
    public void updateVehicles(Direction direction, List<Vehicle> vehicles) {
        currentVehicles.put(direction, vehicles);

        // Update vehicle count label
        long waitingCount = vehicles.stream().filter(v -> !v.hasCrossed()).count();
        Label countLabel = vehicleCountLabels.get(direction);
        countLabel.setText("Waiting: " + waitingCount);

        drawIntersection();
    }

    /**
     * Update status display
     */
    public void updateStatus(boolean cycleActive, int elapsedTime, Direction activeDirection) {
        if (cycleActive && activeDirection != null) {
            statusLabel.setText(String.format("Simulation Running - Elapsed: %d sec - Active: %s",
                                             elapsedTime, activeDirection.getDisplayName()));
            statusLabel.setStyle("-fx-text-fill: green;");
            pauseButton.setText("Pause");
        } else if (activeDirection != null) {
            statusLabel.setText("Simulation Paused");
            statusLabel.setStyle("-fx-text-fill: orange;");
            pauseButton.setText("Resume");
        } else {
            statusLabel.setText("Ready to start simulation");
            statusLabel.setStyle("-fx-text-fill: black;");
            pauseButton.setText("Pause");
        }
    }

    /**
     * Get vehicle densities from input fields
     */
    public Map<Direction, Integer> getVehicleDensities() {
        Map<Direction, Integer> densities = new EnumMap<>(Direction.class);

        for (Direction dir : Direction.values()) {
            TextField field = densityInputs.get(dir);
            try {
                int value = Integer.parseInt(field.getText());
                densities.put(dir, Math.max(0, Math.min(50, value)));
            } catch (NumberFormatException e) {
                densities.put(dir, 0);
            }
        }

        return densities;
    }

    /**
     * Set vehicle densities in input fields
     */
    public void setVehicleDensities(Map<Direction, Integer> densities) {
        for (Direction dir : Direction.values()) {
            TextField field = densityInputs.get(dir);
            field.setText(String.valueOf(densities.get(dir)));
        }
    }

    // Event handler setters
    public void setOnStart(Runnable handler) {
        this.onStart = handler;
    }

    public void setOnPause(Runnable handler) {
        this.onPause = handler;
    }

    public void setOnReset(Runnable handler) {
        this.onReset = handler;
    }

    public void setOnGenerateRandom(Runnable handler) {
        this.onGenerateRandom = handler;
    }
}

