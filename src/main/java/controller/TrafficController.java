package controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.shape.Circle;
import model.Direction;
import model.TrafficLamp;
import model.VehicleManager;
import javafx.animation.TranslateTransition;
import javafx.util.Duration;

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

    private int northDur, southDur, eastDur, westDur;
    private final String[] lightOrder = {"NORTH","SOUTH","EAST", "WEST"};
    private int currentPhase = 0;
    private Timeline mainTimeline;
    //yokbişey


    // FXML'deki Circle'lar
    @FXML private Circle northRed, northYellow, northGreen;
    @FXML private Circle southRed, southYellow, southGreen;
    @FXML private Circle eastRed, eastYellow, eastGreen;
    @FXML private Circle westRed, westYellow, westGreen;
    @FXML private Rectangle carN1,carN2,carN3,carN4,carN5,carN6;
    @FXML private Rectangle carS1,carS2,carS3,carS4,carS5,carS6;
    @FXML private Rectangle carE1,carE2,carE3,carE4,carE5,carE6;
    @FXML private Rectangle carW1,carW2,carW3,carW4,carW5,carW6;



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
    private void resetLights() {
        northRed.setOpacity(0.3); northYellow.setOpacity(0.3); northGreen.setOpacity(0.3);
        southRed.setOpacity(0.3); southYellow.setOpacity(0.3); southGreen.setOpacity(0.3);
        eastRed.setOpacity(0.3); eastYellow.setOpacity(0.3); eastGreen.setOpacity(0.3);
        westRed.setOpacity(0.3); westYellow.setOpacity(0.3); westGreen.setOpacity(0.3);
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

    public void moveCarNorth(Rectangle car, int delaySeconds) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
        t.setByY(300);
        t.setDelay(Duration.seconds(delaySeconds));
        t.play();
    }

    public void moveCarSouth(Rectangle car, int delaySeconds) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
        t.setByY(-300);
        t.setDelay(Duration.seconds(delaySeconds));
        t.play();
    }

    public void moveCarEast(Rectangle car, int delaySeconds) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
        t.setByX(-300);
        t.setDelay(Duration.seconds(delaySeconds));
        t.play();
    }

    public void moveCarWest(Rectangle car, int delaySeconds) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
        t.setByX(300);
        t.setDelay(Duration.seconds(delaySeconds));
        t.play();
    }


    @FXML
    public void startSimulation() {

        startLightCycle();

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

    private void startLightCycle() {
        currentPhase = 0;
        runNextPhase(); // İlk fazı başlat
    }
    private void runNextPhase() {
        resetLights();
        String currentDirection = lightOrder[currentPhase];
        int duration;

        switch (currentDirection) {
            case "NORTH" -> {
                northGreen.setOpacity(1);
                duration = northDur;

                moveCarNorth(carN1,0);
                moveCarNorth(carN2,1);
                moveCarNorth(carN3,2);
                moveCarNorth(carN4,3);
                moveCarNorth(carN5,4);
                moveCarNorth(carN6,5);
            }
            case "EAST" -> {
                eastGreen.setOpacity(1);
                duration = eastDur;

                moveCarEast(carE1,0);
                moveCarEast(carE2,1);
                moveCarEast(carE3,2);
                moveCarEast(carE4,3);
                moveCarEast(carE5,4);
                moveCarEast(carE6,5);

            }
            case "SOUTH" -> {
                southGreen.setOpacity(1);
                duration = southDur;

                moveCarSouth(carS1,0);
                moveCarSouth(carS2,1);
                moveCarSouth(carS3,2);
                moveCarSouth(carS4,3);
                moveCarSouth(carS5,4);
                moveCarSouth(carS6,5);
            }
            case "WEST" -> {
                westGreen.setOpacity(1);
                duration = westDur;

                moveCarWest(carW1,0);
                moveCarWest(carW2,1);
                moveCarWest(carW3,2);
                moveCarWest(carW4,3);
                moveCarWest(carW5,4);
                moveCarWest(carW6,5);
            }
            default -> duration = 10;
        }

        mainTimeline = new Timeline(new KeyFrame(Duration.seconds(duration), e -> {
            currentPhase = (currentPhase + 1) % lightOrder.length;
            runNextPhase();
        }));
        mainTimeline.play();
    }

    @FXML private Pane simulationPane1;

    private VehicleManager vehicleManager;









}
