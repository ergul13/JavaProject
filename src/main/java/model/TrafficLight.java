package model;

/**
 * Model class representing a traffic light for one direction
 */
public class TrafficLight {
    private final Direction direction;
    private LightColor currentColor;
    private int remainingTime; // in seconds
    private int greenDuration; // allocated green time in seconds

    public TrafficLight(Direction direction) {
        this.direction = direction;
        this.currentColor = LightColor.RED;
        this.remainingTime = 0;
        this.greenDuration = 0;
    }

    public Direction getDirection() {
        return direction;
    }

    public LightColor getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(LightColor color) {
        this.currentColor = color;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int time) {
        this.remainingTime = time;
    }

    public int getGreenDuration() {
        return greenDuration;
    }

    public void setGreenDuration(int duration) {
        this.greenDuration = duration;
    }

    /**
     * Decrement remaining time by 1 second
     */
    public void tick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    /**
     * Reset light to red
     */
    public void reset() {
        this.currentColor = LightColor.RED;
        this.remainingTime = 0;
    }
}

