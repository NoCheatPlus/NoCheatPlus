package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent.State;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Very, very important check.
 * @author mc_dev
 *
 */
public class MunchHausen extends Check {
    public MunchHausen(){
        super(CheckType.COMBINED_MUNCHHAUSEN);
    }

    public boolean checkFish(final Player player, final Entity caught, final State state) {
        if (caught == null || !(caught instanceof Player)) return false;
        final Player caughtPlayer = (Player) caught;
        final CombinedData data = CombinedData.getData(player);
        if (player.equals(caughtPlayer)){
            data.munchHausenVL += 1.0;
            if (executeActions(player, data.munchHausenVL, 1.0, CombinedConfig.getConfig(player).munchHausenActions).willCancel()){
                return true;
            }
        }
        else data.munchHausenVL *= 0.96;
        return false;
    }
}
