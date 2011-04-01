package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.actions.Action;

public class CustomAction implements Action {

	private final int id; 
	private final String command;

	public CustomAction(int id, String command) {
		this.id = id;
		this.command = command;
	}

	public void execute(Player player) {
		// TODO: add command execution
	}
}
