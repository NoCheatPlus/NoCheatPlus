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
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

import fr.neatmonster.nocheatplus.checks.combined.CombinedData;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.players.Permissions;

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

    /** The instant heal check. */
    private final InstantHeal instantHeal = new InstantHeal();

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
     * There is an unofficial agreement that if a plugin wants an attack to not get checked by NoCheatPlus, it either
     * has to use a damage type different from ENTITY_ATTACK or fire an event with damage type CUSTOM and damage 0
     * directly before the to-be-ignored event.
     * 
     * @param event
     *            the event
     */
    private void handleCustomDamage(final EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player)
            // Skip the next damage event, because it is with high probability something from the Heroes plugin.
            FightData.getData((Player) event.getDamager()).skipNext = true;
    }

    /**
     * A player attacked something with DamageCause ENTITY_ATTACK. That's most likely what we want to really check.
     * 
     * @param event
     *            The EntityDamageByEntityEvent
     */
    private void handleNormalDamage(final EntityDamageByEntityEvent event) {
        final Player player = (Player) event.getDamager();
        final FightConfig cc = FightConfig.getConfig(player);
        final FightData data = FightData.getData(player);

        // For some reason we decided to skip this event anyway.
        if (data.skipNext) {
            data.skipNext = false;
            return;
        }

        boolean cancelled = false;
        
        // Check for self hit exploits (mind that projectiles should be excluded)
        final Entity cbEntity = event.getEntity();
        if (cbEntity instanceof Player){
        	if (selfHit.isEnabled(player) && selfHit.check(player, cbEntity, data, cc))
        		cancelled = true;
        	else{
            	// Check if improbable
            	if (Improbable.check(player, 2.0f, System.currentTimeMillis()))
            		cancelled = true;
            	CombinedData.getData((Player) cbEntity).improbableCount.add(System.currentTimeMillis(), -2.0f);
        	}
        }
        
        // Combined speed:
        if (Improbable.check(player, 1f, System.currentTimeMillis()))
        	cancelled = true;

        // Get the attacked entity.
        final net.minecraft.server.Entity damaged = ((CraftEntity) cbEntity).getHandle();

        // Run through the main checks.
        if (!cancelled && angle.isEnabled(player) && angle.check(player))
            cancelled = true;

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

        if (!cancelled && speed.isEnabled(player) && speed.check(player))
            cancelled = true;

        if (!cancelled && player.isBlocking() && !player.hasPermission(Permissions.MOVING_SURVIVALFLY_BLOCKING))
            cancelled = true;

        // One of the checks requested the event to be cancelled, so do it.
        if (cancelled)
            event.setCancelled(cancelled);
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
            if (e.getDamager() instanceof Player)
                if (e.getCause() == DamageCause.ENTITY_ATTACK)
                    handleNormalDamage(e);
                else if (e.getCause() == DamageCause.CUSTOM)
                    handleCustomDamage(e);
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
        if (event.getEntity() instanceof Player)
            godMode.death((Player) event.getEntity());
    }

    /**
     * We listen to EntityRegainHealth events of type "satiated" for InstantHeal check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityRegainHealth(final EntityRegainHealthEvent event) {
        /*
         *  _____       _   _ _           ____                  _         _   _            _ _   _     
         * | ____|_ __ | |_(_) |_ _   _  |  _ \ ___  __ _  __ _(_)_ __   | | | | ___  __ _| | |_| |__  
         * |  _| | '_ \| __| | __| | | | | |_) / _ \/ _` |/ _` | | '_ \  | |_| |/ _ \/ _` | | __| '_ \ 
         * | |___| | | | |_| | |_| |_| | |  _ <  __/ (_| | (_| | | | | | |  _  |  __/ (_| | | |_| | | |
         * |_____|_| |_|\__|_|\__|\__, | |_| \_\___|\__, |\__,_|_|_| |_| |_| |_|\___|\__,_|_|\__|_| |_|
         *                        |___/             |___/                                              
         */
        if (event.getEntity() instanceof Player && event.getRegainReason() == RegainReason.SATIATED) {
            final Player player = (Player) event.getEntity();
            if (instantHeal.isEnabled(player) && instantHeal.check(player))
                event.setCancelled(true);
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
