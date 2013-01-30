package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;

/*
 * M""M                                       dP                              
 * M  M                                       88                              
 * M  M 88d888b. dP   .dP .d8888b. 88d888b. d8888P .d8888b. 88d888b. dP    dP 
 * M  M 88'  `88 88   d8' 88ooood8 88'  `88   88   88'  `88 88'  `88 88    88 
 * M  M 88    88 88 .88'  88.  ... 88    88   88   88.  .88 88       88.  .88 
 * M  M dP    dP 8888P'   `88888P' dP    dP   dP   `88888P' dP       `8888P88 
 * MMMM                                                                   .88 
 *                                                                    d8888P  
 *                                                                    
 * M""MMMMMMMM oo            dP                                       
 * M  MMMMMMMM               88                                       
 * M  MMMMMMMM dP .d8888b. d8888P .d8888b. 88d888b. .d8888b. 88d888b. 
 * M  MMMMMMMM 88 Y8ooooo.   88   88ooood8 88'  `88 88ooood8 88'  `88 
 * M  MMMMMMMM 88       88   88   88.  ... 88    88 88.  ... 88       
 * M         M dP `88888P'   dP   `88888P' dP    dP `88888P' dP       
 * MMMMMMMMMMM                                                        
 */
/**
 * Central location to listen to events that are relevant for the inventory checks.
 * 
 * @see InventoryEvent
 */
public class InventoryListener  extends CheckListener {

    /** The drop check. */
    private final Drop       drop       = addCheck(new Drop());

    /** The fast click check. */
    private final FastClick  fastClick  = addCheck(new FastClick());

    /** The instant bow check. */
    private final InstantBow instantBow = addCheck(new InstantBow());

    /** The instant eat check. */
    private final InstantEat instantEat = addCheck(new InstantEat());
    
    protected final Items items 		= addCheck(new Items());
    
    public InventoryListener(){
    	super(CheckType.INVENTORY);
    }

