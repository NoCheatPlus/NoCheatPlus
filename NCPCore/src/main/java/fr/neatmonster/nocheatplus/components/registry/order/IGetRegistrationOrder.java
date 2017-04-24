package fr.neatmonster.nocheatplus.components.registry.order;

/**
 * Just provide a neutral getter, independent of what context it is used in.
 * There will be confusion potential, so this remains subject to an overhaul
 * later on.
 * <hr>
 * Typical uses:
 * <ul>
 * <li>IRegisterWithOrder might just extend this one, renaming and adding more
 * methods pending there.</li>
 * <li>IGetRegistrationOrder can be implemented to enable use of sorting
 * methods.</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public interface IGetRegistrationOrder {

    public RegistrationOrder getRegistrationOrder();

}
