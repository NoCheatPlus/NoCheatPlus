package fr.neatmonster.nocheatplus.checks.access;

import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * Extension for CheckData, enabling disable spell checking removal of sub check
 * data.
 * 
 * @author asofold
 *
 */
public interface IRemoveSubCheckData {

    /**
     * Remove the sub check data of the given CheckType.
     * 
     * @param checkType
     * @return True, if the sub check type has been contained <i>and the
     *         implementation is capable of removing it in general.</i> False,
     *         if the implementation is not capable of removing that type of
     *         data, or if the check type doesn't qualify for a sub check at
     *         all. If false is returned, the entire check group data (or super
     *         check data) might get removed, in order to ensure data removal.
     */
    public boolean removeSubCheckData(CheckType checkType);
}
