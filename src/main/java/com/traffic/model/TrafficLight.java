package com.traffic.model;

public class TrafficLight {
    private final Direction direction;
    private LightState state;
    private double greenDuration;
    private double countdown;

    public TrafficLight(Direction direction) {
        this.direction = direction;
        this.state = LightState.RED;
        this.greenDuration = 0;
        this.countdown = 0;
    }

    public Direction getDirection() {
        return direction;
    }

    public LightState getState() {
        return state;
    }

    public void setState(LightState state) {
        this.state = state;
    }

    public double getGreenDuration() {
        return greenDuration;
    }

    public void setGreenDuration(double greenDuration) {
        this.greenDuration = greenDuration;
    }

    public double getCountdown() {
        return countdown;
    }

    public void setCountdown(double countdown) {
        this.countdown = countdown;
    }

    public void decrementCountdown(double delta) {
        this.countdown -= delta;
        if (this.countdown < 0) this.countdown = 0;
    }

    /**
     * Işığın geçiş izni verip vermediğini kontrol eder.
     */
    public boolean allowsPass() {
        return state == LightState.GREEN;
    }

    public void reset() {
        this.state = LightState.RED;
        this.countdown = 0;
    }
}

