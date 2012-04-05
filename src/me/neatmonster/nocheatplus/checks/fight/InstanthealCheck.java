package me.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.config.Permissions;
import me.neatmonster.nocheatplus.data.Statistics;

/**
 * The instantheal Check should find out if a player tried to artificially
 * accellerate the health regeneration by food
 * 
 */
public class InstanthealCheck extends FightCheck {

    public InstanthealCheck(final NoCheatPlus plugin) {
        super(plugin, "fight.instantheal", Permissions.FIGHT_INSTANTHEAL);
    }

    @Override
    public boolean check(final NoCheatPlusPlayer player, final FightData data, final FightConfig cc) {

        boolean cancelled = false;

        final long time = System.currentTimeMillis();

        // security check if system time ran backwards
        if (data.instanthealLastRegenTime > time) {
            data.instanthealLastRegenTime = 0;
            return false;
        }

        final long difference = time - (data.instanthealLastRegenTime + 3500L);

        data.instanthealBuffer += difference;

        if (data.instanthealBuffer < 0) {
            // Buffer has been fully consumed
            // Increase vl and statistics
            final double vl = data.instanthealVL -= data.instanthealBuffer / 1000;
            incrementStatistics(player, Statistics.Id.FI_INSTANTHEAL, vl);

            data.instanthealBuffer = 0;

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancelled = executeActions(player, cc.instanthealActions, data.instanthealVL);
        } else
            // vl gets decreased
            data.instanthealVL *= 0.9;

        // max 2 seconds buffer
        if (data.instanthealBuffer > 2000L)
            data.instanthealBuffer = 2000L;

        if (!cancelled)
            // New reference time
            data.instanthealLastRegenTime = time;

        return cancelled;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).instanthealVL);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.instanthealCheck;
    }
}
