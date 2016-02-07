package fr.neatmonster.nocheatplus.components;

/**
 * Use, if something better can be done instead of removing all data, in case
 * the system time ran backwards, applying with
 * DataManager.handleSystemTimeRanBackwards. <br>
 * With implementing ICanHandleTimeRunningBackwards, this takes effect as
 * follows:
 * <ul>
 * <li>
 * Instead of CheckDataFactory.removeAllData.</li>
 * <li>
 * Instead of IRemoveData.removeAllData.</li>
 * </ul>
 * 
 * @author asofold
 *
 */
public interface ICanHandleTimeRunningBackwards {

    /**
     * Adjust to system time having run backwards (just "a second ago").
     */
    public void handleTimeRanBackwards();

}
