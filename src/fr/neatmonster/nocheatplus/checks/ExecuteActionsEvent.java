package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This event is to be fired to execute actions in the main thread.
 * @author mc_dev
 *
 */
public class ExecuteActionsEvent extends Event {
	
	private static final HandlerList handlers = new HandlerList();

	private final Check check;
	final Player player;
	/**
	 * If the actions have been executed already.
	 */
	private boolean actionsExecuted = false;
	private boolean cancel = false;
	
	public ExecuteActionsEvent(final Check check, final Player player){
		this.check = check;
		this.player = player;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	/**
	 * Must have :_) ...
	 * @return
	 */
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	public void executeActions(){
		if (actionsExecuted) return;
		cancel = check.executeActions(player);
		actionsExecuted = true;
	}
	
	public boolean getCancel(){
		return cancel;
	}

}
