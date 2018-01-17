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
package fr.neatmonster.nocheatplus.actions.types;

import fr.neatmonster.nocheatplus.actions.AbstractActionList;
import fr.neatmonster.nocheatplus.actions.ParameterHolder;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;

public class CommandActionWithColor<D extends ParameterHolder, L extends AbstractActionList<D, L>> extends CommandAction<D, L> {

    public CommandActionWithColor(String name, int delay, int repeat, String command) {
        super(name, delay, repeat, command);
    }

    @Override
    protected String getMessage(D violationData) {
        return ColorUtil.replaceColors(super.getMessage(violationData));
    }

    /**
     * Convert the commands data into a string that can be used in the configuration files.
     * 
     * @return the string
     */
    @Override
    public String toString() {
        return "cmdc:" + name + ":" + delay + ":" + repeat;
    }

}
