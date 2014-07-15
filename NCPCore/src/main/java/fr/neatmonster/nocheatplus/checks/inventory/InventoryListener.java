package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.components.JoinLeaveListener;
import fr.neatmonster.nocheatplus.utilities.InventoryUtil;

/**
 * Central location to listen to events that are relevant for the inventory checks.
 * 
 * @see InventoryEvent
 */
public class InventoryListener  extends CheckListener implements JoinLeaveListener{

    /** The drop check. */
    private final Drop       drop       = addCheck(new Drop());

    /** The fast click check. */
    private final FastClick  fastClick  = addCheck(new FastClick());

    /** The instant bow check. */
    private final InstantBow instantBow = addCheck(new InstantBow());

    /** The instant eat check. */
    private final InstantEat instantEat = addCheck(new InstantEat());
    
    protected final Items items 		= addCheck(new Items());
    
    private final Open open 			= addCheck(new Open());
    
    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
	private final Location useLoc = new Location(null, 0, 0, 0);
    
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
        // Only if a player shot the arrow.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantBow.isEnabled(player)){
                final long now = System.currentTimeMillis();
                final Location loc = player.getLocation(useLoc);
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
            	useLoc.setWorld(null);
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
        // Only if a player ate food.
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            if (instantEat.isEnabled(player) && instantEat.check(player, event.getFoodLevel())){
            	event.setCancelled(true);
            }
            else if (player.isDead() && BridgeHealth.getHealth(player) <= 0.0) {
            	// Eat after death.
            	event.setCancelled(true);
            }
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
        if (event.getWhoClicked() instanceof Player) {
        	final long now = System.currentTimeMillis();
        	final HumanEntity entity = event.getWhoClicked();
        	if (!(entity instanceof Player)){
        		return;
        	}
            final Player player = (Player) entity;
            final int slot = event.getSlot();
        	if (slot == InventoryView.OUTSIDE || slot < 0){
        		InventoryData.getData(player).lastClickTime = now;
        		return;
        	}
        	
        	final ItemStack cursor = event.getCursor();
        	final ItemStack clicked = event.getCurrentItem();
            
            // Illegal enchantment checks.
            try{
                if (Items.checkIllegalEnchantments(player, clicked)) event.setCancelled(true);
            }
            catch(final ArrayIndexOutOfBoundsException e){} // Hotfix (CB)
            try{
                if (Items.checkIllegalEnchantments(player, cursor)) event.setCancelled(true);
            }
            catch(final ArrayIndexOutOfBoundsException e){} // Hotfix (CB)
            
            final InventoryData data = InventoryData.getData(player);
            
            // Fast inventory manipulation check.
            if (fastClick.isEnabled(player)){
                final InventoryConfig cc = InventoryConfig.getConfig(player);
                if (player.getGameMode() != GameMode.CREATIVE || !cc.fastClickSpareCreative){
                    if (fastClick.check(player, now, event.getView(), slot, cursor, clicked, event.isShiftClick(), data, cc)){
                        // The check requested the event to be cancelled.
                        event.setCancelled(true);
                    }
                    // Feed the improbable.
                    Improbable.feed(player, 0.7f, System.currentTimeMillis());
                }
            }
            data.lastClickTime = now;
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
        
        final Player player = event.getPlayer();
        
        // Illegal enchantments hotfix check.
        final Item item = event.getItemDrop();
        if (item != null){
            // No cancel here.
            Items.checkIllegalEnchantments(player, item.getItemStack());
        }
        
        // If the player died, all their items are dropped so ignore them.
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
        // Only interested in right-clicks while holding an item.
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;
        
        final Player player = event.getPlayer();
        final InventoryData data = InventoryData.getData(player);
        
        boolean resetAll = false;
        
        
        if (event.hasItem()){
            final ItemStack item = event.getItem();
            final Material type = item.getType();
            // TODO: Get Magic values (800) from the config.
            if (type == Material.BOW){
                final long now = System.currentTimeMillis();
                // It was a bow, the player starts to pull the string, remember this time.
                data.instantBowInteract = (data.instantBowInteract > 0 && now - data.instantBowInteract < 800) ? Math.min(System.currentTimeMillis(), data.instantBowInteract) : System.currentTimeMillis();
            }
            else if (type.isEdible() || type == Material.POTION) {
                final long now = System.currentTimeMillis();
                // It was food, the player starts to eat some food, remember this time and the type of food.
                data.instantEatFood = type;
                data.instantEatInteract = (data.instantEatInteract > 0 && now - data.instantEatInteract < 800) ? Math.min(System.currentTimeMillis(), data.instantEatInteract) : System.currentTimeMillis();
                data.instantBowInteract = 0;
            } else resetAll = true;
            
            // Illegal enchantments hotfix check.
            if (Items.checkIllegalEnchantments(player, item)) {
            	event.setCancelled(true);
            }
        }
        else {
        	resetAll = true;
        }
        
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
    	if (player.getGameMode() == GameMode.CREATIVE) {
    		return;
    	}
    	if (player.isDead() && BridgeHealth.getHealth(player) <= 0.0) {
    		// No zombies.
    		event.setCancelled(true);
    		return;
    	}
    	final ItemStack stack = player.getItemInHand();
    	if (stack != null && stack.getType() == Material.MONSTER_EGG && items.isEnabled(player)){
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
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(final PlayerChangedWorldEvent event){
    	open.check(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerPortal(final PlayerPortalEvent event){
    	// Note: ignore cancelother setting.
    	open.check(event.getPlayer());
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityPortal(final EntityPortalEnterEvent event){
    	final Player player = InventoryUtil.getPlayerPassengerRecursively(event.getEntity());
    	if (player != null) {
    		// Note: ignore cancelother setting.
        	open.check(player);
    	}
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(final PlayerTeleportEvent event){
    	// Note: ignore cancelother setting.
    	open.check(event.getPlayer());
    }

	@Override
	public void playerJoins(Player player) {
		// Ignore
	}

	@Override
	public void playerLeaves(Player player) {
		open.check(player);
	}
    
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onVehicleDestroy(final VehicleDestroyEvent event) {
//    	final Entity entity = event.getVehicle();
//    	if (entity instanceof InventoryHolder) { // Fail on 1.4 ?
//    		checkInventoryHolder((InventoryHolder) entity);
//    	}
//    }
//    
//    @EventHandler(priority = EventPriority.MONITOR)
//    public void onBlockBreak(final BlockBreakEvent event) {
//    	final Block block = event.getBlock();
//    	if (block == null) {
//    		return;
//    	}
//    	// TODO: + explosions !? + entity change block + ...
//    }
//
//	private void checkInventoryHolder(InventoryHolder entity) {
//		// TODO Auto-generated method stub
//		
//	}
}
