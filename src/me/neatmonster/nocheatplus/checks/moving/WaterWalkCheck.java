package me.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.checks.CheckUtil;
import me.neatmonster.nocheatplus.data.PreciseLocation;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class WaterWalkCheck extends MovingCheck {

    public WaterWalkCheck(final NoCheatPlus plugin) {
        super(plugin, "moving.waterwalk");
    }

    public PreciseLocation check(final NoCheatPlusPlayer player, final MovingData data, final MovingConfig cc) {
        // Some shortcuts:
        final PreciseLocation to = data.to;
        final PreciseLocation from = data.from;
        final PreciseLocation up = new PreciseLocation();
        up.x = to.x;
        up.y = to.y + 1;
        up.z = to.z;

        // To know if a player "is in water" is useful
        final int fromType = CheckUtil.evaluateLocation(player.getPlayer().getWorld(), from);
        final int toType = CheckUtil.evaluateLocation(player.getPlayer().getWorld(), to);
        final int upType = CheckUtil.evaluateLocation(player.getPlayer().getWorld(), up);

        final boolean fromLiquid = CheckUtil.isLiquid(fromType);
        final boolean toLiquid = CheckUtil.isLiquid(toType);
        final boolean upLiquid = CheckUtil.isLiquid(upType);

        final Block fromBlock = new Location(player.getPlayer().getWorld(), from.x, from.y, from.z).getBlock();

        // Handle the issue with water streams
        boolean waterStreamsFix = false;
        if (fromBlock.getType() == Material.STATIONARY_WATER || fromBlock.getType() == Material.STATIONARY_LAVA
                || (fromBlock.getType() == Material.WATER || fromBlock.getType() == Material.LAVA)
                && fromBlock.getData() == 0x0)
            waterStreamsFix = true;

        // Handle the issue with slabs/stairs
        boolean slabsStairsFix = false;
        for (final BlockFace blockFace : BlockFace.values()) {
            final Material material = fromBlock.getRelative(blockFace).getType();
            if (material == Material.STEP || material == Material.WOOD_STAIRS
                    || material == Material.COBBLESTONE_STAIRS || material == Material.BRICK_STAIRS
                    || material == Material.SMOOTH_STAIRS || material == Material.NETHER_BRICK_STAIRS)
                slabsStairsFix = true;
        }

        // Calculate some distances
        final double deltaX = Math.abs(Math.round(to.x) - to.x);
        final double deltaY = Math.abs(from.y - to.y);
        final double deltaZ = Math.abs(Math.round(to.z) - to.z);
        final double deltaWithSurface = Math.abs(to.y - Math.ceil(to.y)) + (to.y - Math.ceil(to.y) == 0 ? 1 : 0);
        final double resultXZ = (Math.abs(deltaX - 0.30D) + Math.abs(deltaZ - 0.30D)) * 100;
        final double resultY = deltaWithSurface * 100;

        PreciseLocation newToLocation = null;

        // Slowly reduce the level with each event
        data.waterWalkVL *= 0.95;

        if (!slabsStairsFix && fromLiquid && toLiquid && !upLiquid && deltaY == 0D && deltaWithSurface < 0.8D) {
            // If the player is trying to move while being in water
            // Increment violation counter
            data.waterWalkVL += resultY;

            incrementStatistics(player, data.statisticCategory, resultY);

            final boolean cancel = executeActions(player, cc.actions, data.waterWalkVL);

            // Was one of the actions a cancel? Then do it
            if (cancel)
                newToLocation = from;
        } else if (waterStreamsFix && fromLiquid && !toLiquid && (deltaX < 0.28D || deltaX > 0.31D)
                && (deltaZ < 0.28D || deltaZ > 0.31D)) {
            // If the player is trying to jump while being in water
            // Increment violation counter
            data.waterWalkVL += resultXZ;

            incrementStatistics(player, data.statisticCategory, resultXZ);

            final boolean cancel = executeActions(player, cc.actions, data.waterWalkVL);

            // Was one of the actions a cancel? Then do it
            if (cancel)
                newToLocation = from;
        }

        return newToLocation;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).waterWalkVL);
        else
            return super.getParameter(wildcard, player);
    }
}
