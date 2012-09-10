package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

/**
 * 
 * @author mc_dev
 *
 */
public class CombinedListener implements Listener {
	
	protected final Improbable improbable;

	public CombinedListener(){
		this.improbable = new Improbable();
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerToggleSneak(final PlayerToggleSneakEvent event){
		// Check also in case of cancelled events.
		if (Improbable.check(event.getPlayer(), 0.35f, System.currentTimeMillis())) event.setCancelled(true);
	}
	
	@EventHandler(priority=EventPriority.LOW)
	public void onPlayerToggleSprint(final PlayerToggleSprintEvent event){
		// Check also in case of cancelled events.
		if (Improbable.check(event.getPlayer(), 0.35f, System.currentTimeMillis())) event.setCancelled(true);
	}
	
	// (possibly other types of events, but these combine with fighting).
}
