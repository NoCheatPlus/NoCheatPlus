package fr.neatmonster.nocheatplus.checks.blockplace;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckListener;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.combined.Combined;
import fr.neatmonster.nocheatplus.checks.combined.CombinedConfig;
import fr.neatmonster.nocheatplus.checks.combined.Improbable;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.compat.BridgeMisc;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.stats.Counters;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

/**
 * Central location to listen to events that are relevant for the block place checks.
 * 
 * @see BlockPlaceEvent
 */
public class BlockPlaceListener extends CheckListener {

    private static final int p1 = 73856093;
    private static final int p2 = 19349663;
    private static final int p3 = 83492791;

    private static final int getHash(final int x, final int y, final int z) {
        return p1 * x ^ p2 * y ^ p3 * z;
    }

    public static int getCoordHash(final Block block){
        return getHash(block.getX(), block.getY(), block.getZ());
    }

    public static int getBlockPlaceHash(final Block block, final Material mat){
        int hash = getCoordHash(block);
        if (mat != null){
            hash |= mat.name().hashCode();
        }
        hash |= block.getWorld().getName().hashCode();
        return hash;
    }

    /** Against. */
    private final Against against = addCheck(new Against());

    /** AutoSign. */
    private final AutoSign autoSign = addCheck(new AutoSign());

    /** The direction check. */
    private final Direction direction = addCheck(new Direction());

    /** The fast place check. */
    private final FastPlace fastPlace = addCheck(new FastPlace());

    /** The no swing check. */
    private final NoSwing   noSwing   = addCheck(new NoSwing());

    /** The reach check. */
    private final Reach     reach     = addCheck(new Reach());

    /** The speed check. */
    private final Speed     speed     = addCheck(new Speed());

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    private final Counters counters = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstance(Counters.class);
    private final int idBoatsAnywhere = counters.registerKey("boatsanywhere");
    private final int idEnderPearl = counters.registerKey("throwenderpearl");

    private final Class<?> blockMultiPlaceEvent = ReflectionUtil.getClass("org.bukkit.event.block.BlockMultiPlaceEvent");
    private final boolean hasReplacedState = ReflectionUtil.getMethodNoArgs(BlockPlaceEvent.class, "getReplacedState", BlockState.class) != null;

    public BlockPlaceListener(){
        super(CheckType.BLOCKPLACE);
    }

    /**
     * We listen to BlockPlace events for obvious reasons.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onBlockPlace(final BlockPlaceEvent event) {

        final Block block = event.getBlockPlaced();
        final Block blockAgainst = event.getBlockAgainst();
        // Skip any null blocks.
        if (block == null || blockAgainst == null) {
            return;
        }
        // TODO: What if same block?

        // TODO: Revise material use (not block.get... ?)
        //final Material mat = block.getType();
        final Player player = event.getPlayer();
        final Material placedMat = hasReplacedState ? event.getBlockPlaced().getType() : player.getItemInHand().getType(); // Safety first.
        boolean cancelled = false;

        final BlockPlaceData data = BlockPlaceData.getData(player);
        final BlockPlaceConfig cc = BlockPlaceConfig.getConfig(player);

        final boolean shouldSkipSome;
        if (blockMultiPlaceEvent != null && event.getClass() == blockMultiPlaceEvent) {
            if (placedMat == Material.BEDROCK || placedMat == Material.END_CRYSTAL) {
                shouldSkipSome = true;
            }
            else {
                if (data.debug) {
                    debug(player, "Block place " + event.getClass().getName() + " " + placedMat);
                }
                shouldSkipSome = false;
            }
        }
        else {
            shouldSkipSome = false;
        }

        if (placedMat == Material.SIGN){
            // Might move to MONITOR priority.
            data.autoSignPlacedTime = System.currentTimeMillis();
            // Always hash as sign post for improved compatibility with Lockette etc.
            data.autoSignPlacedHash = getBlockPlaceHash(block, Material.SIGN);
        }

        // Fast place check.
        if (fastPlace.isEnabled(player)){
            if (fastPlace.check(player, block, data, cc)) {
                cancelled = true;
            } else {
                // Feed the improbable.
                Improbable.feed(player, 0.5f, System.currentTimeMillis());
            }
        }

        // No swing check (player doesn't swing their arm when placing a lily pad).
        if (!cancelled && !cc.noSwingExceptions.contains(placedMat) && noSwing.isEnabled(player) && noSwing.check(player, data, cc)) {
            // Consider skipping all insta placables or using simplified version (true or true within time frame).
            cancelled = true;
        }

        // Reach check (distance).
        if (!cancelled && !shouldSkipSome && reach.isEnabled(player) && reach.check(player, block, data, cc)) {
            cancelled = true;
        }

        // Direction check.
        if (!cancelled && !shouldSkipSome && direction.isEnabled(player) && direction.check(player, block, blockAgainst, data, cc)) {
            cancelled = true;
        }

        // Surrounding material.
        if (!cancelled && against.isEnabled(player) && against.check(player, block, placedMat, blockAgainst, data, cc)) {
            cancelled = true;
        }

        // If one of the checks requested to cancel the event, do so.
        if (cancelled) {
            event.setCancelled(cancelled);
        } else {
            // Debug log (only if not cancelled, to avoid spam).
            if (data.debug) {
                debug(player, "Block place(" + placedMat + "): " + block.getX() + ", " + block.getY() + ", " + block.getZ());
            }
        }
        // Cleanup
        // Reminder(currently unused): useLoc.setWorld(null);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSignChange(final SignChangeEvent event){
        if (event.getClass() != SignChangeEvent.class){
            // Built in plugin compatibility.
            // TODO: Don't understand why two consecutive events editing the same block are a problem.
            return;
        }
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final String[] lines = event.getLines();
        if (block == null || lines == null || player == null){
            // Somewhat defensive.
            return;
        }
        if (autoSign.isEnabled(player) && autoSign.check(player, block, lines)){
            event.setCancelled(true);
        }
    }

    /**
     * We listen to PlayerAnimation events because it is (currently) equivalent to "player swings arm" and we want to
     * check if they did that between block breaks.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            priority = EventPriority.MONITOR)
    public void onPlayerAnimation(final PlayerAnimationEvent event) {
        // Just set a flag to true when the arm was swung.
        BlockPlaceData.getData(event.getPlayer()).noSwingArmSwung = true;
    }

    /**
     * We listener to PlayerInteract events to prevent players from spamming the server with monster eggs.
     * 
     * @param event
     *            the event
     */
    @EventHandler(ignoreCancelled = false, priority = EventPriority.LOWEST)
    public void onPlayerInteract(final PlayerInteractEvent event) {
        if (event.isCancelled()) {
            // TODO: Might run checks if (event.useInteractedBlock()) ...
            return;
        }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }
        final Player player = event.getPlayer();

