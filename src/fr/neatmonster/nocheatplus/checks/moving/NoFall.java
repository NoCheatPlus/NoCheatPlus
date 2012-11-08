package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;
import java.util.Map;

import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityPlayer;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

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
    private static final void handleOnGround(final EntityPlayer mcPlayer, final MovingData data, final double y, final MovingConfig cc) {
//        final int pD = getDamage(mcPlayer.fallDistance);
//        final int nfD = getDamage(data.noFallFallDistance);
//        final int yD = getDamage((float) (data.noFallMaxY - y));
//        final int maxD = Math.max(Math.max(pD, nfD), yD);
        final int maxD = getDamage(Math.max((float) (data.noFallMaxY - y), Math.max(data.noFallFallDistance, mcPlayer.fallDistance)));
        if (maxD > 0){
            // Damage to be dealt.
            // TODO: more effects like sounds, maybe use custom event with violation added.
            if (cc.debug) System.out.println(mcPlayer.name + " NoFall deal damage: " + maxD);
            final EntityDamageEvent event = new EntityDamageEvent(mcPlayer.getBukkitEntity(), DamageCause.FALL, maxD);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()){
                mcPlayer.damageEntity(DamageSource.FALL, event.getDamage());
            }
            // TODO: let this be done by the damage event (!).
//            data.clearNoFallData(); // -> currently done in the damage eventhandling method.
            mcPlayer.fallDistance = 0;
        }
        else data.clearNoFallData();
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public void check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc) {
	    
	    // Reset the on ground properties only if necessary. 
        if (from.getY() > to.getY()){
        	if (from.getyOnGround() != cc.noFallyOnGround && from.getY() - from.getBlockY() < cc.noFallyOnGround)
        		from.setyOnGround(cc.noFallyOnGround);
        	if (to.getyOnGround() != cc.noFallyOnGround  && to.getY() - to.getBlockY() < cc.noFallyOnGround)
        		to.setyOnGround(cc.noFallyOnGround);
        }
        
        // TODO: Distinguish water depth vs. fall distance!
        
        final boolean fromOnGround = from.isOnGround();
        final boolean fromReset = from.isResetCond();
        final boolean toOnGround = to.isOnGround();
        final boolean toReset = to.isResetCond();
        
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        
        final double yDiff = to.getY() - from.getY();
        // TODO: early returns (...) 
        
        if (fromReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (fromOnGround || data.noFallAssumeGround){
            // Check if to deal damage (fall back damage check).
            if (cc.noFallDealDamage) handleOnGround(mcPlayer, data, from.getY(), cc);
            else{
                mcPlayer.fallDistance = Math.max(mcPlayer.fallDistance, Math.max(data.noFallFallDistance, (float) (data.noFallMaxY - from.getY())));
                data.clearNoFallData();
            }
        }
        else if (toReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (toOnGround){
            // Check if to deal damage.
            if (yDiff < 0) data.noFallFallDistance -= yDiff;
            if (cc.noFallDealDamage) handleOnGround(mcPlayer, data, to.getY(), cc);
            else{
                mcPlayer.fallDistance = Math.max(mcPlayer.fallDistance, Math.max(data.noFallFallDistance, (float) (data.noFallMaxY - to.getY())));
                data.clearNoFallData();
            }
        }
        else{
            // Ensure fall distance is correct ? or anyway !
        }
        
        // Set reference y for nofall (always).
        data.noFallMaxY = Math.max(Math.max(from.getY(), to.getY()), data.noFallMaxY);
        
        // TODO: fall distance might be behind (!)
        // TODO: should be the data.noFallMaxY be counted in ?
        if (cc.debug) System.out.println(player.getName() + " NoFall: mc="+mcPlayer.fallDistance +" / nf=" + data.noFallFallDistance);
        data.noFallFallDistance = Math.max(mcPlayer.fallDistance, data.noFallFallDistance);
        
        // Add y distance.
        if (!toReset && !toOnGround && yDiff < 0){
            data.noFallFallDistance -= yDiff;
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

    @Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.FALL_DISTANCE, String.format(Locale.US, "%.2f", MovingData.getData(violationData.player).noFallFallDistance));
		return parameters;
	}
	
}
