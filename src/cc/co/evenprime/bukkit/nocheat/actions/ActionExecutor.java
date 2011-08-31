package cc.co.evenprime.bukkit.nocheat.actions;

import java.util.HashMap;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * An ActionExecutor does exactly that, executing actions.
 * 
 * @author Evenprime
 * 
 */
public interface ActionExecutor {

    public abstract boolean executeActions(Player player, ActionList actions, int violationLevel, HashMap<String, String> hashMap, ConfigurationCache cc);

}
