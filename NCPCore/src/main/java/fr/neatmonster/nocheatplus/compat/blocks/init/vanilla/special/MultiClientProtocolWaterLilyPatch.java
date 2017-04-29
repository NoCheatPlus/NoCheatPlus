package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla.special;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.AbstractBlockPropertiesPatch;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * WATER_LILY for multi client protocol support between 1.7.x - 1.11.x.
 * 
 * @author asofold
 *
 */
public class MultiClientProtocolWaterLilyPatch extends AbstractBlockPropertiesPatch {
    // TODO: Later just dump these into the generic registry (on activation), let BlockProperties fetch.

    public MultiClientProtocolWaterLilyPatch() {
        activation
        .neutralDescription("WATER_LILY block shape patch for multi client protocol support around 1.7.x - 1.11.x.")
        .advertise(true)
        .pluginExist("ViaVersion")
        // TODO: Other/More ?
        ;
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {
        BlockFlags.addFlags(Material.WATER_LILY, 
                BlockProperties.F_GROUND | BlockProperties.F_HEIGHT8_1 | BlockProperties.F_GROUND_HEIGHT);
    }

}
