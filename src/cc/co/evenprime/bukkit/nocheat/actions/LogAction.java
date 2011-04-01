package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.logging.Level;

public class LogAction implements Action {

	private final int index;
	private final Level level;
	
	public final static LogAction logLow = new LogAction(0, Level.INFO);
	public final static LogAction logMed = new LogAction(1, Level.WARNING);
	public final static LogAction logHigh = new LogAction(2, Level.SEVERE);
	
	public final static LogAction log[] = { logLow, logMed, logHigh };
	
	private LogAction(int index, Level level) {
		this.index = index;
		this.level = level;
	}
	
	public Level getLevel() {
		return level;
	}
	
	public int getIndex() {
		return index;
	}
}
