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

        // İlk aracı dışarı taşı ve sahneden kaldır
        moveOutOfPaneAndRemove(firstCar, Direction.WEST);

        // Kuyruktan çıkar
        queue.poll();

        double spacing = 30;

        // Diğer arabaları hareket ettir
        for (int i = 1; i < cars.size(); i++) {
            Rectangle car = cars.get(i);

            double newLayoutX = car.getLayoutX() + spacing;

            // Animate: önce TranslateTransition ile hareket ettir
            TranslateTransition tt = new TranslateTransition(Duration.seconds(0.3), car);
            tt.setByX(spacing);
            tt.setOnFinished(e -> {
                // Animasyon bittiğinde Translate sıfırla, LayoutX'i güncelle
                car.setTranslateX(0);
                car.setLayoutX(newLayoutX);
            });
            tt.play();
        }

        // Yeni araba ekle
        Rectangle newCar = new Rectangle(25, 16, Color.web("#fcfeff"));
        newCar.setLayoutX(165.0 - queue.size() * spacing);

        double sceneY = firstCar.localToScene(firstCar.getBoundsInLocal()).getMinY();
        double localY = simulationPane1.sceneToLocal(0, sceneY).getY();
        newCar.setLayoutY(localY);

        newCar.setStroke(Color.BLACK);
        newCar.setArcWidth(5);
        newCar.setArcHeight(5);
        newCar.setTranslateX(0);
        newCar.setTranslateY(0);
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
        double moveAmount = 0;

        switch (dir) {
            case WEST -> {
                double currentX = car.getLayoutX() + car.getTranslateX() + car.getWidth();
                moveAmount = simulationPane1.getWidth() - currentX + 50; // biraz fazlası
            }
            // Diğer yönlerde de benzer şekilde
        }

        TranslateTransition move = new TranslateTransition(Duration.seconds(2), car);
        if (dir == Direction.WEST || dir == Direction.EAST) {
            move.setByX(moveAmount);
        } else {
            move.setByY(moveAmount);
        }

        move.setOnFinished(e -> {
            car.setTranslateX(0);
            car.setTranslateY(0);
            simulationPane1.getChildren().remove(car);
            System.out.println("Araba silindi: " + car);
        });

        move.play();
    }




}
