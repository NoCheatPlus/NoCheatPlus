/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.components.data;

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

    // TODO: IDataOn ? Applicable data access (IGetGenericInstance) as argument?

    /**
     * Adjust to system time having run backwards (just "a second ago").
     */
    public void handleTimeRanBackwards();

}
