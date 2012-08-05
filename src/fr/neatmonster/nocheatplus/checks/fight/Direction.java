package fr.neatmonster.nocheatplus.checks.fight;

import net.minecraft.server.Entity;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/*
 * M""""""'YMM oo                              dP   oo                   
 * M  mmmm. `M                                 88                        
 * M  MMMMM  M dP 88d888b. .d8888b. .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMM  M 88 88'  `88 88ooood8 88'  `""   88   88 88'  `88 88'  `88 
 * M  MMMM' .M 88 88       88.  ... 88.  ...   88   88 88.  .88 88    88 
 * M       .MM dP dP       `88888P' `88888P'   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                                           
 */
/**
 * The Direction check will find out if a player tried to interact with something that's not in his field of view.
 */
public class Direction extends Check {

    /**
     * The event triggered by this check.
     */
    public class DirectionEvent extends CheckEvent {

        /**
         * Instantiates a new direction event.
         * 
         * @param player
         *            the player
         */
        public DirectionEvent(final Player player) {
            super(player);
        }
    }

    private final double OFFSET = 0.5D;

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param damaged
     *            the damaged
     * @return true, if successful
     */
    public boolean check(final Player player, final Entity damaged) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        final Location minimum = new Location(player.getWorld(), damaged.boundingBox.a, damaged.boundingBox.b,
                damaged.boundingBox.c);
        final Location maximum = new Location(player.getWorld(), damaged.boundingBox.d, damaged.boundingBox.e,
                damaged.boundingBox.f);
        if (!CheckUtils.intersects(player, minimum, maximum, OFFSET)) {
            // Player failed the check. Let's try to guess how far he was from looking directly to the entity...
            final Vector direction = player.getEyeLocation().getDirection();
            final Vector blockEyes = minimum.add(maximum).multiply(0.5D).subtract(player.getEyeLocation()).toVector();
            final double distance = blockEyes.crossProduct(direction).length() / direction.length();

            // Add the overall violation level of the check.
            data.directionVL += distance;

            // Dispatch a direction event (API)
            final DirectionEvent e = new DirectionEvent(player);
            Bukkit.getPluginManager().callEvent(e);

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            cancel = !e.isCancelled() && executeActions(player, cc.directionActions, data.directionVL);

            if (cancel)
                // If we should cancel, remember the current time too.
                data.directionLastViolationTime = System.currentTimeMillis();
        } else
            // Reward the player by lowering his violation level.
            data.directionVL *= 0.8D;

        // If the player is still in penalty time, cancel the event anyway.
        if (data.directionLastViolationTime + cc.directionPenalty > System.currentTimeMillis()) {
            // A safeguard to avoid people getting stuck in penalty time indefinitely in case the system time of the
            // server gets changed.
            if (data.directionLastViolationTime > System.currentTimeMillis())
                data.directionLastViolationTime = 0;

            // He is in penalty time, therefore request cancelling of the event.
            return true;
        }

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(FightData.getData(player).directionVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.FIGHT_DIRECTION) && FightConfig.getConfig(player).directionCheck;
    }
}
