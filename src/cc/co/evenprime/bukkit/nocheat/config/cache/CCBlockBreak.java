package cc.co.evenprime.bukkit.nocheat.config.cache;

import cc.co.evenprime.bukkit.nocheat.actions.ActionList;
import cc.co.evenprime.bukkit.nocheat.config.Configuration;

/**
 * Configurations specific for the "BlockBreak" checks
 * Every world gets one of these assigned to it.
 * 
 * @author Evenprime
 * 
 */
public class CCBlockBreak {

    public final boolean    check;
    public final boolean    reachCheck;
    public final double     reachDistance;
    public final ActionList reachActions;
    public final boolean    directionCheck;
    public final ActionList directionActions;

    public CCBlockBreak(Configuration data) {

        check = data.getBoolean("blockbreak.check");
        reachCheck = data.getBoolean("blockbreak.reach.check");
        reachDistance = ((double) data.getInteger("blockbreak.reach.reachlimit")) / 100D;
        reachActions = data.getActionList("blockbreak.reach.actions");

        directionCheck = data.getBoolean("blockbreak.direction.check");
        directionActions = data.getActionList("blockbreak.direction.actions");
    }
}
