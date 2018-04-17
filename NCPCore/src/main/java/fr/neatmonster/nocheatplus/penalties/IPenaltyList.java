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
 * Contain applicable penalty types that need to be handled outside of
 * ViolationData.executeActions, for access by ViolationData.
 * <hr/>
 * Excluded should be:
 * <ul>
 * <li>Penalties only applying to the player.</li>
 * </ul>
 * Specifically to be contained are:
 * <ul>
 * <li>InputSpecificPenalty</li>
 * <li>Generic (input specific) penalties.</li>
 * <ul/>
 * <hr/>
 * 
 * @author asofold
 *
 */
public interface IPenaltyList {
    // TODO: Typed ? + typed per input getter (mapped lists)

    /**
     * Generic method to let the JVM deal with generics.
     * 
     * @param registeredInput
     * @param penalty
     */
    public <RI> void addPenalty(Class<RI> registeredInput, IPenalty<RI> penalty);

    /**
     * Apply generic penalties registered exactly for the given type, using the
     * given input.
     * 
     * @param type
     * @param input
     */
    public <RI, I extends RI> void applyPenaltiesPrecisely(Class<RI> type, 
            I input, boolean removeAppliedPenalties);

    /**
     * Apply all generic penalties registered for the type and all super types
     * of the given input
     * 
     * @param input
     * @param removeAppliedPenalties
     *            If set to true, penalties that return true for
     *            {@link IPenalty#apply(Object)} will be removed from the list.
     */
    public <I> void applyAllApplicablePenalties(I input, 
            boolean removeAppliedPenalties);

    /**
     * 
     * @return If any penalties are contained. Note: IPenaltyList.isEmpty()
     *         might return true, despite willCancel set.
     */
    public boolean isEmpty();

    /**
     * Definitive cancel contained.<br/>
     * Note: IPenaltyList.isEmpty() might return true, despite willCancel set.
     * 
     * @return
     */
    public boolean willCancel();

}