        final ItemStack stack = player.getItemInHand();
        if (stack == null) {
            return;
        }

        final Material type = stack.getType();
        if (type == Material.BOAT) {
            // Check boats-anywhere.
            final org.bukkit.block.Block block = event.getClickedBlock();
            final Material mat = block.getType();

            // TODO: allow lava ?
            if (mat == Material.WATER || mat == Material.STATIONARY_WATER) {
                return;
            }

            final org.bukkit.block.Block relBlock = block.getRelative(event.getBlockFace());
            final Material relMat = relBlock.getType();

            // TODO: Placing inside of water, but not "against" ?
            if (relMat == Material.WATER || relMat == Material.STATIONARY_WATER) {
                return;
            }

            // TODO: Add a check type for exemption?
            if (!player.hasPermission(Permissions.BLOCKPLACE_BOATSANYWHERE)) {
                final Result previousUseBlock = event.useInteractedBlock();
                event.setCancelled(true);
                event.setUseItemInHand(Result.DENY);
                event.setUseInteractedBlock(previousUseBlock == Result.DEFAULT ? Result.ALLOW : previousUseBlock);
                counters.addPrimaryThread(idBoatsAnywhere, 1);
            }

        }
        else if (type == Material.MONSTER_EGG) {
            // Check blockplace.speed.
            if (speed.isEnabled(player) && speed.check(player)) {
                // If the check was positive, cancel the event.
                event.setCancelled(true);
            }
        } 
    }

    /**
     * We listen to ProjectileLaunch events to prevent players from launching projectiles too quickly.
     * 
     * @param event
     *            the event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onProjectileLaunch(final ProjectileLaunchEvent event) {
        // The shooter needs to be a player.
        final Projectile projectile = event.getEntity();
        final Player player = BridgeMisc.getShooterPlayer(projectile);
        if (player == null) {
            return;
        }

        // And the projectile must be one the following:
        EntityType type = event.getEntityType();
        switch (type) {
            case ENDER_PEARL:
                break;
            case ENDER_SIGNAL:
                break;
            case EGG:
                break;
            case SNOWBALL:
                break;
            case THROWN_EXP_BOTTLE:
                break;
            case SPLASH_POTION:
                break;
            default:
                return;
        }

        // Do the actual check...
        boolean cancel = false;
        if (speed.isEnabled(player)){
            final long now = System.currentTimeMillis();
            final Location loc = player.getLocation(useLoc);
            if (Combined.checkYawRate(player, loc.getYaw(), now, loc.getWorld().getName())){
                // Yawrate (checked extra).
                cancel = true;
            }
            if (speed.check(player)){
                // If the check was positive, cancel the event.
                cancel = true;
            }
            else if (Improbable.check(player, 0.6f, now, "blockplace.speed")){
                // Combined fighting speed.
                cancel = true;
            }
        }

        // Ender pearl glitch (ab-) use.
        if (!cancel && type == EntityType.ENDER_PEARL){
            if (!CombinedConfig.getConfig(player).enderPearlCheck){
                // Do nothing !
                // TODO: Might have further flags?
            }
            else if (!BlockProperties.isPassable(projectile.getLocation(useLoc))){
                // Launch into a block.
                // TODO: This might be a general check later.       		
                cancel = true;
            }
            else{
                if (!BlockProperties.isPassable(player.getEyeLocation(), projectile.getLocation(useLoc))){
                    // (Spare a useLoc2, for this is seldom rather.)
                    // Something between player 
                    // TODO: This might be a general check later.
                    cancel = true;
                }
                else{
                    final Material mat = player.getLocation(useLoc).getBlock().getType();
                    final long flags = BlockProperties.F_CLIMBABLE | BlockProperties.F_LIQUID | BlockProperties.F_IGN_PASSABLE;
                    if (!BlockProperties.isAir(mat) && (BlockProperties.getBlockFlags(mat) & flags) == 0 && !mcAccess.hasGravity(mat)){
                        // Still fails on piston traps etc.
                        if (!BlockProperties.isPassable(player.getLocation(), projectile.getLocation()) && !BlockProperties.isOnGroundOrResetCond(player, player.getLocation(), MovingConfig.getConfig(player).yOnGround)){
                            cancel = true;
                        }
                    }
                }
            }
            if (cancel) {
                counters.addPrimaryThread(idEnderPearl, 1);
            }
        }

        // Cancelled ?
        if (cancel){
            event.setCancelled(true);
        }
        // Cleanup.
        useLoc.setWorld(null);
    }
}
