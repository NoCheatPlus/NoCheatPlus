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
package fr.neatmonster.nocheatplus.actions.types.penalty;

/**
 * Minimal abstract implementation for player-specific effects.
 * @author asofold
 *
 */
public abstract class AbstractPlayerPenalty implements Penalty {

    /**
     * Always has player-specific effects.
     */
    @Override
    public boolean hasPlayerEffects() {
        return true;
    }

    /**
     * (Override to implement input-specific effects. Should prefer
     * AbstractGenericPenalty instead, though.)
     */
    @Override
    public boolean hasInputSpecificEffects() {
        return false;
    }

}
