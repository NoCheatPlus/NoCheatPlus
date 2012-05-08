package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;

/**
 * The speed check will find out if a player interacts with something that's
 * too far away
 * 
 */
public class SpeedCheck extends FightCheck {

    public class SpeedCheckEvent extends FightEvent {

        public SpeedCheckEvent(final SpeedCheck check, final NCPPlayer player, final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

    public SpeedCheck() {
        super("speed", Permissions.FIGHT_SPEED);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        final long time = System.currentTimeMillis();

        // Check if one second has passed and reset counters and vl in that case
        if (data.speedTime + 1000L <= time) {
            data.speedTime = time;
            data.speedAttackCount = 0;
            data.speedVL = 0;
        }

        // count the attack
        data.speedAttackCount++;

        // too many attacks
        if (data.speedAttackCount > cc.speedAttackLimit) {
            // if there was lag, don't count it towards statistics and vl
            if (!NoCheatPlus.skipCheck()) {
                data.speedVL += 1;
                incrementStatistics(player, Id.FI_SPEED, 1);
            }

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.speedActions, data.speedVL);
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final SpeedCheckEvent event = new SpeedCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.valueOf(Math.round(getData(player).speedVL));
        else if (wildcard == ParameterName.LIMIT)
            return String.valueOf(Math.round(getConfig(player).speedAttackLimit));
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.speedCheck;
    }
}
