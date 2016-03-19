package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.configuration.ConfigurationSection;

import fr.neatmonster.nocheatplus.config.ConfPaths;

/**
 * Parameters for actual flying (context dependent).
 * @author asofold
 *
 */
public class ModelFlying {

    /**
     * The id for this model. The default model id is null.
     */
    public final String id;

    /** The horizontal per-tick distance in creative mode. */
    public static final double HORIZONTAL_SPEED = 0.6D;
    /** The vertical per-tick distance in creative mode. */
    public static final double VERTICAL_ASCEND_SPEED   = 1D;

    /** Modifier for horizontal flying speed in per cent. */
    public final double hModSpeed;
    /** Additional modifier for horizontal flying speed (multiplier), when sprinting. */
    public final double hModSprint;
    /** Modifier for vertical flying speed in per cent, for ascending. */
    public final double vModAscendSpeed;
    /** Maximum flying height above the maximum building height of the map. */
    public final double maxHeight;
    /** Apply modifiers like sprint, flyspeed, walkspeed, potions. */
    public final boolean applyModifiers;
    /** Allow falling with gravity, including friction. */
    public final boolean gravity;

    public ModelFlying(String id, double hModSpeed, double hModSprint, double vModAscendSpeed, double maxHeight, boolean applyModifiers, boolean gravity) {
        // TODO: vertical ascend/descend, limit gain a/d/v, limit abs. distance a/d/v
        // TODO: possibly other friction based envelope constraints.
        // TODO: Check if needed: use fly/walk speed.
        this.id = id;
        this.hModSpeed = hModSpeed;
        this.hModSprint = hModSprint;
        this.vModAscendSpeed = vModAscendSpeed;
        this.maxHeight = maxHeight;
        this.applyModifiers = applyModifiers;
        this.gravity = gravity;
    }

    public ModelFlying(String id, ConfigurationSection config, String prefix, ModelFlying defaults) {
        // For differing models, some paths may or may not be set in the default configuration.
        this(
                id,
                config.getDouble(prefix + ConfPaths.SUB_HORIZONTAL_SPEED, defaults.hModSpeed),
                config.getDouble(prefix + ConfPaths.SUB_HORIZONTAL_MODSPRINT, defaults.hModSprint),
                config.getDouble(prefix + ConfPaths.SUB_VERTICAL_ASCEND_SPEED, defaults.vModAscendSpeed),
                config.getDouble(prefix + ConfPaths.SUB_VERTICAL_MAXHEIGHT, defaults.maxHeight),
                config.getBoolean(prefix + ConfPaths.SUB_MODIFIERS, defaults.applyModifiers),
                config.getBoolean(prefix + ConfPaths.SUB_VERTICAL_GRAVITY, defaults.gravity)
                );
    }

    public ModelFlying() {
        this(null, 100.0, 1.92, 100.0, 128, true, true);
    }

}
