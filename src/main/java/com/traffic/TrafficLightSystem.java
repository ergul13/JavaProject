package com.traffic;

import com.traffic.controller.TrafficController;
import com.traffic.model.TrafficModel;
import com.traffic.view.TrafficView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Araç Yoğunluğuna Dayalı Akıllı Trafik Işığı Kontrol Sistemi
 *
 * Bu uygulama MVC (Model-View-Controller) mimarisini kullanarak
 * 4 yönlü bir kavşakta araç yoğunluğuna göre trafik ışık sürelerini
 * dinamik olarak hesaplar ve simüle eder.
 *
 * Özellikler:
 * - 120 saniyelik sabit döngü süresi
 * - Yoğunluğa göre yeşil ışık süresi hesaplama
 * - 3 saniyelik sabit sarı ışık süresi
 * - Minimum 10sn, maksimum 60sn yeşil ışık sınırları
 * - Çarpışma önleme sistemi
 * - Araç animasyonları
 *
 * @author Traffic Light Control System Team
 */
public class TrafficLightSystem extends Application {

    private TrafficController controller;

    @Override
    public void start(Stage primaryStage) {
        // MVC bileşenlerini oluştur
        TrafficModel model = new TrafficModel();
        TrafficView view = new TrafficView();
        controller = new TrafficController(model, view);

        Scene scene = new Scene(view, 1050, 820);

        // Pencere ayarları
        primaryStage.setTitle("🚦 Araç Yoğunluğuna Dayalı Trafik Işığı Kontrol Sistemi");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        // Pencere kapatıldığında controller'ı durdur
        primaryStage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.stop();
            }
        });

        primaryStage.show();
    }

    @Override
    public void stop() {
        // Uygulama kapatılırken temizlik
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}