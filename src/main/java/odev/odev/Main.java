package odev.odev;

import controller.TrafficController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.IntersectionManager;
import view.TrafficSimulationView;

/**
 * Main Application Entry Point
 * Traffic Light Control System Based on Vehicle Density
 *
 * Architecture: Model-View-Controller (MVC)
 * - Model: IntersectionManager, TrafficLight, Vehicle, Direction
 * - View: TrafficSimulationView
 * - Controller: TrafficController
 *
 * @author Senior Java Developer
 * @version 1.0
 */
public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        // Create Model
        IntersectionManager model = new IntersectionManager();

        // Create View
        TrafficSimulationView view = new TrafficSimulationView();

        // Create Controller (binds Model and View)
        TrafficController controller = new TrafficController(model, view);

        // Initialize controller
        controller.start();

        // Create Scene
        Scene scene = new Scene(view, 1280, 720);

        // Configure Stage
        stage.setTitle("Traffic Light Control System - Vehicle Density Based");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.centerOnScreen();

        // Show
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}