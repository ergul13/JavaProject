package view;

import controller.TrafficController;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;
import model.Direction;

public class TrafficView {
    private TrafficController controller;

    private Circle northLight, southLight, eastLight, westLight;
    private Rectangle vehicleWest;

    public TrafficView(TrafficController controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        Pane rootPane = new Pane();
        rootPane.setPrefSize(1000, 800);
        rootPane.setStyle("-fx-background-color: seagreen;");

        // Gerçekçi yol renkleri
        Color roadColor = Color.web("#2f2f2f");
        Color lineColor = Color.web("#ffffff");

        // Yollar
        Rectangle verticalRoad = new Rectangle(450, 0, 100, 800);
        Rectangle horizontalRoad = new Rectangle(0, 350, 1000, 100);
        verticalRoad.setFill(roadColor);
        horizontalRoad.setFill(roadColor);

        rootPane.getChildren().addAll(verticalRoad, horizontalRoad);

        // Ortadaki kavşak
        Rectangle junction = new Rectangle(450, 350, 100, 100);
        junction.setFill(Color.DARKGRAY);
        rootPane.getChildren().add(junction);

        // Şerit çizgileri
        for (int y = 0; y < 800; y += 40) {
            Line line = new Line(500, y, 500, y + 20);
            line.setStroke(lineColor);
            rootPane.getChildren().add(line);
        }

        for (int x = 0; x < 1000; x += 40) {
            Line line = new Line(x, 400, x + 20, 400);
            line.setStroke(lineColor);
            rootPane.getChildren().add(line);
        }

        // Işıklar
        northLight = new Circle(10, Color.RED);
        northLight.setLayoutX(495);
        northLight.setLayoutY(330);

        southLight = new Circle(10, Color.RED);
        southLight.setLayoutX(505);
        southLight.setLayoutY(470);

        eastLight = new Circle(10, Color.RED);
        eastLight.setLayoutX(570);
        eastLight.setLayoutY(405);

        westLight = new Circle(10, Color.RED);
        westLight.setLayoutX(430);
        westLight.setLayoutY(395);

        rootPane.getChildren().addAll(northLight, southLight, eastLight, westLight);

        // Araç
        vehicleWest = new Rectangle(30, 15, Color.DODGERBLUE);
        vehicleWest.setLayoutX(0);
        vehicleWest.setLayoutY(392);
        rootPane.getChildren().add(vehicleWest);

        VBox controlPanel = new VBox(10);
        controlPanel.setPadding(new Insets(10));
        controlPanel.setLayoutX(820);
        controlPanel.setLayoutY(50);
        controlPanel.setPrefWidth(170);

        TextField northInput = new TextField();
        northInput.setPromptText("North araç sayısı");

        TextField southInput = new TextField();
        southInput.setPromptText("South araç sayısı");

        TextField eastInput = new TextField();
        eastInput.setPromptText("East araç sayısı");

        TextField westInput = new TextField();
        westInput.setPromptText("West araç sayısı");

        CheckBox turnRightCheck = new CheckBox("Sağa Dön (West)");

        Button startButton = new Button("Start");
        Button resetButton = new Button("Reset");

        Label northTime = new Label("North: 0s");
        Label southTime = new Label("South: 0s");
        Label eastTime = new Label("East: 0s");
        Label westTime = new Label("West: 0s");

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

                northLight.setFill(Color.RED);
                southLight.setFill(Color.RED);
                eastLight.setFill(Color.RED);
                westLight.setFill(Color.RED);

                var maxLight = controller.getManager().getAllLights().stream()
                        .max((a, b) -> Integer.compare(a.getGreenTime(), b.getGreenTime()))
                        .orElse(null);

                if (maxLight != null) {
                    switch (maxLight.getDirection()) {
                        case NORTH -> northLight.setFill(Color.GREEN);
                        case SOUTH -> southLight.setFill(Color.GREEN);
                        case EAST -> eastLight.setFill(Color.GREEN);
                        case WEST -> {
                            westLight.setFill(Color.GREEN);
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
                new Alert(Alert.AlertType.ERROR, "Lütfen tüm yönler için geçerli bir sayı girin.").showAndWait();
            }
        });

        resetButton.setOnAction(e -> {
            northInput.clear();
            southInput.clear();
            eastInput.clear();
            westInput.clear();
            turnRightCheck.setSelected(false);

            northTime.setText("North: 0s");
            southTime.setText("South: 0s");
            eastTime.setText("East: 0s");
            westTime.setText("West: 0s");

            northLight.setFill(Color.RED);
            southLight.setFill(Color.RED);
            eastLight.setFill(Color.RED);
            westLight.setFill(Color.RED);

            vehicleWest.setLayoutX(0);
            vehicleWest.setLayoutY(392);
        });

        controlPanel.getChildren().addAll(
                new Label("Araç Sayıları:"),
                northInput, southInput, eastInput, westInput,
                turnRightCheck,
                startButton, resetButton,
                new Label("Yeşil Işık Süreleri:"),
                northTime, southTime, eastTime, westTime
        );

        rootPane.getChildren().add(controlPanel);

        Scene scene = new Scene(rootPane, 1000, 800);
        stage.setTitle("Trafik Işık Kontrol Sistemi");
        stage.setScene(scene);
        stage.show();
    }

    private void animateVehicleStraight(Rectangle vehicle) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(40), e -> {
                    if (vehicle.getLayoutX() < 1000) {
                        vehicle.setLayoutX(vehicle.getLayoutX() + 4);
                    }
                })
        );
        timeline.setCycleCount(150);
        timeline.play();
    }

    private void animateTurnRight(Rectangle vehicle) {
        Timeline timeline = new Timeline();
        timeline.getKeyFrames().add(new KeyFrame(Duration.millis(25), e -> {
            double x = vehicle.getLayoutX();
            double y = vehicle.getLayoutY();
            if (x < 450) {
                vehicle.setLayoutX(x + 3);
            } else if (y > 405) {
                vehicle.setLayoutY(y - 3);
            }
        }));
        timeline.setCycleCount(100);
        timeline.play();
    }
}
