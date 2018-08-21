package fr.neatmonster.nocheatplus.compat.bukkit;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.init.BlockInit;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitShapeModel;
import fr.neatmonster.nocheatplus.compat.bukkit.model.BukkitSlab;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import fr.neatmonster.nocheatplus.utilities.map.BlockFlags;
import fr.neatmonster.nocheatplus.utilities.map.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.map.MaterialUtil;

public class MCAccessBukkitModern extends MCAccessBukkit {

    protected final Map<Material, BukkitShapeModel> shapeModels = new HashMap<Material, BukkitShapeModel>();

    private static final BukkitShapeModel MODEL_SLAB = new BukkitSlab();
    
    public MCAccessBukkitModern() {
        super();
        BlockInit.assertMaterialExists("OAK_LOG");
        BlockInit.assertMaterialExists("VOID_AIR");
    }

    @Override
    public String getMCVersion() {
        return "1.13|?";
    }

    @Override
    public BlockCache getBlockCache() {
        return new BlockCacheBukkitModern(shapeModels);
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {

        // Pre-process for flags.
        // TODO: Also consider removing flags (passable_x4 etc).
        for (final Material mat : Material.values()) {
            if (MaterialUtil.SLABS.contains(mat)) {
                BlockFlags.addFlags(mat, BlockProperties.F_MODEL_SLAB);
            }
        }

        // Sort to processed by flags.
        for (final Material mat : Material.values()) {
            final long flags = BlockProperties.getBlockFlags(mat);
            // Step.
            if (BlockFlags.hasAnyFlag(flags, BlockProperties.F_MODEL_SLAB)) {
                processedBlocks.add(mat);
                shapeModels.put(mat, MODEL_SLAB);
            }
            // Stairs.
            // Fences. // TODO: May need specialized models for edge cases?
            // Thin fences.
            // ... (heads, chests, static, shulker box ...)
        }

        super.setupBlockProperties(worldConfigProvider);
    }



}
