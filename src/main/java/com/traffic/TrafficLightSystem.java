package com.traffic;

import com.traffic.controller.TrafficController;
import com.traffic.model.TrafficModel;
import com.traffic.view.TrafficView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * MVC mimarisini kullanan Trafik Isigi Kontrol Sistemi.
 */
public class TrafficLightSystem extends Application {

    private TrafficController controller;

    @Override
    public void start(Stage primaryStage) {
        TrafficModel model = new TrafficModel();
        TrafficView view = new TrafficView();
        controller = new TrafficController(model, view);

        Scene scene = new Scene(view, 1050, 820);
        primaryStage.setTitle("Arac Yogunluguna Dayali Trafik Isigi Kontrol Sistemi");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);

        primaryStage.setOnCloseRequest(e -> {
            if (controller != null) {
                controller.stop();
            }
        });

        primaryStage.show();
    }

    @Override
    public void stop() {
        if (controller != null) {
            controller.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}