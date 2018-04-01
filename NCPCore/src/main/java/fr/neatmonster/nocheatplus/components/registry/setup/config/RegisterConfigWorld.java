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
package fr.neatmonster.nocheatplus.components.registry.setup.config;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.config.IConfig;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOne;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.components.registry.setup.instance.RegisterInstanceWorld;
import fr.neatmonster.nocheatplus.worlds.WorldFactoryArgument;

/**
 * World config types are automatically registered as config types with the
 * IPlayerDataManager - no further grouping is done.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class RegisterConfigWorld<T extends IConfig> extends RegisterInstanceWorld<T> {

    public RegisterConfigWorld(RegistrationContext registrationContext, 
            Class<T> type) {
        super(registrationContext, type);
    }

    @Override
    public RegisterConfigWorld<T> factory(
            IFactoryOne<WorldFactoryArgument, T> factory) {
        super.factory(factory);
        return this;
    }

    @Override
    public RegisterConfigWorld<T> registerConfigTypesPlayer() {
        registerConfigTypesPlayer = true;
        return this;
    }

    @Override
    public RegisterConfigWorld<T> addToGroups(
            final CheckType checkType, final boolean withDescendantCheckTypes,
            final Class<? super T>... groupTypes) {
        super.addToGroups(checkType, withDescendantCheckTypes, groupTypes);
        return this;
    }



    @Override
    public RegisterConfigWorld<T> registerConfigTypesPlayer(
            CheckType checkType, boolean withDescendantCheckTypes) {
        super.registerConfigTypesPlayer(checkType, withDescendantCheckTypes);
        return this;
    }

    @Override
    public void doRegister() {
        super.doRegister();
        if (!registerConfigTypesPlayer) {
            registerConfigTypesPlayer(NCPAPIProvider.getNoCheatPlusAPI().getPlayerDataManager());
        }
    }

}
