/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
    /** Default ground moving mechanics (jump, lost ground). */
    public final boolean ground;

    /** Ugly, but true. */
    private boolean scaleLevitationEffect = false;

    // TODO: Switch to something else (non final, chaining with getters and setters)? [though models are referenced in past moves]
    public ModelFlying(String id, double hModSpeed, double hModSprint, double vModAscendSpeed, double maxHeight, 
            boolean applyModifiers, boolean gravity, final boolean ground) {
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
        this.ground = ground;
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
                config.getBoolean(prefix + ConfPaths.SUB_VERTICAL_GRAVITY, defaults.gravity),
                config.getBoolean(prefix + ConfPaths.SUB_GROUND, defaults.ground)
                );
    }

    public ModelFlying() {
        this(null, 100.0, 1.92, 100.0, 128, true, true, true);
    }

    public boolean isScaleLevitationEffect() {
        return scaleLevitationEffect;
    }

    public void setScaleLevitationEffect(boolean scaleLevitationEffect) {
        this.scaleLevitationEffect = scaleLevitationEffect;
    }

}
