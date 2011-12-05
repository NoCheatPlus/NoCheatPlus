package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.WildCard;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCMoving;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.MovingData;
import cc.co.evenprime.bukkit.nocheat.data.PreciseLocation;

public abstract class MovingCheck extends Check {

    public MovingCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    /**
     * Return a new destination location or null
     * 
     * @param event
     * @return
     */
    public abstract PreciseLocation check(final NoCheatPlayer player, MovingData data, CCMoving cc);

    public abstract boolean isEnabled(CCMoving moving);

    @Override
    protected ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().moving.history;
    }

    @Override
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {

        switch (wildcard) {

        case LOCATION: {
            PreciseLocation from = player.getData().moving.from;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", from.x, from.y, from.z);
        }

        case MOVEDISTANCE: {
            PreciseLocation from = player.getData().moving.from;
            PreciseLocation to = player.getData().moving.to;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", to.x - from.x, to.y - from.y, to.z - from.z);
        }

        case LOCATION_TO:
            PreciseLocation to = player.getData().moving.to;
            return String.format(Locale.US, "%.2f,%.2f,%.2f", to.x, to.y, to.z);

        default:
            return super.getParameter(wildcard, player);
        }
    }
}
