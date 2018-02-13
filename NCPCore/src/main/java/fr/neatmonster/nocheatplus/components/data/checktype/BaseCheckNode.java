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
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNode;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Auxiliary base class to allow standard configuration flags (AlmostBoolean)
 * with boolean resolution and overriding functionality.
 * 
 * @author asofold
 *
 * @param <N>
 */
public abstract class BaseCheckNode<N extends BaseCheckNode<N>> extends CheckTypeTreeNode<N> {

    // TODO: Not optimal - references that are from the container should be fetched from there.
    protected OverrideType configOverrideType = OverrideType.DEFAULT;

    protected static interface IConfigFlagAccess<N> {
        // Might move somewhere generic.

        public AlmostBooleanWithOverride getConfigState(N node);
        public boolean getState(N node);
        public void setState(N node, boolean state);
        public String getConfigPath(N node);

    }

    public BaseCheckNode(CheckType checkType, N parent,
            CheckTypeTreeNodeFactory<N> factory) {
        super(checkType, parent, factory);
    }

    // TODO: GENERIC (ConfigFlagAccess/Fetch based) Override / update / reset.

    protected void configFlagOverride(final ConfigFile rawConfiguration, 
            final AlmostBoolean active, final OverrideType overrideType,
            final boolean overrideChildren, final IConfigFlagAccess<N> access) {
        @SuppressWarnings("unchecked")
        final boolean applicable = access.getConfigState((N) this).setValue(active, overrideType);
        if (applicable) {
            // The flag is updated here.
            configFlagUpdate(rawConfiguration, false, access);
        }
        if (overrideChildren) {
            // Always override children, as there can be arbitrary sequences of overrides.
            for (final N child : getChildren()) {
                child.configFlagOverride(rawConfiguration, active, overrideType, 
                        true, access);
            }
        }
        else if (applicable) {
            // Updates child nodes recursively, provided they depend on their parent.
            for (final N child : getChildren()) {
                if (access.getConfigState(child).getValue() == AlmostBoolean.MAYBE) {
                    child.configFlagUpdate(rawConfiguration, false, access);
                }
            }
        }
    }

    protected void configFlagUpdate(final ConfigFile rawConfiguration, 
            final boolean forceUpdateChildren, final IConfigFlagAccess<N> access) {
        @SuppressWarnings("unchecked")
        final N thisNode = (N) this; // TODO
        final boolean previousActive = access.getState(thisNode);
        final AlmostBooleanWithOverride configActivation = access.getConfigState(thisNode);
        AlmostBoolean newActive = configActivation.getValue();
        if (newActive != AlmostBoolean.MAYBE) {
            access.setState(thisNode, newActive.decide());
        }
        else {

            // Only fetch from configuration, if override level allows so.
            if (rawConfiguration != null && configActivation.allowsOverrideBy(
                    OverrideType.SPECIFIC)) {
                final String configPath = access.getConfigPath(thisNode);
                if (configPath != null) {
                    // TODO: Contract? Either config is null, or path must exist.
                    newActive = rawConfiguration.getAlmostBoolean(configPath, 
                            AlmostBoolean.MAYBE);
                    configActivation.setValue(newActive, configOverrideType);
                }
            }
            // If not set by configuration, fetch from parent.
            if (newActive == AlmostBoolean.MAYBE) {
                N parent = getParent();
                if (parent == null) {
                    access.setState(thisNode, true);
                }
                else {
                    // Assume top-down updating always.
                    access.setState(thisNode, access.getState(parent));
                }
            }
            else {
                access.setState(thisNode, newActive.decide());
            }
        }
        // Update on changes, or if forced.
        if (forceUpdateChildren || previousActive != access.getState(thisNode)) {
            // Update children.
            for (final N node : this.getChildren()) {
                // Only update, if the state depends on the parent.
                if (forceUpdateChildren 
                        || access.getConfigState(node).getValue() == AlmostBoolean.MAYBE) {
                    node.configFlagUpdate(rawConfiguration, forceUpdateChildren, access);
                }
            }
        }

    }

}
