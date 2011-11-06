package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;

import org.bukkit.Material;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.actions.types.ActionWithParameters.WildCard;
import cc.co.evenprime.bukkit.nocheat.config.cache.CCBlockPlace;
import cc.co.evenprime.bukkit.nocheat.data.BlockPlaceData;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;
import cc.co.evenprime.bukkit.nocheat.data.SimpleLocation;

/**
 * 
 */
public abstract class BlockPlaceCheck extends Check {

    public BlockPlaceCheck(NoCheat plugin, String name, String permission) {
        super(plugin, name, permission);
    }

    public abstract boolean check(NoCheatPlayer player, BlockPlaceData data, CCBlockPlace cc);

    public abstract boolean isEnabled(CCBlockPlace cc);

    @Override
    protected final ExecutionHistory getHistory(NoCheatPlayer player) {
        return player.getData().blockplace.history;
    }

    @Override
    public String getParameter(WildCard wildcard, NoCheatPlayer player) {
        switch (wildcard) {

        case PLACE_LOCATION: {
            SimpleLocation l = player.getData().blockplace.blockPlaced;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        case PLACE_AGAINST: {
            SimpleLocation l = player.getData().blockplace.blockPlacedAgainst;
            if(l.isSet()) {
                return String.format(Locale.US, "%d %d %d", l.x, l.y, l.z);
            } else {
                return "null";
            }
        }

        case BLOCK_TYPE: {
            Material type = player.getData().blockplace.placedType;
            if(type == null) {
                return "null";
            }
            return type.toString();
        }
        default:
            return super.getParameter(wildcard, player);
        }
    }
}
