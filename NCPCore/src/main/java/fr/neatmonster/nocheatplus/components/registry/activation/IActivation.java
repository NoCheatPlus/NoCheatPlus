package fr.neatmonster.nocheatplus.components.registry.activation;

/**
 * The consumer side of testing for feature activation.
 * 
 * @author asofold
 *
 */
public interface IActivation {

    /**
     * Test if the feature is available.
     * @return
     */
    public boolean isAvailable();

}
