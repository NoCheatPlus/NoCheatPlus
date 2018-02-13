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
package fr.neatmonster.nocheatplus.checks.inventory;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.players.DataManager;
import fr.neatmonster.nocheatplus.players.IPlayerData;

/**
 * The InstantEat check will find out if a player eats their food too fast.
 */
public class InstantEat extends Check {

    /**
     * Instantiates a new instant eat check.
     */
    public InstantEat() {
        super(CheckType.INVENTORY_INSTANTEAT);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param level
     *            the level
     * @return true, if successful
     */
    public boolean check(final Player player, final int level) {
        // Take time once.
        final long time = System.currentTimeMillis();

        final IPlayerData pData = DataManager.getPlayerData(player);
        final InventoryData data = pData.getGenericInstance(InventoryData.class);

        boolean cancel = false;

        // Hunger level change seems to not be the result of eating.
        if (data.instantEatFood == null || level <= player.getFoodLevel())
            return false;

        // Rough estimation about how long it should take to eat
        final long expectedTimeWhenEatingFinished = Math.max(data.instantEatInteract, data.lastClickTime) + 700L;

        if (data.instantEatInteract > 0 && expectedTimeWhenEatingFinished < time){
            // Acceptable, reduce VL to reward the player.
            data.instantEatVL *= 0.6D;
        }
        else if (data.instantEatInteract > time){
            // Security test, if time ran backwards.
        }
        else {
            final double difference = (expectedTimeWhenEatingFinished - time) / 100D;

            // Player was too fast, increase their violation level.
            data.instantEatVL += difference;

            // Execute whatever actions are associated with this check and the violation level and find out if we should
            // cancel the event.
            final ViolationData vd = new ViolationData(this, player, data.instantEatVL, 
                    difference, pData.getGenericInstance(InventoryConfig.class).instantEatActions);
            if (data.instantEatFood != null) {
                vd.setParameter(ParameterName.FOOD, data.instantEatFood.toString());
            }
            cancel = executeActions(vd).willCancel();
        }

        data.instantEatInteract = 0;
        data.instantEatFood = null;
        return cancel;
    }

}
