package controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.*;

import java.net.URL;
import java.util.*;

public class TrafficController implements Initializable {

    @FXML private Circle northRed, northYellow, northGreen;
    @FXML private Circle southRed, southYellow, southGreen;
    @FXML private Circle eastRed, eastYellow, eastGreen;
    @FXML private Circle westRed, westYellow, westGreen;

    @FXML private Rectangle carN1,carN2,carN3,carN4,carN5,carN6;
    @FXML private Rectangle carS1,carS2,carS3,carS4,carS5,carS6;
    @FXML private Rectangle carE1,carE2,carE3,carE4,carE5,carE6;
    @FXML private Rectangle carW1,carW2,carW3,carW4,carW5,carW6;

    @FXML private Pane simulationPane1;
    @FXML private Button btnStart, btnRandom;
    @FXML private TextField inputNorth, inputSouth, inputEast, inputWest;
    @FXML private Label calcNorthLabel, calcSouthLabel, calcEastLabel, calcWestLabel;
    @FXML private Label remainNorthLabel, remainSouthLabel, remainEastLabel, remainWestLabel;
    @FXML private Label northVehicleCountLabel, southVehicleCountLabel, eastVehicleCountLabel, westVehicleCountLabel;

    private final Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    private final int YELLOW_TIME = 2;
    private int currentIndex = 0;

    private TrafficCalculator calculator;
    private TrafficLight[] lights;
    private Direction currentDirection;
    private Timeline timeline;
    private VehicleManager vehicleManager;
    private final Map<Direction, TrafficLamp> lamps = new EnumMap<>(Direction.class);
    private final Set<Direction> servedDirections = new HashSet<>();
    private int elapsedTime = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lamps.put(Direction.NORTH, new TrafficLamp(northRed, northYellow, northGreen));
        lamps.put(Direction.SOUTH, new TrafficLamp(southRed, southYellow, southGreen));
        lamps.put(Direction.EAST, new TrafficLamp(eastRed, eastYellow, eastGreen));
        lamps.put(Direction.WEST, new TrafficLamp(westRed, westYellow, westGreen));

        for (TrafficLamp lamp : lamps.values()) lamp.reset();

        vehicleManager = new VehicleManager(simulationPane1);

        addInitialVehicles();

        vehicleManager.setReferenceCar(Direction.NORTH, carN1);
        vehicleManager.setReferenceCar(Direction.SOUTH, carS1);
        vehicleManager.setReferenceCar(Direction.EAST,  carE1);
        vehicleManager.setReferenceCar(Direction.WEST,  carW1);

