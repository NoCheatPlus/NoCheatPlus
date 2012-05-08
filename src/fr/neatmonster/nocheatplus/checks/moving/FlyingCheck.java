package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

/**
 * A check designed for people that are allowed to fly. The complement to
 * the "RunningCheck", which is for people that aren't allowed to fly, and
 * therefore have tighter rules to obey.
 * 
 */
public class FlyingCheck extends MovingCheck {

    public class FlyingCheckEvent extends MovingEvent {

        public FlyingCheckEvent(final FlyingCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    // Determined by trial and error, the flying movement speed of the creative
    // mode
    private static final double creativeSpeed = 0.60D;

    public FlyingCheck() {
        super("flying");
    }

    public PreciseLocation check(final NCPPlayer player, final Object... args) {
        final MovingConfig cc = getConfig(player);
        final MovingData data = getData(player);

        // The setBack is the location that players may get teleported to when
        // they fail the check
        final PreciseLocation setBack = data.runflySetBackPoint;

        final PreciseLocation from = data.from;
        final PreciseLocation to = data.to;

        // If we have no setback, define one now
        if (!setBack.isSet())
            setBack.set(from);

        // Used to store the location where the player gets teleported to
        PreciseLocation newToLocation = null;

        // Before doing anything, do a basic height check to determine if
        // players are flying too high
        final int maxheight = cc.flyingHeightLimit + player.getWorld().getMaxHeight();

        if (to.y - data.vertFreedom > maxheight) {
            newToLocation = new PreciseLocation();
            newToLocation.set(setBack);
            newToLocation.y = maxheight - 10;
            return newToLocation;
        }

        // Calculate some distances
        final double yDistance = to.y - from.y;
        final double xDistance = to.x - from.x;
        final double zDistance = to.z - from.z;

        // How far did the player move horizontally
        final double horizontalDistance = Math.sqrt(xDistance * xDistance + zDistance * zDistance);

        double resultHoriz = 0;
        double resultVert = 0;
        double result = 0;

        // In case of creative game mode give at least 0.60 speed limit horizontal
        double speedLimitHorizontal = player.canFly() ? Math.max(creativeSpeed, cc.flyingSpeedLimitHorizontal)
                : cc.flyingSpeedLimitHorizontal;

        // If the player is affected by potion of swiftness
        speedLimitHorizontal *= player.getSpeedAmplifier();

        // Finally, determine how far the player went beyond the set limits
        resultHoriz = Math.max(0.0D, horizontalDistance - data.horizFreedom - speedLimitHorizontal);

        final boolean sprinting = player.getBukkitPlayer().isSprinting();

        data.bunnyhopdelay--;

        if (resultHoriz > 0 && sprinting)
            // Try to treat it as a the "bunnyhop" problem
            // The bunnyhop problem is that landing and immediatly jumping
            // again leads to a player moving almost twice as far in that step
            if (data.bunnyhopdelay <= 0 && resultHoriz < 0.4D) {
                data.bunnyhopdelay = 9;
                resultHoriz = 0;
            }

        resultHoriz *= 100;

        // Is the player affected by the "jumping" potion
        // This is really just a very, very crude estimation and far from
        // reality
        final double jumpAmplifier = player.getJumpAmplifier();
        if (jumpAmplifier > data.lastJumpAmplifier)
            data.lastJumpAmplifier = jumpAmplifier;

        final double speedLimitVertical = cc.flyingSpeedLimitVertical * data.lastJumpAmplifier;

        if (data.from.y >= data.to.y && data.lastJumpAmplifier > 0)
            data.lastJumpAmplifier--;

        // super simple, just check distance compared to max distance vertical
        resultVert = Math.max(0.0D, yDistance - data.vertFreedom - speedLimitVertical) * 100;

        result = resultHoriz + resultVert;

        // The player went to far, either horizontal or vertical
        if (result > 0) {

            // Increment violation counter and statistics
            data.runflyVL += result;
            if (resultHoriz > 0)
                incrementStatistics(player, Id.MOV_RUNNING, resultHoriz);

            if (resultVert > 0)
                incrementStatistics(player, Id.MOV_FLYING, resultVert);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            final boolean cancel = executeActions(player, cc.flyingActions, data.runflyVL);

            // Was one of the actions a cancel? Then really do it
            if (cancel)
                newToLocation = setBack;
        }

        // Slowly reduce the violation level with each event
        data.runflyVL *= 0.97;

        // If the player did not get cancelled, define a new setback point
        if (newToLocation == null)
            setBack.set(to);

        return newToLocation;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final FlyingCheckEvent event = new FlyingCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).runflyVL));
        else
            return super.getParameter(wildcard, player);
    }
}
