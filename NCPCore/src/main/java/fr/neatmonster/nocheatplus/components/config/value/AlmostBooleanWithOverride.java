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

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;

public class AlmostBooleanWithOverride extends ValueWithOverride<AlmostBoolean> {

    public AlmostBooleanWithOverride() {
        super(AlmostBoolean.MAYBE);
    }

    public AlmostBooleanWithOverride(AlmostBoolean value,
            OverrideType overrideType) {
        super(value, overrideType);
    }

    public AlmostBooleanWithOverride(AlmostBoolean value) {
        super(value);
    }

    public boolean setValue(boolean value, OverrideType overrideType) {
        return setValue(AlmostBoolean.match(value), overrideType);
    }

    /**
     * Convenience: null is translated to MAYBE (default). See:
     * {@link #setValue(boolean, OverrideType)}
     * 
     * @param value
     * @param overrideType
     * @return
     */
    public boolean setValue(Boolean value, OverrideType overrideType) {
        return setValue(value == null ? AlmostBoolean.MAYBE : AlmostBoolean.match(value), 
                overrideType);
    }

    /**
     * Convenience method. See: {@link #setValue(boolean, OverrideType)}
     * 
     * @param other
     * @return
     */
    public boolean setValue(FlagWithOverride other) {
        return this.setValue(other.getValue(), other.getOverrideType());
    }

    /**
     * Convenience method. See: {@link #resetValue(Object, OverrideType)}
     * 
     * @param value
     * @param overrideType
     */
    public void resetValue(boolean value, OverrideType overrideType) {
        resetValue(AlmostBoolean.match(value), overrideType);
    }

    /**
     * Convenience: null is translated to MAYBE (default). See:
     * {@link #resetValue(Object, OverrideType)}
     * 
     * @param value
     * @param overrideType
     */
    public void resetValue(Boolean value, OverrideType overrideType) {
        resetValue(value == null ? AlmostBoolean.MAYBE : AlmostBoolean.match(value), 
                overrideType);
    }

    /**
     * Convenience method. See: {@link #resetValue(Object, OverrideType)}
     * 
     * @param other
     */
    public void resetValue(FlagWithOverride other) {
        resetValue(other.getValue(), other.getOverrideType());
    }

}
