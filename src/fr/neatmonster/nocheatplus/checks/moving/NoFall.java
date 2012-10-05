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
        return 1 + (int) (fallDistance - 3.5);
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
        final int maxD = getDamage(Math.max(mcPlayer.fallDistance, Math.max(data.noFallFallDistance, (float) (data.noFallMaxY - y))));
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
    public void check(final Player player, final MovingData data, final MovingConfig cc) {
	   	final PlayerLocation from = data.from;
	    final PlayerLocation to = data.to;
	    
	    // Reset the on ground properties only if necessary. 
        if (from.getY() > to.getY()){
        	if (from.getyOnGround() != cc.noFallyOnGround && from.getY() - from.getBlockY() < cc.noFallyOnGround)
        		from.setyOnGround(cc.noFallyOnGround);
        	if (to.getyOnGround() != cc.noFallyOnGround  && to.getY() - to.getBlockY() < cc.noFallyOnGround)
        		to.setyOnGround(cc.noFallyOnGround);
        }
        
        // TODO: Distinguish water depth vs. fall distance!
        
        final boolean fromOnGround = from.isOnGround();
        final boolean fromReset = from.isInLiquid() || from.isInWeb() || from.isOnLadder();
        final boolean toOnGround = to.isOnGround();
        final boolean toReset = to.isInLiquid() || to.isInWeb() || to.isOnLadder();
        
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        
        // TODO: early returns (...) 
        
        if (fromReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (fromOnGround){
            // Check if to deal damage (fall back damage check).
            if (cc.noFallDealDamage) handleOnGround(mcPlayer, data, from.getY(), cc);
            else data.clearNoFallData();
        }
        else if (toReset){
            // Just reset.
            data.clearNoFallData();
        }
        else if (toOnGround){
            // Check if to deal damage.
            if (cc.noFallDealDamage) handleOnGround(mcPlayer, data, to.getY(), cc);
            else data.clearNoFallData();
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
        
        final double yDiff = to.getY() - from.getY();
        // Add y distance.
        if (!toReset && !toOnGround && yDiff < 0){
            data.noFallFallDistance -= yDiff;
        }
        
//        // OLD ----------------------------------------
//        
//        
//        data.noFallWasOnGround = data.noFallOnGround;
//        data.noFallOnGround = to.isOnGround();
//
//        // If the player is on the ground, is falling into a liquid, in web or is on a ladder.
//        if (from.isOnGround() && to.isOnGround() || to.isInLiquid() || to.isInWeb() || to.isOnLadder())
//            data.noFallFallDistance = 0;
//        
//        // If the player just touched the ground for the server.
//        if (data.noFallFallDistance > 3.5){
//            
//            
//            
//            if (!data.noFallWasOnGround && data.noFallOnGround) {
//                // If the difference between the fall distance recorded by Bukkit and NoCheatPlus is too big and the fall
//                // distance bigger than 2.
//                
//                // TODO: 3.5 ?
//                if (data.noFallFallDistance - player.getFallDistance() > 0.1D) {
//                    // Add the difference to the violation level.
//                    data.noFallVL += data.noFallFallDistance - player.getFallDistance();
//
//                    // Execute the actions to find out if we need to cancel the event or not.
//                    if (executeActions(player, data.noFallVL, data.noFallFallDistance - player.getFallDistance(),
//                            cc.noFallActions))
//                        // Set the fall distance to its right value.
//                        if (cc.noFallDealDamage){
//                            // TODO: round ?
//                            ((CraftPlayer) player).getHandle().damageEntity(DamageSource.FALL, 1 + (int) (data.noFallFallDistance - 3.5));
//                            data.clearNoFallData();
//                        }
//                        else player.setFallDistance((float) data.noFallFallDistance);
//                } else
//                    // Reward the player by lowering his violation level.
//                    data.noFallVL *= 0.95D;
//            } else{
//                // Reward the player by lowering his violation level.
//                data.noFallVL *= 0.95D;
//                if (cc.noFallDealDamage && data.noFallOnGround){
//                 // TODO: round ?
//                    ((CraftPlayer) player).getHandle().damageEntity(DamageSource.FALL, 1 + (int) (data.noFallFallDistance - 3.5));
//                    data.clearNoFallData();
//                }
//            }
//        }
//        else data.noFallVL *= 0.95D;
//
//        // The player has touched the ground somewhere, reset his fall distance.
//        if (!data.noFallWasOnGround && data.noFallOnGround || data.noFallWasOnGround && !data.noFallOnGround)
//            data.noFallFallDistance = 0;
//
//        if (to.getY() > 0 && from.getY() > to.getY())
//            data.noFallFallDistance += from.getY() - to.getY();
    }

    @Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		final Map<ParameterName, String> parameters = super.getParameterMap(violationData);
		parameters.put(ParameterName.FALL_DISTANCE, String.format(Locale.US, "%.2f", MovingData.getData(violationData.player).noFallFallDistance));
		return parameters;
	}
	
}
