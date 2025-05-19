package model;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class VehicleManager {

    private final Map<Direction, Queue<Rectangle>> queues = new EnumMap<>(Direction.class);
    private final Map<Direction, Rectangle> referenceCars = new EnumMap<>(Direction.class);
    private final Pane simulationPane1;

    public VehicleManager(Pane simulationPane1) {
        this.simulationPane1 = simulationPane1;
        for (Direction dir : Direction.values()) {
            queues.put(dir, new LinkedList<>());
        }
    }

    public void setReferenceCar(Direction dir, Rectangle reference) {
        referenceCars.put(dir, reference);
    }

    public void addInitialVehicle(Direction dir, Rectangle car) {
        queues.get(dir).add(car);
    }

    public void moveVehiclesNorth() {
        moveDirection(Direction.NORTH, 0, +1, false);
    }

    public void moveVehiclesSouth() {
        moveDirection(Direction.SOUTH, 0, -1, false);
    }

    public void moveVehiclesEast() {
        moveDirection(Direction.EAST, -1, 0, true);
    }

    public void moveVehiclesWest() {
        moveDirection(Direction.WEST, +1, 0, true);
    }

    private void moveDirection(Direction dir, int dx, int dy, boolean isHorizontal) {
        Queue<Rectangle> queue = queues.get(dir);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Rectangle firstCar = cars.get(0);

        TranslateTransition moveFirst = new TranslateTransition(Duration.seconds(1), firstCar);
        if (isHorizontal) moveFirst.setByX(dx * 200); else moveFirst.setByY(dy * 200);
        moveFirst.setOnFinished(e -> simulationPane1.getChildren().remove(firstCar));
        moveFirst.play();

        queue.poll();

        for (int i = 1; i < cars.size(); i++) {
            Rectangle car = cars.get(i);
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), car);
            if (isHorizontal) move.setByX(dx * 30); else move.setByY(dy * 30);
            move.setDelay(Duration.seconds(i * 0.2)); // sıralı animasyon
            move.play();
        }

        Rectangle newCar = cloneCar(referenceCars.get(dir));
        if (isHorizontal) {
            newCar.setLayoutX(referenceCars.get(dir).getLayoutX() - queue.size() * 30 * dx);
            newCar.setLayoutY(referenceCars.get(dir).getLayoutY());
        } else {
            newCar.setLayoutX(referenceCars.get(dir).getLayoutX());
            newCar.setLayoutY(referenceCars.get(dir).getLayoutY() - queue.size() * 30 * dy);
        }

        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);
    }

    private Rectangle cloneCar(Rectangle ref) {
        Rectangle car = new Rectangle(ref.getWidth(), ref.getHeight(), ref.getFill());
        car.setArcWidth(ref.getArcWidth());
        car.setArcHeight(ref.getArcHeight());
        car.setStroke(Color.BLACK);
        car.setTranslateX(0);
        car.setTranslateY(0);
        return car;
    }
}
