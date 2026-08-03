package com.denchy.serverrack.det;

/**
 * Nuke (yellow mushroom) look settings. Synced client<->server via payloads,
 * editable in-game via the hold-to-open detonator menu.
 * Server holds the values used by BoomJob.NUKE.
 */
public final class NukeConfig {
    private NukeConfig() {}

    /** Stem column height in blocks the mushroom climbs before the cap blooms. */
    public static int stemHeight = 14;
    /** How fat the cap gets. */
    public static int capRadius = 8;
    /** Ground dust ring reach. */
    public static int ringRadius = 10;
    /** Particle count multiplier, 40 = 1.0x. */
    public static int density = 40;

    public static final int STEM_MIN = 4, STEM_MAX = 40;
    public static final int CAP_MIN = 3, CAP_MAX = 20;
    public static final int RING_MIN = 4, RING_MAX = 28;
    public static final int DENSITY_MIN = 5, DENSITY_MAX = 100;

    public static void set(int stem, int cap, int ring, int dens) {
        stemHeight = clampi(stem, STEM_MIN, STEM_MAX);
        capRadius = clampi(cap, CAP_MIN, CAP_MAX);
        ringRadius = clampi(ring, RING_MIN, RING_MAX);
        density = clampi(dens, DENSITY_MIN, DENSITY_MAX);
    }

    private static int clampi(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
