package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import model.Direction;
import model.TrafficLamp;
import model.VehicleManager;


import java.net.URL;
import java.util.EnumMap;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

import model.TrafficCalculator;
import model.TrafficLight;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.Animation;

import java.util.Set;
import java.util.HashSet;

import javafx.scene.shape.Rectangle;
import javafx.scene.layout.Pane;







public class TrafficController implements Initializable {



    // FXML'deki Circle'lar
    @FXML private Circle northRed, northYellow, northGreen;
    @FXML private Circle southRed, southYellow, southGreen;
    @FXML private Circle eastRed, eastYellow, eastGreen;
    @FXML private Circle westRed, westYellow, westGreen;
    private Set<Direction> servedDirections = new HashSet<>();
    private int elapsedTime = 0;
    private final int YELLOW_TIME = 3;
    private final int TOTAL_CYCLE_TIME = 120;


    // Her yön için bir TrafficLamp nesnesi
    private final Map<Direction, TrafficLamp> lamps = new EnumMap<>(Direction.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        lamps.put(Direction.NORTH, new TrafficLamp(northRed, northYellow, northGreen));
        lamps.put(Direction.SOUTH, new TrafficLamp(southRed, southYellow, southGreen));
        lamps.put(Direction.EAST,  new TrafficLamp(eastRed,  eastYellow,  eastGreen));
        lamps.put(Direction.WEST,  new TrafficLamp(westRed,  westYellow,  westGreen));

        // Başlangıçta tüm ışıkları soluk yap
        for (TrafficLamp lamp : lamps.values()) {
            lamp.reset();
        }
        vehicleManager = new VehicleManager(simulationPane1);
        vehicleManager.setWestReference(carW1); // ← bu metod birazdan oluşturulacak

// WEST yönü için FXML'deki araçları kuyruğa ekle
        vehicleManager.addInitialVehicle(Direction.WEST, carW1);
        vehicleManager.addInitialVehicle(Direction.WEST, carW2);
        vehicleManager.addInitialVehicle(Direction.WEST, carW3);
        vehicleManager.addInitialVehicle(Direction.WEST, carW4);
        vehicleManager.addInitialVehicle(Direction.WEST, carW5);
        vehicleManager.addInitialVehicle(Direction.WEST, carW6);

    }

    // Aktif ışığı yak, diğerlerini kırmızı yap
    public void setActiveLight(Direction active, String color) {
        for (Direction dir : Direction.values()) {
            if (dir == active) lamps.get(dir).set(color);
            else lamps.get(dir).set("RED");
        }
    }

    private int getMostCrowdedIndex(Direction exclude) {
        int max = -1, index = 0;
        for (int i = 0; i < directions.length; i++) {
            if (directions[i] == exclude) continue;
            int count = calculator.getCount(directions[i]);
            if (count > max) {
                max = count;
                index = i;
            }
        }
        return index;
    }
    private int getMostCrowdedIndex() {
        int max = -1, index = 0;
        for (int i = 0; i < directions.length; i++) {
            int count = calculator.getCount(directions[i]);
            if (count > max) {
                max = count;
                index = i;
            }
        }
        return index;
    }
    private int getMostCrowdedIndexExcluding(Set<Direction> excluded) {
        int max = -1, index = 0;
        for (int i = 0; i < directions.length; i++) {
            if (excluded.contains(directions[i])) continue;
            int count = calculator.getCount(directions[i]);
            if (count > max) {
                max = count;
                index = i;
            }
        }
        return index;
    }



    @FXML private javafx.scene.control.Button btnStart;
    @FXML private javafx.scene.control.Label northVehicleCountLabel, southVehicleCountLabel, eastVehicleCountLabel, westVehicleCountLabel;

    private TrafficCalculator calculator;
    private TrafficLight[] lights;
    private Direction[] directions = Direction.values();
    private int currentIndex = 0;
    private Direction currentDirection;

