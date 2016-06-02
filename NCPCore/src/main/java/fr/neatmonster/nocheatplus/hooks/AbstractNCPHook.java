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
package fr.neatmonster.nocheatplus.hooks;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.IViolationInfo;

/**
 * Extend this class for maximum future compatibility.<br>
 * Especially the onCheckFailure method might get extended with check specific arguments, this class will provide
 * compatibility with older method signatures, where possible.
 * 
 * @author asofold
 */
public abstract class AbstractNCPHook implements NCPHook {
    /**
     * 
     * @deprecated See new signature in NCPHook.
     * @param checkType
     * @param player
     * @return
     */
    public boolean onCheckFailure(CheckType checkType, Player player){
        // Implemented because of API change.
        return false;
    }

    @Override
    public boolean onCheckFailure(final CheckType checkType, final Player player, final IViolationInfo info) {
        // Kept for compatibility reasons.
        return onCheckFailure(checkType, player);
    }
    
}