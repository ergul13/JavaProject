package model;

import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.*;

public class VehicleManager {

    private final Map<Direction, Queue<Rectangle>> queues = new EnumMap<>(Direction.class);
    private final Pane simulationPane1;

    public VehicleManager(Pane simulationPane1) {
        this.simulationPane1 = simulationPane1;
        for (Direction dir : Direction.values()) {
            queues.put(dir, new LinkedList<>());
        }
    }

    // FXML'den araç eklemek için
    public void addInitialVehicle(Direction dir, Rectangle car) {
        queues.get(dir).add(car);
    }

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



    public int getVehicleCount(Direction dir) {
        return queues.get(dir).size();
    }
    public void moveVehicleAndRemoveWhenOutside(Rectangle car, Direction dir) {
        TranslateTransition move = new TranslateTransition(Duration.seconds(1), car);

        switch (dir) {
            case WEST -> move.setByX(200);
            case EAST -> move.setByX(-200);
            case NORTH -> move.setByY(-200);
            case SOUTH -> move.setByY(200);
        }

        move.setOnFinished(e -> {
            double finalX = car.getLayoutX() + car.getTranslateX();
            double finalY = car.getLayoutY() + car.getTranslateY();

            boolean outX = finalX < 0 || finalX > simulationPane1.getWidth();
            boolean outY = finalY < 0 || finalY > simulationPane1.getHeight();

            if (outX || outY) {
                simulationPane1.getChildren().remove(car);
            }
        });

        move.play();
    }
    public void moveOutOfPaneAndRemove(Rectangle car, Direction dir) {
        double targetX = car.getLayoutX();
        double targetY = car.getLayoutY();
        double moveAmount = 0;

        switch (dir) {
            case WEST -> {
                moveAmount = simulationPane1.getWidth() - (car.getLayoutX() + car.getWidth());
            }
            case EAST -> {
                moveAmount = -car.getLayoutX() - car.getWidth(); // sola
            }
            case NORTH -> {
                moveAmount = -car.getLayoutY() - car.getHeight(); // yukarı
            }
            case SOUTH -> {
                moveAmount = simulationPane1.getHeight() - (car.getLayoutY() + car.getHeight());
            }
        }

        TranslateTransition move = new TranslateTransition(Duration.seconds(2), car);
        if (dir == Direction.WEST || dir == Direction.EAST) {
            move.setByX(moveAmount);
        } else {
            move.setByY(moveAmount);
        }

        move.setOnFinished(e -> {
            simulationPane1.getChildren().remove(car);
        });

        move.play();
    }


}
