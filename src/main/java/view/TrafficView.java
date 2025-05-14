package view;

import controller.TrafficController;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

public class TrafficView {
    private TrafficController controller;

    public TrafficView(TrafficController controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(800, 800);
        root.setStyle("-fx-background-color: white;");

        Color roadColor = Color.web("#cfd2d4");
        Color laneLineColor = Color.LIGHTBLUE;

        // YOLLAR (KALIN)
        Rectangle verticalRoad = new Rectangle(350, 0, 100, 800);
        Rectangle horizontalRoad = new Rectangle(0, 350, 800, 100);
        verticalRoad.setFill(roadColor);
        horizontalRoad.setFill(roadColor);
        root.getChildren().addAll(verticalRoad, horizontalRoad);

        // ŞERİT ÇİZGİLERİ (her yöne)
        for (int i = 0; i < 6; i++) {
            double offset = i * 10;
            root.getChildren().add(new Line(400 + offset - 25, 0, 400 + offset - 25, 350));
            root.getChildren().add(new Line(400 + offset - 25, 450, 400 + offset - 25, 800));
            root.getChildren().add(new Line(0, 400 + offset - 25, 350, 400 + offset - 25));
            root.getChildren().add(new Line(450, 400 + offset - 25, 800, 400 + offset - 25));
        }
        root.getChildren().forEach(n -> {
            if (n instanceof Line) {
                ((Line) n).setStroke(laneLineColor);
                ((Line) n).getStrokeDashArray().addAll(4.0, 4.0);
            }
        });

        // TRAFİK IŞIKLARI (Yolun kenarına, 4 yön)
        root.getChildren().addAll(
                trafficLight(300, 300, 0),
                trafficLight(470, 300, 0),
                trafficLight(300, 470, 0),
                trafficLight(470, 470, 0)
        );

        // ARAÇLAR
        root.getChildren().addAll(
                car(390, 60, Color.DARKGREEN),   // North
                car(390, 120, Color.DEEPSKYBLUE),
                car(390, 180, Color.ORANGE),
                car(390, 240, Color.NAVY),

                car(60, 390, Color.ORANGE),      // West
                car(120, 390, Color.DARKBLUE),

                car(660, 390, Color.DARKGREEN),  // East
                car(390, 660, Color.DEEPSKYBLUE), // South
                car(390, 720, Color.PURPLE)
        );

        // OKLAR VE SAYILAR
        root.getChildren().addAll(
                directionArrow(400, 320, 400, 280), // North
                directionArrow(320, 400, 280, 400), // West
                directionArrow(400, 480, 400, 520), // South
                directionArrow(480, 400, 520, 400), // East

                numberLabel("96", 530, 300, Color.RED), // East
                numberLabel("60", 200, 300, Color.GREEN), // West
                numberLabel("63", 500, 650, Color.RED), // South
                numberLabel("114", 200, 450, Color.RED) // West2
        );

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Referans Görsel Statik Çizimi");
        stage.show();
    }

    private Group trafficLight(double x, double y, int rotation) {
        Rectangle base = new Rectangle(x, y, 40, 20);
        base.setFill(Color.BLACK);
        Circle red = new Circle(x + 10, y + 10, 5, Color.RED);
        Circle yellow = new Circle(x + 20, y + 10, 5, Color.YELLOW);
        Circle green = new Circle(x + 30, y + 10, 5, Color.LIMEGREEN);
        return new Group(base, red, yellow, green);
    }

    private Rectangle car(double x, double y, Color color) {
        Rectangle car = new Rectangle(x, y, 20, 30);
        car.setArcHeight(4);
        car.setArcWidth(4);
        car.setFill(color);
        car.setStroke(Color.BLACK);
        return car;
    }

    private Polygon directionArrow(double startX, double startY, double endX, double endY) {
        Polygon arrow = new Polygon();
        arrow.getPoints().addAll(
                startX, startY,
                endX - 5, endY - 5,
                endX + 5, endY - 5
        );
        arrow.setFill(Color.DODGERBLUE);
        return arrow;
    }

    private Label numberLabel(String text, double x, double y, Color color) {
        Label label = new Label(text);
        label.setTextFill(color);
        label.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        label.setLayoutX(x);
        label.setLayoutY(y);
        return label;
    }
}
