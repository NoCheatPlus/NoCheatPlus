package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;

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

        check = data.getBoolean("blockplace.check");
        onliquidCheck = data.getBoolean("blockplace.onliquid.check");
        onliquidActions = data.getActionList("blockplace.onliquid.actions");

        reachCheck = data.getBoolean("blockplace.reach.check");
        reachDistance = data.getInteger("blockplace.reach.reachlimit");
        reachActions = data.getActionList("blockplace.reach.actions");

    }
}
