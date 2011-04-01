package cc.co.evenprime.bukkit.nocheat.actions;

public class CancelAction implements Action {

	public static final CancelAction deny = new CancelAction();
	public static final CancelAction reset = new CancelAction();
	
	private CancelAction() { }
}
