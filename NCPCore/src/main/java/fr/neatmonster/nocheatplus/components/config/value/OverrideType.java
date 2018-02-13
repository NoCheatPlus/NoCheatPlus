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

public enum OverrideType {
    /**
     * Initial value after object creation. Gets overridden by everything.
     */
    INITIAL(-100),
    /**
     * Explicitly set, but clearly meant temporary (gets overridden by all
     * others, except initial).
     */
    VOLATILE(0),
    /** Set from a default configuration (inherited). */
    DEFAULT(0),
    /**
     * Set from a specific configuration (not inherited). SPECIFIC has the same
     * priority as DEFAULT, because both should be mutually exclusive, assuming
     * specific values to get applied after the default ones, always, if in
     * doubt. This value exists for distinction, it may not be accurate, in case
     * an underlying configuration contains values of varying origin - typically
     * this would lead to SPECIFIC, as opposed to a plain reference or copy of
     * the default configuration.
     */
    SPECIFIC(0),
    /** Explicitly set, to not get overridden for inherited properties. */
    CUSTOM(50),
    /** Last resort, e.g. on permanent incompatibility. */
    PERMANENT(100)
    ;

    private int priority;

    private OverrideType(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return this.priority;
    }

    public boolean allowsOverrideBy(OverrideType overrideType) {
        return overrideType.getPriority() >= this.priority;
    }

}