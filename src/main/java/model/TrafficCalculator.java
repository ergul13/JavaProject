package model;

import java.util.Random;
import java.util.Set;

public class TrafficCalculator {
    private static final int TOTAL_CYCLE_TIME = 120;
    private static final int YELLOW_TIME = 3;
    private static final int MIN_GREEN = 10;
    private static final int MAX_GREEN = 60;

    private int northCount;
    private int southCount;
    private int eastCount;
    private int westCount;

    private final TrafficSensor northSensor = new TrafficSensor(Direction.NORTH);
    private final TrafficSensor southSensor = new TrafficSensor(Direction.SOUTH);
    private final TrafficSensor eastSensor  = new TrafficSensor(Direction.EAST);
    private final TrafficSensor westSensor  = new TrafficSensor(Direction.WEST);

    private final Random random = new Random();

    public void setInitialCounts(Integer north, Integer south, Integer east, Integer west) {
        northCount = (north != null) ? north : random.nextInt(26);
        southCount = (south != null) ? south : random.nextInt(26);
        eastCount  = (east  != null) ? east  : random.nextInt(26);
        westCount  = (west  != null) ? west  : random.nextInt(26);
    }

    public void simulateSecond() {
        northCount += northSensor.detectVehicles();
        southCount += southSensor.detectVehicles();
        eastCount  += eastSensor.detectVehicles();
        westCount  += westSensor.detectVehicles();
    }

    public TrafficLight createTrafficLight(int greenDuration) {
        TrafficLight light = new TrafficLight();
        light.setGreenDuration(greenDuration);
        return light;
    }

    public TrafficLight[] calculateInitialLights() {
        int totalVehicles = northCount + southCount + eastCount + westCount;
        int availableGreen = TOTAL_CYCLE_TIME - 4 * YELLOW_TIME;
        if (totalVehicles == 0) totalVehicles = 1;
        TrafficLight[] lights = new TrafficLight[4];
        lights[0] = createTrafficLight(scaleGreen(northCount, totalVehicles, availableGreen));
        lights[1] = createTrafficLight(scaleGreen(southCount, totalVehicles, availableGreen));
        lights[2] = createTrafficLight(scaleGreen(eastCount, totalVehicles, availableGreen));
        lights[3] = createTrafficLight(scaleGreen(westCount, totalVehicles, availableGreen));
        return lights;
    }

    public TrafficLight[] calculateLightsFromDirections(Set<Direction> directions, int availableGreen) {
        int totalVehicles = 0;
        for (Direction dir : directions) {
            totalVehicles += getCount(dir);
        }
        if (totalVehicles == 0) totalVehicles = 1;
        int[] greenDurations = new int[4];
        double[] rawValues = new double[4];
        int sum = 0;
        for (Direction dir : directions) {
            int count = getCount(dir);
            double raw = (count / (double) totalVehicles) * availableGreen;
            int green = (int) Math.floor(raw);
            rawValues[dir.ordinal()] = raw;
            greenDurations[dir.ordinal()] = Math.max(MIN_GREEN, Math.min(MAX_GREEN, green));
            sum += greenDurations[dir.ordinal()];
        }
        int diff = availableGreen - sum;
        while (diff > 0) {
            Direction max = directions.stream().max((a, b) -> Integer.compare(getCount(a), getCount(b))).orElse(Direction.NORTH);
            if (greenDurations[max.ordinal()] < MAX_GREEN) {
                greenDurations[max.ordinal()]++;
                diff--;
            } else {
                break;
            }
        }
        TrafficLight[] lights = new TrafficLight[4];
        for (Direction dir : Direction.values()) {
            if (directions.contains(dir)) {
                lights[dir.ordinal()] = createTrafficLight(greenDurations[dir.ordinal()]);
            } else {
                lights[dir.ordinal()] = createTrafficLight(0);
            }
        }
        return lights;
    }

    private int scaleGreen(int count, int total, int availableGreen) {
        int green = (int) ((count / (double) total) * availableGreen);
        return Math.max(MIN_GREEN, Math.min(MAX_GREEN, green));
    }

    public int getCount(Direction d) {
        return switch (d) {
            case NORTH -> northCount;
            case SOUTH -> southCount;
            case EAST -> eastCount;
            case WEST -> westCount;
        };
    }

    public Direction getMostCrowdedDirectionExcluding(Direction exclude) {
        int max = -1;
        Direction result = Direction.NORTH;
        for (Direction dir : Direction.values()) {
            if (dir == exclude) continue;
            int count = getCount(dir);
            if (count > max) {
                max = count;
                result = dir;
            }
        }
        return result;
    }
}
