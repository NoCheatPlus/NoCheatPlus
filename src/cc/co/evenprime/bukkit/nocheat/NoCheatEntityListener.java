package cc.co.evenprime.bukkit.nocheat;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityListener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class NoCheatEntityListener extends EntityListener {

	@Override
	public void onEntityDeath(EntityDeathEvent event) {
		
		if(NoCheatConfiguration.dupebydeathCheckActive) {
			DupePrevention.playerDeath(event);
		}
	}
}
