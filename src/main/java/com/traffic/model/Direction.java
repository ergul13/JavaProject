package com.traffic.model;

/**
 * Kavşaktaki 4 yönü temsil eden enum.
 * Saat yönünde sıralama: Kuzey (NORTH) -> Doğu (EAST) -> Güney (SOUTH) -> Batı (WEST)
 */
public enum Direction {
    NORTH(0, "Kuzey"),
    EAST(1, "Doğu"),
    SOUTH(2, "Güney"),
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

    /**
     * Belirli bir dönüş yönüne göre hedef yönü döndürür.
     * @param turn Dönüş yönü (STRAIGHT, LEFT, RIGHT)
     * @return Hedef yön
     */
    public Direction getTargetDirection(TurnDirection turn) {
        switch (turn) {
            case STRAIGHT:
                return getOpposite();
            case LEFT:
                return getLeft();
            case RIGHT:
                return getRight();
            default:
                return getOpposite();
        }
    }

    /**
     * Karşı yönü döndürür.
     */
    public Direction getOpposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
            default: return this;
        }
    }

    /**
     * Sol taraftaki yönü döndürür (saat yönünün tersine).
     */
    public Direction getLeft() {
        switch (this) {
            case NORTH: return WEST;
            case WEST: return SOUTH;
            case SOUTH: return EAST;
            case EAST: return NORTH;
            default: return this;
        }
    }

    /**
     * Sağ taraftaki yönü döndürür (saat yönünde).
     */
    public Direction getRight() {
        switch (this) {
            case NORTH: return EAST;
            case EAST: return SOUTH;
            case SOUTH: return WEST;
            case WEST: return NORTH;
            default: return this;
        }
    }
}
