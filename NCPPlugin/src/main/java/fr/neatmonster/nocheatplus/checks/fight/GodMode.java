package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MM'"""""`MM                dP M"""""`'"""`YM                dP          
 * M' .mmm. `M                88 M  mm.  mm.  M                88          
 * M  MMMMMMMM .d8888b. .d888b88 M  MMM  MMM  M .d8888b. .d888b88 .d8888b. 
 * M  MMM   `M 88'  `88 88'  `88 M  MMM  MMM  M 88'  `88 88'  `88 88ooood8 
 * M. `MMM' .M 88.  .88 88.  .88 M  MMM  MMM  M 88.  .88 88.  .88 88.  ... 
 * MM.     .MM `88888P' `88888P8 M  MMM  MMM  M `88888P' `88888P8 `88888P' 
 * MMMMMMMMMMM                   MMMMMMMMMMMMMM                            
 */
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
     * Checks a player.
     * 
     * @param player
     *            the player
     * @return true, if successful
     */
    public boolean check(final Player player) {
        final FightData data = FightData.getData(player);

        boolean cancel = false;

        // Check at most once a second.
        if (data.godModeLastTime < System.currentTimeMillis() - 1000L) {
            data.godModeLastTime = System.currentTimeMillis();

            final int age = player.getTicksLived();

            // How much older did he get?
            final int ageDelta = Math.max(0, age - data.godModeLastAge);

            if (player.getNoDamageTicks() > 0 && ageDelta < 15) {
                // He is invulnerable and didn't age fast enough, that costs some points.
                data.godModeBuffer -= 15 - ageDelta;

                // Still points left?
                if (data.godModeBuffer <= 0) {
                    // No, that means we can increase his violation level.
                    data.godModeVL -= data.godModeBuffer;

                    // Execute whatever actions are associated with this check and the violation level and find out if
                    // we should cancel the event.
                    cancel = executeActions(player, data.godModeVL, -data.godModeBuffer,
                            FightConfig.getConfig(player).godModeActions);
                }
            } else {
                // Give some new points, once a second.
                data.godModeBuffer += 15;

                // Decrease the violation level.
                data.godModeVL *= 0.95;
            }

            if (data.godModeBuffer < 0)
                // Can't have less than 0!
                data.godModeBuffer = 0;
            else if (data.godModeBuffer > 30)
                // And 30 is enough for simple lag situations.
                data.godModeBuffer = 30;

            // Start age counting from a new time.
            data.godModeLastAge = age;
        }

        return cancel;
    }
    
    /**
     * New style god mode check. Much more sensitive.
     * @param player
     * @param damage
     * @return
     */
    public boolean check(final Player player, final int damage){
    	final FightData data = FightData.getData(player);
    	
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
    	
    	final int health = player.getHealth();
    	
    	if (data.godModeHealth > health ){
    		data.godModeHealthDecreaseTick = tick;
    		legit = set = resetAcc = true;
    	}
    	
    	// Invulnerable or inconsistent.
    	// TODO: might check as well if NCP has taken over invulnerable ticks of this player.
    	if (invulnerabilityTicks > 0 && noDamageTicks != invulnerabilityTicks || tick < data.lastDamageTick){
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
    	if (delta <= 0  || data.lastNoDamageTicks == 0 || dTick > data.lastNoDamageTicks || damage > player.getLastDamage()){
    		// Not resetting acc.
    		legit = set = true;
    	}
    	
    	if (noDamageTicks == 10 || dTick == 1 && noDamageTicks < 19){
    		set = true;
    	}

    	if (delta == 1){
    		// Ignore these, but keep reference value from before.
    		legit = true;
    	}
    	
//    	Bukkit.getServer().broadcastMessage("God " + player.getName() + " delta=" + delta + " dt=" + dTick + " dndt=" + dNDT + " acc=" + data.godModeAcc + " d=" + damage + " ndt=" + noDamageTicks + " h=" + health + " slag=" + TickTask.getLag(dTick));
    	
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
    		if (dht <= 20) return false; 
    	}
    	
    	// Check for client side lag.
    	final long now = System.currentTimeMillis();
    	final long maxAge = 5000;  // Allows 5 seconds lag max. TODO: Balance, test.
    	long keepAlive = mcAccess.getKeepAliveTime(player);
    	if (keepAlive > now || keepAlive == Long.MIN_VALUE){
    		keepAlive = CheckUtils.guessKeepAliveTime(player, now, maxAge);
    	}
    	if (keepAlive != Double.MIN_VALUE && now - keepAlive > 1000 && now - keepAlive < maxAge){
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
    		if (executeActions(player, data.godModeVL, delta, FightConfig.getConfig(player).godModeActions)){
    			cancel = true;
    		}
    		else cancel = false;
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
     * If a player apparently died, make sure he really dies after some time if he didn't already, by setting up a
     * Bukkit task.
     * 
     * @param player
     *            the player
     */
    public void death(final Player player) {
    	// TODO: Is this still relevant ?
        // First check if the player is really dead (e.g. another plugin could have just fired an artificial event).
        if (player.getHealth() <= 0 && player.isDead())
            try {
                // Schedule a task to be executed in roughly 1.5 seconds.
                final NoCheatPlus plugin = (NoCheatPlus) Bukkit.getPluginManager().getPlugin("NoCheatPlus");
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // Check again if the player should be dead, and if the game didn't mark him as dead.
                            if (mcAccess.shouldBeZombie(player)){
                                // Artificially "kill" him.
                            	mcAccess.setDead(player, 19);
                            }
                        } catch (final Exception e) {}
                    }
                }, 30);
            } catch (final Exception e) {}
    }
}
