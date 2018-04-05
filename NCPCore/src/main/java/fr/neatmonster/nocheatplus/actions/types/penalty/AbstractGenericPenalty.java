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

import org.bukkit.entity.Player;

/**
 * Convenience implementation for input-specific effects (other than Player).
 * 
 * @author asofold
 *
 * @param <RI>
 *            The input type accepted by this penalty.
 */
public abstract class AbstractGenericPenalty<RI> implements GenericPenalty<RI> {

    /** The input type accepted by this penalty. */
    private final Class<RI> registeredInput;

    public AbstractGenericPenalty(Class<RI> registeredInput) {
        this.registeredInput = registeredInput;
    }

    /**
     * Always has input-specific effects.
     */
    @Override
    public boolean hasInputSpecificEffects() {
        return true;
    }

    @Override
    public Class<RI> getRegisteredInput() {
        return registeredInput;
    }

    /**
     * (Override to use player-specific effects. Consider using
     * AbstractPlayerPenalty instead, for simple player-specific-only
     * penalties.)
     */
    @Override
    public boolean hasPlayerEffects() {
        return false;
    }

    /**
     * Override to use player-specific effects.
     */
    @Override
    public void apply(Player player) {
    }

    /**
     * Implements isAssignableFrom test, to delegate to applyGenericEffects(RI).
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> void apply(T input) {
        if (registeredInput.isAssignableFrom(input.getClass())) {
            applyGenericEffects((RI) input);
        }
    }

    @Override
    public  void applyPrecisely(final RI input) {
        applyGenericEffects(input);
    }

    @Override
    public void addToPenaltyList(final IPenaltyList penaltyList) {
        penaltyList.addGenericPenalty(registeredInput, this);
    }

    /**
     * Override for implementation of input-specific effects.
     * 
     * @param input
     */
    protected abstract void applyGenericEffects(RI input);

}
