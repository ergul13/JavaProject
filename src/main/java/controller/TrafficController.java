package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;
import model.*;
import view.TrafficSimulationView;

import java.util.Map;

/**
 * Controller class implementing MVC pattern
 * Bridges the Model (IntersectionManager) and View (TrafficSimulationView)
 */
public class TrafficController {
    private final IntersectionManager model;
    private final TrafficSimulationView view;
    private Timeline simulationTimeline;

    public TrafficController(IntersectionManager model, TrafficSimulationView view) {
        this.model = model;
        this.view = view;

        initializeTimeline();
        setupViewHandlers();
        updateView();
    }

    /**
     * Initialize the simulation timeline (1 second per tick)
     */
    private void initializeTimeline() {
        simulationTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            model.update();
            updateView();

            // Auto-stop when cycle completes
            if (!model.isCycleActive()) {
                simulationTimeline.stop();
            }
        }));
        simulationTimeline.setCycleCount(Timeline.INDEFINITE);
    }

    /**
     * Setup event handlers for view controls
     */
    private void setupViewHandlers() {
        // Start button
        view.setOnStart(() -> {
            if (!model.isCycleActive()) {
                // Get densities from view inputs
                Map<Direction, Integer> densities = view.getVehicleDensities();
                for (Direction dir : Direction.values()) {
                    model.setVehicleDensity(dir, densities.get(dir));
                }

                // Start cycle
                model.startCycle();

                // Update green time allocations
                for (Direction dir : Direction.values()) {
                    TrafficLight light = model.getTrafficLight(dir);
                    view.updateGreenTimeAllocation(dir, light.getGreenDuration());
                }

                simulationTimeline.play();
                updateView();
            }
        });

        // Pause button
        view.setOnPause(() -> {
            if (model.isCycleActive()) {
                model.pause();
                simulationTimeline.pause();
            } else if (model.getCurrentActiveDirection() != null) {
                // Resume
                model.resume();
                simulationTimeline.play();
            }
        });

        // Reset button
        view.setOnReset(() -> {
            simulationTimeline.stop();
            model.reset();
            updateView();
        });

        // Random density generator
        view.setOnGenerateRandom(() -> {
            model.generateRandomDensities();
            view.setVehicleDensities(model.getAllVehicleDensities());
        });
    }

    /**
     * Update view with current model state
     */
    private void updateView() {
        // Update traffic lights
        for (Direction dir : Direction.values()) {
            TrafficLight light = model.getTrafficLight(dir);
            view.updateTrafficLight(dir, light.getCurrentColor(), light.getRemainingTime());
        }

        // Update vehicles
        for (Direction dir : Direction.values()) {
            view.updateVehicles(dir, model.getVehicles(dir));
        }

        // Update status
        view.updateStatus(
            model.isCycleActive(),
            model.getElapsedCycleTime(),
            model.getCurrentActiveDirection()
        );
    }

    /**
     * Start the controller
     */
    public void start() {
        updateView();
    }
}

