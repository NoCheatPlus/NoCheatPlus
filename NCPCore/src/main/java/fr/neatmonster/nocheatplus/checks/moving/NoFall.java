package fr.neatmonster.nocheatplus.checks.moving;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/*
 * M"""""""`YM          MM""""""""`M          dP dP 
 * M  mmmm.  M          MM  mmmmmmmM          88 88 
 * M  MMMMM  M .d8888b. M'      MMMM .d8888b. 88 88 
 * M  MMMMM  M 88'  `88 MM  MMMMMMMM 88'  `88 88 88 
 * M  MMMMM  M 88.  .88 MM  MMMMMMMM 88.  .88 88 88 
 * M  MMMMM  M `88888P' MM  MMMMMMMM `88888P8 dP dP 
 * MMMMMMMMMMM          MMMMMMMMMMMM                
 */
/**
 * A check to see if people cheat by tricking the server to not deal them fall damage.
 */
public class NoFall extends Check {

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
    protected static final int getDamage(final float fallDistance){
        return (int) Math.round(fallDistance - 3.0);
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
        final int maxD = getDamage(Math.max((float) (data.noFallMaxY - y), Math.max(data.noFallFallDistance, player.getFallDistance())));
        if (maxD > 0){
            // Damage to be dealt.
            // TODO: more effects like sounds, maybe use custom event with violation added.
            if (cc.debug) System.out.println(player.getName() + " NoFall deal damage" + (reallyOnGround ? "" : "violation") + ": " + maxD);
            // TODO: might not be necessary: if (mcPlayer.invulnerableTicks <= 0)  [no damage event for resetting]
            data.noFallSkipAirCheck = true;
			dealFallDamage(player, maxD);
        }
        else data.clearNoFallData();
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


    private void dealFallDamage(final Player player, final int damage) {
    	final EntityDamageEvent event = new EntityDamageEvent(player, DamageCause.FALL, damage);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()){
        	// TODO: account for no damage ticks etc.
        	player.setLastDamageCause(event);
            mcAccess.dealFallDamage(player, event.getDamage());
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
                if (cc.debug){
                	System.out.println(player.getName() + " NoFall: Reset fall distance (anticriticals): mc=" + StringUtil.fdec3.format(mcFallDistance) +" / nf=" + StringUtil.fdec3.format(data.noFallFallDistance) );
                }
            	if (data.noFallFallDistance > 0){
            		data.noFallFallDistance = 0;
            	}
            	if (mcFallDistance > 0){
            		player.setFallDistance(0);
        		}
        	}
        }
        
        if (cc.debug){
        	System.out.println(player.getName() + " NoFall: mc=" + StringUtil.fdec3.format(mcFallDistance) +" / nf=" + StringUtil.fdec3.format(data.noFallFallDistance) + (oldNFDist < data.noFallFallDistance ? " (+" + StringUtil.fdec3.format(data.noFallFallDistance - oldNFDist) + ")" : "") + " | ymax=" + StringUtil.fdec3.format(data.noFallMaxY));
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
        if (data.noFallFallDistance - fallDistance > 0){
            // Might use tolerance, might log, might use method (compare: MovingListener.onEntityDamage).
            // Might consider triggering violations here as well.
            final float yDiff = (float) (data.noFallMaxY - player.getLocation().getY());
            final float maxDist = Math.max(yDiff, Math.max(data.noFallFallDistance, fallDistance));
            player.setFallDistance(maxDist);
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
	
}
