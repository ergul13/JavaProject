package model;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class TrafficLamp {
    private final Circle red;
    private final Circle yellow;
    private final Circle green;

    public TrafficLamp(Circle red, Circle yellow, Circle green) {
        this.red = red;
        this.yellow = yellow;
        this.green = green;
        reset();
    }

    public void set(String color) {
        red.setOpacity(color.equals("RED") ? 1.0 : 0.3);
        yellow.setOpacity(color.equals("YELLOW") ? 1.0 : 0.3);
        green.setOpacity(color.equals("GREEN") ? 1.0 : 0.3);
    }

    public void reset() {
        red.setOpacity(0.3);
        yellow.setOpacity(0.3);
        green.setOpacity(0.3);
    }
}
