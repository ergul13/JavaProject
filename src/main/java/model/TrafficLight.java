package model;

public class TrafficLight {

    private int greenDuration;   // saniye cinsinden
    private final int yellowDuration = 3; // sabit 3 saniye
    private int redDuration;

    private int remainingTime;
    private LightState state;

    public enum LightState {
        GREEN, YELLOW, RED
    }

    public TrafficLight() {
        this.greenDuration = 0;
        this.redDuration = 0;
        this.remainingTime = 0;
        this.state = LightState.RED; // başlangıç durumu
    }

    public void setGreenDuration(int seconds) {
        this.greenDuration = seconds;
        this.redDuration = 120 - seconds - yellowDuration;
    }

    public int getGreenDuration() {
        return greenDuration;
    }

    public int getYellowDuration() {
        return yellowDuration;
    }

    public int getRedDuration() {
        return redDuration;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public LightState getState() {
        return state;
    }

    public void setState(LightState state) {
        this.state = state;
        if (state == LightState.GREEN) {
            remainingTime = greenDuration;
        } else if (state == LightState.YELLOW) {
            remainingTime = yellowDuration;
        } else {
            remainingTime = redDuration;
        }
    }

    public void tick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }
}
