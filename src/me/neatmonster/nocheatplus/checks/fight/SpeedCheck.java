package me.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.Statistics.Id;

/**
 * The speed check will find out if a player interacts with something that's
 * too far away
 * 
 */
public class SpeedCheck extends FightCheck {

    public SpeedCheck(final NoCheatPlus plugin) {
        super(plugin, "fight.speed", Permissions.FIGHT_SPEED);
    }

    @Override
    public boolean check(final NoCheatPlusPlayer player, final FightData data, final FightConfig cc) {

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
            if (!plugin.skipCheck()) {
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
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", getData(player).speedVL);
        else if (wildcard == ParameterName.LIMIT)
            return String.format(Locale.US, "%d", getConfig(player.getConfigurationStore()).speedAttackLimit);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.speedCheck;
    }
}
