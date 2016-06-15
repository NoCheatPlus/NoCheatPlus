package fr.neatmonster.nocheatplus.components.registry.activation;

/**
 * An activation checker with description (can be checked for activation
 * conditions and has some kind of description for logging). Possibly just a
 * delegate thing.
 * 
 * @author asofold
 *
 */
public interface IDescriptiveActivation extends IActivation {

    /**
     * Retrieve a neutral (short) description fit for logging under which
     * conditions this feature can be used, regardless of what isAvailable may
     * have returned.
     * 
     * @return
     */
    public String getNeutralDescription();

}
