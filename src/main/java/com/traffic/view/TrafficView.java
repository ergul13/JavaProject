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
 * MVC View sınıfı.
 * Trafik simülasyonunun görsel arayüzünü oluşturur ve günceller.
 */
public class TrafficView extends BorderPane {
    // Canvas boyutları
    private static final int CANVAS_SIZE = 800;
    private static final int ROAD_WIDTH = 100;
    private static final int ROAD_START = 350;
    private static final int ROAD_END = 450;

    // UI Bileşenleri
    private Canvas canvas;
    private TextField tfNorth, tfSouth, tfEast, tfWest;
    private Button btnStart, btnPause, btnReset, btnRandom;

    // Bilgi paneli
    private Label lblNorthInfo, lblSouthInfo, lblEastInfo, lblWestInfo;
    private Label lblTotalTime, lblCarsPassed, lblRemainingCars;
    private Label lblCurrentPhase, lblStatus;

    public TrafficView() {
        initializeUI();
    }

    /**
     * Arayüz bileşenlerini oluşturur.
     */
    private void initializeUI() {
        // Sol kontrol paneli
        VBox controlPanel = createControlPanel();
        this.setLeft(controlPanel);

        // Ana simülasyon alanı
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
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(20));
        panel.setStyle("-fx-background-color: linear-gradient(to bottom, #2c3e50, #34495e); " +
                       "-fx-border-color: #1a252f; -fx-border-width: 0 3 0 0;");
        panel.setPrefWidth(260);

