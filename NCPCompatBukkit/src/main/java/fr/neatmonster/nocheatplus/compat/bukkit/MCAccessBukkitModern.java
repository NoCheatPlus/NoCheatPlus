package fr.neatmonster.nocheatplus.compat.bukkit;

import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;

public class MCAccessBukkitModern extends MCAccessBukkit {

    public MCAccessBukkitModern() {
        super();
    }
    @Override
    public String getMCVersion() {
        return "1.13|?";
    }

    @Override
    public BlockCache getBlockCache() {
        return new BlockCacheBukkitModern(null);
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {

        // TODO: Initialize some blocks and add to this.processedBlocks.

        super.setupBlockProperties(worldConfigProvider);
    }



}
