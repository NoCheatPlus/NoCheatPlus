package fr.neatmonster.nocheatplus.components.registry.setup.instance;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.worlds.WorldFactoryArgument;

public class RegisterInstanceWorld<T> extends RegisterInstance<T, WorldFactoryArgument> {

    public RegisterInstanceWorld(RegistrationContext registrationContext, 
            Class<T> type) {
        super(registrationContext, type, 
                NCPAPIProvider.getNoCheatPlusAPI().getWorldDataManager());
    }

}
