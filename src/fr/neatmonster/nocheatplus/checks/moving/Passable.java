package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Map;

import org.bukkit.Location;
import org.bukkit.entity.Player;

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
		final int toId = to.getTypeId();
		if (!BlockProperties.isPassable(to.getBlockAccess(), to.getX(), to.getY(), to.getZ(), toId)){
			// Allow moving into the same block.
			if (from.isSameBlock(to)){
				if (!BlockProperties.isPassable(from.getBlockAccess(), from.getX(), from.getY(), from.getZ(), from.getTypeId())) return null;
			}
			// Return the reset position.
			data.passableVL += 1d;
			final ViolationData vd = new ViolationData(this, player, data.passableVL, 1, cc.passableActions);
			if (vd.needsParameters()) vd.setParameter(ParameterName.BLOCK_ID, "" + toId);
			if (executeActions(vd)){
				final Location newTo = from.getLocation();
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
