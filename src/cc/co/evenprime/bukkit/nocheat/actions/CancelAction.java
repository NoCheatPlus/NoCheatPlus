package cc.co.evenprime.bukkit.nocheat.actions;

/**
 * 
 * @author Evenprime
 *
 */
public class CancelAction extends Action {
	
	public final static CancelAction cancel = new CancelAction();
	
	private CancelAction() { super(1, true); }
	
	public String getName() {
		return "cancel";
	}
}
