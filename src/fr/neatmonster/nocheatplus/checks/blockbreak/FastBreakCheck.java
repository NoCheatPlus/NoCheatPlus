package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.SimpleLocation;

/**
 * A check used to verify if the player isn't placing his blocks too quickly
 * 
 */
public class FastBreakCheck extends BlockBreakCheck {

    public class FastBreakCheckEvent extends BlockBreakEvent {

        public FastBreakCheckEvent(final FastBreakCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public FastBreakCheck() {
        super("fastbreak");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockBreakConfig cc = getConfig(player);
        final BlockBreakData data = getData(player);

        // Get the minimum break time for the player's game mode
        int breakTime = cc.fastBreakIntervalSurvival;
        if (player.getBukkitPlayer().getGameMode() == GameMode.CREATIVE)
            breakTime = cc.fastBreakIntervalCreative;

        // Elapsed time since the previous block was broken
        final long elapsedTime = System.currentTimeMillis() - data.lastBreakTime;

        boolean cancel = false;

        // Has the player broken the blocks too quickly
        if (data.lastBreakTime != 0 && elapsedTime < breakTime) {
            if (!NoCheatPlus.skipCheck()) {
                if (data.previousRefused) {
                    // He failed, increase vl and statistics
                    data.fastBreakVL += breakTime - elapsedTime;
                    incrementStatistics(player, Id.BB_FASTBREAK, breakTime - elapsedTime);
                    // Execute whatever actions are associated with this check and the
                    // violation level and find out if we should cancel the event
                    cancel = executeActions(player, cc.fastBreakActions, data.fastBreakVL);
                }
                data.previousRefused = true;
            }
        } else {
            // Reward with lowering of the violation level
            data.fastBreakVL *= 0.90D;
            data.previousRefused = false;
        }

        data.lastBreakTime = System.currentTimeMillis();

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final FastBreakCheckEvent event = new FastBreakCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).fastBreakVL));
        else if (wildcard == ParameterName.BLOCK_TYPE) {
            final SimpleLocation location = getData(player).lastDamagedBlock;
            if (location.isSet())
                return new Location(player.getWorld(), location.x, location.y, location.z).getBlock().getType().name()
                        .toLowerCase().replace("_", " ");
            else
                return "UNKNOWN";
        } else
            return super.getParameter(wildcard, player);
    }
}
