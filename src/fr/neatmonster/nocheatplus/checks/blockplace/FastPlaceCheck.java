package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * A check used to verify if the player isn't breaking his blocks too quickly
 * 
 */
public class FastPlaceCheck extends BlockPlaceCheck {

    public class FastPlaceCheckEvent extends BlockPlaceEvent {

        public FastPlaceCheckEvent(final FastPlaceCheck check, final NCPPlayer player, final ActionList actions,
                final double vL) {
            super(check, player, actions, vL);
        }
    }

    public FastPlaceCheck() {
        super("fastplace");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final BlockPlaceConfig cc = getConfig(player);
        final BlockPlaceData data = getData(player);

        boolean cancel = false;

        // Has the player placed blocks too quickly
        if (data.lastPlaceTime != 0 && System.currentTimeMillis() - data.lastPlaceTime < cc.fastPlaceInterval) {
            if (!NoCheatPlus.skipCheck()) {
                if (data.previousRefused) {
                    // He failed, increase vl and statistics
                    data.fastPlaceVL += cc.fastPlaceInterval - System.currentTimeMillis() + data.lastPlaceTime;
                    incrementStatistics(player, Id.BP_FASTPLACE, cc.fastPlaceInterval - System.currentTimeMillis()
                            + data.lastPlaceTime);

                    // Execute whatever actions are associated with this check and the
                    // violation level and find out if we should cancel the event
                    cancel = executeActions(player, cc.fastPlaceActions, data.fastPlaceVL);
                }
                data.previousRefused = true;
            }
        } else {
            // Reward with lowering of the violation level
            data.fastPlaceVL *= 0.90D;
            data.previousRefused = false;
        }

        data.lastPlaceTime = System.currentTimeMillis();

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final FastPlaceCheckEvent event = new FastPlaceCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).fastPlaceVL));
        else
            return super.getParameter(wildcard, player);
    }
}
