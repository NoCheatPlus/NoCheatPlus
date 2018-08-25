package fr.neatmonster.nocheatplus.compat.bukkit.model;

import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;

public class BukkitDirectionalCentered extends AbstractBukkitCentered {

    public BukkitDirectionalCentered(double inset, double length,
            boolean invertFace) {
        super(inset, length, invertFace);
    }

    @Override
    protected BlockFace getFacing(final BlockData blockData) {
        if (blockData instanceof Directional) {
            return ((Directional) blockData).getFacing();
        }
        else {
            return BlockFace.SELF;
        }
    }

}
