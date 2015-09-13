package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.configuration.ConfigurationSection;

import fr.neatmonster.nocheatplus.config.ConfPaths;

/**
 * Parameters for actual flying (context dependent).
 * @author asofold
 *
 */
public class ModelFlying {

    /** The horizontal per-tick distance in creative mode. */
    public static final double HORIZONTAL_SPEED = 0.6D;
    /** The vertical per-tick distance in creative mode. */
    public static final double VERTICAL_SPEED   = 1D;

    /** Modifier for horizontal flying speed in per cent. */
    public final double hMod;
    /** Additional modifier for horizontal flying speed (multiplier), when sprinting. */
    public final double hModSprint;
    /** Modifier for vertical flying speed in per cent. */
    public final double vMod;
    /** Maximum flying height to add to the maximum height of the map. */
    public final double maxHeight;

    public ModelFlying(double hMod, double hModSprint, double vMod, double maxHeight) {
        this.hMod = hMod;
        this.hModSprint = hModSprint;
        this.vMod = vMod;
        this.maxHeight = maxHeight;
    }

    public ModelFlying(ConfigurationSection config, String prefix, ModelFlying defaults) {
        // TODO: Add @Hidden annotation (counts for child nodes) and add all defaults.
        hMod = config.getDouble(prefix + ConfPaths.SUB_HORIZONTALSPEED, defaults.hMod);
        hModSprint = config.getDouble(prefix + ConfPaths.SUB_MODSPRINT, defaults.hModSprint); // Currently a hidden setting.
        vMod = config.getDouble(prefix + ConfPaths.SUB_VERTICALSPEED, defaults.vMod);
        maxHeight = config.getDouble(prefix + ConfPaths.SUB_MAXHEIGHT, defaults.maxHeight);
    }

    public ModelFlying() {
        this(100.0, 1.92, 100, 128);
    }

}
