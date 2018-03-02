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
