package model;

public class TrafficLight {
    private Direction direction;
    private int vehicleCount;
    private int greenTime;

    public TrafficLight(Direction direction, int vehicleCount) {
        this.direction = direction;
        this.vehicleCount = vehicleCount;
    }

    public Direction getDirection() {
        return direction;
    }

    public int getVehicleCount() {
        return vehicleCount;
    }

    public void setVehicleCount(int count) {
        this.vehicleCount = count;
    }

    public int getGreenTime() {
        return greenTime;
    }

    public void setGreenTime(int greenTime) {
        this.greenTime = greenTime;
    }
}
