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
        // Kullanıcıdan veri gelmezse rastgele 0-25 ata
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
        int total = northCount + southCount + eastCount + westCount;
        int availableGreen = TOTAL_CYCLE_TIME - 4 * YELLOW_TIME;

        if (total == 0) total = 1; // 0’a bölme hatası engellenir

        TrafficLight[] lights = new TrafficLight[4];

        lights[0] = createTrafficLight(scaleGreen(northCount, total, availableGreen));
        lights[1] = createTrafficLight(scaleGreen(southCount, total, availableGreen));
        lights[2] = createTrafficLight(scaleGreen(eastCount, total, availableGreen));
        lights[3] = createTrafficLight(scaleGreen(westCount, total, availableGreen));

        return lights; // sırasıyla NORTH, SOUTH, EAST, WEST
    }
    public TrafficLight[] calculateLightsFromDirections(Set<Direction> directions, int availableGreen) {
        int total = 0;
        for (Direction dir : directions) {
            total += getCount(dir);
        }
        if (total == 0) total = 1;

        int[] greenDurations = new int[4];
        double[] rawValues = new double[4];
        int sum = 0;

        for (Direction dir : directions) {
            int count = getCount(dir);
            double raw = (count / (double) total) * availableGreen;
            int g = (int) Math.floor(raw);
            rawValues[dir.ordinal()] = raw;
            greenDurations[dir.ordinal()] = Math.max(10, Math.min(60, g));
            sum += greenDurations[dir.ordinal()];
        }

        // Kalan saniyeyi yoğun yönlere dağıt
        int diff = availableGreen - sum;
        while (diff > 0) {
            Direction max = directions.stream().max((a, b) -> Integer.compare(getCount(a), getCount(b))).orElse(Direction.NORTH);
            if (greenDurations[max.ordinal()] < 60) {
                greenDurations[max.ordinal()]++;
                diff--;
            } else {
                break; // artık eklenemiyor
            }
        }

        TrafficLight[] lights = new TrafficLight[4];
        for (Direction dir : Direction.values()) {
            if (directions.contains(dir)) {
                lights[dir.ordinal()] = createTrafficLight(greenDurations[dir.ordinal()]);
            } else {
                lights[dir.ordinal()] = createTrafficLight(0); // yanmayacak yön
            }
        }
        return lights;
    }



    private int scaleGreen(int count, int total, int availableGreen) {
        int g = (int) ((count / (double) total) * availableGreen);
        return Math.max(MIN_GREEN, Math.min(MAX_GREEN, g));
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