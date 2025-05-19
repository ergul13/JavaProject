package model;

public class TrafficLight {

    public enum LightState {
        GREEN, YELLOW, RED
    }

    private LightState state = LightState.RED;
    private int greenDuration;
    private int remainingTime;

    public TrafficLight(int greenDuration) {
        this.greenDuration = greenDuration;
        this.remainingTime = greenDuration;
        this.state = LightState.GREEN;
    }

    public void tick() {
        if (remainingTime > 0) {
            remainingTime--;
        }
    }

    public LightState getState() {
        return state;
    }

    public void setState(LightState newState) {
        this.state = newState;
        if (newState == LightState.GREEN) {
            this.remainingTime = greenDuration;
        } else if (newState == LightState.YELLOW) {
            this.remainingTime = 3; // sabit sarı süresi
        }
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public int getGreenDuration() {
        return greenDuration;
    }
}
