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

public class FlagWithOverride extends AbstractValueWithOverride {

    private boolean value;

    /**
     * Initialize to null / INITIAL.
     */
    public FlagWithOverride() {
        this(false);
    }

    /**
     * Initialize to the given value / INITIAL.
     * 
     * @param value
     */
    public FlagWithOverride(boolean value) {
        this(value, OverrideType.INITIAL);
    }

    /**
     * Custom initialization.
     * 
     * @param value
     * @param overrideType
     */
    public FlagWithOverride(boolean value, OverrideType overrideType) {
        this.value = value;
        this.overrideType = overrideType;
    }

    public boolean getValue() {
        return value;
    }

    /**
     * Override, if the given overrideType satisfies the conditions for
     * overriding.
     * 
     * @param value
     * @param overrideType
     *            The override source/reason/priority.
     * @return True, iff the internally stored value has been updated.
     */
    public boolean setValue(boolean value, OverrideType overrideType) {
        if (allowsOverrideBy(overrideType)) {
            this.value = value;
            this.overrideType = overrideType;
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Convenience method. See: {@link #setValue(boolean, OverrideType)}
     * @param other
     * @return
     */
    public boolean setValue(FlagWithOverride other) {
        return setValue(other.getValue(), other.getOverrideType());
    }

    /**
     * Convenience method. See: {@link #setValue(boolean, OverrideType)}
     * @param other
     * @return
     */
    public boolean setValue(ValueWithOverride<Boolean> other) {
        return setValue(other.getValue(), other.getOverrideType());
    }

    /**
     * Allow resetting if the given overrideType is PERMANENT or allows
     * overriding.
     * 
     * @param value
     * @param overrideType
     */
    public void resetValue(boolean value, OverrideType overrideType) {
        if (allowsOverrideBy(overrideType) 
                || OverrideType.PERMANENT.allowsOverrideBy(overrideType)) {
            this.value = value;
            this.overrideType = overrideType;
        }
    }

    /**
     * Convenience method. See: {@link #resetValue(boolean, OverrideType)}
     * @param other
     * @return
     */
    public void resetValue(FlagWithOverride other) {
        resetValue(other.getValue(), other.getOverrideType());
    }

    /**
     * Convenience method. See: {@link #resetValue(boolean, OverrideType)}
     * @param other
     * @return
     */
    public void resetValue(ValueWithOverride<Boolean> other) {
        resetValue(other.getValue(), other.getOverrideType());
    }

}
