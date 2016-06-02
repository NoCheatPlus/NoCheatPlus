/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.workaround;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;

import fr.neatmonster.nocheatplus.workaround.IWorkaround;
import fr.neatmonster.nocheatplus.workaround.SimpleWorkaroundRegistry;
import fr.neatmonster.nocheatplus.workaround.WorkaroundCountDown;
import fr.neatmonster.nocheatplus.workaround.WorkaroundCounter;

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

    // (vEnvHacks)
    // TODO: The use once thing could be shared by several spots (e.g. all double-0 top of slope).
    /**  Workaround: One time use max of jump phase twice zero dist. */
    // TODO: This might be changed to (or extended with addition of) use once within air jump phase.
    public static final String W_M_SF_SLIME_JP_2X0 = "m.sf.slime.jp.2x0"; // hum. sha-1 instead?
    /** Zero vdist after negative vdist, "early" jump phase, cobweb. venvHacks */
    public static final String W_M_SF_WEB_0V1 = "m.sf.web.0v1";
    public static final String W_M_SF_WEB_0V2 = "m.sf.web.0v2";
    public static final String W_M_SF_WEB_MICROGRAVITY1 = "m.sf.web.microgravity1";
    public static final String W_M_SF_WEB_MICROGRAVITY2 = "m.sf.web.microgravity2";

    // oddSlope
    public static final String W_M_SF_SLOPE1 = "m.sf.slope1";
    public static final String W_M_SF_SLOPE2 = "m.sf.slope2";

    // TODO: oddLiquid

    // TODO: oddGravity

    // TODO: oddFriction

    // (TODO: oddElytra)

    // Vehicle: oddInAirDescend
    /**
     * Vehicle descending in-air, skip one vehicle move event during late in-air
     * phase.
     */
    public static final String W_M_V_ENV_INAIR_SKIP = "m.v.env.inair.skip";
    /** Just a counter for back to surface for in-water moves (water-water). */
    public static final String W_M_V_ENV_INWATER_BTS = "m.v.env.inwater.bts";

    ///////////////////////
    // Group ids.
    ///////////////////////

    // MOVING_SURVIVALFLY
    /**
     * Group: Reset when not in air jump phase. Both used for players and
     * vehicles with players inside.
     */
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
                new WorkaroundCountDown(W_M_V_ENV_INAIR_SKIP, 1),
        };
        ws_moving.addAll(Arrays.asList(resetNotInAir));
        setWorkaroundBluePrint(resetNotInAir);
        setGroup(G_RESET_NOTINAIR, resetNotInAir);

        // Just counters.
        final String[] counters = new String[] {
                // Player
                W_M_SF_WEB_0V1,
                W_M_SF_WEB_0V2,
                W_M_SF_WEB_MICROGRAVITY1,
                W_M_SF_WEB_MICROGRAVITY2,
                W_M_SF_SLOPE1,
                W_M_SF_SLOPE2,
                // Vehicle
                W_M_V_ENV_INWATER_BTS
        };
        for (final String id : counters) {
            final WorkaroundCounter counter = new WorkaroundCounter(id);
            ws_moving.add(counter);
            setWorkaroundBluePrint(counter);
        }

        // Finally register the set.
        setWorkaroundSetByIds(WS_MOVING, getCheckedIdSet(ws_moving), G_RESET_NOTINAIR);

        // TODO: Command to log global and for players.
    }

}
