package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;

/**
 * 
 * @author Evenprime
 *
 */
public class CCInteract {

    public final boolean     check;

    public final boolean     durabilityCheck;

    public final ActionList durabilityActions;

    public CCInteract(Configuration data) {

        check = data.getBoolean("interact.check");
        durabilityCheck = data.getBoolean("interact.durability.check");
        durabilityActions = data.getActionList("interact.durability.actions");
    }

}
