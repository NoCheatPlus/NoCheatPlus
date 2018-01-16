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

import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.utilities.CheckTypeUtil;

/**
 * @deprecated Use fr.neatmonster.utilities.CheckTypeUtil instead.
 * 
 * @author asofold
 */
@Deprecated
public class APIUtils {

    @Deprecated
    public static final Set<CheckType> getChildren(final CheckType type) {
        return getDescendants(type);
    }

    /**
     * Get an unmodifiable collection of descendants for the given check type.
     * Always returns a collection, does not contain check type itself.
     * 
     * @param type
     *            the check type
     * @return the descendants
     */
    @Deprecated
    public static final Set<CheckType> getDescendants(
            final CheckType type) {
        return CheckTypeUtil.getDescendants(type);
    }

    /**
     * Get an unmodifiable collection of direct children for the given check
     * type. Always returns a collection, does not contain check type itself.
     * 
     * @param type
     *            the check type
     * @return the children
     */
    @Deprecated
    public static final Set<CheckType> getDirectChildren(
            final CheckType type) {
        return CheckTypeUtil.getDirectChildren(type);
    }

    @Deprecated
    public static final Set<CheckType> getWithChildren(final CheckType type) {
        return getWithDescendants(type);
    }

    /**
     * Get an unmodifiable collection of the given check type with descendants.
     * Always returns a collection, does contain the check type itself.
     * 
     * @param type
     *            the check type
     * @return the given check type with descendants
     */
    @Deprecated
    public static final Set<CheckType> getWithDescendants(
            final CheckType type) {
        return CheckTypeUtil.getWithDescendants(type);
    }

    /**
     * Check if the supposed ancestor is an ancestor of the supposed descendant.
     * Equality doesn't match here.
     * 
     * @param supposedAncestor
     *            the supposed parent
     * @param supposedDescendant
     *            the supposed child
     * @return true, if is parent
     */
    @Deprecated
    public static final boolean isAncestor(final CheckType supposedAncestor,
            final CheckType supposedDescendant) {
        return CheckTypeUtil.isAncestor(supposedAncestor, supposedDescendant);
    }

    @Deprecated
    public static final boolean isParent(final CheckType supposedAncestor,
            final CheckType supposedDescendant) {
        return isAncestor(supposedAncestor, supposedDescendant);
    }

    /**
     * Test if the check type requires synchronization. This indicates, if a
     * check can be called off primary thread at all.
     * <hr>
     * That should be CHAT and NET checks, currently.
     * 
     * @param type
     *            the check type
     * @return true, if successful
     */
    @Deprecated
    public static final boolean needsSynchronization(final CheckType type) {
        return CheckTypeUtil.needsSynchronization(type);
    }


}
