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

    // Her yeşil ışıkta çağrılır
    public void moveVehiclesWest() {
        Queue<Rectangle> queue = queues.get(Direction.WEST);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Rectangle firstCar = cars.get(0);

        double yPosition = 222; // carW1 Y
        double spacing = 30;
        double startX = 165; // carW1 X

        // 1. İlk arabayı geçir (karşıya)
        TranslateTransition moveFirst = new TranslateTransition(Duration.seconds(1), firstCar);
        moveFirst.setByX(200); // karşıya geçiş
        moveFirst.setOnFinished(e -> simulationPane1.getChildren().remove(firstCar));
        moveFirst.play();

        // 2. Kuyruktan çıkar
        queue.poll();

        // 3. Diğerlerini 1 adım öne kaydır
        for (int i = 1; i < cars.size(); i++) {
            Rectangle car = cars.get(i);
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), car);
            move.setByX(spacing);
            move.play();
        }

        // 4. Yeni araba oluştur, en arkaya koy (carW1 baz alınarak)
        Rectangle newCar = new Rectangle(25, 16, Color.web("#fcfeff"));
        newCar.setLayoutX(165.0 - queue.size() * 30); // spacing
        newCar.setLayoutY(12); // 222 - 210 = 12 → tam hizaya gelir
        newCar.setStroke(Color.BLACK);
        newCar.setArcWidth(5);
        newCar.setArcHeight(5);
        newCar.setTranslateX(0);
        newCar.setTranslateY(0);
        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);


        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);
    }
    public void moveVehiclesEast() {
        Queue<Rectangle> queue = queues.get(Direction.EAST);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Rectangle firstCar = cars.get(0);

        TranslateTransition moveFirst = new TranslateTransition(Duration.seconds(1), firstCar);
        moveFirst.setByX(-200); // EAST → sola
        moveFirst.setOnFinished(e -> simulationPane1.getChildren().remove(firstCar));
        moveFirst.play();

        queue.poll();

        for (int i = 1; i < cars.size(); i++) {
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), cars.get(i));
            move.setByX(-30); // geri kalanlar sola kayar
            move.play();
        }

        Rectangle newCar = cloneCarLike(firstCar);
        newCar.setLayoutX(firstCar.getLayoutX() + queue.size() * 30);
        newCar.setLayoutY(firstCar.getLayoutY());
        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);
    }

    public void moveVehiclesNorth() {
        Queue<Rectangle> queue = queues.get(Direction.NORTH);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Rectangle firstCar = cars.get(0);

        TranslateTransition moveFirst = new TranslateTransition(Duration.seconds(1), firstCar);
        moveFirst.setByY(200); // NORTH → aşağı
        moveFirst.setOnFinished(e -> simulationPane1.getChildren().remove(firstCar));
        moveFirst.play();

        queue.poll();

        for (int i = 1; i < cars.size(); i++) {
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), cars.get(i));
            move.setByY(30); // aşağı kay
            move.play();
        }

        Rectangle newCar = cloneCarLike(firstCar);
        newCar.setLayoutX(firstCar.getLayoutX());
        newCar.setLayoutY(firstCar.getLayoutY() - (queue.size() * 30));
        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);
    }

    public void moveVehiclesSouth() {
        Queue<Rectangle> queue = queues.get(Direction.SOUTH);
        if (queue.isEmpty()) return;

        List<Rectangle> cars = new ArrayList<>(queue);
        Rectangle firstCar = cars.get(0);

        TranslateTransition moveFirst = new TranslateTransition(Duration.seconds(1), firstCar);
        moveFirst.setByY(-200); // SOUTH → yukarı
        moveFirst.setOnFinished(e -> simulationPane1.getChildren().remove(firstCar));
        moveFirst.play();

        queue.poll();

        for (int i = 1; i < cars.size(); i++) {
            TranslateTransition move = new TranslateTransition(Duration.seconds(0.3), cars.get(i));
            move.setByY(-30);
            move.play();
        }

        Rectangle newCar = cloneCarLike(firstCar);
        newCar.setLayoutX(firstCar.getLayoutX());
        newCar.setLayoutY(firstCar.getLayoutY() + (queue.size() * 30));
        simulationPane1.getChildren().add(newCar);
        queue.add(newCar);
    }



    private Rectangle cloneCarLike(Rectangle reference) {
        Rectangle clone = new Rectangle(
                reference.getLayoutX(),
                reference.getLayoutY(),
                reference.getWidth(),
                reference.getHeight()
        );
        clone.setFill(reference.getFill());
        clone.setArcWidth(reference.getArcWidth());
        clone.setArcHeight(reference.getArcHeight());

        // ✅ her ihtimale karşı Translate'leri sıfırla
        clone.setTranslateX(0);
        clone.setTranslateY(0);

        return clone;
    }


    private Rectangle westReferenceCar;

    public void setWestReference(Rectangle reference) {
        this.westReferenceCar = reference;

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
