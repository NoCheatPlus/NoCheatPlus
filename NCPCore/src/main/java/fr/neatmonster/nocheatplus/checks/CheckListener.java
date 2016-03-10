package fr.neatmonster.nocheatplus.checks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.IDebugPlayer;
import fr.neatmonster.nocheatplus.components.IHoldSubComponents;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.components.NCPListener;
import fr.neatmonster.nocheatplus.utilities.CheckUtils;

/**
 * This class provides naming etc for registration with ListenerManager.
 * For listeners registered by NoCheatPlus only.
 * @author mc_dev
 *
 */
public class CheckListener extends NCPListener implements MCAccessHolder, IHoldSubComponents, IDebugPlayer {

    /** Check group / type which this listener is for. */
    protected final CheckType checkType;
    protected MCAccess mcAccess;

    /** */
    protected final List<Object> queuedComponents = new LinkedList<Object>();

    public CheckListener(CheckType checkType){
        this.checkType = checkType; 
        this.mcAccess = NCPAPIProvider.getNoCheatPlusAPI().getMCAccess();
    }

    @Override
    public String getComponentName() {
        final String part = super.getComponentName();
        return checkType == null ? part : part + "_" + checkType.name();
    }

    @Override
    public void setMCAccess(MCAccess mcAccess) {
        this.mcAccess = mcAccess;
    }

    @Override
    public MCAccess getMCAccess() {
        return mcAccess;
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
