package fr.neatmonster.nocheatplus.components.registry.feature;

/**
 * Call runPostRegister, after ordinary steps of component registration have
 * taken place. This doesn't necessarily way for sub-component that register on
 * the next tick.
 * 
 * @author asofold
 *
 */
public interface IPostRegisterRunnable {

    public void runPostRegister();

}
