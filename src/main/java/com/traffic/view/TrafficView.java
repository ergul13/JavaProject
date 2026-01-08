package com.traffic.view;

import com.traffic.model.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

/**
 * MVC View sinifi - Trafik simulasyonunun gorsel arayuzunu yonetir.
 */
public class TrafficView extends BorderPane {
    private static final int CANVAS_SIZE = 800;
    private static final int ROAD_WIDTH = 100;
    private static final int ROAD_START = 350;
    private static final int ROAD_END = 450;

    private Canvas canvas;
    private TextField tfNorth, tfSouth, tfEast, tfWest;
    private Button btnStart, btnPause, btnReset, btnRandom;
    private Label lblNorthInfo, lblSouthInfo, lblEastInfo, lblWestInfo;
    private Label lblTotalTime, lblCarsPassed, lblRemainingCars;
    private Label lblCurrentPhase, lblStatus;

    public TrafficView() {
        initializeUI();
    }

    private void initializeUI() {
        VBox controlPanel = createControlPanel();
        this.setLeft(controlPanel);

        Pane simulationPane = new Pane();
        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        simulationPane.getChildren().add(canvas);
        simulationPane.setStyle("-fx-background-color: #2d2d2d;");
        this.setCenter(simulationPane);
    }

    /**
     * Sol kontrol panelini oluşturur.
     */
    private VBox createControlPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); " +
                       "-fx-border-color: #1a252f; -fx-border-width: 0 3 0 0;");
        panel.setPrefWidth(240);

        // Baslik
        Label titleLabel = new Label("Trafik Kontrol Sistemi");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 15));
        titleLabel.setStyle("-fx-text-fill: #ecf0f1;");

        Separator sep1 = createStyledSeparator();

        // Arac Yogunlugu Girisi
        Label inputTitle = new Label("Arac Yogunlugu");
        inputTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        inputTitle.setStyle("-fx-text-fill: #3498db;");

        tfNorth = createStyledTextField("10");
        tfSouth = createStyledTextField("10");
        tfEast = createStyledTextField("10");
        tfWest = createStyledTextField("10");

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(8);

        inputGrid.add(createDirectionLabel("Kuzey:"), 0, 0);
        inputGrid.add(tfNorth, 1, 0);
        inputGrid.add(createDirectionLabel("Guney:"), 0, 1);
        inputGrid.add(tfSouth, 1, 1);
        inputGrid.add(createDirectionLabel("Dogu:"), 0, 2);
        inputGrid.add(tfEast, 1, 2);
        inputGrid.add(createDirectionLabel("Bati:"), 0, 3);
        inputGrid.add(tfWest, 1, 3);

        btnRandom = createStyledButton("Rastgele", "#9b59b6", "#8e44ad");
        btnRandom.setMaxWidth(Double.MAX_VALUE);

        Separator sep2 = createStyledSeparator();

        // Kontrol butonlari
        Label controlTitle = new Label("Kontroller");
        controlTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        controlTitle.setStyle("-fx-text-fill: #3498db;");

        btnStart = createStyledButton("Baslat", "#27ae60", "#1e8449");
        btnPause = createStyledButton("Durdur", "#f39c12", "#d68910");
        btnReset = createStyledButton("Sifirla", "#e74c3c", "#c0392b");

        btnStart.setPrefWidth(65);
        btnPause.setPrefWidth(65);
        btnReset.setPrefWidth(65);
        btnPause.setDisable(true);

        HBox buttonBox = new HBox(8, btnStart, btnPause, btnReset);
        buttonBox.setAlignment(Pos.CENTER);

        Separator sep3 = createStyledSeparator();

        // Yesil Isik Sureleri
        Label infoTitle = new Label("Yesil Isik Sureleri");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        infoTitle.setStyle("-fx-text-fill: #2ecc71;");

        lblNorthInfo = createStyledInfoLabel("Kuzey: --");
        lblSouthInfo = createStyledInfoLabel("Guney: --");
        lblEastInfo = createStyledInfoLabel("Dogu:  --");
        lblWestInfo = createStyledInfoLabel("Bati:  --");

        VBox greenTimesBox = new VBox(5, lblNorthInfo, lblSouthInfo, lblEastInfo, lblWestInfo);
        greenTimesBox.setStyle("-fx-background-color: rgba(46, 204, 113, 0.1); " +
                              "-fx-padding: 10; -fx-background-radius: 6; " +
                              "-fx-border-color: #27ae60; -fx-border-radius: 6; -fx-border-width: 1;");

        Separator sep4 = createStyledSeparator();

        // Simulasyon Durumu
        Label statusTitle = new Label("Simulasyon Durumu");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
        statusTitle.setStyle("-fx-text-fill: #e67e22;");

        lblCurrentPhase = createStyledInfoLabel("Aktif Yon: --");
        lblTotalTime = createStyledInfoLabel("Sure: 0.0s");
        lblCarsPassed = createStyledInfoLabel("Gecen: 0 arac");
        lblRemainingCars = createStyledInfoLabel("Kalan: 0 arac");
        lblStatus = createStyledInfoLabel("Durum: Bekliyor");

        VBox statusBox = new VBox(5, lblCurrentPhase, lblTotalTime, lblCarsPassed, lblRemainingCars, lblStatus);
        statusBox.setStyle("-fx-background-color: rgba(230, 126, 34, 0.1); " +
                          "-fx-padding: 10; -fx-background-radius: 6; " +
                          "-fx-border-color: #e67e22; -fx-border-radius: 6; -fx-border-width: 1;");

        // Tümünü panele ekle
        panel.getChildren().addAll(
                titleLabel, sep1,
                inputTitle, inputGrid, btnRandom, sep2,
                controlTitle, buttonBox, sep3,
                infoTitle, greenTimesBox, sep4,
                statusTitle, statusBox
        );

        return panel;
    }

    private Separator createStyledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #465c6e;");
        return sep;
    }

    private Label createDirectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #bdc3c7;");
        return label;
    }

    private TextField createStyledTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setPrefWidth(90);
        tf.setStyle("-fx-background-color: #ecf0f1; " +
                   "-fx-border-color: #3498db; -fx-border-radius: 5; " +
                   "-fx-background-radius: 5; -fx-font-size: 13; " +
                   "-fx-font-weight: bold; -fx-alignment: center;");
        return tf;
    }

    private Button createStyledButton(String text, String bgColor, String hoverColor) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-font-size: 12;");
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: " + hoverColor + "; " +
                    "-fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-font-size: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: " + bgColor + "; " +
                    "-fx-text-fill: white; -fx-font-weight: bold; " +
                    "-fx-background-radius: 8; -fx-cursor: hand; " +
                    "-fx-font-size: 12;"));
        return btn;
    }

    private Label createStyledInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", FontWeight.NORMAL, 12));
        label.setStyle("-fx-text-fill: #ecf0f1;");
        return label;
    }

    public void draw(TrafficModel model) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        drawBackground(gc);
        drawRoads(gc);
        drawStopLines(gc);
        drawTrafficLights(gc, model);
        drawCars(gc, model);
        updateInfoPanel(model);
    }

    private void drawBackground(GraphicsContext gc) {
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        gc.setFill(Color.rgb(34, 85, 34));
        gc.fillRect(0, 0, ROAD_START, ROAD_START);
        gc.fillRect(ROAD_END, 0, ROAD_START, ROAD_START);
        gc.fillRect(0, ROAD_END, ROAD_START, ROAD_START);
        gc.fillRect(ROAD_END, ROAD_END, ROAD_START, ROAD_START);
    }

    private void drawRoads(GraphicsContext gc) {
        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRect(ROAD_START, 0, ROAD_WIDTH, CANVAS_SIZE);
        gc.fillRect(0, ROAD_START, CANVAS_SIZE, ROAD_WIDTH);

        gc.setFill(Color.rgb(70, 70, 70));
        gc.fillRect(ROAD_START, ROAD_START, ROAD_WIDTH, ROAD_WIDTH);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.setLineDashes(15, 10);

        gc.strokeLine(400, 0, 400, ROAD_START);
        gc.strokeLine(400, ROAD_END, 400, CANVAS_SIZE);
        gc.strokeLine(0, 400, ROAD_START, 400);
        gc.strokeLine(ROAD_END, 400, CANVAS_SIZE, 400);

        gc.setLineDashes(null);

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);

        gc.strokeLine(ROAD_START, 0, ROAD_START, ROAD_START);
        gc.strokeLine(ROAD_START, ROAD_END, ROAD_START, CANVAS_SIZE);
        gc.strokeLine(ROAD_END, 0, ROAD_END, ROAD_START);
        gc.strokeLine(ROAD_END, ROAD_END, ROAD_END, CANVAS_SIZE);

        gc.strokeLine(0, ROAD_START, ROAD_START, ROAD_START);
        gc.strokeLine(ROAD_END, ROAD_START, CANVAS_SIZE, ROAD_START);
        gc.strokeLine(0, ROAD_END, ROAD_START, ROAD_END);
        gc.strokeLine(ROAD_END, ROAD_END, CANVAS_SIZE, ROAD_END);
    }

    private void drawStopLines(GraphicsContext gc) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);
        gc.strokeLine(ROAD_START + 5, Car.STOP_NORTH, 400 - 5, Car.STOP_NORTH);
        gc.strokeLine(400 + 5, Car.STOP_SOUTH, ROAD_END - 5, Car.STOP_SOUTH);
        gc.strokeLine(Car.STOP_EAST, ROAD_START + 5, Car.STOP_EAST, 400 - 5);
        gc.strokeLine(Car.STOP_WEST, 400 + 5, Car.STOP_WEST, ROAD_END - 5);
    }

    private void drawTrafficLights(GraphicsContext gc, TrafficModel model) {
        drawSingleTrafficLight(gc, 315, 280, Direction.NORTH, model);
        drawSingleTrafficLight(gc, 460, 455, Direction.SOUTH, model);
        drawSingleTrafficLight(gc, 455, 280, Direction.EAST, model);
        drawSingleTrafficLight(gc, 315, 455, Direction.WEST, model);
    }

    private void drawSingleTrafficLight(GraphicsContext gc, double x, double y, Direction dir, TrafficModel model) {
        TrafficLight light = model.getTrafficLight(dir);

        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRoundRect(x, y, 30, 65, 5, 5);

        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, 30, 65, 5, 5);

        Color redColor = Color.rgb(60, 20, 20);
        Color yellowColor = Color.rgb(60, 60, 20);
        Color greenColor = Color.rgb(20, 60, 20);

        switch (light.getState()) {
            case RED:
                redColor = Color.rgb(255, 50, 50);
                break;
            case YELLOW:
                yellowColor = Color.rgb(255, 255, 50);
                break;
            case GREEN:
                greenColor = Color.rgb(50, 255, 50);
                break;
        }

        gc.setFill(redColor);
        gc.fillOval(x + 5, y + 5, 20, 20);

        gc.setFill(yellowColor);
        gc.fillOval(x + 5, y + 25, 20, 20);

        gc.setFill(greenColor);
        gc.fillOval(x + 5, y + 45, 20, 20);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        String countdownText = String.format("%.0f", Math.max(0, light.getCountdown()));
        gc.fillText(countdownText, x + 8, y - 5);

        gc.setFont(Font.font("System", 10));
        gc.fillText(dir.getDisplayName(), x, y + 78);
    }

    private void drawCars(GraphicsContext gc, TrafficModel model) {
        for (Car car : model.getCars()) {
            if (car.hasPassed()) continue;
            drawRotatedCar(gc, car.getX(), car.getY(), car.getAngle(), car.getColor(), car.getTurnDirection());
        }
    }

    private void drawRotatedCar(GraphicsContext gc, double x, double y, double angle, Color carColor, TurnDirection turn) {
        int w = Car.CAR_WIDTH;
        int h = Car.CAR_LENGTH;
        double centerX = x + w / 2.0;
        double centerY = y + h / 2.0;

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(-angle);
        gc.translate(-w / 2.0, -h / 2.0);

        gc.setFill(carColor);
        gc.fillRoundRect(0, 0, w, h, 5, 5);
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeRoundRect(0, 0, w, h, 5, 5);

        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(3, 5, w - 6, 10);
        gc.fillRect(3, h - 15, w - 6, 8);

        gc.setFill(Color.YELLOW);
        gc.fillOval(3, h - 5, 5, 5);
        gc.fillOval(w - 8, h - 5, 5, 5);

        gc.setFill(Color.DARKRED);
        gc.fillOval(3, 0, 4, 4);
        gc.fillOval(w - 7, 0, 4, 4);

        if (turn != TurnDirection.STRAIGHT) {
            gc.setFill(Color.ORANGE);
            if (turn == TurnDirection.LEFT) {
                gc.fillOval(-2, h - 10, 4, 4);
            } else {
                gc.fillOval(w - 2, h - 10, 4, 4);
            }
        }

        // Canvas state'i geri yükle
        gc.restore();
    }


    /**
     * Bilgi panelini günceller.
     */
    private void updateInfoPanel(TrafficModel model) {
        lblNorthInfo.setText(String.format("Kuzey: %.1f sn", model.getGreenDuration(Direction.NORTH)));
        lblSouthInfo.setText(String.format("Guney: %.1f sn", model.getGreenDuration(Direction.SOUTH)));
        lblEastInfo.setText(String.format("Dogu:  %.1f sn", model.getGreenDuration(Direction.EAST)));
        lblWestInfo.setText(String.format("Bati:  %.1f sn", model.getGreenDuration(Direction.WEST)));

        lblCurrentPhase.setText(String.format("Aktif: %s (%s)",
                model.getCurrentDirection().getDisplayName(),
                model.getCurrentLightState().getDisplayName()));
        lblTotalTime.setText(String.format("Sure: %.1f / %d sn",
                model.getTotalElapsedTime(), TrafficModel.TOTAL_CYCLE_TIME));
        lblCarsPassed.setText(String.format("Gecen: %d arac", model.getTotalCarsPassed()));
        lblRemainingCars.setText(String.format("Kalan: %d arac", model.getRemainingCars()));

        if (model.isCycleComplete()) {
            lblStatus.setText("Durum: Tamamlandi");
            lblStatus.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else if (model.isRunning()) {
            lblStatus.setText("Durum: Calisiyor");
            lblStatus.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        } else if (model.isDataLoaded()) {
            lblStatus.setText("Durum: Duraklatildi");
            lblStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            lblStatus.setText("Durum: Bekliyor");
            lblStatus.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
        }
    }

    // Getter metodları (Controller için)
    public TextField getTfNorth() { return tfNorth; }
    public TextField getTfSouth() { return tfSouth; }
    public TextField getTfEast() { return tfEast; }
    public TextField getTfWest() { return tfWest; }

    public Button getBtnStart() { return btnStart; }
    public Button getBtnPause() { return btnPause; }
    public Button getBtnReset() { return btnReset; }
    public Button getBtnRandom() { return btnRandom; }
}
