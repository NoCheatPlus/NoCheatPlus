package fr.neatmonster.nocheatplus.actions.types.penalty;

import org.bukkit.configuration.MemorySection;

import fr.neatmonster.nocheatplus.actions.ActionFactory;

/**
 * Get config-dependent IPenaltyFactory instances
 * 
 * @author asofold
 *
 */
public interface IPenaltyFactoryFactory {

    /**
     * 
     * 
     * @param library
     * @param actionFactory
     * @return
     */
    public IPenaltyFactory newPenaltyFactory(MemorySection library, ActionFactory actionFactory);

}
