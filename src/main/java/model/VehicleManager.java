package model;

import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class VehicleManager {

    private final Map<Direction, Queue<Rectangle>> vehicleQueues = new EnumMap<>(Direction.class);
    private Rectangle westReferenceCar;

    public VehicleManager() {
        for (Direction d : Direction.values()) {
            vehicleQueues.put(d, new LinkedList<>());
        }
    }

    public void setWestReference(Rectangle reference) {
        this.westReferenceCar = reference;
    }

    public void addInitialVehicle(Direction dir, Rectangle vehicle) {
        vehicleQueues.get(dir).add(vehicle);
    }

    public void moveVehiclesWest() {
        Queue<Rectangle> queue = vehicleQueues.get(Direction.WEST);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        //Collections.reverse(cars); // 👈 Ters sıralama düzeltildi

        int delay = 0;
        for (Rectangle car : cars) {
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByX(300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }

        queue.clear(); // geçişten sonra sıfırla
    }

    public void moveVehiclesEast() {
        Queue<Rectangle> queue = vehicleQueues.get(Direction.EAST);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);


        int delay = 0;
        for (Rectangle car : cars) {
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByX(-300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }

        queue.clear();
    }

    public void moveVehiclesNorth() {
        Queue<Rectangle> queue = vehicleQueues.get(Direction.NORTH);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Collections.reverse(cars);

        int delay = 0;
        for (Rectangle car : cars) {
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByY(300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }

        queue.clear();
    }

    public void moveVehiclesSouth() {
        Queue<Rectangle> queue = vehicleQueues.get(Direction.SOUTH);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);


        int delay = 0;
        for (Rectangle car : cars) {
            TranslateTransition t = new TranslateTransition(Duration.seconds(2), car);
            t.setByY(-300);
            t.setDelay(Duration.seconds(delay));
            t.play();
            delay++;
        }

        queue.clear();
    }
}
