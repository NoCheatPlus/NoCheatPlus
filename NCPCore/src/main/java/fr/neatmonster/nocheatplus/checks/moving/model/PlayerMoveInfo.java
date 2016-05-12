package fr.neatmonster.nocheatplus.checks.moving.model;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/**
 * Player specific MoveInfo.
 * 
 * @author asofold
 *
 */
public class PlayerMoveInfo extends MoveInfo<PlayerLocation, Player> {

    public PlayerMoveInfo(final MCAccess mcAccess){
        super(mcAccess, new PlayerLocation(mcAccess, null), new PlayerLocation(mcAccess, null));
    }

    @Override
    protected void set(PlayerLocation rLoc, Location loc, Player player, double yOnGround) {
        rLoc.set(loc, player, yOnGround);
    }

}
