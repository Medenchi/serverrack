package com.denchy.serverrack.smoke;

/**
 * Global smoke settings. Synced client<->server through packets.
 * Editable in-game via the U menu.
 */
public final class SmokeConfig {
    private SmokeConfig() {}

    // How often (in ticks) each active rack emits smoke. Lower = more frequent.
    public static int frequencyTicks = 4;
    // How many particles per emission burst.
    public static int particlesPerEmit = 3;
    // How far (in blocks) smoke may spread along a ceiling from its source column.
    public static int ceilingRadius = 6;
    // Max vertical distance smoke rises before it looks for a ceiling to hug.
    public static int maxRiseHeight = 24;
    // Density cap: how thick a ceiling pocket can get (visual scaling 0..this).
    public static int maxDensity = 40;

    // Clamp ranges (used by the screen when the user types numbers).
    public static final int FREQ_MIN = 1, FREQ_MAX = 40;
    public static final int COUNT_MIN = 1, COUNT_MAX = 32;
    public static final int RADIUS_MIN = 1, RADIUS_MAX = 24;
    public static final int RISE_MIN = 2, RISE_MAX = 64;
    public static final int DENSITY_MIN = 1, DENSITY_MAX = 200;

    public static void set(int freq, int count, int radius, int rise, int density) {
        frequencyTicks = clamp(freq, FREQ_MIN, FREQ_MAX);
        particlesPerEmit = clamp(count, COUNT_MIN, COUNT_MAX);
        ceilingRadius = clamp(radius, RADIUS_MIN, RADIUS_MAX);
        maxRiseHeight = clamp(rise, RISE_MIN, RISE_MAX);
        maxDensity = clamp(density, DENSITY_MIN, DENSITY_MAX);
    }

    public static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
