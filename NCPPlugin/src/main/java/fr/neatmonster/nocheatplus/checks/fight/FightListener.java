package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/*
 * MM""""""""`M oo          dP         dP   M""MMMMMMMM oo            dP                                       
 * MM  mmmmmmmM             88         88   M  MMMMMMMM               88                                       
 * M'      MMMM dP .d8888b. 88d888b. d8888P M  MMMMMMMM dP .d8888b. d8888P .d8888b. 88d888b. .d8888b. 88d888b. 
 * MM  MMMMMMMM 88 88'  `88 88'  `88   88   M  MMMMMMMM 88 Y8ooooo.   88   88ooood8 88'  `88 88ooood8 88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88    88   88   M  MMMMMMMM 88       88   88   88.  ... 88    88 88.  ... 88       
 * MM  MMMMMMMM dP `8888P88 dP    dP   dP   M         M dP `88888P'   dP   `88888P' dP    dP `88888P' dP       
 * MMMMMMMMMMMM         .88                 MMMMMMMMMMM                                                        
 *                  d8888P                                                                                     
 */
/**
 * Central location to listen to events that are relevant for the fight checks.
 * 
 * @see FightEvent
 */
public class FightListener extends CheckListener {

    /** The angle check. */
    private final Angle       angle       = addCheck(new Angle());

    /** The critical check. */
    private final Critical    critical    = addCheck(new Critical());

    /** The direction check. */
    private final Direction   direction   = addCheck(new Direction());

    /** The god mode check. */
    private final GodMode     godMode     = addCheck(new GodMode());

    /** The knockback check. */
    private final Knockback   knockback   = addCheck(new Knockback());

    /** The no swing check. */
    private final NoSwing     noSwing     = addCheck(new NoSwing());

    /** The reach check. */
    private final Reach       reach       = addCheck(new Reach());
    
    /** The self hit check */
    private final SelfHit     selfHit     = addCheck(new SelfHit());

    /** The speed check. */
    private final Speed       speed       = new Speed();
    
    public FightListener(){
    	super(CheckType.FIGHT);
    }

    /**
     * A player attacked something with DamageCause ENTITY_ATTACK. That's most likely what we want to really check.
     * 
     * @param event
     *            The EntityDamageByEntityEvent
     * @return 
     */
    private boolean handleNormalDamage(final Player player, final Entity damaged) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);
        
        // Hotfix attempt for enchanted books.
        // TODO: maybe a generaluzed version for the future...
        final ItemStack stack = player.getItemInHand();
        // Illegal enchantments hotfix check.
        if (Items.checkIllegalEnchantments(player, stack)) return true;
        
        boolean cancelled = false;
        
        final String worldName = player.getWorld().getName();
        final int tick = TickTask.getTick();
        final long now = System.currentTimeMillis();
        final boolean worldChanged = !worldName.equals(data.lastWorld);
        
        final Location loc =  player.getLocation();
        final Location targetLoc = damaged.getLocation();
