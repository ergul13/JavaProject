package com.traffic.controller;

import com.traffic.model.TrafficModel;
import com.traffic.view.TrafficView;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Alert;

import java.util.Random;

/**
 * MVC Controller sinifi - Model ve View arasindaki iletisimi yonetir.
 */
public class TrafficController {
    private final TrafficModel model;
    private final TrafficView view;
    private AnimationTimer animationTimer;
    private long lastUpdateTime;
    private final Random random;

    public TrafficController(TrafficModel model, TrafficView view) {
        this.model = model;
        this.view = view;
        this.random = new Random();
        this.lastUpdateTime = 0;
        initializeEventHandlers();
        initializeAnimationTimer();
        view.draw(model);
    }

    private void initializeEventHandlers() {
        view.getBtnRandom().setOnAction(e -> handleRandomize());
        view.getBtnStart().setOnAction(e -> handleStart());
        view.getBtnPause().setOnAction(e -> handlePause());
        view.getBtnReset().setOnAction(e -> handleReset());
    }

    private void handleRandomize() {
        if (model.isRunning()) {
            showWarning("Simulasyon Calisiyor", "Simulasyon calisirken degerler degistirilemez.");
            return;
        }
        view.getTfNorth().setText(String.valueOf(5 + random.nextInt(46)));
        view.getTfSouth().setText(String.valueOf(5 + random.nextInt(46)));
        view.getTfEast().setText(String.valueOf(5 + random.nextInt(46)));
        view.getTfWest().setText(String.valueOf(5 + random.nextInt(46)));
    }

    private void handleStart() {
        try {
            int north = parsePositiveInt(view.getTfNorth().getText(), "Kuzey");
            int south = parsePositiveInt(view.getTfSouth().getText(), "Guney");
            int east = parsePositiveInt(view.getTfEast().getText(), "Dogu");
            int west = parsePositiveInt(view.getTfWest().getText(), "Bati");

            int total = north + south + east + west;
            if (total == 0) {
                showError("Gecersiz Deger", "En az bir arac olmalidir.");
                return;
            }

            if (total > 200) {
                showWarning("Cok Fazla Arac", "Performans icin toplam arac sayisi 200 ile sinirlandirildi.");
                double ratio = 200.0 / total;
                north = (int) Math.max(1, north * ratio);
                south = (int) Math.max(1, south * ratio);
                east = (int) Math.max(1, east * ratio);
                west = (int) Math.max(1, west * ratio);

                view.getTfNorth().setText(String.valueOf(north));
                view.getTfSouth().setText(String.valueOf(south));
                view.getTfEast().setText(String.valueOf(east));
                view.getTfWest().setText(String.valueOf(west));
            }

            if (!model.isDataLoaded() || model.isCycleComplete()) {
                model.setVehicleCounts(north, south, east, west);
            }

            model.setRunning(true);
            lastUpdateTime = 0;
            updateButtonStates(true);
            disableInputFields(true);

        } catch (NumberFormatException ex) {
            showError("Gecersiz Giris", ex.getMessage());
        }
    }

    private void handlePause() {
        model.setRunning(false);
        updateButtonStates(false);
    }

    private void handleReset() {
        model.resetAll();
        lastUpdateTime = 0;
        updateButtonStates(false);
        disableInputFields(false);
        view.draw(model);
    }

    private void initializeAnimationTimer() {
        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastUpdateTime == 0) {
                    lastUpdateTime = now;
                    return;
                }

                double deltaTime = (now - lastUpdateTime) / 1_000_000_000.0;
                lastUpdateTime = now;
                deltaTime = Math.min(deltaTime, 0.1);

                model.update(deltaTime);
                view.draw(model);

                if (model.isCycleComplete() && !model.isRunning()) {
                    updateButtonStates(false);
                    showInfo("Simulasyon Tamamlandi",
                            String.format("Dongu tamamlandi!\nGecen arac: %d / %d\nToplam sure: %.1f saniye",
                                    model.getTotalCarsPassed(), model.getTotalCars(),
                                    model.getTotalElapsedTime()));
                }
            }
        };
        animationTimer.start();
    }

    private int parsePositiveInt(String text, String fieldName) throws NumberFormatException {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new NumberFormatException(fieldName + " icin negatif deger girilemez.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " icin gecerli bir sayi giriniz.");
        }
    }

    private void updateButtonStates(boolean isRunning) {
        view.getBtnStart().setDisable(isRunning);
        view.getBtnPause().setDisable(!isRunning);
        view.getBtnReset().setDisable(isRunning);
    }

    private void disableInputFields(boolean disable) {
        view.getTfNorth().setDisable(disable);
        view.getTfSouth().setDisable(disable);
        view.getTfEast().setDisable(disable);
        view.getTfWest().setDisable(disable);
        view.getBtnRandom().setDisable(disable);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}
