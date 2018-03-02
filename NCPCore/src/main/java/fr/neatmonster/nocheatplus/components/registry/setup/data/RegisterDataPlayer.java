package fr.neatmonster.nocheatplus.components.registry.setup.data;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.IData;
import fr.neatmonster.nocheatplus.components.registry.factory.IFactoryOne;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.components.registry.setup.instance.RegisterInstancePlayer;
import fr.neatmonster.nocheatplus.players.PlayerFactoryArgument;

public class RegisterDataPlayer<T extends IData> extends RegisterInstancePlayer<T> {

    public RegisterDataPlayer(RegistrationContext registrationContext, 
            Class<T> type) {
        super(registrationContext, type);
    }

    @Override
    public RegisterDataPlayer<T> factory(
            IFactoryOne<PlayerFactoryArgument, T> factory) {
        super.factory(factory);
        return this;
    }

    @Override
    public RegisterDataPlayer<T> addToGroups(
            final CheckType checkType, final boolean withDescendantCheckTypes,
            final Class<? super T>... groupTypes) {
        super.addToGroups(checkType, withDescendantCheckTypes, groupTypes);
        return this;
    }

    @Override
    public RegisterDataPlayer<T> registerDataTypesPlayer(
            CheckType checkType, boolean withDescendantCheckTypes) {
        super.registerDataTypesPlayer(checkType, withDescendantCheckTypes);
        return this;
    }

    @Override
    public RegisterDataPlayer<T> removeSubCheckData(
            CheckType checkType, boolean withDescendantCheckTypes) {
        super.removeSubCheckData(checkType, withDescendantCheckTypes);
        return this;
    }

}
