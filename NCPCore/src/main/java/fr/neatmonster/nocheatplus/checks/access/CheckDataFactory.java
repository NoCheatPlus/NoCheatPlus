package fr.neatmonster.nocheatplus.checks.access;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.components.IRemoveData;

/**
 * A factory for creating and accessing data.
 * 
 * @author asofold
 */
public interface CheckDataFactory extends IRemoveData{

    /**
     * Gets the data of the specified player.
     * 
     * @param player
     *            the player
     * @return the data
     */
    public ICheckData getData(final Player player);
    
    @Override
    public ICheckData removeData(final String playerName);

}
