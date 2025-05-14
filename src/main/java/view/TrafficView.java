package view;

import controller.TrafficController;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class TrafficView {
    private TrafficController controller;
    private final Map<String, Integer> vehicleCounts = new HashMap<>();
    private final Map<String, Label> durationLabels = new HashMap<>();

    public TrafficView(TrafficController controller) {
        this.controller = controller;
    }

    public void start(Stage stage) {
        Pane root = new Pane();
        root.setPrefSize(800, 800);
        root.setStyle("-fx-background-color: white;");

        // Çift şeritli yollar
        Rectangle verticalIn = new Rectangle(370, 0, 20, 800);
        Rectangle verticalOut = new Rectangle(410, 0, 20, 800);
        Rectangle horizontalIn = new Rectangle(0, 370, 800, 20);
        Rectangle horizontalOut = new Rectangle(0, 410, 800, 20);

        Color roadColor = Color.LIGHTGRAY;
        verticalIn.setFill(roadColor); verticalOut.setFill(roadColor);
        horizontalIn.setFill(roadColor); horizontalOut.setFill(roadColor);

        root.getChildren().addAll(verticalIn, verticalOut, horizontalIn, horizontalOut);

        // Kavşak merkezi
        Rectangle center = new Rectangle(370, 370, 60, 60);
        center.setFill(Color.DARKGRAY);
        root.getChildren().add(center);

        // Araçlar – yönlerine göre çiz
        root.getChildren().addAll(
                drawVehicle(375, 100, 20, 30, Color.DARKGREEN), // North
                drawVehicle(405, 700, 20, 30, Color.DEEPSKYBLUE), // South
                drawVehicle(100, 375, 30, 20, Color.ORANGE), // West
                drawVehicle(700, 405, 30, 20, Color.PURPLE)  // East
        );

        // Trafik ışıkları (dikey)
        root.getChildren().addAll(
                drawTrafficLight(350, 300), // West
                drawTrafficLight(430, 300), // East
                drawTrafficLight(350, 460), // West
                drawTrafficLight(430, 460)  // East
        );

        // Kontrol paneli
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(10));
        panel.setLayoutX(600);
        panel.setLayoutY(50);

        Label title = new Label("Araç Sayısı");
        TextField northField = new TextField("0");
        TextField southField = new TextField("0");
        TextField eastField = new TextField("0");
        TextField westField = new TextField("0");

        Button randomBtn = new Button("Rastgele Oluştur");
        Button hesaplaBtn = new Button("Işık Sürelerini Hesapla");

        Label northTime = new Label("North: 0 sn");
        Label southTime = new Label("South: 0 sn");
        Label eastTime = new Label("East: 0 sn");
        Label westTime = new Label("West: 0 sn");

        durationLabels.put("NORTH", northTime);
        durationLabels.put("SOUTH", southTime);
        durationLabels.put("EAST", eastTime);
        durationLabels.put("WEST", westTime);

        randomBtn.setOnAction(e -> {
            Random r = new Random();
            northField.setText(String.valueOf(r.nextInt(100)));
            southField.setText(String.valueOf(r.nextInt(100)));
            eastField.setText(String.valueOf(r.nextInt(100)));
            westField.setText(String.valueOf(r.nextInt(100)));
        });

        hesaplaBtn.setOnAction(e -> {
            int n = Integer.parseInt(northField.getText());
            int s = Integer.parseInt(southField.getText());
            int es = Integer.parseInt(eastField.getText());
            int w = Integer.parseInt(westField.getText());

            vehicleCounts.put("NORTH", n);
            vehicleCounts.put("SOUTH", s);
            vehicleCounts.put("EAST", es);
            vehicleCounts.put("WEST", w);

            calculateDurations();
        });

        panel.getChildren().addAll(title,
                new Label("North:"), northField,
                new Label("South:"), southField,
                new Label("East:"), eastField,
                new Label("West:"), westField,
                randomBtn, hesaplaBtn,
                new Label("Yeşil Işık Süreleri:"),
                northTime, southTime, eastTime, westTime
        );
        root.getChildren().add(panel);

        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("Kavşak – Statik Görünüm ve Hesaplama");
        stage.show();
    }

    private Rectangle drawVehicle(double x, double y, double width, double height, Color color) {
        Rectangle car = new Rectangle(width, height, color);
        car.setArcWidth(5);
        car.setArcHeight(5);
        car.setStroke(Color.BLACK);
        car.setLayoutX(x);
        car.setLayoutY(y);
        return car;
    }

    private Group drawTrafficLight(double x, double y) {
        Rectangle base = new Rectangle(x, y, 15, 45);
        base.setFill(Color.BLACK);
        Circle red = new Circle(x + 7.5, y + 7.5, 5, Color.RED);
        Circle yellow = new Circle(x + 7.5, y + 22.5, 5, Color.GOLDENROD);
        Circle green = new Circle(x + 7.5, y + 37.5, 5, Color.DARKGREEN);
        return new Group(base, red, yellow, green);
    }

    private void calculateDurations() {
        int total = vehicleCounts.values().stream().mapToInt(Integer::intValue).sum();
        int cycleTime = 120; // sabit toplam süre
        int min = 10, max = 60;

        for (Map.Entry<String, Integer> entry : vehicleCounts.entrySet()) {
            String dir = entry.getKey();
            int count = entry.getValue();
            int duration = total == 0 ? min :
                    Math.max(min, Math.min(max, (int) ((count / (double) total) * cycleTime)));
            durationLabels.get(dir).setText(dir.substring(0, 1).toUpperCase() + dir.substring(1).toLowerCase() + ": " + duration + " sn");
        }
    }
}
