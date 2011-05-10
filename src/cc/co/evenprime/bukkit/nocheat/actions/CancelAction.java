package cc.co.evenprime.bukkit.nocheat.actions;

/**
 * 
 * @author Evenprime
 *
 */
public class CancelAction extends Action {

	public final static CancelAction cancel = new CancelAction();

	private CancelAction() { super(1, 1); }

	public String getName() {
		return "cancel";
	}
}
