package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * MM'""""'YMM                              dP   oo                   MM""""""""`M dP          
 * M' .mmm. `M                              88                        MM  mmmmmmmM 88          
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. d8888P dP dP   .dP .d8888b. M'      MMMM 88 dP    dP 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `88   88   88 88   d8' 88ooood8 MM  MMMMMMMM 88 88    88 
 * M. `MMM' .M 88       88.  ... 88.  .88   88   88 88 .88'  88.  ... MM  MMMMMMMM 88 88.  .88 
 * MM.     .dM dP       `88888P' `88888P8   dP   dP 8888P'   `88888P' MM  MMMMMMMM dP `8888P88 
 * MMMMMMMMMMM                                                        MMMMMMMMMMMM         .88 
 *                                                                                     d8888P  
 */
/**
 * A check designed for people that are allowed to fly. The complement to the "SurvivalFly", which is for people that
 * aren't allowed to fly, and therefore have tighter rules to obey.
 */
public class CreativeFly extends Check {

    /** The horizontal speed in creative mode. */
    private static final double HORIZONTAL_SPEED = 0.6D;

    /** The vertical speed in creative mode. */
    private static final double VERTICAL_SPEED   = 1D;

    /**
     * Instantiates a new creative fly check.
     */
    public CreativeFly() {
        super(CheckType.MOVING_CREATIVEFLY);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     * @return the location
     */
    public Location check(final Player player, final PlayerLocation from, final PlayerLocation to) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        final MovingData data = MovingData.getData(player);

        // If we have no setback, define one now.
        if (data.setBack == null)
            data.setBack = from.getLocation();

        // Before doing anything, do a basic height check to determine if players are flying too high.
        final int maximumHeight = cc.creativeFlyMaxHeight + player.getWorld().getMaxHeight();
        if (to.getY() - data.verticalFreedom > maximumHeight)
            return new Location(player.getWorld(), data.setBack.getX(), maximumHeight - 10D, data.setBack.getZ(),
                    to.getYaw(), to.getPitch());

        // Calculate some distances.
        final double xDistance = to.getX() - from.getX();
        final double yDistance = to.getY() - from.getY();
        final double zDistance = to.getZ() - from.getZ();

        // How far did the player move horizontally?
        final double hDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        // If the player is affected by potion of swiftness.
        final EntityPlayer entity = ((CraftPlayer) player).getHandle();
        final double limitH = cc.creativeFlyHorizontalSpeed
                / 100D
                * HORIZONTAL_SPEED
                * (entity.hasEffect(MobEffectList.FASTER_MOVEMENT) ? 1D + 0.2D * (entity.getEffect(
                        MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1D) : 1D);

        // Finally, determine how far the player went beyond the set limits.
        double resultH = Math.max(0.0D, hDistance - data.horizontalFreedom - limitH);

        final boolean sprinting = player.isSprinting() && player.getFoodLevel() > 5;

        data.bunnyhopDelay--;

        if (resultH > 0 && sprinting)
            // Try to treat it as a the "bunnyhop" problem. The bunnyhop problem is that landing and immediately jumping
            // again leads to a player moving almost twice as far in that step.
            if (data.bunnyhopDelay <= 0 && resultH < 0.4D) {
                data.bunnyhopDelay = 9;
                resultH = 0D;
            }

        resultH *= 100D;

        // Is the player affected by the "jumping" potion. This is really just a very, very crude estimation and far
        // from reality.
        double jumpAmplifier = 1D;
        if (entity.hasEffect(MobEffectList.JUMP)) {
            final int amplifier = entity.getEffect(MobEffectList.JUMP).getAmplifier();
            if (amplifier > 20)
                jumpAmplifier = 1.5D * (entity.getEffect(MobEffectList.JUMP).getAmplifier() + 1D);
            else
                jumpAmplifier = 1.2D * (entity.getEffect(MobEffectList.JUMP).getAmplifier() + 1D);
        }
        if (jumpAmplifier > data.jumpAmplifier)
            data.jumpAmplifier = jumpAmplifier;

        final double limitV = cc.creativeFlyVerticalSpeed / 100D * VERTICAL_SPEED * data.jumpAmplifier;

        if (from.getY() >= to.getY() && data.jumpAmplifier > 0D)
            data.jumpAmplifier--;

        // Super simple, just check distance compared to max distance vertical.
        final double resultV = Math.max(0D, yDistance - data.verticalFreedom - limitV) * 100D;

        final double result = resultH + resultV;

        // The player went to far, either horizontal or vertical.
        if (result > 0D) {
            if (data.creativeFlyPreviousRefused) {
                // Increment violation level.
                data.creativeFlyVL += result;

                // Execute whatever actions are associated with this check and the violation level and find out if we
                // should cancel the event.
                if (executeActions(player, data.creativeFlyVL, result, cc.creativeFlyActions))
                    // Compose a new location based on coordinates of "newTo" and viewing direction of "event.getTo()"
                    // to allow the player to look somewhere else despite getting pulled back by NoCheatPlus.
                    return new Location(player.getWorld(), data.setBack.getX(), data.setBack.getY(),
                            data.setBack.getZ(), to.getYaw(), to.getPitch());
            } else
                data.creativeFlyPreviousRefused = true;
        } else
            data.creativeFlyPreviousRefused = false;

        // Slowly reduce the violation level with each event.
        data.creativeFlyVL *= 0.97D;

        // If the event did not get cancelled, define a new setback point.
        data.setBack = to.getLocation();
        return null;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        final MovingData data = MovingData.getData(violationData.player);
        if (wildcard == ParameterName.LOCATION_FROM)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.from.getX(), data.from.getY(), data.from.getZ());
        else if (wildcard == ParameterName.LOCATION_TO)
            return String.format(Locale.US, "%.2f, %.2f, %.2f", data.to.getX(), data.to.getY(), data.to.getZ());
        else if (wildcard == ParameterName.DISTANCE)
            return String.format(Locale.US, "%.2f", data.to.getLocation().subtract(data.from.getLocation())
                    .lengthSquared());
        else
            return super.getParameter(wildcard, violationData);
    }
}
