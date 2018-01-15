package fr.neatmonster.nocheatplus.compat.blocks.init.vanilla.special;

import java.util.LinkedList;
import java.util.List;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.activation.ActivationUtil;
import fr.neatmonster.nocheatplus.compat.blocks.AbstractBlockPropertiesPatch;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;

/**
 * WATER_LILY for multi client protocol support between 1.7.x - 1.11.x.
 * 
 * @author asofold
 *
 */
public class MultiClientProtocolBlockShapePatch extends AbstractBlockPropertiesPatch {
    // TODO: Later just dump these into the generic registry (on activation), let BlockProperties fetch.

    public MultiClientProtocolBlockShapePatch() {
        activation
        .neutralDescription("Block shape patch for multi client protocol support around 1.7.x - 1.12.x.")
        .advertise(true)
        .setConditionsAND()
        .notUnitTest()
        .condition(ActivationUtil.getMultiProtocolSupportPluginActivation())
        // TODO: Other/More ?
        ;
    }

    @Override
    public void setupBlockProperties(WorldConfigProvider<?> worldConfigProvider) {

        final List<String> done = new LinkedList<String>();

        BlockFlags.addFlags(Material.WATER_LILY, 
                BlockProperties.F_GROUND 
                | BlockProperties.F_HEIGHT8_1 
                | BlockProperties.F_GROUND_HEIGHT);
        done.add("WATER_LILY");

        BlockFlags.addFlags(Material.SOIL, 
                BlockProperties.F_MIN_HEIGHT16_15 
                | BlockProperties.F_HEIGHT100 
                | BlockProperties.F_GROUND_HEIGHT);
        done.add("SOIL");

        try {
            BlockFlags.addFlags(Material.GRASS_PATH, 
                    BlockProperties.F_MIN_HEIGHT16_15 
                    | BlockProperties.F_HEIGHT100 
                    | BlockProperties.F_GROUND_HEIGHT);
            done.add("GRASS_PATH");
        }
        catch (Throwable t) {
            // TODO: What throws for enum not there.
        }

        StaticLog.logInfo("Applied block patches for multi client protocol support: " 
                + StringUtil.join(done, ", "));
    }

}
