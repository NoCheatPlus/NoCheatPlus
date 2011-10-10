package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;
import cc.co.evenprime.bukkit.nocheat.config.DefaultConfiguration;

/**
 * 
 * @author Evenprime
 * 
 */
public class CCBlockPlace {

    public final boolean    check;
    public final boolean    onliquidCheck;
    public final ActionList onliquidActions;

    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;

    public CCBlockPlace(Configuration data) {

        check = data.getBoolean(DefaultConfiguration.BLOCKPLACE_CHECK);
        
        onliquidCheck = data.getBoolean(DefaultConfiguration.BLOCKPLACE_ONLIQUID_CHECK);
        onliquidActions = data.getActionList(DefaultConfiguration.BLOCKPLACE_ONLIQUID_ACTIONS);

        reachCheck = data.getBoolean(DefaultConfiguration.BLOCKPLACE_REACH_CHECK);
        reachDistance = data.getInteger(DefaultConfiguration.BLOCKPLACE_REACH_LIMIT);
        reachActions = data.getActionList(DefaultConfiguration.BLOCKPLACE_REACH_ACTIONS);

    }
}
