package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.ParameterName;
import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.data.Statistics;

public class GodmodeCheck extends FightCheck {

    public GodmodeCheck(NoCheat plugin) {
        super(plugin, "fight.godmode", Permissions.FIGHT_GODMODE);
    }

    @Override
    public boolean check(NoCheatPlayer player, FightData data, FightConfig cc) {

        boolean cancelled = false;

        long time = System.currentTimeMillis();
        // Check at most once a second
        if(data.godmodeLastDamageTime + 1000 < time) {
            data.godmodeLastDamageTime = time;

            // How old is the player now?
            int age = player.getTicksLived();
            // How much older did he get?
            int ageDiff = Math.max(0, age - data.godmodeLastAge);
            // Is he invulnerable?
            int nodamageTicks = player.getPlayer().getNoDamageTicks();

            if(nodamageTicks > 0 && ageDiff < 15) {
                // He is invulnerable and didn't age fast enough, that costs some points
                data.godmodeBuffer -= (15 - ageDiff);

                // Still points left?
                if(data.godmodeBuffer <= 0) {
                    // No
                    data.godmodeVL -= data.godmodeBuffer;
                    incrementStatistics(player, Statistics.Id.FI_GODMODE, -data.godmodeBuffer);
                    cancelled = executeActions(player, cc.godmodeActions.getActions(data.godmodeVL));
                }
            } else {
                // Give some new points, once a second
                data.godmodeBuffer += 15;
                data.godmodeVL *= 0.95;
            }

            if(data.godmodeBuffer < 0) {
                data.godmodeBuffer = 0;
            } else if(data.godmodeBuffer > 40) {
                data.godmodeBuffer = 40;
            }

            // Start age counting from a new time
            data.godmodeLastAge = age;
        }

        return cancelled;
    }

    @Override
    public boolean isEnabled(FightConfig cc) {
        return cc.godmodeCheck;
    }

    @Override
    public String getParameter(ParameterName wildcard, NoCheatPlayer player) {

        if(wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", (int) getData(player.getDataStore()).godmodeVL);
        else
            return super.getParameter(wildcard, player);
    }
}