    /**
     * We listen to EntityShootBow events for the InstantBow check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onEntityShootBow(final EntityShootBowEvent event) {
        /*
         *  _____       _   _ _           ____  _                 _     ____                
         * | ____|_ __ | |_(_) |_ _   _  / ___|| |__   ___   ___ | |_  | __ )  _____      __
         * |  _| | '_ \| __| | __| | | | \___ \| '_ \ / _ \ / _ \| __| |  _ \ / _ \ \ /\ / /
         * | |___| | | | |_| | |_| |_| |  ___) | | | | (_) | (_) | |_  | |_) | (_) \ V  V / 
         * |_____|_| |_|\__|_|\__|\__, | |____/|_| |_|\___/ \___/ \__| |____/ \___/ \_/\_/  
         *                        |___/                                                     
         */
        // Only if a player shot the arrow.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantBow.isEnabled(player)){
                final long now = System.currentTimeMillis();
                final Location loc = player.getLocation();
                if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName())){
                    // No else if with this, could be cancelled due to other checks feeding, does not have actions.
                    event.setCancelled(true);
                }
                // Still check instantBow, whatever yawrate says.
            	if (instantBow.check(player, event.getForce(), now)){
            	    // The check requested the event to be cancelled.
            	    event.setCancelled(true);
            	}
            	else if (Improbable.check(player, 0.6f, now, "inventory.instantbow")){
                    // Combined fighting speed (Else if: Matter of taste, preventing extreme cascading and actions spam).
                    event.setCancelled(true);
            	}
            }  
        }
    }

    /**
     * We listen to FoodLevelChange events because Bukkit doesn't provide a PlayerFoodEating Event (or whatever it would
     * be called).
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        /*
         *  _____               _   _                   _    ____ _                            
         * |  ___|__   ___   __| | | |    _____   _____| |  / ___| |__   __ _ _ __   __ _  ___ 
         * | |_ / _ \ / _ \ / _` | | |   / _ \ \ / / _ \ | | |   | '_ \ / _` | '_ \ / _` |/ _ \
         * |  _| (_) | (_) | (_| | | |__|  __/\ V /  __/ | | |___| | | | (_| | | | | (_| |  __/
         * |_|  \___/ \___/ \__,_| |_____\___| \_/ \___|_|  \____|_| |_|\__,_|_| |_|\__, |\___|
         *                                                                          |___/      
         */
        // Only if a player ate food.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantEat.isEnabled(player) && instantEat.check(player, event.getFoodLevel()))
                event.setCancelled(true);
            // Forget the food material, as the info is no longer needed.
            InventoryData.getData(player).instantEatFood = null;
        }
    }

    /**
     * We listen to InventoryClick events for the FastClick check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onInventoryClick(final InventoryClickEvent event) {
        /*
         *  ___                      _                      ____ _ _      _    
         * |_ _|_ ____   _____ _ __ | |_ ___  _ __ _   _   / ___| (_) ___| | __
         *  | || '_ \ \ / / _ \ '_ \| __/ _ \| '__| | | | | |   | | |/ __| |/ /
         *  | || | | \ V /  __/ | | | || (_) | |  | |_| | | |___| | | (__|   < 
         * |___|_| |_|\_/ \___|_| |_|\__\___/|_|   \__, |  \____|_|_|\___|_|\_\
         *                                         |___/                       
         */
        if (event.getWhoClicked() instanceof Player) {
            final Player player = (Player) event.getWhoClicked();
            
            // Illegal enchantment checks.
            try{
                if (Items.checkIllegalEnchantments(player, event.getCurrentItem())) event.setCancelled(true);
            }
            catch(final ArrayIndexOutOfBoundsException e){} // Hotfix (CB)
            try{
                if (Items.checkIllegalEnchantments(player, event.getCursor())) event.setCancelled(true);
            }
            catch(final ArrayIndexOutOfBoundsException e){} // Hotfix (CB)
            
            // Fast inventory manipulation check.
            if (fastClick.isEnabled(player)){
                if (player.getGameMode() != GameMode.CREATIVE || !InventoryConfig.getConfig(player).fastClickSpareCreative){
                    if (fastClick.check(player)){
                        // The check requested the event to be cancelled.
                        event.setCancelled(true);
                    }
                    // Feed the improbable.
                    Improbable.feed(player, 0.7f, System.currentTimeMillis());
                }
            }
        }
    }

    /**
     * We listen to DropItem events for the Drop check.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerDropItem(final PlayerDropItemEvent event) {
        /*
         *  ____  _                         ____                  
         * |  _ \| | __ _ _   _  ___ _ __  |  _ \ _ __ ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__| | | | | '__/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |    | |_| | | | (_) | |_) |
         * |_|   |_|\__,_|\__, |\___|_|    |____/|_|  \___/| .__/ 
         *                |___/                            |_|    
         */
        
        final Player player = event.getPlayer();
        
        // Illegal enchantments hotfix check.
        final Item item = event.getItemDrop();
        if (item != null){
            // No cancel here.
            Items.checkIllegalEnchantments(player, item.getItemStack());
        }
        
        // If the player died, all his items are dropped so ignore him.
        if (event.getPlayer().isDead())
            return;

        if (drop.isEnabled(event.getPlayer())){
        	if (drop.check(event.getPlayer())){
        		// TODO: Is the following command still correct? If so, adapt actions.
                // Cancelling drop events is not save (in certain circumstances items will disappear completely). So don't
                // do it and kick players instead by default.
                event.setCancelled(true);
        	}
        }


    }

    /**
     * We listen to PlayerInteract events for the InstantEat and InstantBow checks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public final void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
        // Only interested in right-clicks while holding an item.
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        final Player player = event.getPlayer();
        final InventoryData data = InventoryData.getData(player);
        
        boolean resetAll = false;
        
        
        if (event.hasItem()){
            final ItemStack item = event.getItem();
            final Material type = item.getType();
            if (type == Material.BOW){
                final long now = System.currentTimeMillis();
                // It was a bow, the player starts to pull the string, remember this time.
                data.instantBowInteract = (data.instantBowInteract > 0 && now - data.instantBowInteract < 800) ? Math.min(System.currentTimeMillis(), data.instantBowInteract) : System.currentTimeMillis();
            }
            else if (type.isEdible()) {
                final long now = System.currentTimeMillis();
                // It was food, the player starts to eat some food, remember this time and the type of food.
                data.instantEatFood = type;
                data.instantEatInteract = (data.instantEatInteract > 0 && now - data.instantEatInteract < 800) ? Math.min(System.currentTimeMillis(), data.instantEatInteract) : System.currentTimeMillis();
                data.instantBowInteract = 0;
            } else resetAll = true;
            
            // Illegal enchantments hotfix check.
            if (Items.checkIllegalEnchantments(player, item)) event.setCancelled(true);
        }
        else resetAll = true;
        
        if (resetAll){
            // Nothing that we are interested in, reset data.
            data.instantBowInteract = 0;
            data.instantEatInteract = 0;
            data.instantEatFood = null;
        }
    }
    
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public final void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
    	final Player player = event.getPlayer();
    	if (player.getGameMode() == GameMode.CREATIVE) return;
    	final ItemStack stack = player.getItemInHand();
    	if (stack != null && stack.getTypeId() == Material.MONSTER_EGG.getId() && items.isEnabled(player)){
    		event.setCancelled(true);
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeldChange(final PlayerItemHeldEvent event){
        final Player player = event.getPlayer();
        final InventoryData data = InventoryData.getData(player);
        data.instantBowInteract = 0;
        data.instantEatInteract = 0;
        data.instantEatFood = null;
        
        // Illegal enchantments hotfix check.
        final PlayerInventory inv = player.getInventory();
        Items.checkIllegalEnchantments(player, inv.getItem(event.getNewSlot()));
        Items.checkIllegalEnchantments(player, inv.getItem(event.getPreviousSlot()));
    }
}