        // Başlık
        Label titleLabel = new Label("🚦 Trafik Kontrol Sistemi");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 16));
        titleLabel.setStyle("-fx-text-fill: #ecf0f1;");
        titleLabel.setWrapText(true);

        // Ayırıcı
        Separator sep1 = createStyledSeparator();

        // Araç Yoğunluğu Girişi
        Label inputTitle = new Label("📊 Araç Yoğunluğu");
        inputTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        inputTitle.setStyle("-fx-text-fill: #3498db;");

        tfNorth = createStyledTextField("10");
        tfSouth = createStyledTextField("10");
        tfEast = createStyledTextField("10");
        tfWest = createStyledTextField("10");

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(12);
        inputGrid.setVgap(8);
        inputGrid.setStyle("-fx-padding: 10;");

        Label lblNorth = createDirectionLabel("⬆ Kuzey:");
        Label lblSouth = createDirectionLabel("⬇ Güney:");
        Label lblEast = createDirectionLabel("➡ Doğu:");
        Label lblWest = createDirectionLabel("⬅ Batı:");

        inputGrid.add(lblNorth, 0, 0);
        inputGrid.add(tfNorth, 1, 0);
        inputGrid.add(lblSouth, 0, 1);
        inputGrid.add(tfSouth, 1, 1);
        inputGrid.add(lblEast, 0, 2);
        inputGrid.add(tfEast, 1, 2);
        inputGrid.add(lblWest, 0, 3);
        inputGrid.add(tfWest, 1, 3);

        // Rastgele butonu
        btnRandom = createStyledButton("🎲 Rastgele Oluştur", "#9b59b6", "#8e44ad");
        btnRandom.setMaxWidth(Double.MAX_VALUE);

        // Ayırıcı
        Separator sep2 = createStyledSeparator();

        // Kontrol butonları
        Label controlTitle = new Label("🎮 Simülasyon Kontrolleri");
        controlTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        controlTitle.setStyle("-fx-text-fill: #3498db;");

        btnStart = createStyledButton("▶ Başlat", "#27ae60", "#1e8449");
        btnPause = createStyledButton("⏸ Durdur", "#f39c12", "#d68910");
        btnReset = createStyledButton("🔄 Sıfırla", "#e74c3c", "#c0392b");

        btnStart.setPrefWidth(70);
        btnPause.setPrefWidth(70);
        btnReset.setPrefWidth(70);
        btnPause.setDisable(true);

        HBox buttonBox = new HBox(8, btnStart, btnPause, btnReset);
        buttonBox.setAlignment(Pos.CENTER);

        // Ayırıcı
        Separator sep3 = createStyledSeparator();

        // Bilgi paneli
        Label infoTitle = new Label("⏱ Yeşil Işık Süreleri");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        infoTitle.setStyle("-fx-text-fill: #2ecc71;");

        lblNorthInfo = createStyledInfoLabel("⬆ Kuzey: --");
        lblSouthInfo = createStyledInfoLabel("⬇ Güney: --");
        lblEastInfo = createStyledInfoLabel("➡ Doğu:  --");
        lblWestInfo = createStyledInfoLabel("⬅ Batı:  --");

        VBox greenTimesBox = new VBox(6, lblNorthInfo, lblSouthInfo, lblEastInfo, lblWestInfo);
        greenTimesBox.setStyle("-fx-background-color: rgba(46, 204, 113, 0.1); " +
                              "-fx-padding: 12; -fx-background-radius: 8; " +
                              "-fx-border-color: #27ae60; -fx-border-radius: 8; -fx-border-width: 1;");

        // Durum bilgisi
        Separator sep4 = createStyledSeparator();

        Label statusTitle = new Label("📈 Simülasyon Durumu");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        statusTitle.setStyle("-fx-text-fill: #e67e22;");

        lblCurrentPhase = createStyledInfoLabel("🚦 Aktif Yön: --");
        lblTotalTime = createStyledInfoLabel("⏰ Süre: 0.0s");
        lblCarsPassed = createStyledInfoLabel("✅ Geçen: 0 araç");
        lblRemainingCars = createStyledInfoLabel("🚗 Kalan: 0 araç");
        lblStatus = createStyledInfoLabel("📍 Durum: Bekliyor");

        VBox statusBox = new VBox(6, lblCurrentPhase, lblTotalTime, lblCarsPassed, lblRemainingCars, lblStatus);
        statusBox.setStyle("-fx-background-color: rgba(230, 126, 34, 0.1); " +
                          "-fx-padding: 12; -fx-background-radius: 8; " +
                          "-fx-border-color: #e67e22; -fx-border-radius: 8; -fx-border-width: 1;");

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

    /**
     * Stillendirilmiş ayırıcı oluşturur.
     */
    private Separator createStyledSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: #465c6e;");
        return sep;
    }

    /**
     * Yön etiketi oluşturur.
     */
    private Label createDirectionLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("System", FontWeight.BOLD, 12));
        label.setStyle("-fx-text-fill: #bdc3c7;");
        return label;
    }

    /**
     * Stillendirilmiş text field oluşturur.
     */
    private TextField createStyledTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setPrefWidth(90);
        tf.setStyle("-fx-background-color: #ecf0f1; " +
                   "-fx-border-color: #3498db; -fx-border-radius: 5; " +
                   "-fx-background-radius: 5; -fx-font-size: 13; " +
                   "-fx-font-weight: bold; -fx-alignment: center;");
        return tf;
    }

    /**
     * Stillendirilmiş buton oluşturur.
     */
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

    /**
     * Stillendirilmiş bilgi etiketi oluşturur.
     */
    private Label createStyledInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", FontWeight.NORMAL, 12));
        label.setStyle("-fx-text-fill: #ecf0f1;");
        return label;
    }


    /**
     * Ana çizim metodu - model durumuna göre tüm görsel öğeleri günceller.
     */
    public void draw(TrafficModel model) {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Arkaplan
        drawBackground(gc);

        // Yollar
        drawRoads(gc);

        // Durak çizgileri
        drawStopLines(gc);

        // Trafik ışıkları
        drawTrafficLights(gc, model);

        // Araçlar
        drawCars(gc, model);

        // Bilgi panelini güncelle
        updateInfoPanel(model);
    }

    /**
     * Arkaplanı çizer.
     */
    private void drawBackground(GraphicsContext gc) {
        // Yeşil çim arkaplan
        gc.setFill(Color.DARKGREEN);
        gc.fillRect(0, 0, CANVAS_SIZE, CANVAS_SIZE);

        // Daha koyu yeşil köşeler (park alanları)
        gc.setFill(Color.rgb(34, 85, 34));
        gc.fillRect(0, 0, ROAD_START, ROAD_START);
        gc.fillRect(ROAD_END, 0, ROAD_START, ROAD_START);
        gc.fillRect(0, ROAD_END, ROAD_START, ROAD_START);
        gc.fillRect(ROAD_END, ROAD_END, ROAD_START, ROAD_START);
    }

    /**
     * Yolları çizer.
     */
    private void drawRoads(GraphicsContext gc) {
        // Ana yollar (gri asfalt)
        gc.setFill(Color.rgb(60, 60, 60));
        gc.fillRect(ROAD_START, 0, ROAD_WIDTH, CANVAS_SIZE); // Dikey yol
        gc.fillRect(0, ROAD_START, CANVAS_SIZE, ROAD_WIDTH); // Yatay yol

        // Kavşak alanı (biraz daha açık)
        gc.setFill(Color.rgb(70, 70, 70));
        gc.fillRect(ROAD_START, ROAD_START, ROAD_WIDTH, ROAD_WIDTH);

        // Şerit çizgileri (kesikli beyaz)
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.setLineDashes(15, 10);

        // Dikey yol orta çizgisi
        gc.strokeLine(400, 0, 400, ROAD_START);
        gc.strokeLine(400, ROAD_END, 400, CANVAS_SIZE);

        // Yatay yol orta çizgisi
        gc.strokeLine(0, 400, ROAD_START, 400);
        gc.strokeLine(ROAD_END, 400, CANVAS_SIZE, 400);

        gc.setLineDashes(null);

        // Yol kenarları (sarı çizgi)
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);

        // Dikey yol kenarları
        gc.strokeLine(ROAD_START, 0, ROAD_START, ROAD_START);
        gc.strokeLine(ROAD_START, ROAD_END, ROAD_START, CANVAS_SIZE);
        gc.strokeLine(ROAD_END, 0, ROAD_END, ROAD_START);
        gc.strokeLine(ROAD_END, ROAD_END, ROAD_END, CANVAS_SIZE);

        // Yatay yol kenarları
        gc.strokeLine(0, ROAD_START, ROAD_START, ROAD_START);
        gc.strokeLine(ROAD_END, ROAD_START, CANVAS_SIZE, ROAD_START);
        gc.strokeLine(0, ROAD_END, ROAD_START, ROAD_END);
        gc.strokeLine(ROAD_END, ROAD_END, CANVAS_SIZE, ROAD_END);
    }

    /**
     * Durak çizgilerini çizer.
     */
    private void drawStopLines(GraphicsContext gc) {
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(4);

        // Kuzey yönü (alttan gelen araçlar için)
        gc.strokeLine(ROAD_START + 5, Car.STOP_NORTH, 400 - 5, Car.STOP_NORTH);

        // Güney yönü (üstten gelen araçlar için)
        gc.strokeLine(400 + 5, Car.STOP_SOUTH, ROAD_END - 5, Car.STOP_SOUTH);

        // Doğu yönü (sağdan gelen araçlar için)
        gc.strokeLine(Car.STOP_EAST, ROAD_START + 5, Car.STOP_EAST, 400 - 5);

        // Batı yönü (soldan gelen araçlar için)
        gc.strokeLine(Car.STOP_WEST, 400 + 5, Car.STOP_WEST, ROAD_END - 5);
    }

    /**
     * Trafik ışıklarını çizer.
     */
    private void drawTrafficLights(GraphicsContext gc, TrafficModel model) {
        // Her yön için trafik ışığı
        drawSingleTrafficLight(gc, 315, 280, Direction.NORTH, model);  // Kuzey (alt-sol köşe)
        drawSingleTrafficLight(gc, 460, 455, Direction.SOUTH, model);  // Güney (üst-sağ köşe)
        drawSingleTrafficLight(gc, 455, 280, Direction.EAST, model);   // Doğu (alt-sağ köşe)
        drawSingleTrafficLight(gc, 315, 455, Direction.WEST, model);   // Batı (üst-sol köşe)
    }

    /**
     * Tek bir trafik ışığını çizer.
     */
    private void drawSingleTrafficLight(GraphicsContext gc, double x, double y, Direction dir, TrafficModel model) {
        TrafficLight light = model.getTrafficLight(dir);

        // Işık kutusu arkaplanı
        gc.setFill(Color.rgb(30, 30, 30));
        gc.fillRoundRect(x, y, 30, 65, 5, 5);

        // Işık çerçevesi
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, 30, 65, 5, 5);

        // Işık renkleri
        Color redColor = Color.rgb(60, 20, 20);
        Color yellowColor = Color.rgb(60, 60, 20);
        Color greenColor = Color.rgb(20, 60, 20);

        // Aktif ışığı parlat
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

        // Kırmızı ışık
        gc.setFill(redColor);
        gc.fillOval(x + 5, y + 5, 20, 20);

        // Sarı ışık
        gc.setFill(yellowColor);
        gc.fillOval(x + 5, y + 25, 20, 20);

        // Yeşil ışık
        gc.setFill(greenColor);
        gc.fillOval(x + 5, y + 45, 20, 20);

        // Geri sayım göstergesi
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Monospaced", FontWeight.BOLD, 14));
        String countdownText = String.format("%.0f", Math.max(0, light.getCountdown()));
        gc.fillText(countdownText, x + 8, y - 5);

        // Yön etiketi
        gc.setFont(Font.font("System", 10));
        gc.fillText(dir.getDisplayName(), x, y + 78);
    }

    /**
     * Araçları çizer.
     */
    private void drawCars(GraphicsContext gc, TrafficModel model) {
        for (Car car : model.getCars()) {
            if (car.hasPassed()) continue;

            double x = car.getX();
            double y = car.getY();

            // Araç gövdesi
            gc.setFill(car.getColor());
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);

            Direction currentDir = car.getDirection();

            switch (currentDir) {
                case NORTH:
                case SOUTH:
                    // Dikey araç
                    drawVerticalCar(gc, x, y, currentDir == Direction.NORTH, car.getTurnDirection());
                    break;
                case EAST:
                case WEST:
                    // Yatay araç
                    drawHorizontalCar(gc, x, y, currentDir == Direction.WEST, car.getTurnDirection());
                    break;
            }
        }
    }

    /**
     * Dikey araç çizer.
     */
    private void drawVerticalCar(GraphicsContext gc, double x, double y, boolean facingUp, TurnDirection turn) {
        int w = Car.CAR_WIDTH;
        int h = Car.CAR_LENGTH;

        // Gövde
        gc.fillRoundRect(x, y, w, h, 5, 5);
        gc.strokeRoundRect(x, y, w, h, 5, 5);

        // Pencereler
        gc.setFill(Color.LIGHTBLUE);
        if (facingUp) {
            gc.fillRect(x + 3, y + 5, w - 6, 10);  // Ön cam
            gc.fillRect(x + 3, y + h - 15, w - 6, 8);  // Arka cam
        } else {
            gc.fillRect(x + 3, y + h - 15, w - 6, 10);  // Ön cam
            gc.fillRect(x + 3, y + 7, w - 6, 8);  // Arka cam
        }

        // Farlar
        gc.setFill(Color.YELLOW);
        if (facingUp) {
            gc.fillOval(x + 3, y + h - 5, 5, 5);
            gc.fillOval(x + w - 8, y + h - 5, 5, 5);
        } else {
            gc.fillOval(x + 3, y, 5, 5);
            gc.fillOval(x + w - 8, y, 5, 5);
        }

        // Dönüş yönü göstergesi (sinyal lambası)
        drawTurnIndicator(gc, x, y, w, h, turn, true, facingUp);
    }

    /**
     * Yatay araç çizer.
     */
    private void drawHorizontalCar(GraphicsContext gc, double x, double y, boolean facingRight, TurnDirection turn) {
        int w = Car.CAR_LENGTH;
        int h = Car.CAR_WIDTH;

        // Gövde
        gc.fillRoundRect(x, y, w, h, 5, 5);
        gc.strokeRoundRect(x, y, w, h, 5, 5);

        // Pencereler
        gc.setFill(Color.LIGHTBLUE);
        if (facingRight) {
            gc.fillRect(x + w - 15, y + 3, 10, h - 6);  // Ön cam
            gc.fillRect(x + 7, y + 3, 8, h - 6);  // Arka cam
        } else {
            gc.fillRect(x + 5, y + 3, 10, h - 6);  // Ön cam
            gc.fillRect(x + w - 15, y + 3, 8, h - 6);  // Arka cam
        }

        // Farlar
        gc.setFill(Color.YELLOW);
        if (facingRight) {
            gc.fillOval(x + w - 5, y + 3, 5, 5);
            gc.fillOval(x + w - 5, y + h - 8, 5, 5);
        } else {
            gc.fillOval(x, y + 3, 5, 5);
            gc.fillOval(x, y + h - 8, 5, 5);
        }

        // Dönüş yönü göstergesi
        drawTurnIndicator(gc, x, y, w, h, turn, false, facingRight);
    }

    /**
     * Dönüş sinyali göstergesi çizer.
     */
    private void drawTurnIndicator(GraphicsContext gc, double x, double y, int w, int h,
                                   TurnDirection turn, boolean isVertical, boolean facingPositive) {
        if (turn == TurnDirection.STRAIGHT) return;

        gc.setFill(Color.ORANGE);

        if (isVertical) {
            // Dikey araç
            if (facingPositive) { // Yukarı gidiyor
                if (turn == TurnDirection.LEFT) {
                    gc.fillOval(x - 2, y + h - 10, 4, 4);
                } else {
                    gc.fillOval(x + w - 2, y + h - 10, 4, 4);
                }
            } else { // Aşağı gidiyor
                if (turn == TurnDirection.LEFT) {
                    gc.fillOval(x + w - 2, y + 6, 4, 4);
                } else {
                    gc.fillOval(x - 2, y + 6, 4, 4);
                }
            }
        } else {
            // Yatay araç
            if (facingPositive) { // Sağa gidiyor
                if (turn == TurnDirection.LEFT) {
                    gc.fillOval(x + w - 10, y - 2, 4, 4);
                } else {
                    gc.fillOval(x + w - 10, y + h - 2, 4, 4);
                }
            } else { // Sola gidiyor
                if (turn == TurnDirection.LEFT) {
                    gc.fillOval(x + 6, y + h - 2, 4, 4);
                } else {
                    gc.fillOval(x + 6, y - 2, 4, 4);
                }
            }
        }
    }

    /**
     * Bilgi panelini günceller.
     */
    private void updateInfoPanel(TrafficModel model) {
        // Yeşil ışık süreleri
        lblNorthInfo.setText(String.format("⬆ Kuzey: %.1f sn", model.getGreenDuration(Direction.NORTH)));
        lblSouthInfo.setText(String.format("⬇ Güney: %.1f sn", model.getGreenDuration(Direction.SOUTH)));
        lblEastInfo.setText(String.format("➡ Doğu:  %.1f sn", model.getGreenDuration(Direction.EAST)));
        lblWestInfo.setText(String.format("⬅ Batı:  %.1f sn", model.getGreenDuration(Direction.WEST)));

        // Simülasyon durumu
        lblCurrentPhase.setText(String.format("🚦 Aktif: %s (%s)",
                model.getCurrentDirection().getDisplayName(),
                model.getCurrentLightState().getDisplayName()));
        lblTotalTime.setText(String.format("⏰ Süre: %.1f / %d sn",
                model.getTotalElapsedTime(), TrafficModel.TOTAL_CYCLE_TIME));
        lblCarsPassed.setText(String.format("✅ Geçen: %d araç", model.getTotalCarsPassed()));
        lblRemainingCars.setText(String.format("🚗 Kalan: %d araç", model.getRemainingCars()));

        // Durum göstergesi
        if (model.isCycleComplete()) {
            lblStatus.setText("📍 Durum: ✅ Tamamlandı");
            lblStatus.setStyle("-fx-text-fill: #2ecc71; -fx-font-weight: bold;");
        } else if (model.isRunning()) {
            lblStatus.setText("📍 Durum: ▶ Çalışıyor");
            lblStatus.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        } else if (model.isDataLoaded()) {
            lblStatus.setText("📍 Durum: ⏸ Duraklatıldı");
            lblStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            lblStatus.setText("📍 Durum: ⏹ Bekliyor");
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
