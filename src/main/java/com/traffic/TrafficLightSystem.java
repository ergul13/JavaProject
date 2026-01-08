package com.traffic;

import com.traffic.model.TrafficModel;
import com.traffic.view.TrafficView;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.Random;

public class TrafficLightSystem extends Application {
    private TrafficModel model;
    private TrafficView view;
    private AnimationTimer timer;
    private long lastTime = 0;

    @Override
    public void start(Stage primaryStage) {
        model = new TrafficModel();
        view = new TrafficView();

        view.btnRandom.setOnAction(e -> {
            Random r = new Random();
            view.tfNorth.setText(String.valueOf(r.nextInt(50) + 5));
            view.tfSouth.setText(String.valueOf(r.nextInt(50) + 5));
            view.tfEast.setText(String.valueOf(r.nextInt(50) + 5));
            view.tfWest.setText(String.valueOf(r.nextInt(50) + 5));
        });

        view.btnStart.setOnAction(e -> {
            try {
                int n = Integer.parseInt(view.tfNorth.getText());
                int s = Integer.parseInt(view.tfSouth.getText());
                int eDir = Integer.parseInt(view.tfEast.getText());
                int w = Integer.parseInt(view.tfWest.getText());

                if (!model.isRunning()) {
                    model.setVehicleCounts(n, s, eDir, w);
                }
                model.setRunning(true);
                view.btnStart.setDisable(true);
                view.btnPause.setDisable(false);
            } catch (NumberFormatException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Please enter valid numbers.");
                alert.show();
            }
        });

        view.btnPause.setOnAction(e -> {
            model.setRunning(false);
            view.btnStart.setDisable(false);
            view.btnPause.setDisable(true);
        });

        view.btnReset.setOnAction(e -> {
            model.setRunning(false);
            model.resetSimulationState();
            view.draw(model);
            view.btnStart.setDisable(false);
            view.btnPause.setDisable(true);
        });

        timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime == 0) {
                    lastTime = now;
                    return;
                }
                double secondsElapsed = (now - lastTime) / 1_000_000_000.0;
                lastTime = now;

                model.update(secondsElapsed);
                view.draw(model);
            }
        };
        timer.start();

        Scene scene = new Scene(view, 1050, 800);
        primaryStage.setTitle("Traffic Light Control System - MVC");
        primaryStage.setScene(scene);
        primaryStage.show();

        view.draw(model);
    }

    public static void main(String[] args) {
        launch(args);
    }
}