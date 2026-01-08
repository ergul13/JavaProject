package com.traffic.model;

/**
 * Trafik ışığının durumlarını temsil eden enum.
 */
public enum LightState {
    GREEN("Yeşil"),
    YELLOW("Sarı"),
    RED("Kırmızı");

    private final String displayName;

    LightState(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

