package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckEvent;
import fr.neatmonster.nocheatplus.players.Permissions;
import fr.neatmonster.nocheatplus.utilities.LagMeasureTask;

/*
 * MM""""""""`M                     dP   MM"""""""`YM dP                            
 * MM  mmmmmmmM                     88   MM  mmmmm  M 88                            
 * M'      MMMM .d8888b. .d8888b. d8888P M'        .M 88 .d8888b. .d8888b. .d8888b. 
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   MM  MMMMMMMM 88 88'  `88 88'  `"" 88ooood8 
 * MM  MMMMMMMM 88.  .88       88   88   MM  MMMMMMMM 88 88.  .88 88.  ... 88.  ... 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   MM  MMMMMMMM dP `88888P8 `88888P' `88888P' 
 * MMMMMMMMMMMM                          MMMMMMMMMMMM                               
 */
/**
 * A check used to verify if the player isn't placing blocks too quickly.
 */
public class FastPlace extends Check {

    /**
     * The event triggered by this check.
     */
    public class FastPlaceEvent extends CheckEvent {

        /**
         * Instantiates a new fast place event.
         * 
         * @param player
         *            the player
         */
        public FastPlaceEvent(final Player player) {
            super(player);
        }
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param block
     *            the block
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block) {
        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);
        final BlockPlaceData data = BlockPlaceData.getData(player);

        boolean cancel = false;

        // Has the player placed blocks too quickly?
        if (data.fastPlaceLastTime != 0 && System.currentTimeMillis() - data.fastPlaceLastTime < cc.fastPlaceInterval) {
            if (!LagMeasureTask.skipCheck()) {
                if (data.fastPlaceLastRefused) {
                    // He failed, increase his violation level.
                    data.fastPlaceVL += cc.fastPlaceInterval - System.currentTimeMillis() + data.fastPlaceLastTime;

                    // Distance a fast place event (API).
                    final FastPlaceEvent e = new FastPlaceEvent(player);
                    Bukkit.getPluginManager().callEvent(e);

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = !e.isCancelled() && executeActions(player, cc.fastPlaceActions, data.fastPlaceVL);
                }
                data.fastPlaceLastRefused = true;
            }
        } else {
            // Reward him by lowering his violation level.
            data.fastPlaceVL *= 0.9D;

            data.fastPlaceLastRefused = false;
        }

        data.fastPlaceLastTime = System.currentTimeMillis();

        return cancel;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName, org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final Player player) {
        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(BlockPlaceData.getData(player).fastPlaceVL));
        else
            return super.getParameter(wildcard, player);
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#isEnabled(org.bukkit.entity.Player)
     */
    @Override
    protected boolean isEnabled(final Player player) {
        return !player.hasPermission(Permissions.BLOCKPLACE_FASTPLACE)
                && BlockPlaceConfig.getConfig(player).fastPlaceCheck;
    }
}
