package fr.neatmonster.nocheatplus.checks.blockinteract;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.InteractRayTracing;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class Visible extends Check {

    private BlockCache blockCache;

    /**
     * Strict set to false, due to false positives.
     */
    private final InteractRayTracing rayTracing = new InteractRayTracing(false);

    private final List<String> tags = new ArrayList<String>();

    public Visible() {
        super(CheckType.BLOCKINTERACT_VISIBLE);
        blockCache = mcAccess.getBlockCache(null);
        rayTracing.setMaxSteps(60); // TODO: Configurable ?
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#setMCAccess(fr.neatmonster.nocheatplus.compat.MCAccess)
     */
    @Override
    public void setMCAccess(MCAccess mcAccess) {
        super.setMCAccess(mcAccess);
        // Renew the BlockCache instance.
        blockCache = mcAccess.getBlockCache(null);
    }

    public boolean check(final Player player, final Location loc, final Block block, final BlockFace face, final Action action, final BlockInteractData data, final BlockInteractConfig cc) {
        // TODO: This check might make parts of interact/blockbreak/... + direction (+?) obsolete.
        // TODO: Might confine what to check for (left/right-click, target blocks depending on item in hand, container blocks).
        final boolean collides;
        final int blockX = block.getX();
        final int blockY = block.getY();
        final int blockZ = block.getZ();
        final double eyeX = loc.getX();
        final double eyeY = loc.getY() + player.getEyeHeight();
        final double eyeZ = loc.getZ();

        tags.clear();
        if (TrigUtil.isSameBlock(blockX, blockY, blockZ, eyeX, eyeY, eyeZ)) {
            // Player is interacting with the block their head is in.
            // TODO: Should the reachable-face-check be done here too (if it is added at all)?
            collides = false;
        }
        else{
            // Ray-tracing.
            final Vector direction = loc.getDirection();
            // Initialize.
            blockCache.setAccess(loc.getWorld());
            rayTracing.setBlockCache(blockCache);
            collides = checkRayTracing(eyeX, eyeY, eyeZ, direction.getX(), direction.getY(), direction.getZ(), blockX, blockY, blockZ, face, tags);
            // Cleanup.
            rayTracing.cleanup();
            blockCache.cleanup();
        }

        //        if (data.debug) {//  && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
        //            // TODO: Log more useful information to the trace file instead, probably depending on the block type?
        //            // TODO: Tags
        //            final float moveYaw = MovingData.getData(player).toYaw;
        //            String refYaw = "";
        //            if (moveYaw != loc.getYaw()) {
        //                refYaw = " (moved-yaw=" + moveYaw + ")";
        //            }
        //            player.sendMessage("Interact visible: " + (action == Action.RIGHT_CLICK_BLOCK ? "right" : "left") + " yaw=" + loc.getYaw() + refYaw + " pitch=" + loc.getPitch() + " collide=" + collides);
        //        }

        // Actions ?
        boolean cancel = false;
        if (collides){
            data.visibleVL += 1;
            final ViolationData vd = new ViolationData(this, player, data.visibleVL, 1, cc.visibleActions);
            if (data.debug || vd.needsParameters()) {
                // TODO: Consider adding the start/end/block-type information if debug is set.
                vd.setParameter(ParameterName.TAGS, StringUtil.join(tags, "+"));
            }
            if (executeActions(vd)){
                cancel = true;
            }
        }
        else{
            data.visibleVL *= 0.99;
        }

        return cancel;
    }

    private boolean checkRayTracing(final double eyeX, final double eyeY, final double eyeZ, final double dirX, final double dirY, final double dirZ, final int blockX, final int blockY, final int blockZ, final BlockFace face, final List<String> tags){
        // Block of eyes.
        final int eyeBlockX = Location.locToBlock(eyeX);
        final int eyeBlockY = Location.locToBlock(eyeY);
        final int eyeBlockZ = Location.locToBlock(eyeZ);
        // Distance in blocks from eyes to clicked block.
        final int bdX = blockX - eyeBlockX;
        final int bdY = blockY - eyeBlockY;
        final int bdZ = blockZ - eyeBlockZ;

        // Coarse orientation check.
        if (bdX != 0 && dirX * bdX <= 0.0 || bdY != 0 && dirY * bdY <= 0.0 || bdZ != 0 && dirZ * bdZ <= 0.0) {
            // TODO: There seem to be false positives, do add debug logging with/before violation handling.
            tags.add("coarse_orient");
            return true;
        }

        // TODO: If medium strict, check if the given BlockFace seems acceptable.

        // Time windows for coordinates passing through the target block.
        final double tMinX = getMinTime(eyeX, eyeBlockX, dirX, bdX);
        final double tMinY = getMinTime(eyeY, eyeBlockY, dirY, bdY);
        final double tMinZ = getMinTime(eyeZ, eyeBlockZ, dirZ, bdZ);
        final double tMaxX = getMaxTime(eyeX, eyeBlockX, dirX, tMinX);
        final double tMaxY = getMaxTime(eyeY, eyeBlockY, dirY, tMinY);
        final double tMaxZ = getMaxTime(eyeZ, eyeBlockZ, dirZ, tMinZ);

        // Check if the the block is hit by the direction at all (timing interval).
        if (tMinX > tMaxY && tMinX > tMaxZ || 
                tMinY > tMaxX && tMinY > tMaxZ || 
                tMinZ > tMaxX && tMaxZ > tMaxY) {
            // TODO: Option to tolerate a minimal difference in t and use a corrected position then.
            tags.add("block_miss");
//            Bukkit.getServer().broadcastMessage("visible: " + tMinX + "," + tMaxX + " | " + tMinY + "," + tMaxY + " | " + tMinZ + "," + tMaxZ);
            return true;
        }

        // Point of time of collision.
        final double tCollide = Math.max(tMinX, Math.max(tMinY, tMinZ));

        // Collision location (corrected to be on the clicked block).
        double collideX = toBlock(eyeX + dirX * tCollide, blockX);
        double collideY = toBlock(eyeY + dirY * tCollide, blockY);
        double collideZ = toBlock(eyeZ + dirZ * tCollide, blockZ);
        // Correct the last-on-block to be on the edge (could be two).
        if (tMinX == tCollide) {
            collideX = Math.round(collideX);
        }
        if (tMinY == tCollide) {
            collideY = Math.round(collideY);
        }
        if (tMinZ == tCollide) {
            collideZ = Math.round(collideZ);
        }
        /*
         * TODO: Still false positives on transitions between blocks. Could
         * correcting towards the eye location rather than just rounding solve
         * it?
         */

        // Perform ray-tracing.
        rayTracing.set(eyeX, eyeY, eyeZ, collideX, collideY, collideZ, blockX, blockY, blockZ);
        rayTracing.loop();
        if (rayTracing.collides()) {
            tags.add("raytracing");
            return true;
        }
        else if (rayTracing.getStepsDone() > rayTracing.getMaxSteps()) {
            tags.add("raytracing_maxsteps");
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Time until on the block (time = steps of dir).
     * @param eye
     * @param eyeBlock
     * @param dir
     * @param blockDiff
     * @return
     */
    private final double getMinTime(final double eye, final int eyeBlock, final double dir, final int blockDiff) {
        if (blockDiff == 0) {
            // Already on the block.
            return 0.0;
        }
        // Calculate the time needed to be on the (close edge of the block coordinate).
        final double eyeOffset = Math.abs(eye - eyeBlock); // (abs not needed)
        return ((dir < 0.0 ? eyeOffset : 1.0 - eyeOffset) + (double) (Math.abs(blockDiff) - 1)) / Math.abs(dir);
    }

    /**
     * Time when not on the block anymore (after having hit it, time = steps of dir).
     * @param eye
     * @param eyeBlock
     * @param dir
     * @param blockDiff
     * @param tMin Result of getMinTime for this coordinate.
     * @return
     */
    private final double getMaxTime(final double eye, final int eyeBlock, final double dir, final double tMin) {
        if (dir == 0.0) {
            // Always on (blockDiff == 0 as well).
            return Double.MAX_VALUE;
        }
        if (tMin == 0.0) {
            //  Already on the block, return "rest on block".
            final double eyeOffset = Math.abs(eye - eyeBlock); // (abs not needed)
            return (dir < 0.0 ? eyeOffset : 1.0 - eyeOffset) / Math.abs(dir);
        }
        // Just the time within range.
        return tMin + 1.0 /  Math.abs(dir);
    }
    
    /**
     * Correct the coordinate to be on the block (only if outside, for
     * correcting inside-block to edge tMin has to be checked.
     * 
     * @param coord
     * @param block
     * @return
     */
    private final double toBlock(final double coord, final int block) {
        final int blockDiff = block - Location.locToBlock(coord);
        if (blockDiff == 0) {
            return coord;
        }
        else {
            return Math.round(coord);
        }
    }

}
