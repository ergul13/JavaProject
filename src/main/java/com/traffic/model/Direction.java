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
     * Sol taraftaki yönü döndürür (sürücünün soluna dönüş).
     * Gerçek trafik kurallarına göre:
     * - NORTH'dan (yukarı) gelen → sola dönünce WEST'e (sol tarafa) gider
     * - SOUTH'dan (aşağı) gelen → sola dönünce EAST'e (sağ tarafa) gider
     * - EAST'den (sağdan) gelen → sola dönünce NORTH'a (yukarı) gider
     * - WEST'den (soldan) gelen → sola dönünce SOUTH'a (aşağı) gider
     */
    public Direction getLeft() {
        switch (this) {
            case NORTH: return WEST;   // Yukarı giderken sola dön → Sola git
            case SOUTH: return EAST;   // Aşağı giderken sola dön → Sağa git
            case EAST: return NORTH;   // Sola giderken sola dön → Yukarı git
            case WEST: return SOUTH;   // Sağa giderken sola dön → Aşağı git
            default: return this;
        }
    }

    /**
     * Sağ taraftaki yönü döndürür (sürücünün sağına dönüş).
     * Gerçek trafik kurallarına göre:
     * - NORTH'dan (yukarı) gelen → sağa dönünce EAST'e (sağ tarafa) gider
     * - SOUTH'dan (aşağı) gelen → sağa dönünce WEST'e (sol tarafa) gider
     * - EAST'den (sağdan) gelen → sağa dönünce SOUTH'a (aşağı) gider
     * - WEST'den (soldan) gelen → sağa dönünce NORTH'a (yukarı) gider
     */
    public Direction getRight() {
        switch (this) {
            case NORTH: return EAST;   // Yukarı giderken sağa dön → Sağa git
            case SOUTH: return WEST;   // Aşağı giderken sağa dön → Sola git
            case EAST: return SOUTH;   // Sola giderken sağa dön → Aşağı git
            case WEST: return NORTH;   // Sağa giderken sağa dön → Yukarı git
            default: return this;
        }
    }
}
