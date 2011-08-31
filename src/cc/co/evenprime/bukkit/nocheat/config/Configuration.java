package cc.co.evenprime.bukkit.nocheat.config;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.log.LogLevel;

/**
 * A configuration. It should allow access to settings associated with a string
 * 
 * @author Evenprime
 * 
 */
public interface Configuration {

    public abstract boolean getBoolean(String string);

    public abstract ActionList getActionList(String string);

    public abstract int getInteger(String string);

    public abstract String getString(String string);

    public abstract LogLevel getLogLevel(String string);

}
