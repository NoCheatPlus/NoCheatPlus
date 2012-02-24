package cc.co.evenprime.bukkit.nocheat.checks.fight;

import java.util.Locale;
import net.minecraft.server.EntityPlayer;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
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
        if(data.godmodeLastDamageTime + 1000L < time) {
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
                    cancelled = executeActions(player, cc.godmodeActions, data.godmodeVL);
                }
            } else {
                // Give some new points, once a second
                data.godmodeBuffer += 15;
                data.godmodeVL *= 0.95;
            }

            if(data.godmodeBuffer < 0) {
                data.godmodeBuffer = 0;
            } else if(data.godmodeBuffer > 30) {
                data.godmodeBuffer = 30;
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

    /**
     * If a player apparently died, make sure he really dies after some time
     * if he didn't already.
     *
     * @param player
     */
    public void death(CraftPlayer player) {
        if(player.getHealth() <= 0 && player.isDead()) {
            try {
                final EntityPlayer entity = player.getHandle();

                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {

                    public void run() {
                        try {
                            if(entity.getHealth() <= 0 && !entity.dead) {
                                entity.deathTicks = 19;
                                entity.a(true);
                            }
                        } catch(Exception e) {}
                    }
                }, 30);
            } catch(Exception e) {}
        }
    }
}
