package fr.neatmonster.nocheatplus.compat.blocks;

import fr.neatmonster.nocheatplus.components.registry.activation.Activation;

public abstract class AbstractBlockPropertiesPatch implements IPatchBlockPropertiesSetup {

    protected final Activation activation = new Activation();

    @Override
    public boolean isAvailable() {
        return activation.isAvailable();
    }

    @Override
    public String getNeutralDescription() {
        return activation.getNeutralDescription();
    }

    @Override
    public boolean advertise() {
        return activation.advertise();
    }

}
