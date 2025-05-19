package model;

import java.util.EnumMap;
import java.util.Map;

public class TrafficCalculator {

    private final Map<Direction, Integer> counts = new EnumMap<>(Direction.class);
    private final Map<Direction, Integer> durations = new EnumMap<>(Direction.class);

    private final int MIN_TIME = 10;
    private final int MAX_TIME = 60;
    private final int TOTAL_TIME = 120;

    public void setInitialCounts(Integer n, Integer s, Integer e, Integer w) {
        counts.put(Direction.NORTH, n != null ? n : 0);
        counts.put(Direction.SOUTH, s != null ? s : 0);
        counts.put(Direction.EAST,  e != null ? e : 0);
        counts.put(Direction.WEST,  w != null ? w : 0);

        calculateDurations();
    }

    private void calculateDurations() {
        int totalVehicles = counts.values().stream().mapToInt(Integer::intValue).sum();
        if (totalVehicles == 0) {
            for (Direction dir : Direction.values()) durations.put(dir, TOTAL_TIME / 4);
            return;
        }

        Map<Direction, Integer> tempDurations = new EnumMap<>(Direction.class);
        int usedTime = 0;

        for (Direction dir : Direction.values()) {
            int raw = (counts.get(dir) * TOTAL_TIME) / totalVehicles;
            int clamped = Math.max(MIN_TIME, Math.min(MAX_TIME, raw));
            tempDurations.put(dir, clamped);
            usedTime += clamped;
        }

        int diff = TOTAL_TIME - usedTime;
        while (diff != 0) {
            for (Direction dir : Direction.values()) {
                int dur = tempDurations.get(dir);
                if (diff > 0 && dur < MAX_TIME) {
                    tempDurations.put(dir, dur + 1);
                    diff--;
                } else if (diff < 0 && dur > MIN_TIME) {
                    tempDurations.put(dir, dur - 1);
                    diff++;
                }
                if (diff == 0) break;
            }
        }

        durations.putAll(tempDurations);
    }

    public int getCount(Direction dir) {
        return counts.getOrDefault(dir, 0);
    }

    public int getDuration(Direction dir) {
        return durations.getOrDefault(dir, MIN_TIME);
    }

    public Map<Direction, Integer> getDurations() {
        return durations;
    }

    public void simulateSecond() {
        // zaman ilerletme işlemleri gerekirse buraya
    }
}
