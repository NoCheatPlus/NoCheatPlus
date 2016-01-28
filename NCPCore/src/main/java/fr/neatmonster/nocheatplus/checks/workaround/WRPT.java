package fr.neatmonster.nocheatplus.checks.workaround;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import fr.neatmonster.nocheatplus.workaround.IWorkaround;
import fr.neatmonster.nocheatplus.workaround.SimpleWorkaroundRegistry;
import fr.neatmonster.nocheatplus.workaround.WorkaroundCountDown;

/**
 * Workaround registry for primary thread use. Potentially cover all checks.
 * 
 * @author asofold
 *
 */
public class WRPT extends SimpleWorkaroundRegistry {

    ///////////////////////
    // Workaround ids.
    ///////////////////////

    // MOVING_SURVIVALFLY
    // TODO: The use once thing could be shared by several spots (e.g. all double-0 top of slope).
    /**  Workaround: One time use max of jump phase twice zero dist. */
    // TODO: This might be changed to (or extended with addition of) use once within air jump phase.
    public static final String W_M_SF_SLIME_JP_2X0 = "m.sf.slime.jp.2x0"; // hum. sha-1 instead?

    ///////////////////////
    // Group ids.
    ///////////////////////

    // MOVING_SURVIVALFLY
    /** Group: Reset when not in air jump phase. */
    public static final String G_RESET_NOTINAIR = "reset.notinair";

    ///////////////////////
    // WorkaroundSet ids.
    ///////////////////////

    // MOVING
    /** WorkaroundSet: for use in MovingData. */
    public static final String WS_MOVING = "moving";

    public WRPT() {
        // Fill in blueprints, groups, workaround sets.

        // MOVING
        final Collection<IWorkaround> ws_moving = new LinkedList<IWorkaround>();
        // MOVING_SURVIVALFLY
        // Reset once on ground or reset-condition.
        final WorkaroundCountDown[] resetNotInAir = new WorkaroundCountDown[] {
                new WorkaroundCountDown(W_M_SF_SLIME_JP_2X0, 1),
        };
        ws_moving.addAll(Arrays.asList(resetNotInAir));
        setWorkaroundBluePrint(resetNotInAir);
        setGroup(G_RESET_NOTINAIR, resetNotInAir);

        // Finally register the set.
        setWorkaroundSetByIds(WS_MOVING, getCheckedIdSet(ws_moving), G_RESET_NOTINAIR);

        // TODO: just counters

        // TODO: Command to log.
    }

}
