package com.traffic.controller;

import com.traffic.model.TrafficModel;
import com.traffic.view.TrafficView;
import javafx.animation.AnimationTimer;
import javafx.scene.control.Alert;

import java.util.Random;

/**
 * MVC Controller sınıfı.
 * Model ve View arasındaki iletişimi yönetir.
 * Kullanıcı girdilerini işler ve simülasyonu kontrol eder.
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

        // İlk çizimi yap
        view.draw(model);
    }

    /**
     * Buton event handler'larını başlatır.
     */
    private void initializeEventHandlers() {
        // Rastgele Oluştur butonu
        view.getBtnRandom().setOnAction(e -> handleRandomize());

        // Başlat butonu
        view.getBtnStart().setOnAction(e -> handleStart());

        // Durdur butonu
        view.getBtnPause().setOnAction(e -> handlePause());

        // Sıfırla butonu
        view.getBtnReset().setOnAction(e -> handleReset());
    }

    /**
     * Rastgele araç sayıları oluşturur.
     */
    private void handleRandomize() {
        if (model.isRunning()) {
            showWarning("Simülasyon Çalışıyor", "Simülasyon çalışırken değerler değiştirilemez.");
            return;
        }

        view.getTfNorth().setText(String.valueOf(5 + random.nextInt(46))); // 5-50 arası
        view.getTfSouth().setText(String.valueOf(5 + random.nextInt(46)));
        view.getTfEast().setText(String.valueOf(5 + random.nextInt(46)));
        view.getTfWest().setText(String.valueOf(5 + random.nextInt(46)));
    }

    /**
     * Simülasyonu başlatır.
     */
    private void handleStart() {
        try {
            int north = parsePositiveInt(view.getTfNorth().getText(), "Kuzey");
            int south = parsePositiveInt(view.getTfSouth().getText(), "Güney");
            int east = parsePositiveInt(view.getTfEast().getText(), "Doğu");
            int west = parsePositiveInt(view.getTfWest().getText(), "Batı");

            // Toplam araç kontrolü
            int total = north + south + east + west;
            if (total == 0) {
                showError("Geçersiz Değer", "En az bir araç olmalıdır.");
                return;
            }

            if (total > 200) {
                showWarning("Çok Fazla Araç", "Performans için toplam araç sayısı 200 ile sınırlandırıldı.");
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

            // Model henüz yüklenmediyse veya döngü tamamlandıysa, yeni yoğunlukları yükle
            if (!model.isDataLoaded() || model.isCycleComplete()) {
                model.setVehicleCounts(north, south, east, west);
            }

            model.setRunning(true);
            lastUpdateTime = 0;

            updateButtonStates(true);
            disableInputFields(true);

        } catch (NumberFormatException ex) {
            showError("Geçersiz Giriş", ex.getMessage());
        }
    }

    /**
     * Simülasyonu duraklatır.
     */
    private void handlePause() {
        model.setRunning(false);
        updateButtonStates(false);
    }

    /**
     * Simülasyonu sıfırlar.
     */
    private void handleReset() {
        model.resetAll();
        lastUpdateTime = 0;

        updateButtonStates(false);
        disableInputFields(false);

        view.draw(model);
    }

    /**
     * AnimationTimer'ı başlatır.
     */
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

                // Çok büyük delta değerlerini sınırla (tab değiştirme gibi durumlarda)
                deltaTime = Math.min(deltaTime, 0.1);

                // Modeli güncelle
                model.update(deltaTime);

                // View'i güncelle
                view.draw(model);

                // Döngü tamamlandıysa UI'yi güncelle
                if (model.isCycleComplete() && !model.isRunning()) {
                    updateButtonStates(false);
                    showInfo("Simülasyon Tamamlandı",
                            String.format("Döngü tamamlandı!\nGeçen araç: %d / %d\nToplam süre: %.1f saniye",
                                    model.getTotalCarsPassed(), model.getTotalCars(),
                                    model.getTotalElapsedTime()));
                }
            }
        };
        animationTimer.start();
    }

    /**
     * Pozitif tamsayı parse eder.
     */
    private int parsePositiveInt(String text, String fieldName) throws NumberFormatException {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new NumberFormatException(fieldName + " için negatif değer girilemez.");
            }
            return value;
        } catch (NumberFormatException e) {
            throw new NumberFormatException(fieldName + " için geçerli bir sayı giriniz.");
        }
    }

    /**
     * Buton durumlarını günceller.
     */
    private void updateButtonStates(boolean isRunning) {
        view.getBtnStart().setDisable(isRunning);
        view.getBtnPause().setDisable(!isRunning);
        view.getBtnReset().setDisable(isRunning);
    }

    /**
     * Giriş alanlarını etkinleştirir/devre dışı bırakır.
     */
    private void disableInputFields(boolean disable) {
        view.getTfNorth().setDisable(disable);
        view.getTfSouth().setDisable(disable);
        view.getTfEast().setDisable(disable);
        view.getTfWest().setDisable(disable);
        view.getBtnRandom().setDisable(disable);
    }

    /**
     * Hata mesajı gösterir.
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Uyarı mesajı gösterir.
     */
    private void showWarning(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Bilgi mesajı gösterir.
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * AnimationTimer'ı durdurur (uygulama kapatılırken çağrılmalı).
     */
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
    }
}

