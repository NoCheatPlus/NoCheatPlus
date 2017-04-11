/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.moving.player;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.LocationData;
import fr.neatmonster.nocheatplus.checks.moving.model.PlayerMoveData;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.hooks.NCPExemptionManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;

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
    public static final double getDamage(final float fallDistance) {
        return fallDistance - Magic.FALL_DAMAGE_DIST;
    }

    /**
     * Deal damage if appropriate. To be used for if the player is on ground
     * somehow. Contains checking for skipping conditions (getAllowFlight set +
     * configured to skip).
     * 
     * @param mcPlayer
     * @param data
     * @param y
     */
    private void handleOnGround(final Player player, final double y, final boolean reallyOnGround, final MovingData data, final MovingConfig cc) {
        // Damage to be dealt.
        final double maxD = estimateDamage(player, y, data);
        if (maxD >= 1.0) {
            // Check skipping conditions.
            if (cc.noFallSkipAllowFlight && player.getAllowFlight()) {
                data.clearNoFallData();
                data.noFallSkipAirCheck = true;
                // Not resetting the fall distance here, let Minecraft or the issue tracker deal with that.
            }
            else {
                // TODO: more effects like sounds, maybe use custom event with violation added.
                if (data.debug) {
                    debug(player, "NoFall deal damage" + (reallyOnGround ? "" : "violation") + ": " + maxD);
                }
                // TODO: might not be necessary: if (mcPlayer.invulnerableTicks <= 0)  [no damage event for resetting]
                // TODO: Detect fake fall distance accumulation here as well.
                data.noFallSkipAirCheck = true;
                dealFallDamage(player, maxD);
            }
        }
        else {
            data.clearNoFallData();
            player.setFallDistance(0);
        }
    }

    /**
     * Convenience method to estimate fall damage at a certain y-level, checking data and mc-fall-distance.
     * @param player
     * @param y
     * @param data
     * @return
     */
    public double estimateDamage(final Player player, final double y, final MovingData data) {
        //return getDamage(Math.max((float) (data.noFallMaxY - y), Math.max(data.noFallFallDistance, player.getFallDistance())));
        return getDamage(Math.max((float) (data.noFallMaxY - y), data.noFallFallDistance));
    }

    /**
     * 
     * @param player
     * @param minY
     * @param reallyOnGround
     * @param data
     * @param cc
     */
    private void adjustFallDistance(final Player player, final double minY, final boolean reallyOnGround, final MovingData data, final MovingConfig cc) {
        final float noFallFallDistance = Math.max(data.noFallFallDistance, (float) (data.noFallMaxY - minY));
        if (noFallFallDistance >= Magic.FALL_DAMAGE_DIST) {
            final float fallDistance = player.getFallDistance();
            if (noFallFallDistance - fallDistance >= 0.5f // TODO: Why not always adjust, if greater?
                    || noFallFallDistance >= Magic.FALL_DAMAGE_DIST && fallDistance < Magic.FALL_DAMAGE_DIST // Ensure damage.
                    ) {
                player.setFallDistance(noFallFallDistance);
            }
        }
        data.clearNoFallData();
    }


    private void dealFallDamage(final Player player, final double damage) {
        if (mcAccess.getHandle().dealFallDamageFiresAnEvent().decide()) {
            // TODO: Better decideOptimistically?
            mcAccess.getHandle().dealFallDamage(player, damage);
        }
        else {
            final EntityDamageEvent event = BridgeHealth.getEntityDamageEvent(player, DamageCause.FALL, damage);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                // TODO: account for no damage ticks etc.
                player.setLastDamageCause(event);
                mcAccess.getHandle().dealFallDamage(player, BridgeHealth.getDamage(event));
            }
        }

        // Currently resetting is done from within the damage event handler.
        // TODO: MUST detect if event fired at all (...) and override, if necessary. Best probe once per class (with YES).
        //        data.clearNoFallData();
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
    public void check(final Player player, final PlayerLocation pFrom, final PlayerLocation pTo, final MovingData data, final MovingConfig cc) {

        final PlayerMoveData thisMove = data.playerMoves.getCurrentMove();
        final LocationData from = thisMove.from;
        final LocationData to = thisMove.to;

        final double fromY = from.getY();
        final double toY = to.getY();

        final double yDiff = toY - fromY;

        final double oldNFDist = data.noFallFallDistance;

        // Reset-cond is not touched by yOnGround.
        // TODO: Distinguish water depth vs. fall distance ?
        final boolean fromReset = from.resetCond;
        final boolean toReset = to.resetCond;

        final boolean fromOnGround, toOnGround;
        // Adapt yOnGround if necessary (sf uses another setting).
        if (yDiff < 0 && cc.yOnGround < cc.noFallyOnGround) {
            // In fact this is somewhat heuristic, but it seems to work well.
            // Missing on-ground seems to happen with running down pyramids rather.
            // TODO: Should be obsolete.
            adjustYonGround(pFrom, pTo , cc.noFallyOnGround);
            fromOnGround = pFrom.isOnGround();
            toOnGround = pTo.isOnGround();
        } else {
            fromOnGround = from.onGround;
            toOnGround = to.onGround;
        }

        // TODO: early returns (...) 

        final double minY = Math.min(fromY, toY);

        if (fromReset) {
            // Just reset.
            data.clearNoFallData();
            // Ensure very big/strange moves don't yield violations.
            if (toY - fromY <= -Magic.FALL_DAMAGE_DIST) {
                data.noFallSkipAirCheck = true;
            }
        }
        else if (fromOnGround || !toOnGround && thisMove.touchedGround) {
            // Check if to deal damage (fall back damage check).
            if (cc.noFallDealDamage) {
                handleOnGround(player, minY, true, data, cc);
            }
            else {
                adjustFallDistance(player, minY, true, data, cc);
            }
            // Ensure very big/strange moves don't yield violations.
            if (toY - fromY <= -Magic.FALL_DAMAGE_DIST) {
                data.noFallSkipAirCheck = true;
            }
        }
        else if (toReset) {
            // Just reset.
            data.clearNoFallData();
        }
        else if (toOnGround) {
            // Check if to deal damage.
            if (yDiff < 0) {
                // In this case the player has traveled further: add the difference.
                data.noFallFallDistance -= yDiff;
            }
            if (cc.noFallDealDamage) {
                handleOnGround(player, minY, true, data, cc);
            }
            else {
                adjustFallDistance(player, minY, true, data, cc);
            }
        }
        else {
            // Ensure fall distance is correct, or "anyway"?
        }

        // Set reference y for nofall (always).
        // TODO: Consider setting this before handleOnGround (at least for resetTo).
        data.noFallMaxY = Math.max(Math.max(fromY, toY), data.noFallMaxY);

        // TODO: fall distance might be behind (!)
        // TODO: should be the data.noFallMaxY be counted in ?
        final float mcFallDistance = player.getFallDistance(); // Note: it has to be fetched here.
        // SKIP: data.noFallFallDistance = Math.max(mcFallDistance, data.noFallFallDistance);

        // Add y distance.
        if (!toReset && !toOnGround && yDiff < 0) {
            data.noFallFallDistance -= yDiff;
        }
        else if (cc.noFallAntiCriticals && (toReset || toOnGround || (fromReset || fromOnGround || thisMove.touchedGround) && yDiff >= 0)) {
            final double max = Math.max(data.noFallFallDistance, mcFallDistance);
            if (max > 0.0 && max < 0.75) { // (Ensure this does not conflict with deal-damage set to false.) 
                if (data.debug) {
                    debug(player, "NoFall: Reset fall distance (anticriticals): mc=" + mcFallDistance +" / nf=" + data.noFallFallDistance);
                }
                if (data.noFallFallDistance > 0) {
                    data.noFallFallDistance = 0;
                }
                if (mcFallDistance > 0f) {
                    player.setFallDistance(0f);
                }
            }
        }

        if (data.debug) {
            debug(player, "NoFall: mc=" + mcFallDistance +" / nf=" + data.noFallFallDistance + (oldNFDist < data.noFallFallDistance ? " (+" + (data.noFallFallDistance - oldNFDist) + ")" : "") + " | ymax=" + data.noFallMaxY);
        }

    }

    /**
     * Set yOnGround for from and to, if needed, should be obsolete.
     * @param from
     * @param to
     * @param cc
     */
    private void adjustYonGround(final PlayerLocation from, final PlayerLocation to, final double yOnGround) {
        if (!from.isOnGround()) {
            from.setyOnGround(yOnGround);
        }
        if (!to.isOnGround()) {
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
        // TODO: Might also detect too high mc fall dist.
        if (data.noFallFallDistance > fallDistance) {
            final double playerY = player.getLocation(useLoc).getY();
            useLoc.setWorld(null);
            if (player.isFlying() || player.getGameMode() == GameMode.CREATIVE
                    || player.getAllowFlight() && MovingConfig.getConfig(player).noFallSkipAllowFlight) {
                // Forestall potential issues with flying plugins.
                player.setFallDistance(0f);
                data.noFallFallDistance = 0f;
                data.noFallMaxY = playerY;
            } else {
                // Might use tolerance, might log, might use method (compare: MovingListener.onEntityDamage).
                // Might consider triggering violations here as well.
                final float yDiff = (float) (data.noFallMaxY - playerY);
                final float maxDist = Math.max(yDiff, data.noFallFallDistance);
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
        return cc.noFallCheck && !NCPExemptionManager.isExempted(player, CheckType.MOVING_NOFALL, true) 
                && !player.hasPermission(Permissions.MOVING_NOFALL);
    }

}
