package model;

import java.util.*;

public class VehicleManager {
    private Map<Direction, TrafficLight> lights = new EnumMap<>(Direction.class);

    public VehicleManager() {
        for (Direction d : Direction.values()) {
            lights.put(d, new TrafficLight(d, 0));
        }
    }

    public void setVehicleCount(Direction direction, int count) {
        lights.get(direction).setVehicleCount(count);
    }

    public void calculateGreenTimes() {
        int totalVehicles = lights.values().stream().mapToInt(TrafficLight::getVehicleCount).sum();
        int cycleTime = 120;

        for (TrafficLight light : lights.values()) {
            int count = light.getVehicleCount();
            int green = (int) ((count / (double) totalVehicles) * cycleTime);
            green = Math.max(10, Math.min(green, 60));
            light.setGreenTime(green);
        }
    }

    public Collection<TrafficLight> getAllLights() {
        return lights.values();
    }
}
