package cc.co.evenprime.bukkit.nocheat.actions;

/**
 * 
 * @author Evenprime
 *
 */
import java.util.logging.Level;

public class LogAction extends Action {

	public final Level level;

	public final static LogAction loglow = new LogAction(1, false, Level.INFO);
	public final static LogAction logmed = new LogAction(1, false, Level.WARNING);
	public final static LogAction loghigh = new LogAction(1, false, Level.SEVERE);

	public final static LogAction[] log = { loglow, logmed, loghigh };

	private LogAction(int firstAfter, boolean repeat, Level level) {
		super(firstAfter, repeat);
		this.level = level;
	}

	public String getName() {
		if(level.equals(Level.INFO)) 
			return "loglow";
		else if(level.equals(Level.WARNING))
			return "logmed";
		else if(level.equals(Level.SEVERE))
			return "loghigh";
		else
			return "";
	}
}
