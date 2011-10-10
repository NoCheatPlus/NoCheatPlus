package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;

/**
 * 
 * @author Evenprime
 * 
 */
public class CCInteract {

    public final boolean    check;

    public final boolean    durabilityCheck;
    public final ActionList durabilityActions;

    public CCInteract(Configuration data) {

        check = data.getBoolean(DefaultConfiguration.INTERACT_CHECK);
        durabilityCheck = data.getBoolean(DefaultConfiguration.INTERACT_DURABILITY_CHECK);
        durabilityActions = data.getActionList(DefaultConfiguration.INTERACT_DURABILITY_ACTIONS);
    }
}
