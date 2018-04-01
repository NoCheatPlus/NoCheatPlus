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
package fr.neatmonster.nocheatplus.components.registry.setup;

import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.components.config.IConfig;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.registry.setup.config.RegisterConfigWorld;
import fr.neatmonster.nocheatplus.components.registry.setup.data.RegisterDataPlayer;
import fr.neatmonster.nocheatplus.components.registry.setup.data.RegisterDataWorld;
import fr.neatmonster.nocheatplus.components.registry.setup.instance.RegisterInstancePlayer;
import fr.neatmonster.nocheatplus.components.registry.setup.instance.RegisterInstanceWorld;

/**
 * Convenience: set up a check or similar with NCP registries.
 * <hr/>
 * For now, this is only a registry helper for internal registration. Later on,
 * this will aggregate components, dependencies and activation conditions,
 * enabling automatic registration and unregistering. Multiple contexts
 * registering the same types could lead to inconsistencies, if one gets
 * unregistered - the planned dependency mechanism is meant to mend this (if not
 * already registered, register the dependency first, then the component using
 * it).
 * <hr/>
 * <ul>
 * <li>Register factories and type relations for instances, config, data.</li>
 * <li>(When to register, dependencies, IActivation.)</li>
 * <li>(Register further components, such as listeners.)</li>
 * <li>(When to unregister.)</li>
 * <li>(More precise definition what/how to unregister.)</li>
 * </ul>
 * <hr/>
 * 
 * @author asofold
 *
 */
public class RegistrationContext implements IDoRegister {

    /*
     * / TODO: ILockable <-> tie to instantiation by global registry, tie sub
     * objects to this.
     */

    // TODO: Base some things rather on ICheckConfig, ICheckData ?

    /**
     * General type of registration, in terms of how often / when it's tried to
     * get registered.
     * <hr/>
     * 
     * @author asofold
     *
     */
    public static enum RegistrationType {
        /** Try to register directly, one time only. */
        ONCE;
    }

    /**
     * Removal type, in terms of a rough description for when to unregister the
     * contained components.
     * 
     * @author asofold
     *
     */
    public static enum RemovalType {
        /**
         * Never remove registration. Still might get removed/dented with the
         * plugin getting disabled.
         */
        NEVER;
    }


    //////////////////////////////
    // Instance
    //////////////////////////////

    // TODO: Put a global registry in control of instantiation, add id(s) / tags.

    private final List<IDoRegister> registerItems = new LinkedList<IDoRegister>();

    //////////////////////////////
    // Getter
    //////////////////////////////

    public RegistrationType registrationType() {
        return RegistrationType.ONCE;
    }

    public RemovalType removalType() {
        return RemovalType.NEVER;
    }

    /**
     * Create a new (attached) per world instance registration object.
     * 
     * @return
     */
    public <T extends IConfig> RegisterInstanceWorld<T> registerInstanceWorld(Class<T> type) {
        RegisterInstanceWorld<T> item = new RegisterInstanceWorld<T>(this, type);
        registerItems.add(item);
        return item;
    }

    /**
     * Create a new (attached) per world config registration object. The config
     * types are registered with the IPlayerDataManager too (no further grouping
     * , no factory).
     * 
     * @return
     */
    public <T extends IConfig> RegisterConfigWorld<T> registerConfigWorld(Class<T> configType) {
        RegisterConfigWorld<T> item = new RegisterConfigWorld<T>(this, configType);
        registerItems.add(item);
        return item;
    }

    /**
     * Create a new (attached) per world data registration object. The data
     * types are registered with the IPlayerDataManager too (no further grouping
     * , no factory).
     * 
     * @return
     */
    public  <T extends IData> RegisterDataWorld<T> registerDataWorld(Class<T> dataType) {
        RegisterDataWorld<T> item = new RegisterDataWorld<T>(this, dataType);
        registerItems.add(item);
        return item;
    }

    /**
     * Create a new (attached) per instance data registration object.
     * 
     * @return
     */
    public <T extends IData> RegisterInstancePlayer<T> registerInstancePlayer(Class<T> type) {
        RegisterInstancePlayer<T> item = new RegisterInstancePlayer<T>(this, type);
        registerItems.add(item);
        return item;
    }

    /**
     * Create a new (attached) per player data registration object.
     * 
     * @return
     */
    public <T extends IData> RegisterDataPlayer<T> registerDataPlayer(Class<T> dataType) {
        RegisterDataPlayer<T> item = new RegisterDataPlayer<T>(this, dataType);
        registerItems.add(item);
        return item;
    }


    //////////////////////////////
    // Setter
    //////////////////////////////


    //////////////////////////////
    // Other functionality.
    //////////////////////////////

    @Override
    public void doRegister() {
        // TODO: ILockable, ...
        // TODO: Exception handling.
        for (IDoRegister item : registerItems) {
            item.doRegister();
        }
        // TODO: (Capability to roll back?)
    }

}
