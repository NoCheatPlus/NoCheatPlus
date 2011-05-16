package cc.co.evenprime.bukkit.nocheat.actions;

/**
 * 
 * @author Evenprime
 *
 */

public class CustomAction extends Action {

	public final String command;

	public CustomAction(int firstAfter, boolean repeat, String command) {
		super(firstAfter, repeat);
		this.command = command;
	}

	public String getName() {
		return "custom";
	}
	
	public String getValue() {
		if(firstAfter <= 1 && repeat) {
			return command;
		}
		else if(repeat) {
			return "["+firstAfter+"] "+ command;
		}
		else {
			return "["+firstAfter+","+repeat+"] "+ command;
		}
	}
}
