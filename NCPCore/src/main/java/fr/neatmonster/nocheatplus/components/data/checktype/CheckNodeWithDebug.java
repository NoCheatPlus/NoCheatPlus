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
package fr.neatmonster.nocheatplus.components.data.checktype;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.AlmostBooleanWithOverride;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.config.ConfigFile;

public abstract class CheckNodeWithDebug<N extends CheckNodeWithDebug<N>> extends BaseCheckNode<N> implements IBaseCheckNode {

    /**
     * Deja vu: java 10?
     * 
     * @author asofold
     *
     * @param <N>
     */
    protected static class AccessDebug<N extends CheckNodeWithDebug<N>> implements IConfigFlagAccess<N> {

        @Override
        public AlmostBooleanWithOverride getConfigState(N node) {
            return node.configDebug;
        }

        @Override
        public boolean getState(N node) {
            return node.debug;
        }

        @Override
        public void setState(N node, boolean state) {
            node.debug = state;
        }

        @Override
        public String getConfigPath(N node) {
            return node.getCheckType().getConfigPathDebug();
        }

        @Override
        public boolean getMissingParentState() {
            return false; // Only allow explicit activation.
        }

    };

    @SuppressWarnings("rawtypes")
    protected static final AccessDebug accessDebug = new AccessDebug();

    ///////////////
    // Instance
    ///////////////

    public CheckNodeWithDebug(CheckType checkType, N parent,
            CheckTypeTreeNodeFactory<N> factory) {
        super(checkType, parent, factory);
    }

    protected final AlmostBooleanWithOverride configDebug = new AlmostBooleanWithOverride();
    protected boolean debug = false;

    @Override
    public boolean isDebugActive() {
        return debug;
    }

    // TODO: Visibility of methods -> public but not expose via interface or protected.

    // TODO: @Override
    @SuppressWarnings("unchecked")
    protected void overrideDebug(
            final AlmostBoolean active, 
            final OverrideType overrideType, 
            final boolean overrideChildren) {
        override(active, overrideType, overrideChildren, accessDebug);
    }

    // TODO: @Override
    @SuppressWarnings("unchecked")
    protected void updateDebug(
            final ConfigFile rawConfiguration, 
            final boolean forceUpdateChildren) {
        update(rawConfiguration, forceUpdateChildren, accessDebug);
    }

    // TODO: @Override
    @SuppressWarnings("unchecked")
    protected void updateDebug(
            final boolean forceUpdateChildren) {
        update(forceUpdateChildren, accessDebug);
    }

    // TODO: resetDebug(...) 

}
