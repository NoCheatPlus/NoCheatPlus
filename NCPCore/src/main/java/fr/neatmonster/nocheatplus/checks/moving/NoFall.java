package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * A check to see if people cheat by tricking the server to not deal them fall damage.
 */
public class NoFall extends Check {

    /** For temporary use: LocUtil.clone before passing deeply, call setWorld(null) after use. */
    private final Location useLoc = new Location(null, 0, 0, 0);

    /**
     * Instantiates a new no fall check.
     */
    public NoFall() {
        super(CheckType.MOVING_NOFALL);
    }

    /**
     * Calculate the damage in hearts from the given fall distance.
     * @param fallDistance
     * @return
     */
    protected static final double getDamage(final float fallDistance){
        return fallDistance - 3.0;
    }

    /**
     * Deal damage if appropriate. To be used for if the player is on ground somehow.
     * @param mcPlayer
     * @param data
     * @param y
     */
    private final void handleOnGround(final Player player, final double y, final boolean reallyOnGround, final MovingData data, final MovingConfig cc) {
        //        final int pD = getDamage(mcPlayer.fallDistance);
        //        final int nfD = getDamage(data.noFallFallDistance);
        //        final int yD = getDamage((float) (data.noFallMaxY - y));
        //        final int maxD = Math.max(Math.max(pD, nfD), yD);
        final double maxD = estimateDamage(player, y, data);
        if (maxD >= 1.0){
            // Damage to be dealt.
            // TODO: more effects like sounds, maybe use custom event with violation added.
            if (data.debug) {
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " NoFall deal damage" + (reallyOnGround ? "" : "violation") + ": " + maxD);
            }
            // TODO: might not be necessary: if (mcPlayer.invulnerableTicks <= 0)  [no damage event for resetting]
            data.noFallSkipAirCheck = true;
            dealFallDamage(player, maxD);
        }
        else data.clearNoFallData();
    }

    /**
     * Convenience method to estimate fall damage at a certain y-level, checking data and mc-fall-distance.
     * @param player
     * @param y
     * @param data
     * @return
     */
    public double estimateDamage(final Player player, final double y, final MovingData data) {
        return getDamage(Math.max((float) (data.noFallMaxY - y), Math.max(data.noFallFallDistance, player.getFallDistance())));
    }

    private final void adjustFallDistance(final Player player, final double minY, final boolean reallyOnGround, final MovingData data, final MovingConfig cc) {
        final float noFallFallDistance = Math.max(data.noFallFallDistance, (float) (data.noFallMaxY - minY));
        if (noFallFallDistance >= 3.0){
            final float fallDistance = player.getFallDistance();
            if (noFallFallDistance - fallDistance >= 0.5f || noFallFallDistance >= 3.5f && noFallFallDistance < 3.5f){
                player.setFallDistance(noFallFallDistance);
            }
        }
        data.clearNoFallData();
    }


    private void dealFallDamage(final Player player, final double damage) {
        if (mcAccess.dealFallDamageFiresAnEvent().decide()) {
            // TODO: Better decideOptimistically?
            mcAccess.dealFallDamage(player, damage);
        }
        else {
            final EntityDamageEvent event = BridgeHealth.getEntityDamageEvent(player, DamageCause.FALL, damage);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                // TODO: account for no damage ticks etc.
                player.setLastDamageCause(event);
                mcAccess.dealFallDamage(player, BridgeHealth.getDamage(event));
            }
        }

        // TODO: let this be done by the damage event (!).
        //        data.clearNoFallData(); // -> currently done in the damage eventhandling method.
        player.setFallDistance(0);
    }

    /**
     * Checks a player. Expects from and to using cc.yOnGround.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public void check(final Player player, final Location loc, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {

        final double fromY = from.getY();
        final double toY = to.getY();

        // TODO: account for player.getLocation.getY (how exactly ?)
        final double yDiff = toY - fromY;

        final double oldNFDist = data.noFallFallDistance;

        // Reset-cond is not touched by yOnGround.
        // TODO: Distinguish water depth vs. fall distance ?
        final boolean fromReset = from.isResetCond();
        final boolean toReset = to.isResetCond();

        // Adapt yOnGround if necessary (sf uses another setting).
        if (yDiff < 0 && cc.yOnGround < cc.noFallyOnGround) {
            // In fact this is somewhat heuristic, but it seems to work well.
            // Missing on-ground seems to happen with running down pyramids rather.
            // TODO: Should be obsolete.
            adjustYonGround(from, to , cc.noFallyOnGround);
        }

        final boolean fromOnGround = from.isOnGround();
        final boolean toOnGround = to.isOnGround();


        // TODO: early returns (...) 

        final double pY =  loc.getY();
        final double minY = Math.min(fromY, Math.min(toY, pY));

        if (fromReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (fromOnGround || data.noFallAssumeGround){
            // Check if to deal damage (fall back damage check).
            if (cc.noFallDealDamage) handleOnGround(player, minY, true, data, cc);
            else adjustFallDistance(player, minY, true, data, cc);
        }
        else if (toReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (toOnGround){
            // Check if to deal damage.
            if (yDiff < 0){
                // In this case the player has traveled further: add the difference.
                data.noFallFallDistance -= yDiff;
            }
            if (cc.noFallDealDamage) handleOnGround(player, minY, true, data, cc);
            else adjustFallDistance(player, minY, true, data, cc);
        }
        else{
            // Ensure fall distance is correct, or "anyway"?
        }

        // Set reference y for nofall (always).
        // TODO: Consider setting this before handleOnGround (at least for resetTo).
        data.noFallMaxY = Math.max(Math.max(fromY, Math.max(toY, pY)), data.noFallMaxY);

        // TODO: fall distance might be behind (!)
        // TODO: should be the data.noFallMaxY be counted in ?
        final float mcFallDistance = player.getFallDistance(); // Note: it has to be fetched here.
        data.noFallFallDistance = Math.max(mcFallDistance, data.noFallFallDistance);

        // Add y distance.
        if (!toReset && !toOnGround && yDiff < 0){
            data.noFallFallDistance -= yDiff;
        }
        else if (cc.noFallAntiCriticals && (toReset || toOnGround || (fromReset || fromOnGround || data.noFallAssumeGround) && yDiff >= 0)){
            final double max = Math.max(data.noFallFallDistance, mcFallDistance);
            if (max > 0.0 && max < 0.75){ // (Ensure this does not conflict with deal-damage set to false.) 
                if (data.debug){
                    NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " NoFall: Reset fall distance (anticriticals): mc=" + mcFallDistance +" / nf=" + data.noFallFallDistance);
                }
                if (data.noFallFallDistance > 0){
                    data.noFallFallDistance = 0;
                }
                if (mcFallDistance > 0){
                    player.setFallDistance(0);
                }
            }
        }

        if (data.debug){
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, player.getName() + " NoFall: mc=" + mcFallDistance +" / nf=" + data.noFallFallDistance + (oldNFDist < data.noFallFallDistance ? " (+" + (data.noFallFallDistance - oldNFDist) + ")" : "") + " | ymax=" + data.noFallMaxY);
        }

    }

    /**
     * Set yOnGround for from and to, if needed, should be obsolete.
     * @param from
     * @param to
     * @param cc
     */
    private void adjustYonGround(final PlayerLocation from, final PlayerLocation to, final double yOnGround) {
        if (!from.isOnGround()){
            from.setyOnGround(yOnGround);
        }
        if (!to.isOnGround()){
            to.setyOnGround(yOnGround);
        }
    }

    /**
     * Quit or kick: adjust fall distance if necessary.
     * @param player
     */
    public void onLeave(final Player player) {
        final MovingData data = MovingData.getData(player);
        final float fallDistance = player.getFallDistance();
        if (data.noFallFallDistance - fallDistance > 0.0) {
            final double playerY = player.getLocation(useLoc).getY();
            useLoc.setWorld(null);
            if (player.getAllowFlight() || player.isFlying() || player.getGameMode() == GameMode.CREATIVE) {
                // Forestall potential issues with flying plugins.
                player.setFallDistance(0f);
                data.noFallFallDistance = 0f;
                data.noFallMaxY = playerY;
            } else {
                // Might use tolerance, might log, might use method (compare: MovingListener.onEntityDamage).
                // Might consider triggering violations here as well.
                final float yDiff = (float) (data.noFallMaxY - playerY);
                final float maxDist = Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance));
                player.setFallDistance(maxDist);
            }
        }
    }

    /**
     * This is called if a player fails a check and gets set back, to avoid using that to avoid fall damage the player might be dealt damage here. 
     * @param player
     * @param data
     */
    public void checkDamage(final Player player, final MovingData data, final double y) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        // Deal damage.
        handleOnGround(player, y, false, data, cc);
    }

    /**
     * Convenience method bypassing the factories.
     * @param player
     * @param cc
     * @return
     */
    public boolean isEnabled(final Player player , final MovingConfig cc) {
        return cc.noFallCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_NOFALL) && !player.hasPermission(Permissions.MOVING_NOFALL);
    }

}
