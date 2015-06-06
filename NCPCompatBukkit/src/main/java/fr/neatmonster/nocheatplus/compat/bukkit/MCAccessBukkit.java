package fr.neatmonster.nocheatplus.compat.bukkit;


import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;

import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;

public class MCAccessBukkit extends MCAccessBukkitBase implements BlockPropertiesSetup{

    public MCAccessBukkit() {
        super();
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {
        // Note deprecation suppression: These ids should be unique for a server run, that should be ok for setting up generic properties.
        // TODO: (?) Set some generic properties matching what BlockCache.getShape returns.
        final Set<Material> fullBlocks = new HashSet<Material>();
        for (final Material mat : new Material[]{
                // TODO: Ice !? / Packed ice !?
                Material.GLASS, Material.GLOWSTONE, Material.ICE, Material.LEAVES,
                Material.COMMAND, Material.BEACON,
                Material.PISTON_BASE,
        }) {
            fullBlocks.add(mat);
        }
        for (final Material mat : Material.values()) {
            if (!mat.isBlock()) {
                continue;
            }
            if (fullBlocks.contains(mat)) {
                continue;
            }
            if (!mat.isOccluding() || !mat.isSolid() || mat.isTransparent()) {
                // Uncertain bounding-box, allow passing through.
                long flags = BlockProperties.F_IGN_PASSABLE;
                if ((BlockProperties.isSolid(mat) || BlockProperties.isGround(mat)) && !BlockProperties.isLiquid(mat)) {
                    // Block can be ground, so allow standing on any height.
                    flags |= BlockProperties.F_GROUND_HEIGHT;
                }
                BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
            }
        }
        // Blocks that are reported to be full and solid, but which are not.
        for (final Material mat : new Material[]{
                Material.ENDER_PORTAL_FRAME,
        }) {
            final long flags = BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT;
            BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
        }
    }

}
