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
package fr.neatmonster.nocheatplus.compat;

import java.util.UUID;

import fr.neatmonster.nocheatplus.utilities.IdUtil;

// TODO: Auto-generated Javadoc
/**
 * The Class AttribUtil.
 */
public class AttribUtil {
    
    /** The Constant ID_SPRINT_BOOST. */
    public static final UUID ID_SPRINT_BOOST = IdUtil.UUIDFromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

    /**
     * Get a multiplier for an AttributeModifier.
     *
     * @param operator
     *            Exclusively allows operator 2. Otherwise will throw an
     *            IllegalArgumentException.
     * @param value
     *            The modifier value (AttributeModifier).
     * @return A multiplier for direct use.
     * @throws IllegalArgumentException
     *             if the modifier is not 2.
     */
    public static double getMultiplier(final int operator, final double value) {
        // TODO: Might allow 1 too, as it should "work", despite less accurate.
        switch(operator) {
            case 2:
                return 1.0 + value;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }

}
