package model;

import javafx.animation.TranslateTransition;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class VehicleManager {

    private final Map<Direction, Queue<Rectangle>> vehicleQueues = new EnumMap<>(Direction.class);
    private Rectangle westReference;

    public VehicleManager() {
        for (Direction d : Direction.values()) {
            vehicleQueues.put(d, new LinkedList<>());
        }
    }

    public void setWestReference(Rectangle ref) {
        this.westReference = ref;
    }

    public void addInitialVehicle(Direction dir, Rectangle vehicle) {
        vehicleQueues.get(dir).add(vehicle);
    }

    public void moveVehiclesWest() {
        Queue<Rectangle> westQueue = vehicleQueues.get(Direction.WEST);
        int delay = 0;
        while (!westQueue.isEmpty()) {
            Rectangle car = westQueue.poll();
            if (car == null) continue;
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByX(300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }
    }

    public void moveVehiclesNorth() {
        Queue<Rectangle> q = vehicleQueues.get(Direction.NORTH);
        int delay = 0;
        while (!q.isEmpty()) {
            Rectangle car = q.poll();
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByY(300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }
    }

    public void moveVehiclesSouth() {
        Queue<Rectangle> q = vehicleQueues.get(Direction.SOUTH);
        int delay = 0;
        while (!q.isEmpty()) {
            Rectangle car = q.poll();
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByY(-300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }
    }

    public void moveVehiclesEast() {
        Queue<Rectangle> q = vehicleQueues.get(Direction.EAST);
        int delay = 0;
        while (!q.isEmpty()) {
            Rectangle car = q.poll();
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByX(-300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }
    }
}
