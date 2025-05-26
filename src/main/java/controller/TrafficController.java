package controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import model.Direction;
import model.TrafficCalculator;
import model.TrafficLamp;
import model.TrafficLight;
import model.VehicleManager;

import java.net.URL;
import java.util.*;

public class TrafficController implements Initializable {
    private static final int YELLOW_TIME = 3;
    private static final int TOTAL_CYCLE_TIME = 120;
    private static final Direction[] LIGHT_ORDER = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private final Map<Direction, TrafficLamp> lamps = new EnumMap<>(Direction.class);

    private VehicleManager vehicleManager;
    private TrafficCalculator calculator;
    private TrafficLight[] lights;
    private int currentIndex = 0;
    private Direction currentDirection;
    private Timeline timeline;

    @FXML private Circle northRed, northYellow, northGreen;
    @FXML private Circle southRed, southYellow, southGreen;
    @FXML private Circle eastRed, eastYellow, eastGreen;
    @FXML private Circle westRed, westYellow, westGreen;

    @FXML private Rectangle carN1,carN2,carN3,carN4,carN5,carN6;
    @FXML private Rectangle carS1,carS2,carS3,carS4,carS5,carS6;
    @FXML private Rectangle carE1,carE2,carE3,carE4,carE5,carE6;
    @FXML private Rectangle carW1,carW2,carW3,carW4,carW5,carW6;

    @FXML private Pane simulationPane1;

    @FXML private Button btnStart;
    @FXML private Label northVehicleCountLabel, southVehicleCountLabel, eastVehicleCountLabel, westVehicleCountLabel;
    @FXML private Label calcNorthLabel, calcSouthLabel, calcEastLabel, calcWestLabel;
    @FXML private Label remainNorthLabel, remainSouthLabel, remainEastLabel, remainWestLabel;
    @FXML private TextField inputNorth, inputSouth, inputEast, inputWest;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lamps.put(Direction.NORTH, new TrafficLamp(northRed, northYellow, northGreen));
        lamps.put(Direction.SOUTH, new TrafficLamp(southRed, southYellow, southGreen));
        lamps.put(Direction.EAST,  new TrafficLamp(eastRed,  eastYellow,  eastGreen));
        lamps.put(Direction.WEST,  new TrafficLamp(westRed,  westYellow,  westGreen));
        lamps.values().forEach(TrafficLamp::reset);

        vehicleManager = new VehicleManager();
        vehicleManager.setWestReference(carW1);

        Rectangle[] northCars = {carN6, carN5, carN4, carN3, carN2, carN1};
        Rectangle[] southCars = {carS6, carS5, carS4, carS3, carS2, carS1};
        Rectangle[] eastCars  = {carE6, carE5, carE4, carE3, carE2, carE1};
        Rectangle[] westCars  = {carW6, carW5, carW4, carW3, carW2, carW1};

        for (Rectangle car : northCars) vehicleManager.addInitialVehicle(Direction.NORTH, car);
        for (Rectangle car : southCars) vehicleManager.addInitialVehicle(Direction.SOUTH, car);
        for (Rectangle car : eastCars)  vehicleManager.addInitialVehicle(Direction.EAST, car);
        for (Rectangle car : westCars)  vehicleManager.addInitialVehicle(Direction.WEST, car);
    }

    @FXML
    public void startSimulation() {
        calculator = new TrafficCalculator();
        Integer n = parseInput(inputNorth.getText());
        Integer s = parseInput(inputSouth.getText());
        Integer e = parseInput(inputEast.getText());
        Integer w = parseInput(inputWest.getText());
        calculator.setInitialCounts(n, s, e, w);
        lights = calculator.calculateInitialLights();
        showCalculatedTimes();
        updateVehicleCountLabels();
        currentIndex = 0;
        currentDirection = LIGHT_ORDER[currentIndex];
        lights[currentDirection.ordinal()].setState(TrafficLight.LightState.GREEN);
        setActiveLight(currentDirection, "GREEN");
        startTimeline();
    }

    private void startTimeline() {
        if (timeline != null) timeline.stop();
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            calculator.simulateSecond();
            updateVehicleCountLabels();
            TrafficLight currentLight = lights[currentDirection.ordinal()];
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
                    currentIndex = (currentIndex + 1) % LIGHT_ORDER.length;
                    currentDirection = LIGHT_ORDER[currentIndex];
                    lights[currentDirection.ordinal()].setState(TrafficLight.LightState.GREEN);
                    setActiveLight(currentDirection, "GREEN");
                }
            }
        }));
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
    }

    public void setActiveLight(Direction active, String color) {
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

    private void showCalculatedTimes() {
        calcNorthLabel.setText("North: " + lights[0].getGreenDuration() + "s");
        calcSouthLabel.setText("South: " + lights[1].getGreenDuration() + "s");
        calcEastLabel.setText("East: " + lights[2].getGreenDuration() + "s");
        calcWestLabel.setText("West: " + lights[3].getGreenDuration() + "s");
    }

    private void updateRemainingTimeLabel(Direction dir, int remainingTime) {
        switch (dir) {
            case NORTH -> remainNorthLabel.setText("North: " + remainingTime + "s");
            case SOUTH -> remainSouthLabel.setText("South: " + remainingTime + "s");
            case EAST  -> remainEastLabel.setText("East: " + remainingTime + "s");
            case WEST  -> remainWestLabel.setText("West: " + remainingTime + "s");
        }
    }

    @FXML
    public void resetSimulation() {
        if (timeline != null) timeline.stop();
        lamps.values().forEach(TrafficLamp::reset);
        calcNorthLabel.setText("North: 0s");
        calcSouthLabel.setText("South: 0s");
        calcEastLabel.setText("East: 0s");
        calcWestLabel.setText("West: 0s");
        remainNorthLabel.setText("North: 0s");
        remainSouthLabel.setText("South: 0s");
        remainEastLabel.setText("East: 0s");
        remainWestLabel.setText("West: 0s");
        northVehicleCountLabel.setText("North: 0s");
        southVehicleCountLabel.setText("South: 0s");
        eastVehicleCountLabel.setText("East: 0s");
        westVehicleCountLabel.setText("West: 0s");
        currentIndex = 0;
        calculator = null;
        lights = null;
    }

    private Integer parseInput(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return Math.max(0, value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
