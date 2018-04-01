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
package fr.neatmonster.nocheatplus.components.registry.setup.data;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOne;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.components.registry.setup.instance.RegisterInstanceWorld;
import fr.neatmonster.nocheatplus.worlds.WorldFactoryArgument;

/**
 * Per world data types are automatically registered as (data) types with the
 * IPlayerDataManager - no further grouping is done.
 * 
 * @author asofold
 *
 * @param <T>
 */
public class RegisterDataWorld<T extends IData> extends RegisterInstanceWorld<T> {

    public RegisterDataWorld(RegistrationContext registrationContext, 
            Class<T> type) {
        super(registrationContext, type);
    }

    @Override
    public RegisterDataWorld<T> factory(
            IFactoryOne<WorldFactoryArgument, T> factory) {
        super.factory(factory);
        return this;
    }

    @Override
    public RegisterDataWorld<T> addToGroups(
            final CheckType checkType, final boolean withDescendantCheckTypes,
            final Class<? super T>... groupTypes) {
        super.addToGroups(checkType, withDescendantCheckTypes, groupTypes);
        return this;
    }

    @Override
    public RegisterDataWorld<T> registerDataTypesPlayer(
            CheckType checkType, boolean withDescendantCheckTypes) {
        super.registerDataTypesPlayer(checkType, withDescendantCheckTypes);
        return this;
    }

    @Override
    public RegisterDataWorld<T> removeSubCheckData(
            CheckType checkType, boolean withDescendantCheckTypes) {
        super.removeSubCheckData(checkType, withDescendantCheckTypes);
        return this;
    }

    @Override
    public void doRegister() {
        super.doRegister();
        if (!registerDataTypesPlayer) {
            registerDataTypesPlayer(NCPAPIProvider.getNoCheatPlusAPI().getPlayerDataManager());
        }
    }

}
