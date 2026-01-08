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
        VBox panel = new VBox(10);
        panel.setPadding(new Insets(15));
        panel.setStyle("-fx-background-color: #f5f5f5; -fx-border-color: #cccccc; -fx-border-width: 0 2 0 0;");
        panel.setPrefWidth(220);

        // Başlık
        Label titleLabel = new Label("🚦 Trafik Kontrol Sistemi");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
        titleLabel.setStyle("-fx-text-fill: #333;");

        // Ayırıcı
        Separator sep1 = new Separator();

        // Araç Yoğunluğu Girişi
        Label inputTitle = new Label("Araç Yoğunluğu:");
        inputTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        tfNorth = createTextField("10");
        tfSouth = createTextField("10");
        tfEast = createTextField("10");
        tfWest = createTextField("10");

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(5);
        inputGrid.add(new Label("Kuzey:"), 0, 0);
        inputGrid.add(tfNorth, 1, 0);
        inputGrid.add(new Label("Güney:"), 0, 1);
        inputGrid.add(tfSouth, 1, 1);
        inputGrid.add(new Label("Doğu:"), 0, 2);
        inputGrid.add(tfEast, 1, 2);
        inputGrid.add(new Label("Batı:"), 0, 3);
        inputGrid.add(tfWest, 1, 3);

        // Rastgele butonu
        btnRandom = new Button("🎲 Rastgele Oluştur");
        btnRandom.setMaxWidth(Double.MAX_VALUE);
        btnRandom.setStyle("-fx-background-color: #9b59b6; -fx-text-fill: white;");

        // Ayırıcı
        Separator sep2 = new Separator();

        // Kontrol butonları
        Label controlTitle = new Label("Simülasyon Kontrolleri:");
        controlTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        btnStart = new Button("▶ Başlat");
        btnPause = new Button("⏸ Durdur");
        btnReset = new Button("🔄 Sıfırla");

        btnStart.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnPause.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white;");
        btnReset.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");

        btnPause.setDisable(true);

        HBox buttonBox = new HBox(5, btnStart, btnPause, btnReset);
        buttonBox.setAlignment(Pos.CENTER);

        // Ayırıcı
        Separator sep3 = new Separator();

        // Bilgi paneli
        Label infoTitle = new Label("Yeşil Işık Süreleri:");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        lblNorthInfo = createInfoLabel("Kuzey: --");
        lblSouthInfo = createInfoLabel("Güney: --");
        lblEastInfo = createInfoLabel("Doğu: --");
        lblWestInfo = createInfoLabel("Batı: --");

        VBox greenTimesBox = new VBox(3, lblNorthInfo, lblSouthInfo, lblEastInfo, lblWestInfo);
        greenTimesBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 8; -fx-background-radius: 5;");

        // Durum bilgisi
        Separator sep4 = new Separator();

        Label statusTitle = new Label("Simülasyon Durumu:");
        statusTitle.setFont(Font.font("System", FontWeight.BOLD, 12));

        lblCurrentPhase = createInfoLabel("Aktif Yön: --");
        lblTotalTime = createInfoLabel("Toplam Süre: 0.0s");
        lblCarsPassed = createInfoLabel("Geçen Araç: 0");
        lblRemainingCars = createInfoLabel("Kalan Araç: 0");
        lblStatus = createInfoLabel("Durum: Bekliyor");
        lblStatus.setStyle("-fx-font-weight: bold;");

        VBox statusBox = new VBox(3, lblCurrentPhase, lblTotalTime, lblCarsPassed, lblRemainingCars, lblStatus);
        statusBox.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 8; -fx-background-radius: 5;");

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
     * Standart text field oluşturur.
     */
    private TextField createTextField(String defaultValue) {
        TextField tf = new TextField(defaultValue);
        tf.setPrefWidth(80);
        tf.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 3;");
        return tf;
    }

    /**
     * Bilgi etiketi oluşturur.
     */
    private Label createInfoLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Monospaced", 11));
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

            switch (car.getDirection()) {
                case NORTH:
                case SOUTH:
                    // Dikey araç
                    drawVerticalCar(gc, x, y, car.getDirection() == Direction.NORTH);
                    break;
                case EAST:
                case WEST:
                    // Yatay araç
                    drawHorizontalCar(gc, x, y, car.getDirection() == Direction.WEST);
                    break;
            }
        }
    }

    /**
     * Dikey araç çizer.
     */
    private void drawVerticalCar(GraphicsContext gc, double x, double y, boolean facingUp) {
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
    }

    /**
     * Yatay araç çizer.
     */
    private void drawHorizontalCar(GraphicsContext gc, double x, double y, boolean facingRight) {
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
    }

    /**
     * Bilgi panelini günceller.
     */
    private void updateInfoPanel(TrafficModel model) {
        // Yeşil ışık süreleri
        lblNorthInfo.setText(String.format("Kuzey: %.1f sn", model.getGreenDuration(Direction.NORTH)));
        lblSouthInfo.setText(String.format("Güney: %.1f sn", model.getGreenDuration(Direction.SOUTH)));
        lblEastInfo.setText(String.format("Doğu:  %.1f sn", model.getGreenDuration(Direction.EAST)));
        lblWestInfo.setText(String.format("Batı:  %.1f sn", model.getGreenDuration(Direction.WEST)));

        // Simülasyon durumu
        lblCurrentPhase.setText(String.format("Aktif: %s (%s)",
                model.getCurrentDirection().getDisplayName(),
                model.getCurrentLightState().getDisplayName()));
        lblTotalTime.setText(String.format("Süre: %.1f / %d sn",
                model.getTotalElapsedTime(), TrafficModel.TOTAL_CYCLE_TIME));
        lblCarsPassed.setText(String.format("Geçen: %d araç", model.getTotalCarsPassed()));
        lblRemainingCars.setText(String.format("Kalan: %d araç", model.getRemainingCars()));

        // Durum göstergesi
        if (model.isCycleComplete()) {
            lblStatus.setText("Durum: ✅ Tamamlandı");
            lblStatus.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
        } else if (model.isRunning()) {
            lblStatus.setText("Durum: ▶ Çalışıyor");
            lblStatus.setStyle("-fx-text-fill: #3498db; -fx-font-weight: bold;");
        } else if (model.isDataLoaded()) {
            lblStatus.setText("Durum: ⏸ Duraklatıldı");
            lblStatus.setStyle("-fx-text-fill: #f39c12; -fx-font-weight: bold;");
        } else {
            lblStatus.setText("Durum: ⏹ Bekliyor");
            lblStatus.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
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
