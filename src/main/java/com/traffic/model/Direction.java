package com.traffic.model;

/**
 * Kavşaktaki 4 yönü temsil eden enum.
 * Kuzey (NORTH), Güney (SOUTH), Doğu (EAST), Batı (WEST)
 */
public enum Direction {
    NORTH(0, "Kuzey"),
    SOUTH(1, "Güney"),
    EAST(2, "Doğu"),
    WEST(3, "Batı");

    private final int index;
    private final String displayName;

    Direction(int index, String displayName) {
        this.index = index;
        this.displayName = displayName;
    }

    public int getIndex() {
        return index;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Direction fromIndex(int index) {
        for (Direction d : values()) {
            if (d.index == index) return d;
        }
        return NORTH;
    }
}
