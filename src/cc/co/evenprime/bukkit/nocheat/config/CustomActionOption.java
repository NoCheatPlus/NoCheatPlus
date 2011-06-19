package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;

public class CustomActionOption extends ChildOption {

	private int firstAfter;
	private boolean repeat;
	private String command;


	public CustomActionOption(String identifier, String command) {

		super(identifier);

		this.parseCommand(command);
	}

	private void parseCommand(String com) {

		if(com.matches("\\[[0-9]*,[a-z]*\\] .*")) {
			String s[] = com.split(" ", 2);
			String s2[] = s[0].replace("[", "").replace("]", "").split(",");
			firstAfter = Integer.parseInt(s2[0]);
			repeat = Boolean.parseBoolean(s2[1]);
			command = s[1];
		}
		else if(com.matches("\\[[0-9]*\\] .*")) {
			String s[] = com.split(" ", 2);
			firstAfter = Integer.parseInt(s[0].replace("[", "").replace("]", ""));
			repeat = true;
			command = s[1];
		}
		else
		{
			firstAfter = 1;
			repeat = true;
			command = com;
		}
	}

	@Override
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


	public CustomAction getCustomActionValue() {
		return new CustomAction(firstAfter, repeat, command);		
	}

	public String getCommandValue() {
		return command;
	}

	public void setCommandValue(String command) {
		this.command = command;
	}

	public void setRepeatValue(boolean value) {
		this.repeat = value;

	}

	public boolean getRepeatValue() {
		return repeat;
	}

	public int getFirstAfterValue() {
		return firstAfter;
	}

	public void setFirsttAfterValue(int value) {
		this.firstAfter = value;

	}

}
