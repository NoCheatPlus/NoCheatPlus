package me.neatmonster.nocheatplus.checks.blockplace;

import java.util.Locale;

import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.actions.ParameterName;
import me.neatmonster.nocheatplus.data.Statistics.Id;

public class FastPlaceCheck extends BlockPlaceCheck {

    public FastPlaceCheck(final NoCheatPlus plugin) {
        super(plugin, "blockplace.fastplace");
    }

    public boolean check(final NoCheatPlusPlayer player, final BlockPlaceData data, final BlockPlaceConfig cc) {

        boolean cancel = false;

        // Has the player placed blocks too quickly
        if (data.lastPlaceTime != 0 && System.currentTimeMillis() - data.lastPlaceTime < cc.fastPlaceInterval) {
            // He failed, increase vl and statistics
            data.fastPlaceVL += cc.fastPlaceInterval - System.currentTimeMillis() + data.lastPlaceTime;
            incrementStatistics(player, Id.BP_FASTPLACE, cc.fastPlaceInterval - System.currentTimeMillis()
                    + data.lastPlaceTime);

            // Execute whatever actions are associated with this check and the
            // violation level and find out if we should cancel the event
            cancel = executeActions(player, cc.fastPlaceActions, data.fastPlaceVL);
        } else
            // Reward with lowering of the violation level
            data.fastPlaceVL *= 0.90D;

        data.lastPlaceTime = System.currentTimeMillis();

        return cancel;

    }

    @Override
    public String getParameter(final ParameterName wildcard, final NoCheatPlusPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player).fastPlaceVL);
        else
            return super.getParameter(wildcard, player);
    }
}