        btnRandom.setOnAction(e -> assignRandomVehicleCounts());
    }

    private void addInitialVehicles() {
        vehicleManager.addInitialVehicle(Direction.NORTH, carN1);
        vehicleManager.addInitialVehicle(Direction.NORTH, carN2);
        vehicleManager.addInitialVehicle(Direction.NORTH, carN3);
        vehicleManager.addInitialVehicle(Direction.NORTH, carN4);
        vehicleManager.addInitialVehicle(Direction.NORTH, carN5);
        vehicleManager.addInitialVehicle(Direction.NORTH, carN6);

        vehicleManager.addInitialVehicle(Direction.SOUTH, carS1);
        vehicleManager.addInitialVehicle(Direction.SOUTH, carS2);
        vehicleManager.addInitialVehicle(Direction.SOUTH, carS3);
        vehicleManager.addInitialVehicle(Direction.SOUTH, carS4);
        vehicleManager.addInitialVehicle(Direction.SOUTH, carS5);
        vehicleManager.addInitialVehicle(Direction.SOUTH, carS6);

        vehicleManager.addInitialVehicle(Direction.EAST, carE1);
        vehicleManager.addInitialVehicle(Direction.EAST, carE2);
        vehicleManager.addInitialVehicle(Direction.EAST, carE3);
        vehicleManager.addInitialVehicle(Direction.EAST, carE4);
        vehicleManager.addInitialVehicle(Direction.EAST, carE5);
        vehicleManager.addInitialVehicle(Direction.EAST, carE6);

        vehicleManager.addInitialVehicle(Direction.WEST, carW1);
        vehicleManager.addInitialVehicle(Direction.WEST, carW2);
        vehicleManager.addInitialVehicle(Direction.WEST, carW3);
        vehicleManager.addInitialVehicle(Direction.WEST, carW4);
        vehicleManager.addInitialVehicle(Direction.WEST, carW5);
        vehicleManager.addInitialVehicle(Direction.WEST, carW6);
    }

    @FXML
    public void startSimulation() {
        calculator = new TrafficCalculator();

        Integer n = parseInput(inputNorth.getText());
        Integer s = parseInput(inputSouth.getText());
        Integer e = parseInput(inputEast.getText());
        Integer w = parseInput(inputWest.getText());

        calculator.setInitialCounts(n, s, e, w);
        lights = new TrafficLight[4];
        for (int i = 0; i < 4; i++) {
            Direction dir = directions[i];
            lights[i] = new TrafficLight(calculator.getDuration(dir));
        }

        showCalculatedTimes();
        updateVehicleCountLabels();

        servedDirections.clear();
        elapsedTime = 0;

        currentIndex = 0;
        currentDirection = directions[currentIndex];
        lights[currentIndex].setState(TrafficLight.LightState.GREEN);
        setActiveLight(currentDirection, "GREEN");

        startTimeline();
    }

    private void assignRandomVehicleCounts() {
        Random random = new Random();
        int north = random.nextInt(21);
        int east = random.nextInt(21);
        int south = random.nextInt(21);
        int west = random.nextInt(21);

        inputNorth.setText(String.valueOf(north));
        inputEast.setText(String.valueOf(east));
        inputSouth.setText(String.valueOf(south));
        inputWest.setText(String.valueOf(west));
    }

    private void startTimeline() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            calculator.simulateSecond();
            updateVehicleCountLabels();

            TrafficLight currentLight = lights[currentIndex];
            currentLight.tick();
            updateRemainingTimeLabel(currentDirection, currentLight.getRemainingTime());

            if (currentLight.getState() == TrafficLight.LightState.GREEN) {
                switch (currentDirection) {
                    case NORTH -> vehicleManager.moveVehiclesNorth();
                    case EAST  -> vehicleManager.moveVehiclesEast();
                    case SOUTH -> vehicleManager.moveVehiclesSouth();
                    case WEST  -> vehicleManager.moveVehiclesWest();
                }
            }

            if (currentLight.getRemainingTime() <= 0) {
                if (currentLight.getState() == TrafficLight.LightState.GREEN) {
                    currentLight.setState(TrafficLight.LightState.YELLOW);
                    setActiveLight(currentDirection, "YELLOW");
                } else if (currentLight.getState() == TrafficLight.LightState.YELLOW) {
                    currentLight.setState(TrafficLight.LightState.RED);
                    setActiveLight(currentDirection, "RED");

                    elapsedTime += calculator.getDuration(currentDirection) + YELLOW_TIME;
                    servedDirections.add(currentDirection);

                    if (servedDirections.size() == 4) {
                        servedDirections.clear();
                        elapsedTime = 0;
                        calculator.setInitialCounts(
                                parseInput(inputNorth.getText()),
                                parseInput(inputSouth.getText()),
                                parseInput(inputEast.getText()),
                                parseInput(inputWest.getText())
                        );
                        for (int i = 0; i < 4; i++) {
                            Direction dir = directions[i];
                            lights[i] = new TrafficLight(calculator.getDuration(dir));
                        }
                        showCalculatedTimes();
                    }

                    currentIndex = (currentIndex + 1) % 4;
                    currentDirection = directions[currentIndex];
                    lights[currentIndex].setState(TrafficLight.LightState.GREEN);
                    setActiveLight(currentDirection, "GREEN");
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    private void setActiveLight(Direction active, String color) {
        for (Direction dir : Direction.values()) {
            if (dir == active) lamps.get(dir).set(color);
            else lamps.get(dir).set("RED");
        }
    }

    private void updateVehicleCountLabels() {
        northVehicleCountLabel.setText("North: " + calculator.getCount(Direction.NORTH));
        southVehicleCountLabel.setText("South: " + calculator.getCount(Direction.SOUTH));
        eastVehicleCountLabel.setText("East: " + calculator.getCount(Direction.EAST));
        westVehicleCountLabel.setText("West: " + calculator.getCount(Direction.WEST));
    }

    private void updateRemainingTimeLabel(Direction dir, int remainingTime) {
        switch (dir) {
            case NORTH -> remainNorthLabel.setText("North: " + remainingTime + "s");
            case SOUTH -> remainSouthLabel.setText("South: " + remainingTime + "s");
            case EAST  -> remainEastLabel.setText("East: " + remainingTime + "s");
            case WEST  -> remainWestLabel.setText("West: " + remainingTime + "s");
        }
    }

    private void showCalculatedTimes() {
        calcNorthLabel.setText("North: " + calculator.getDuration(Direction.NORTH) + "s");
        calcSouthLabel.setText("South: " + calculator.getDuration(Direction.SOUTH) + "s");
        calcEastLabel.setText("East: " + calculator.getDuration(Direction.EAST) + "s");
        calcWestLabel.setText("West: " + calculator.getDuration(Direction.WEST) + "s");
    }

    @FXML
    public void resetSimulation() {
        if (timeline != null) timeline.stop();
        for (TrafficLamp lamp : lamps.values()) lamp.reset();

        calcNorthLabel.setText("North: 0s");
        calcSouthLabel.setText("South: 0s");
        calcEastLabel.setText("East: 0s");
        calcWestLabel.setText("West: 0s");

        remainNorthLabel.setText("North: 0s");
        remainSouthLabel.setText("South: 0s");
        remainEastLabel.setText("East: 0s");
        remainWestLabel.setText("West: 0s");

        northVehicleCountLabel.setText("North: 0");
        southVehicleCountLabel.setText("South: 0");
        eastVehicleCountLabel.setText("East: 0");
        westVehicleCountLabel.setText("West: 0");

        currentIndex = 0;
        calculator = null;
        lights = null;
    }

    private Integer parseInput(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return Math.max(0, value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