//        final double targetDist = CheckUtils.distance(loc, targetLoc); // TODO: Calculate distance as is done in fight.reach !
        final double targetMove;
        final int tickAge;
        final long msAge; // Milliseconds the ticks actually took.
        final double normalizedMove; // Blocks per second.
        // TODO: relative distance (player - target)!
        if (data.lastAttackedX == Integer.MAX_VALUE || tick < data.lastAttackTick || worldChanged || tick - data.lastAttackTick > 20){
        	// TODO: 20 ?
        	tickAge = 0;
        	targetMove = 0.0;
        	normalizedMove = 0.0;
        	msAge = 0;
        }
        else{
        	tickAge = tick - data.lastAttackTick;
        	// TODO: Maybe use 3d distance if dy(normalized) is too big. 
        	targetMove = CheckUtils.distance(data.lastAttackedX, data.lastAttackedZ, targetLoc.getX(), targetLoc.getZ());
        	msAge = (long) (50f * TickTask.getLag(50L * tickAge) * (float) tickAge);
        	normalizedMove = msAge == 0 ? targetMove : targetMove * Math.min(20.0, 1000.0 / (double) msAge);
        }
        // TODO: calculate factor for dists: ticks * 50 * lag
        
        // TODO: dist < width => skip some checks (direction, ..)
    	
        // Check for self hit exploits (mind that projectiles should be excluded)
        if (damaged instanceof Player){
        	final Player damagedPlayer = (Player) damaged;
        	if (cc.debug && damagedPlayer.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        		damagedPlayer.sendMessage("Attacked by " + player.getName() + ": inv=" + mcAccess.getInvulnerableTicks(damagedPlayer) + " ndt=" + damagedPlayer.getNoDamageTicks());
        	}
        	if (selfHit.isEnabled(player) && selfHit.check(player, damagedPlayer, data, cc))
        		cancelled = true;
        }
        
        if (cc.cancelDead){
        	if (damaged.isDead()) cancelled = true;
        	// Only allow damaging others if taken damage this tick.
            if (player.isDead() && data.damageTakenByEntityTick != TickTask.getTick()){
            	cancelled = true;
            }
        }
        


        // Run through the main checks.
        if (!cancelled && speed.isEnabled(player)){
        	if (speed.check(player, now)){
        		cancelled = true;
        		// Still feed the improbable.
        		Improbable.feed(player, 2f, now);
        	}
        	else if (normalizedMove > 2.0 && Improbable.check(player, 1f, now, "fight.speed")){
        		// Feed improbable in case of ok-moves too.
        		// TODO: consider only feeding if attacking with higher average speed (!)
        		cancelled = true;
        	}
        }

		if (angle.isEnabled(player)) {
			// The "fast turning" checks are checked in any case because they accumulate data.
			// Improbable yaw changing.
			if (Combined.checkYawRate(player, loc.getYaw(), now, worldName, cc.yawRateCheck)) {
				// (Check or just feed).
				// TODO: Work into this somehow attacking the same aim and/or similar aim position (not cancel then).
				cancelled = true;
			}
			// Angle check.
			if (angle.check(player, worldChanged)) cancelled = true;
		}

        if (!cancelled && critical.isEnabled(player) && critical.check(player))
            cancelled = true;

        if (!cancelled && direction.isEnabled(player) && direction.check(player, damaged))
            cancelled = true;

        if (!cancelled && knockback.isEnabled(player) && knockback.check(player))
            cancelled = true;

        if (!cancelled && noSwing.isEnabled(player) && noSwing.check(player))
            cancelled = true;

        if (!cancelled && reach.isEnabled(player) && reach.check(player, damaged))
            cancelled = true;

        if (!cancelled && player.isBlocking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))
            cancelled = true;
        
        // Set values.
        data.lastWorld = worldName;
    	data.lastAttackTick = tick;
    	data.lastAttackedX = targetLoc.getX();
    	data.lastAttackedY = targetLoc.getY();
    	data.lastAttackedZ = targetLoc.getZ();
