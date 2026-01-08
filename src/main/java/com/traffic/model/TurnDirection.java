package com.traffic.model;

/**
 * Araçların kavşakta gidebileceği yönleri temsil eden enum.
 * STRAIGHT: Düz git
 * LEFT: Sola dön
 * RIGHT: Sağa dön
 */
public enum TurnDirection {
    STRAIGHT("Düz"),
    LEFT("Sol"),
    RIGHT("Sağ");

    private final String displayName;

    TurnDirection(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

