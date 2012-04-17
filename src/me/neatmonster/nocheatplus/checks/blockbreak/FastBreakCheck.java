package me.neatmonster.nocheatplus.checks.blockbreak;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

import org.bukkit.GameMode;

/**
 * A check used to verify if the player isn't placing his blocks too quickly
 * 
 */
public class FastBreakCheck extends BlockBreakCheck {

    public FastBreakCheck(final NoCheatPlus plugin) {
        super(plugin, "blockbreak.fastbreak");
    }

    public boolean check(final NoCheatPlusPlayer player, final BlockBreakData data, final BlockBreakConfig cc) {

        // Get the minimum break time for the player's game mode
        int breakTime = cc.fastBreakIntervalSurvival;
        if (player.getPlayer().getGameMode() == GameMode.CREATIVE)
            breakTime = cc.fastBreakIntervalCreative;

        // Elapsed time since the previous block was broken
        final long elapsedTime = System.currentTimeMillis() - data.lastBreakTime;

        boolean cancel = false;

        // Has the player broken the blocks too quickly
        if (data.lastBreakTime != 0 && elapsedTime < breakTime) {
            if (data.previousRefused) {
                // He failed, increase vl and statistics
                data.fastBreakVL += breakTime - elapsedTime;
                incrementStatistics(player, Id.BB_FASTBREAK, breakTime - elapsedTime);
                // Execute whatever actions are associated with this check and the
                // violation level and find out if we should cancel the event
                cancel = executeActions(player, cc.fastBreakActions, data.fastBreakVL);
            }
            data.previousRefused = true;
        } else {
            // Reward with lowering of the violation level
            data.fastBreakVL *= 0.90D;
            data.previousRefused = false;
        }

        data.lastBreakTime = System.currentTimeMillis();

        return cancel;

    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).fastBreakVL);
        else
            return super.getParameter(wildcard, player);
    }
}
