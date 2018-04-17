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
package fr.neatmonster.nocheatplus.penalties;

/**
 * Convenience implementation for input-specific effects (other than Player).
 * 
 * @author asofold
 *
 * @param <RI>
 *            The input type accepted by this penalty.
 */
public abstract class AbstractPenalty<RI> implements IPenalty<RI> {

    /** The input type accepted by this penalty. */
    private final Class<RI> registeredInput;

    public AbstractPenalty(Class<RI> registeredInput) {
        this.registeredInput = registeredInput;
    }

    @Override
    public Class<RI> getRegisteredInput() {
        return registeredInput;
    }

    @Override
    public void addToPenaltyList(final IPenaltyList penaltyList) {
        penaltyList.addPenalty(registeredInput, this);
    }

}
