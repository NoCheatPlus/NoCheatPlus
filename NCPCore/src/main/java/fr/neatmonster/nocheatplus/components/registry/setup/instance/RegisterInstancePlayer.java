package fr.neatmonster.nocheatplus.components.registry.setup.instance;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.components.registry.setup.RegistrationContext;
import fr.neatmonster.nocheatplus.players.PlayerFactoryArgument;

public class RegisterInstancePlayer<T> extends RegisterInstance<T, PlayerFactoryArgument> {

    public RegisterInstancePlayer(RegistrationContext registrationContext, 
            Class<T> type) {
        super(registrationContext, type, 
                NCPAPIProvider.getNoCheatPlusAPI().getPlayerDataManager());
    }

}
