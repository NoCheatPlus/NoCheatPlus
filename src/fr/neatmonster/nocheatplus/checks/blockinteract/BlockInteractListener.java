package fr.neatmonster.nocheatplus.checks.blockinteract;

import java.util.Arrays;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/*
 * M#"""""""'M  dP                   dP       M""M            dP                                         dP   
 * ##  mmmm. `M 88                   88       M  M            88                                         88   
 * #'        .M 88 .d8888b. .d8888b. 88  .dP  M  M 88d888b. d8888P .d8888b. 88d888b. .d8888b. .d8888b. d8888P 
 * M#  MMMb.'YM 88 88'  `88 88'  `"" 88888"   M  M 88'  `88   88   88ooood8 88'  `88 88'  `88 88'  `""   88   
 * M#  MMMM'  M 88 88.  .88 88.  ... 88  `8b. M  M 88    88   88   88.  ... 88       88.  .88 88.  ...   88   
 * M#       .;M dP `88888P' `88888P' dP   `YP M  M dP    dP   dP   `88888P' dP       `88888P8 `88888P'   dP   
 * M#########M                                MMMM                                                            
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
 * Central location to listen to events that are relevant for the block interact checks.
 * 
 * @see BlockInteractEvent
 */
public class BlockInteractListener implements Listener {
    private final Direction  direction = new Direction();
    private final NoSwing    noSwing   = new NoSwing();
    private final Reach      reach     = new Reach();

    private final Material[] materials = new Material[] {Material.BED_BLOCK, Material.BURNING_FURNACE,
            Material.BREWING_STAND, Material.CAKE_BLOCK, Material.CAULDRON, Material.CHEST, Material.DIODE_BLOCK_OFF,
            Material.DIODE_BLOCK_ON, Material.DISPENSER, Material.DRAGON_EGG, Material.ENCHANTMENT_TABLE,
            Material.ENDER_CHEST, Material.FENCE_GATE, Material.FURNACE, Material.IRON_DOOR_BLOCK, Material.LEVER,
            Material.NOTE_BLOCK, Material.STONE_BUTTON, Material.TRAP_DOOR, Material.WOODEN_DOOR, Material.WORKBENCH};

    /**
     * We listen to PlayerAnimation events because it is (currently) equivalent to "player swings arm" and we want to
     * check if he did that between interactions with blocks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        /*
         *  ____  _                            _          _                 _   _             
         * |  _ \| | __ _ _   _  ___ _ __     / \   _ __ (_)_ __ ___   __ _| |_(_) ___  _ __  
         * | |_) | |/ _` | | | |/ _ \ '__|   / _ \ | '_ \| | '_ ` _ \ / _` | __| |/ _ \| '_ \ 
         * |  __/| | (_| | |_| |  __/ |     / ___ \| | | | | | | | | | (_| | |_| | (_) | | | |
         * |_|   |_|\__,_|\__, |\___|_|    /_/   \_\_| |_|_|_| |_| |_|\__,_|\__|_|\___/|_| |_|
         *                |___/                                                               
         */
        // Just set a flag to true when the arm was swung.
        BlockInteractData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listen to PlayerInteractEvent events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    protected void onPlayerInteract(final PlayerInteractEvent event) {
        /*
         *  ____  _                         ___       _                      _   
         * |  _ \| | __ _ _   _  ___ _ __  |_ _|_ __ | |_ ___ _ __ __ _  ___| |_ 
         * | |_) | |/ _` | | | |/ _ \ '__|  | || '_ \| __/ _ \ '__/ _` |/ __| __|
         * |  __/| | (_| | |_| |  __/ |     | || | | | ||  __/ | | (_| | (__| |_ 
         * |_|   |_|\__,_|\__, |\___|_|    |___|_| |_|\__\___|_|  \__,_|\___|\__|
         *                |___/                                                  
         */
        final Player player = event.getPlayer();

        if (event.getClickedBlock() == null || event.getAction() != Action.LEFT_CLICK_BLOCK
                && event.getAction() != Action.RIGHT_CLICK_BLOCK)
            return;

        final Block block = event.getClickedBlock();

        boolean cancelled = false;

        // First the no swing check.
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK
                || Arrays.asList(materials).contains(event.getClickedBlock().getType())) {
            if (noSwing.isEnabled(player) && noSwing.check(player))
                cancelled = true;
        } else
            BlockInteractData.getData(player).noSwingArmSwung = false;

        // Second the reach check.
        if (!cancelled && reach.isEnabled(player) && reach.check(player, block.getLocation()))
            cancelled = true;

        // Third the direction check
        if (!cancelled && direction.isEnabled(player) && direction.check(player, block.getLocation()))
            cancelled = true;

        // If one of the checks requested to cancel the event, do so.
        if (cancelled)
            event.setCancelled(cancelled);
    }
}