//    	data.lastAttackedDist = targetDist;
    	
        return cancelled;
    }

    /**
     * We listen to EntityDamage events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        /*
         *  _____       _   _ _           ____                                   
         * | ____|_ __ | |_(_) |_ _   _  |  _ \  __ _ _ __ ___   __ _  __ _  ___ 
         * |  _| | '_ \| __| | __| | | | | | | |/ _` | '_ ` _ \ / _` |/ _` |/ _ \
         * | |___| | | | |_| | |_| |_| | | |_| | (_| | | | | | | (_| | (_| |  __/
         * |_____|_| |_|\__|_|\__|\__, | |____/ \__,_|_| |_| |_|\__,_|\__, |\___|
         *                        |___/                               |___/      
         */
    	
    	final Entity damaged = event.getEntity();
    	final boolean damagedIsPlayer = damaged instanceof Player;
    	final boolean damagedIsDead = damaged.isDead();
    	if (damagedIsPlayer && !damagedIsDead) {
            final Player player = (Player) event.getEntity();
//            if (godMode.isEnabled(player) && godMode.check(player)){
            if (godMode.isEnabled(player) && godMode.check(player, event.getDamage())){
                // It requested to "cancel" the players invulnerability, so set his noDamageTicks to 0.
                player.setNoDamageTicks(0);
            }
        }
    	// Attacking entities.
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
        	if (damagedIsPlayer && !damagedIsDead){
        	    // TODO: check once more when to set this (!) in terms of order.
        		FightData.getData((Player) damaged).damageTakenByEntityTick = TickTask.getTick();
        	}
        	final Entity damager = e.getDamager();
            if (damager instanceof Player){
                final Player player = (Player) damager;
                if (e.getCause() == DamageCause.ENTITY_ATTACK){
                	if (handleNormalDamage(player, damaged)) e.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamageMonitor(final EntityDamageEvent event) {
    	final Entity damaged = event.getEntity();
    	if (damaged instanceof Player){
    		final Player player = (Player) damaged;
    		final FightData data = FightData.getData(player);
    		final int ndt = player.getNoDamageTicks();
    		if (data.lastDamageTick == TickTask.getTick() && data.lastNoDamageTicks != ndt){
    			// Plugin compatibility thing.
    			data.lastNoDamageTicks = ndt;
    		}
    	}
    }

    /**
     * We listen to death events to prevent a very specific method of doing godmode.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onEntityDeathEvent(final EntityDeathEvent event) {
        /*
         *  _____       _   _ _           ____             _   _     
         * | ____|_ __ | |_(_) |_ _   _  |  _ \  ___  __ _| |_| |__  
         * |  _| | '_ \| __| | __| | | | | | | |/ _ \/ _` | __| '_ \ 
         * | |___| | | | |_| | |_| |_| | | |_| |  __/ (_| | |_| | | |
         * |_____|_| |_|\__|_|\__|\__, | |____/ \___|\__,_|\__|_| |_|
         *                        |___/                              
         */
        // Only interested in dying players.
        final Entity entity = event.getEntity();
        if (entity instanceof Player){
            final Player player = (Player) entity;
            if (godMode.isEnabled(player)) godMode.death(player);
        }
    }

    /**
     * We listen to PlayerAnimation events because it is used for arm swinging.
     * 
     * @param event
     *            the event
     */
    @EventHandler(priority = EventPriority.MONITOR)
    protected void onPlayerAnimation(final PlayerAnimationEvent event) {
        /*
         *  ____  _                            _          _                 _   _             
         * |  _ \| | __ _ _   _  ___ _ __     / \   _ __ (_)_ __ ___   __ _| |_(_) ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__|   / _ \ | '_ \| | '_ ` _ \ / _` | __| |/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |     / ___ \| | | | | | | | | | (_| | |_| | (_) | | | |
         * |_|   |_|\__,_|\__, |\___|_|    /_/   \_\_| |_|_|_| |_| |_|\__,_|\__|_|\___/|_| |_|
         *                |___/                                                               
         */
        // Set a flag telling us that the arm has been swung.
        FightData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listen to the PlayerToggleSprint events for the Knockback check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerToggleSprint(final PlayerToggleSprintEvent event) {
        /*
         *  ____  _                         _____                 _        ____             _       _   
         * |  _ \| | __ _ _   _  ___ _ __  |_   _|__   __ _  __ _| | ___  / ___| _ __  _ __(_)_ __ | |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|   | |/ _ \ / _` |/ _` | |/ _ \ \___ \| '_ \| '__| | '_ \| __|
         * |  __/| | (_| | |_| |  __/ |      | | (_) | (_| | (_| | |  __/  ___) | |_) | |  | | | | | |_ 
         * |_|   |_|\__,_|\__, |\___|_|      |_|\___/ \__, |\__, |_|\___| |____/| .__/|_|  |_|_| |_|\__|
         *                |___/                       |___/ |___/               |_|                     
         */
        if (event.isSprinting()) FightData.getData(event.getPlayer()).knockbackSprintTime = System.currentTimeMillis();
    }
    
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event){
    	final Entity entity = event.getEntity();
    	if (!(entity instanceof Player)) return;
    	final Player player = (Player) entity;
    	final FightData data = FightData.getData(player);
    	// Remember the time.
    	data.regainHealthTime = System.currentTimeMillis();
    	// Set god-mode health to maximum.
    	data.godModeHealth = Math.max(data.godModeHealth, player.getHealth() + event.getAmount());

    }
}
