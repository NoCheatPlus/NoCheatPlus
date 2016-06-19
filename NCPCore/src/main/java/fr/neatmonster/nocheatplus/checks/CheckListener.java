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
package fr.neatmonster.nocheatplus.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.debug.IDebugPlayer;
import fr.neatmonster.nocheatplus.components.registry.event.IGenericInstanceHandle;
import fr.neatmonster.nocheatplus.components.registry.feature.IHoldSubComponents;
import fr.neatmonster.nocheatplus.components.registry.feature.NCPListener;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * This class provides naming etc for registration with ListenerManager.
 * For listeners registered by NoCheatPlus only.
 * @author mc_dev
 *
 */
public class CheckListener extends NCPListener implements IHoldSubComponents, IDebugPlayer {

    /** Check group / type which this listener is for. */
    protected final CheckType checkType;
    protected final IGenericInstanceHandle<MCAccess> mcAccess;

    /** */
    protected final List<Object> queuedComponents = new LinkedList<Object>();

    public CheckListener(CheckType checkType){
        this.checkType = checkType; 
        this.mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getGenericInstanceHandle(MCAccess.class);
    }

    @Override
    public String getComponentName() {
        final String part = super.getComponentName();
        return checkType == null ? part : part + "_" + checkType.name();
    }

    /**
     * Convenience method to add checks as components to NCP with a delay (IHoldSubComponent).
     * This should not be used after having added the check to the ComponentRegistry (NCP-API).
     * @param check
     * @return The given Check instance, for chaining.
     */
    protected <C extends Check>  C addCheck(C check){
        // Could also set up a map from check type to check, etc.
        queuedComponents.add(check);
        return check;
    }

    @Override
    public Collection<Object> getSubComponents() {
        final List<Object> res = new ArrayList<Object>(this.queuedComponents);
        this.queuedComponents.clear();
        return res;
    }

    @Override
    public void debug(final Player player, final String message) {
        CheckUtils.debug(player, checkType, message);
    }

}
