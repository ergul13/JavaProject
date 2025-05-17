package model;

import java.util.Random;

public class TrafficSensor {

    private final Direction direction;
    private final Random random;

    public TrafficSensor(Direction direction) {
        this.direction = direction;
        this.random = new Random();
    }

    // Sensör bu metotla veri gönderir
    public int detectVehicles() {
        return random.nextInt(3); // 0, 1, ya da 2
    }

    public Direction getDirection() {
        return direction;
    }
}