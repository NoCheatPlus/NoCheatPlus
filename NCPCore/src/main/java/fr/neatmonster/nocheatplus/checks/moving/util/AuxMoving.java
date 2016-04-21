package fr.neatmonster.nocheatplus.checks.moving.util;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.moving.MovingConfig;
import fr.neatmonster.nocheatplus.checks.moving.MovingData;
import fr.neatmonster.nocheatplus.checks.moving.locations.MoveInfo;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.IRegisterAsGenericInstance;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;

/**
 * Non-static utility, (to be) registered as generic instance.
 * 
 * @author asofold
 *
 */
public class AuxMoving implements MCAccessHolder, IRegisterAsGenericInstance {

    // TODO: Move more non-static stuff here.

    private MCAccess mcAccess = null;

    /**
     * Unused instances.<br>
     * Might be better due to cascading events in case of actions or plugins doing strange things.
     */
    private final List<MoveInfo> parkedInfo = new ArrayList<MoveInfo>(10);

    public MoveInfo useMoveInfo() {
        if (parkedInfo.isEmpty()) {
            return new MoveInfo(mcAccess);
        }
        else {
            return parkedInfo.remove(parkedInfo.size() - 1);
        }
    }

    /**
     * Cleanup and add to parked.
     * @param moveInfo
     */
    public void returnMoveInfo(final MoveInfo moveInfo) {
        moveInfo.cleanup();
        parkedInfo.add(moveInfo);
    }

    /**
     * Convenience method to do both data.resetPositions and
     * data.adjustMediumProperties, wrapping given loc with a PlayerLocation
     * instance.
     * 
     * @param player
     * @param loc
     * @param data
     * @param cc
     */
    public void resetPositionsAndMediumProperties(final Player player, final Location loc, final MovingData data, final MovingConfig cc) {
        final MoveInfo moveInfo = useMoveInfo();
        moveInfo.set(player, loc, null, cc.yOnGround);
        data.resetPositions(moveInfo.from);
        data.adjustMediumProperties(moveInfo.from);
        returnMoveInfo(moveInfo);
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
    }

    /**
     * Clear parked MovingInfo instances. Called on reload and data removal.
     */
    public void clear() {
        // Call cleanup on all parked info, just in case.
        for (final MoveInfo info : parkedInfo) {
            info.cleanup();
        }
        parkedInfo.clear();
    }

}
