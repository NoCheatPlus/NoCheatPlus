package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetData;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * The GodMode check will find out if a player tried to stay invulnerable after being hit or after dying.
 */
public class GodMode extends Check {

    /**
     * Instantiates a new god mode check.
     */
    public GodMode() {
        super(CheckType.FIGHT_GODMODE);
    }

    /**
     * New style god mode check. Much more sensitive.
     * @param player
     * @param damage
     * @return
     */
    public boolean check(final Player player, final double damage, final FightData data){
        final int tick = TickTask.getTick();

        final int noDamageTicks = Math.max(0, player.getNoDamageTicks());
        final int invulnerabilityTicks = mcAccess.getInvulnerableTicks(player);

        // TODO: cleanup this leugique beume...

        boolean legit = false; // Return, reduce vl.
        boolean set = false; // Set tick/ndt and return
        boolean resetAcc = false; // Reset acc counter.
        boolean resetAll = false; // Reset all and return

        // Check difference to expectation:
        final int dTick = tick - data.lastDamageTick;
        final int dNDT = data.lastNoDamageTicks - noDamageTicks;
        final int delta = dTick - dNDT;

        final double health = BridgeHealth.getHealth(player);

        // TODO: Adjust to double values.

        if (data.godModeHealth > health ){
            data.godModeHealthDecreaseTick = tick;
            legit = set = resetAcc = true;
        }

        // TODO: Might account for ndt/2 on regain health (!).

        // Invulnerable or inconsistent.
        // TODO: might check as well if NCP has taken over invulnerable ticks of this player.
        if (invulnerabilityTicks != Integer.MAX_VALUE && invulnerabilityTicks > 0 || tick < data.lastDamageTick){
            // (Second criteria is for MCAccessBukkit.)
            legit = set = resetAcc = true;
        }

        // Reset accumulator.
        if (20 + data.godModeAcc < dTick || dTick > 40){
            legit = resetAcc = true;
            set = true; // TODO
        }

        // Check if reduced more than expected or new/count down fully.
        // TODO: Mostly workarounds.
        if (delta <= 0  || data.lastNoDamageTicks <= player.getMaximumNoDamageTicks() / 2 || dTick > data.lastNoDamageTicks || damage > BridgeHealth.getLastDamage(player)|| damage == 0.0){
            // Not resetting acc.
            legit = set = true;
        }

        if (dTick == 1 && noDamageTicks < 19){
            set = true;
        }

        if (delta == 1){
            // Ignore these, but keep reference value from before.
            legit = true;
        }

        //    	Bukkit.getServer().broadcastMessage("God " + player.getName() + " delta=" + delta + " dt=" + dTick + " dndt=" + dNDT + " acc=" + data.godModeAcc + " d=" + damage + " ndt=" + noDamageTicks + " h=" + health + " slag=" + TickTask.getLag(dTick, true));

        // TODO: might check last damage taken as well (really taken with health change)

        // Resetting
        data.godModeHealth = health;

        if (resetAcc || resetAll){
            data.godModeAcc = 0;
        }
        if (legit){
            data.godModeVL *= 0.97;
        }
        if (resetAll){
            // Reset all.
            data.lastNoDamageTicks = 0;
            data.lastDamageTick = 0;
            return false;
        }
        else if (set){
            // Only set the tick values.
            data.lastNoDamageTicks = noDamageTicks;
            data.lastDamageTick = tick;
            return false;
        }
        else if (legit){
            // Just return;
            return false;
        }

        if (tick < data.godModeHealthDecreaseTick){
            data.godModeHealthDecreaseTick = 0;
        }
        else{
            final int dht = tick - data.godModeHealthDecreaseTick;
            if (dht <= 20) {
                return false; 
            }
        }

        final FightConfig cc = FightConfig.getConfig(player); 

        // Check for client side lag.
        final long now = System.currentTimeMillis();
        final long maxAge = cc.godModeLagMaxAge;
        long keepAlive = Long.MIN_VALUE;
        if (NCPAPIProvider.getNoCheatPlusAPI().hasFeatureTag("checks", "KeepAliveFrequency")) {
            keepAlive = ((NetData) (CheckType.NET_KEEPALIVEFREQUENCY.getDataFactory().getData(player))).lastKeepAliveTime;
        }
        keepAlive = Math.max(keepAlive, CheckUtils.guessKeepAliveTime(player, now, maxAge));

        if (keepAlive != Double.MIN_VALUE && now - keepAlive > cc.godModeLagMinAge && now - keepAlive < maxAge){
            // Assume lag.
            return false;
        }

        // Violation probably.
        data.godModeAcc += delta;

        boolean cancel = false;
        // TODO: bounds
        if (data.godModeAcc > 2){
            // TODO: To match with old checks vls / actions, either change actions or apply a factor.
            data.godModeVL += delta;
            if (executeActions(player, data.godModeVL, delta, FightConfig.getConfig(player).godModeActions).willCancel()){
                cancel = true;
            }
            else {
                cancel = false;
            }
        }
        else{
            cancel = false;
        }

        // Set tick values.
        data.lastNoDamageTicks = noDamageTicks;
        data.lastDamageTick = tick;

        return cancel;
    }

    /**
     * If a player apparently died, make sure they really die after some time if they didn't already, by setting up a
     * Bukkit task.
     * 
     * @param player
     *            the player
     */
    public void death(final Player player) {
        // TODO: Is this still relevant ?
        // First check if the player is really dead (e.g. another plugin could have just fired an artificial event).
        if (BridgeHealth.getHealth(player) <= 0.0 && player.isDead()) {
            try {
                // Schedule a task to be executed in roughly 1.5 seconds.
                // TODO: Get plugin otherwise !?
                Bukkit.getScheduler().scheduleSyncDelayedTask(Bukkit.getPluginManager().getPlugin("NoCheatPlus"), new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Check again if the player should be dead, and if the game didn't mark them as dead.
                            if (mcAccess.shouldBeZombie(player)){
                                // Artificially "kill" them.
                                mcAccess.setDead(player, 19);
                            }
                        } catch (final Exception e) {}
                    }
                }, 30);
            } catch (final Exception e) {}
        }
    }
}
