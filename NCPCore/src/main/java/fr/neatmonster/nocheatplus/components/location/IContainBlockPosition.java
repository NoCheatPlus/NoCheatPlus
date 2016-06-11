package fr.neatmonster.nocheatplus.components.location;

/**
 * Minimal interface for checking if a block positions is contained.
 * 
 * @author asofold
 *
 */
public interface IContainBlockPosition {

    public boolean containsBlockPosition(int x, int y, int z);

    public boolean containsBlockPosition(IGetBlockPosition blockPosition);

}
