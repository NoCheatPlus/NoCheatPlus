package cc.co.evenprime.bukkit.nocheat.config.tree;

import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * A node for the configuration tree
 * 
 * @author Evenprime
 * 
 */
public class LogLevelOption extends ChildOption {

    /**
	 * 
	 */
    private static final long serialVersionUID = -1609308017422576285L;

    private LogLevel          option;

    public LogLevelOption(String identifier, LogLevel initialValue) {

        super(identifier);
        this.option = initialValue;
    }

    @Override
    public String getStringValue() {
        return option.name;
    }

    public boolean setStringValue(String value) {
        option = LogLevel.getLogLevelFromString(value);
        return true;
    }

    public void setLogLevelValue(LogLevel value) {
        this.option = value;
    }

    public LogLevel getLogLevelValue() {
        return this.option;
    }

    public LogLevelOption clone() {
        return new LogLevelOption(this.getIdentifier(), this.option);
    }
}
