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
 * Generic value with override functionality.
 * 
 * @author asofold
 *
 * @param <V>
 */
public class ValueWithOverride<V> extends AbstractValueWithOverride {

    private V value;

    /**
     * Initialize to null / INITIAL.
     */
    public ValueWithOverride() {
        this(null);
    }

    /**
     * Initialize to the given value / INITIAL.
     * 
     * @param value
     */
    public ValueWithOverride(V value) {
        this(value, OverrideType.INITIAL);
    }

    /**
     * Custom initialization.
     * 
     * @param value
     * @param overrideType
     */
    public ValueWithOverride(V value, OverrideType overrideType) {
        this.value = value;
        this.overrideType = overrideType;
    }

    public V getValue() {
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
    public boolean setValue(V value, OverrideType overrideType) {
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
    public boolean setValue(ValueWithOverride<V> other) {
        return this.setValue(other.getValue(), other.getOverrideType());
    }

    /**
     * Allow resetting if the given overrideType is PERMANENT or allows
     * overriding.
     * 
     * @param value
     * @param overrideType
     */
    public void resetValue(V value, OverrideType overrideType) {
        if (allowsOverrideBy(overrideType) 
                || OverrideType.PERMANENT.allowsOverrideBy(overrideType)) {
            this.value = value;
            this.overrideType = overrideType;
        }
    }

    /**
     * Convenience method. See: {@link #resetValue(Object, OverrideType)}
     * @param other
     */
    public void resetValue(ValueWithOverride<V> other) {
        resetValue(other.getValue(), other.getOverrideType());
    }

}
