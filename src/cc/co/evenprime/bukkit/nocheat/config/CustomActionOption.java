package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.actions.CustomAction;

public class CustomActionOption extends ChildOption {

	private int firstAfter;
	private int repeat;
	private String command;
	
	
	public CustomActionOption(String identifier, String command) {
			
		super(identifier);
		
		this.parseCommand(command);
	}

	
	private void parseCommand(String command) {
		
		if(command.matches("\\[[0-9]*,[0-9]*\\] .*")) {
			String s[] = command.split(" ", 2);
			String s2[] = s[0].replace("[", "").replace("]", "").split(",");
			this.firstAfter = Integer.parseInt(s2[0]);
			this.repeat = Integer.parseInt(s2[1]);
			this.command = s[1];
		}
		else if(command.matches("\\[[0-9]*\\] .*")) {
			String s[] = command.split(" ", 2);
			this.firstAfter = Integer.parseInt(s[0].replace("[", "").replace("]", ""));
			this.repeat = 1;
			this.command = s[1];
		}
		else
		{
			this.command = command;
			this.firstAfter = 1;
			this.repeat = 1;
		}
	}
	
	@Override
	public String getValue() {
		if(firstAfter <= 1) {
			return command;
		}
		else if(repeat <= 0) {
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

	public void setRepeatValue(int value) {
		this.repeat = value;
		
	}

	public int getRepeatValue() {
		return repeat;
	}

	public int getFirstAfterValue() {
		return firstAfter;
	}

	public void setFirsttAfterValue(int value) {
		this.firstAfter = value;
		
	}

}
