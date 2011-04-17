package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;

public class MovingEntityListener extends EntityListener {

	private MovingCheck check;
	
	public MovingEntityListener(MovingCheck check) {
		this.check = check;
	}
	
	
	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			MovingCheck.updateVelocity(event.getEntity().getVelocity(), NoCheatData.getPlayerData((Player)event.getEntity()));
		}
		
	}
}
