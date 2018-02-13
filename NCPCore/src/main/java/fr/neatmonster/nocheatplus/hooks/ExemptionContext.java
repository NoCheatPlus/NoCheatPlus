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
package fr.neatmonster.nocheatplus.hooks;

/**
 * Both hashCode and equals only compare the ids, so equals is not fit for
 * comparing side conditions - AWAIT a method like
 * isEquivalentTo(ExemptionContext) for that purpose, rather.
 * <hr>
 * Note that some ids like 0 and -1 are reserved. The registry will only deal
 * positive ids.
 * 
 * @author asofold
 *
 */
public class ExemptionContext {

    /** Id is -1. */
    public static final ExemptionContext LEGACY_NON_NESTED = new ExemptionContext(-1);
    /** Id is 0. */
    public static final ExemptionContext ANONYMOUS_NESTED = new ExemptionContext(0);

    ///////////////
    // Instance
    ///////////////

    /*
     * 
     *  TODO: How to use (one context = one thing, or one context contains multiple ids.
     *  -> so contexts contain contexts (...).
     */

    private Integer id;

    public ExemptionContext(final Integer id) {
        if (id == null) {
            throw new NullPointerException("The id must not be null.");
        }
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        else if (obj instanceof ExemptionContext) {
            return id.equals(((ExemptionContext) obj).getId());
        }
        else if (obj instanceof Integer) {
            return id.equals((Integer) obj);
        }
        else {
            return false;
        }
    }

}
