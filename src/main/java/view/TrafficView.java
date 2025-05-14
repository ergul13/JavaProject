package view;

import controller.TrafficController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Ellipse;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Direction;

public class TrafficView {
    private TrafficController controller;
    private Rectangle vehicleWest;
    private Group trafficLightNorth, trafficLightSouth, trafficLightEast, trafficLightWest;

    public TrafficView(TrafficController controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(1000, 800);
        root.setStyle("-fx-background-color: seagreen;");

        Rectangle verticalRoad = new Rectangle(470, 0, 60, 800);
        Rectangle horizontalRoad = new Rectangle(0, 370, 1000, 60);
        verticalRoad.setFill(Color.web("#2f2f2f"));
        horizontalRoad.setFill(Color.web("#2f2f2f"));
        root.getChildren().addAll(verticalRoad, horizontalRoad);

        Ellipse circle = new Ellipse(500, 400, 80, 80);
        circle.setFill(Color.DARKGRAY);
        root.getChildren().add(circle);

        for (int y = 0; y < 800; y += 40) {
            Line line = new Line(500, y, 500, y + 20);
            line.setStroke(Color.WHITE);
            root.getChildren().add(line);
        }
        for (int x = 0; x < 1000; x += 40) {
            Line line = new Line(x, 400, x + 20, 400);
            line.setStroke(Color.WHITE);
            root.getChildren().add(line);
        }

        trafficLightNorth = createTrafficLight(490, 270);
        trafficLightSouth = createTrafficLight(490, 520);
        trafficLightEast = createTrafficLight(580, 390);
        trafficLightWest = createTrafficLight(370, 390);
        root.getChildren().addAll(trafficLightNorth, trafficLightSouth, trafficLightEast, trafficLightWest);

        vehicleWest = new Rectangle(30, 15, Color.DODGERBLUE);
        vehicleWest.setLayoutX(0);
        vehicleWest.setLayoutY(392);
        root.getChildren().add(vehicleWest);

        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setLayoutX(820);
        panel.setLayoutY(50);
        TextField northInput = new TextField("0");
        TextField southInput = new TextField("0");
        TextField eastInput = new TextField("0");
        TextField westInput = new TextField("0");
        CheckBox turnRightCheck = new CheckBox("Sağa Dön (West)");
        Button startButton = new Button("Start");
        Button resetButton = new Button("Reset");
        Label northTime = new Label("North: 0s");
        Label southTime = new Label("South: 0s");
        Label eastTime = new Label("East: 0s");
        Label westTime = new Label("West: 0s");

        panel.getChildren().addAll(
                new Label("Araç Sayıları:"),
                northInput, southInput, eastInput, westInput,
                turnRightCheck, startButton, resetButton,
                new Label("Yeşil Işık Süreleri:"),
                northTime, southTime, eastTime, westTime
        );
        root.getChildren().add(panel);

        startButton.setOnAction(e -> {
            try {
                int n = Integer.parseInt(northInput.getText());
                int s = Integer.parseInt(southInput.getText());
                int eDir = Integer.parseInt(eastInput.getText());
                int w = Integer.parseInt(westInput.getText());
                controller.setVehicleDensity(Direction.NORTH, n);
                controller.setVehicleDensity(Direction.SOUTH, s);
                controller.setVehicleDensity(Direction.EAST, eDir);
                controller.setVehicleDensity(Direction.WEST, w);
                controller.updateSignalTimings();
                setAllLights(Color.RED);
                var maxLight = controller.getManager().getAllLights().stream()
                        .max((a, b) -> Integer.compare(a.getGreenTime(), b.getGreenTime())).orElse(null);
                if (maxLight != null) {
                    switch (maxLight.getDirection()) {
                        case NORTH -> setTrafficLightColor(trafficLightNorth, Color.GREEN);
                        case SOUTH -> setTrafficLightColor(trafficLightSouth, Color.GREEN);
                        case EAST -> setTrafficLightColor(trafficLightEast, Color.GREEN);
                        case WEST -> {
                            setTrafficLightColor(trafficLightWest, Color.GREEN);
                            if (turnRightCheck.isSelected()) {
                                animateTurnRight(vehicleWest);
                            } else {
                                animateVehicleStraight(vehicleWest);
                            }
                        }
                    }
                }
                for (var light : controller.getManager().getAllLights()) {
                    switch (light.getDirection()) {
                        case NORTH -> northTime.setText("North: " + light.getGreenTime() + "s");
                        case SOUTH -> southTime.setText("South: " + light.getGreenTime() + "s");
                        case EAST -> eastTime.setText("East: " + light.getGreenTime() + "s");
                        case WEST -> westTime.setText("West: " + light.getGreenTime() + "s");
                    }
                }
            } catch (NumberFormatException ex) {
                new Alert(Alert.AlertType.ERROR, "Geçerli sayılar girin.").showAndWait();
            }
        });

        resetButton.setOnAction(e -> {
            vehicleWest.setLayoutX(0);
            vehicleWest.setLayoutY(392);
            setAllLights(Color.RED);
        });

        Scene scene = new Scene(root, 1000, 800);
        stage.setTitle("Trafik Kavşak Simülasyonu");
        stage.setScene(scene);
        stage.show();
    }

    private Group createTrafficLight(double x, double y) {
        Rectangle pole = new Rectangle(x, y, 20, 60);
        pole.setArcWidth(10);
        pole.setArcHeight(10);
        pole.setFill(Color.BLACK);
        Circle red = new Circle(x + 10, y + 10, 6, Color.RED);
        Circle yellow = new Circle(x + 10, y + 30, 6, Color.DARKGRAY);
        Circle green = new Circle(x + 10, y + 50, 6, Color.DARKGRAY);
        return new Group(pole, red, yellow, green);
    }

    private void setTrafficLightColor(Group light, Color color) {
        for (javafx.scene.Node node : light.getChildren()) {
            if (node instanceof Circle c) c.setFill(Color.DARKGRAY);
        }
        for (javafx.scene.Node node : light.getChildren()) {
            if (node instanceof Circle c) {
                double offset = c.getCenterY() - ((Rectangle) light.getChildren().get(0)).getY();
                if ((color == Color.RED && offset < 20) ||
                        (color == Color.YELLOW && offset >= 20 && offset < 40) ||
                        (color == Color.GREEN && offset >= 40)) {
                    c.setFill(color);
                }
            }
        }
    }

    private void setAllLights(Color color) {
        setTrafficLightColor(trafficLightNorth, color);
        setTrafficLightColor(trafficLightSouth, color);
        setTrafficLightColor(trafficLightEast, color);
        setTrafficLightColor(trafficLightWest, color);
    }

    private void animateVehicleStraight(Rectangle vehicle) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(30), e -> {
            if (vehicle.getLayoutX() < 1000)
                vehicle.setLayoutX(vehicle.getLayoutX() + 4);
        }));
        t.setCycleCount(150);
        t.play();
    }

    private void animateTurnRight(Rectangle vehicle) {
        Timeline t = new Timeline(new KeyFrame(Duration.millis(25), e -> {
            double x = vehicle.getLayoutX();
            double y = vehicle.getLayoutY();
            if (x < 470) vehicle.setLayoutX(x + 3);
            else if (y > 420) vehicle.setLayoutY(y - 3);
        }));
        t.setCycleCount(100);
        t.play();
    }
}