    private Timeline timeline;
    private void startTimeline() {
        if (timeline != null) timeline.stop();

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            calculator.simulateSecond();
            updateVehicleCountLabels();

            TrafficLight currentLight = lights[currentIndex];
            currentLight.tick();
            updateRemainingTimeLabel(currentDirection, currentLight.getRemainingTime());

            if (currentDirection == Direction.WEST && currentLight.getState() == TrafficLight.LightState.GREEN) {
                vehicleManager.moveVehiclesWest();
            }


            if (currentLight.getRemainingTime() <= 0) {
                if (currentLight.getState() == TrafficLight.LightState.GREEN) {
                    currentLight.setState(TrafficLight.LightState.YELLOW);
                    setActiveLight(currentDirection, "YELLOW");
                } else if (currentLight.getState() == TrafficLight.LightState.YELLOW) {
                    currentLight.setState(TrafficLight.LightState.RED);
                    setActiveLight(currentDirection, "RED");

                    elapsedTime += currentLight.getGreenDuration() + YELLOW_TIME;
                    servedDirections.add(currentDirection);

                    if (servedDirections.size() == 4) {
                        lights = calculator.calculateInitialLights();
                        showCalculatedTimes();
                        servedDirections.clear();
                        elapsedTime = 0;
                    }

                    Set<Direction> remaining = new HashSet<>();
                    for (Direction dir : directions) {
                        if (!servedDirections.contains(dir)) remaining.add(dir);
                    }

                    int remainingTime = TOTAL_CYCLE_TIME - elapsedTime;
                    if (!remaining.isEmpty()) {
                        lights = calculator.calculateLightsFromDirections(remaining, remainingTime);
                        showCalculatedTimes();
                    }

                    currentIndex = getMostCrowdedIndexExcluding(servedDirections);
                    currentDirection = directions[currentIndex];
                    lights[currentIndex].setState(TrafficLight.LightState.GREEN);
                    setActiveLight(currentDirection, "GREEN");
                }
            }
        }));

        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();
        TrafficLight currentLight = lights[currentIndex];

        if (currentLight.getState() == TrafficLight.LightState.GREEN) {
            if (currentDirection == Direction.WEST) {
                vehicleManager.moveVehiclesWest();
            }
        }




    }
    private void updateVehicleCountLabels() {
        northVehicleCountLabel.setText("North: " + calculator.getCount(Direction.NORTH));
        southVehicleCountLabel.setText("South: " + calculator.getCount(Direction.SOUTH));
        eastVehicleCountLabel.setText("East: " + calculator.getCount(Direction.EAST));
        westVehicleCountLabel.setText("West: " + calculator.getCount(Direction.WEST));
    }



    @FXML private javafx.scene.control.Label calcNorthLabel, calcSouthLabel, calcEastLabel, calcWestLabel;
    @FXML private javafx.scene.control.Label remainNorthLabel, remainSouthLabel, remainEastLabel, remainWestLabel;

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

        // Işıkları kapat
        for (TrafficLamp lamp : lamps.values()) lamp.reset();

        // Etiketleri sıfırla
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

        // Simülasyon verilerini temizle
        currentIndex = 0;
        calculator = null;
        lights = null;
    }
    private Integer parseInput(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            return Math.max(0, value); // negatifse sıfırla
        } catch (NumberFormatException e) {
            return null; // boş veya geçersizse random üretilecek
        }
    }

    @FXML private javafx.scene.control.TextField inputNorth, inputSouth, inputEast, inputWest;

    @FXML
    public void startSimulation() {
        calculator = new TrafficCalculator();

        Integer n = parseInput(inputNorth.getText());
        Integer s = parseInput(inputSouth.getText());
        Integer e = parseInput(inputEast.getText());
        Integer w = parseInput(inputWest.getText());

        calculator.setInitialCounts(n, s, e, w); // null olmayan değerler kullanılır

        lights = calculator.calculateInitialLights();
        showCalculatedTimes();
        updateVehicleCountLabels();

        servedDirections.clear();
        elapsedTime = 0;

        currentIndex = getMostCrowdedIndex();
        currentDirection = directions[currentIndex];
        lights[currentIndex].setState(TrafficLight.LightState.GREEN);
        setActiveLight(currentDirection, "GREEN");

        startTimeline();
    }
    @FXML private Rectangle carW1, carW2, carW3, carW4, carW5, carW6;
    @FXML private Pane simulationPane1;

    private VehicleManager vehicleManager;









}
