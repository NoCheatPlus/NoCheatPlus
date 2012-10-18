package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

public class Passable extends Check {

	public Passable() {
		super(CheckType.MOVING_PASSABLE);
	}
	
	public Location check(final Player player, final PlayerLocation from, final PlayerLocation to, final MovingData data, final MovingConfig cc){
		// Simple check.
		if (!to.isPassable()){
		    Location loc = null; // players location if should be used.
			// Allow moving into the same block.
			if (from.isSameBlock(to)){
				if (!from.isPassable()){
				    final double eyeY = to.getY() + player.getEyeHeight();
				    final int eyeBlockY = Location.locToBlock(eyeY);
				    if (eyeBlockY != to.getBlockY()){
				        if (BlockProperties.isPassable(to.getBlockAccess(), to.getX(), eyeY, to.getZ(), to.getTypeId(to.getBlockX(), eyeBlockY, to.getBlockZ()))){
				            // Allow moving inside the same block if head is free.
				            return null;
				        }
				    }
				    // Only allow moving further out of the block (still allows going round in circles :p)
				    // TODO: account for actual bounding box.
				    final Vector blockMiddle = new Vector(0.5 + from.getBlockX(), 0.5 + from.getBlockY(), 0.5 + from.getBlockZ());
				    // TODO: Allow moving out of one block towards non-solid blocks (closest only ?).
				    // TODO: Allow moving out of half steps ?
				    // TODO: Allow moving towards non solid blocks.
				    if (blockMiddle.distanceSquared(from.getVector()) < blockMiddle.distanceSquared(to.getVector())) {
				        // Further check for the players location as possible set back.
				        loc = player.getLocation();
				        if (to.isSamePos(loc) ){
				            loc = null;
				        }
				        else if (!BlockProperties.isPassable(from.getBlockAccess(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()))){
				            // Allow the move
				            return null;
				        }
				        // else is passable: use the location instead of from.
				    }
				}
			}
			// Return the reset position.
			data.passableVL += 1d;
			final ViolationData vd = new ViolationData(this, player, data.passableVL, 1, cc.passableActions);
			if (vd.needsParameters()) vd.setParameter(ParameterName.BLOCK_ID, "" + to.getTypeId());
			if (executeActions(vd)){
			    // TODO: Consider another set back position for this, also keeping track of players moving around in blocks.
				final Location newTo;
				if (!from.isPassable() && loc == null){
				    // Check if passable.
				    loc = player.getLocation();
				    if (to.isSamePos(loc) || !BlockProperties.isPassable(from.getBlockAccess(), loc.getX(), loc.getY(), loc.getZ(), from.getTypeId(from.getBlockX(), from.getBlockY(), from.getBlockZ()))){
				        loc = null;
				    }
				}
				if (loc != null) newTo = loc;
				else newTo = from.getLocation();
				newTo.setYaw(to.getYaw());
				newTo.setPitch(to.getPitch());
				return newTo;
			}		
		}
		else{
			data.passableVL *= 0.99;
		}
		return null;
	}

	@Override
	protected Map<ParameterName, String> getParameterMap(final ViolationData violationData) {
		// TODO Auto-generated method stub
		return super.getParameterMap(violationData);
	}
	
	

}
