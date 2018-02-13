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
package fr.neatmonster.nocheatplus.components.config.value;

/**
 * Store a value (override this class for actual value), store an OverrideType,
 * provide some auxiliary methods for checking. Not intended as an API feature
 * to pass to the outside.
 * <hr/>
 * Sub classes should implement a 'boolean setValue(value, OverrideType)' method
 * checking for allowsOverrideBy and returning true iff the value has been
 * overridden, and a getValue method. A reset method may be needed for certain
 * contexts, to hard-reset to an underlying parent configuration - behavior in
 * case of OverrideType.PERMANENT needs to be specified.
 * 
 * @author asofold
 *
 */
public class AbstractValueWithOverride {

    protected OverrideType overrideType = OverrideType.INITIAL;

    /**
     * Retrieve the currently set OverrideType instance.
     * 
     * @return
     */
    public OverrideType getOverrideType() {
        return overrideType;
    }

    /**
     * Test if this value would allow an override by the given instance - would,
     * provided the type is appropriate at all.
     * 
     * @param other
     */
    public boolean wouldAllowOverrideBy(AbstractValueWithOverride other) {
        return allowsOverrideBy(other.overrideType);
    }

    /**
     * Test if this instance allows an override for the given overrideSource.
     * 
     * @param overrideType
     * @return
     */
    public boolean allowsOverrideBy(OverrideType overrideType) {
        return this.overrideType.allowsOverrideBy(overrideType);
    }


}
