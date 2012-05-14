package fr.neatmonster.nocheatplus.checks.moving;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * This check is used to verify that players aren't walking on water
 * 
 */
public class WaterWalkCheck extends MovingCheck {

    public class WaterWalkCheckEvent extends MovingEvent {

        public WaterWalkCheckEvent(final WaterWalkCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public WaterWalkCheck() {
        super("waterwalk");
    }

    public PreciseLocation check(final NCPPlayer player, final Object... args) {
        // Get the configuration and data of the player
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        // Check if the player comes from a liquid
        final PreciseLocation from = data.from;
        final int fromType = CheckUtils.evaluateLocation(player.getWorld(), from);
        final boolean fromLiquid = CheckUtils.isLiquid(fromType);

        // If he is not, return
        if (!fromLiquid)
            return null;

        // Check if the player is going into a liquid
        final PreciseLocation to = data.to;
        final int toType = CheckUtils.evaluateLocation(player.getWorld(), to);
        final boolean toLiquid = CheckUtils.isLiquid(toType);

        boolean upLiquid = false;
        // If he is, check if the block containing his head is liquid too
        if (toLiquid) {
            final PreciseLocation upFrom = new PreciseLocation();
            upFrom.set(from);
            upFrom.y++;
            final int upFromType = CheckUtils.evaluateLocation(player.getWorld(), upFrom);
            final PreciseLocation upTo = new PreciseLocation();
            upTo.set(to);
            upTo.y++;
            final int upToType = CheckUtils.evaluateLocation(player.getWorld(), upTo);
            upLiquid = CheckUtils.isLiquid(upFromType) || CheckUtils.isLiquid(upToType);
        }

        // Here is the interesting part, we get the bounding box of the player
        final EntityPlayer entity = ((CraftPlayer) player.getBukkitPlayer()).getHandle();
        final AxisAlignedBB aabb = entity.boundingBox.clone();
        // Grow it of the minimum value (to collide with blocks)
        aabb.grow(Double.MIN_VALUE, Double.MIN_VALUE, Double.MIN_VALUE);
        // Now check if the player is forced to walk horizontally
        // If he has a block above his head or if he his walking on stairs
        final boolean isForced = isSpecial(player.getWorld(), aabb.a, aabb.e, aabb.c, true)
                || isSpecial(player.getWorld(), aabb.d, aabb.e, aabb.c, true)
                || isSpecial(player.getWorld(), aabb.a, aabb.e, aabb.f, true)
                || isSpecial(player.getWorld(), aabb.d, aabb.e, aabb.f, true)
                || isSpecial(player.getWorld(), aabb.a, aabb.b, aabb.c, false)
                || isSpecial(player.getWorld(), aabb.d, aabb.b, aabb.c, false)
                || isSpecial(player.getWorld(), aabb.a, aabb.b, aabb.f, false)
                || isSpecial(player.getWorld(), aabb.d, aabb.b, aabb.f, false);

        // Calculate the delta with the surface
        final double dSurface = Math.abs(to.y - Math.ceil(to.y)) + (to.y - Math.ceil(to.y) == 0 ? 1 : 0);
        // Calculate the velocity of the player
        final double velocity = player.getBukkitPlayer().getVelocity().lengthSquared();

        // If the player his walking straight (and not force to do so)
        if (fromLiquid && toLiquid && !upLiquid && !isForced && to.y == from.y && dSurface < 0.8D) {
            // Increment the violation counter
            data.waterWalkVL += 100D * dSurface;
            // Increment the statistics
            incrementStatistics(player, Id.MOV_WATERWALK, 100D * dSurface);
            // Execute the actions
            if (executeActions(player, cc.actions, data.waterWalkVL))
                // Cancel the move if required
                return from;
        }

        // If the player is trying to jump above the water
        else if (fromLiquid && !toLiquid && !isForced && velocity > 0.09D) {
            // Increment the violation counter
            data.waterWalkVL += 100D * (velocity - 0.09D);
            // Increment the statistics
            incrementStatistics(player, Id.MOV_WATERWALK, 100D * (velocity - 0.09D));
            // Execute the actions
            if (executeActions(player, cc.actions, data.waterWalkVL))
                // Cancel the move if required
                return from;
        }

        // Slowly reduce the level with each event to reward the player
        data.waterWalkVL *= 0.95;

        return null;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final WaterWalkCheckEvent event = new WaterWalkCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).waterWalkVL));
        else
            return super.getParameter(wildcard, player);
    }

    /**
     * Checks if a block special (if above is true, it checks if the block's type isn't air, otherwise it checks if the
     * block is stairs/fence or not)
     * 
     * @param world
     * @param x
     * @param y
     * @param z
     * @param above
     * @return is the block special?
     */
    private boolean isSpecial(final World world, final double x, final double y, final double z, final boolean above) {
        Material material = new Location(world, x, y, z).getBlock().getType();
        if (above)
            return material != Material.AIR;
        else {
            if (material == Material.BRICK_STAIRS || material == Material.COBBLESTONE_STAIRS
                    || material == Material.NETHER_BRICK_STAIRS || material == Material.SMOOTH_STAIRS
                    || material == Material.STEP || material == Material.WOOD_STAIRS)
                return true;
            material = new Location(world, x, y - 1, z).getBlock().getType();
            if (material == Material.FENCE || material == Material.IRON_FENCE || material == Material.NETHER_FENCE)
                return true;
            return false;
        }
    }
}
