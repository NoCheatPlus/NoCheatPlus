package fr.neatmonster.nocheatplus.compat.blocks;

import fr.neatmonster.nocheatplus.components.registry.activation.IDescriptiveActivation;

/**
 * A "patch", only applied if isAvailable() returns true - class construction
 * and testing functions should be fail-safe. The neutral description will
 * always be logged, if an item activates, advertise() might not be of
 * relevance.
 * 
 * @author asofold
 *
 */
public interface IPatchBlockPropertiesSetup extends BlockPropertiesSetup, IDescriptiveActivation {

}
