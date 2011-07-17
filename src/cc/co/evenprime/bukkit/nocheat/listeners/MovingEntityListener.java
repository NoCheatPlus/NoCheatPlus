package cc.co.evenprime.bukkit.nocheat.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityListener;

import cc.co.evenprime.bukkit.nocheat.DataManager;
import cc.co.evenprime.bukkit.nocheat.checks.MovingCheck;

public class MovingEntityListener extends EntityListener {

	private final MovingCheck check;
	private final DataManager dataManager;

	public MovingEntityListener(DataManager dataManager, MovingCheck check) {
		this.dataManager = dataManager;
		this.check = check;
	}

	@Override
	public void onEntityDamage(EntityDamageEvent event) {
		if(event.getEntity() instanceof Player) {
			check.updateVelocity(event.getEntity().getVelocity(), dataManager.getMovingData((Player)event.getEntity()));
		}
	}
}
