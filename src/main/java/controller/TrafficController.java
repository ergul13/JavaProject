package controller;

import model.*;

public class TrafficController {
    private VehicleManager manager;

    public TrafficController(VehicleManager manager) {
        this.manager = manager;
    }

    public void setVehicleDensity(Direction direction, int count) {
        manager.setVehicleCount(direction, count);
    }

    public void updateSignalTimings() {
        manager.calculateGreenTimes();
    }

    public VehicleManager getManager() {
        return manager;
    }
}
