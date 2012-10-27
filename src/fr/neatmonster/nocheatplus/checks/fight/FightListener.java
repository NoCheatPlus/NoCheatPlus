package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.inventory.Items;
import fr.neatmonster.nocheatplus.players.Permissions;
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
public class FightListener implements Listener {

    /** The angle check. */
    private final Angle       angle       = new Angle();

    /** The critical check. */
    private final Critical    critical    = new Critical();

    /** The direction check. */
    private final Direction   direction   = new Direction();

    /** The god mode check. */
    private final GodMode     godMode     = new GodMode();

    /** The knockback check. */
    private final Knockback   knockback   = new Knockback();

    /** The no swing check. */
    private final NoSwing     noSwing     = new NoSwing();

    /** The reach check. */
    private final Reach       reach       = new Reach();
    
    /** The self hit check */
    private final SelfHit     selfHit     = new SelfHit();

    /** The speed check. */
    private final Speed       speed       = new Speed();

    /**
     * A player attacked something with DamageCause ENTITY_ATTACK. That's most likely what we want to really check.
     * 
     * @param event
     *            The EntityDamageByEntityEvent
     * @return 
     */
    private boolean handleNormalDamage(final Player player, final Entity cbEntity) {
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);
        
        // Hotfix attempt for enchanted books.
        // TODO: maybe a generaluzed version for the future...
        final ItemStack stack = player.getItemInHand();
        // Illegal enchantments hotfix check.
        if (Items.checkIllegalEnchantments(player, stack)) return true;
        
        boolean cancelled = false;
        
        final String worldName = player.getWorld().getName();
        
        // Check for self hit exploits (mind that projectiles should be excluded)
        if (cbEntity instanceof Player){
        	final Player damagedPlayer = (Player) cbEntity;
        	if (selfHit.isEnabled(player) && selfHit.check(player, damagedPlayer, data, cc))
        		cancelled = true;
        }
        
        if (cc.cancelDead){
        	if (cbEntity.isDead()) cancelled = true;
        	// Only allow damaging others if taken damage this tick.
            if (player.isDead() && data.damageTakenTick != TickTask.getTick()){
            	cancelled = true;
            }
        }
        
        final long now = System.currentTimeMillis();
        
        final boolean worldChanged = !worldName.equals(data.lastWorld);

        // Get the attacked entity.
        final net.minecraft.server.Entity damaged = ((CraftEntity) cbEntity).getHandle();

        // Run through the main checks.
        if (!cancelled && speed.isEnabled(player)){
        	if (speed.check(player, now))
        		cancelled = true;
	        // Combined speed:
        	else if (Improbable.check(player, 1f, now))
	        	cancelled = true;
        }

        
        if (!cancelled && angle.isEnabled(player)){
            // Improbable yaw.
            if (Combined.checkYawRate(player, player.getLocation().getYaw(), now, worldName, cc.yawRateCheck)){
            	// (Check or just feed).
            	cancelled = true;
            }
            // Angle check.
        	if (angle.check(player, worldChanged)) cancelled = true;
        }
        else{
        	// Always feed yaw rate here.
        	Combined.feedYawRate(player, player.getLocation().getYaw(), now, worldName);
        }

        if (!cancelled && critical.isEnabled(player) && critical.check(player))
            cancelled = true;

        if (!cancelled && direction.isEnabled(player) && direction.check(player, damaged))
            cancelled = true;

        if (!cancelled && knockback.isEnabled(player) && knockback.check(player))
            cancelled = true;

        if (!cancelled && noSwing.isEnabled(player) && noSwing.check(player))
            cancelled = true;

        if (!cancelled && reach.isEnabled(player) && reach.check(player, cbEntity))
            cancelled = true;

        if (!cancelled && player.isBlocking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))
            cancelled = true;
        
        data.lastWorld = worldName;
        return cancelled;
    }

    /**
     * We listen to EntityDamage events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityDamage(final EntityDamageEvent event) {
        /*
         *  _____       _   _ _           ____                                   
         * | ____|_ __ | |_(_) |_ _   _  |  _ \  __ _ _ __ ___   __ _  __ _  ___ 
         * |  _| | '_ \| __| | __| | | | | | | |/ _` | '_ ` _ \ / _` |/ _` |/ _ \
         * | |___| | | | |_| | |_| |_| | | |_| | (_| | | | | | | (_| | (_| |  __/
         * |_____|_| |_|\__|_|\__|\__, | |____/ \__,_|_| |_| |_|\__,_|\__, |\___|
         *                        |___/                               |___/      
         */
        // Filter some unwanted events right now.
        if (event instanceof EntityDamageByEntityEvent) {
            final EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;
            final Entity damaged = e.getEntity();
        	if (damaged instanceof Player){
        	    // TODO: check once more when to set this (!) in terms of order.
        		FightData.getData((Player) damaged).damageTakenTick = TickTask.getTick();
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

    /**
     * We listen to EntityDamage events (again) for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamage_(final EntityDamageEvent event) {
        /*
         *  _____       _   _ _           ____                                   
         * | ____|_ __ | |_(_) |_ _   _  |  _ \  __ _ _ __ ___   __ _  __ _  ___ 
         * |  _| | '_ \| __| | __| | | | | | | |/ _` | '_ ` _ \ / _` |/ _` |/ _ \
         * | |___| | | | |_| | |_| |_| | | |_| | (_| | | | | | | (_| | (_| |  __/
         * |_____|_| |_|\__|_|\__|\__, | |____/ \__,_|_| |_| |_|\__,_|\__, |\___|
         *                        |___/                               |___/      
         */
        // Filter unwanted events right here.
        if (event.getEntity() instanceof Player && !event.getEntity().isDead()) {
            final Player player = (Player) event.getEntity();
            if (godMode.isEnabled(player) && godMode.check(player))
                // It requested to "cancel" the players invulnerability, so set his noDamageTicks to 0.
                player.setNoDamageTicks(0);
        }
    }

    /**
     * We listen to death events to prevent a very specific method of doing godmode.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
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
    @EventHandler(
            priority = EventPriority.MONITOR)
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
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.MONITOR)
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
}
