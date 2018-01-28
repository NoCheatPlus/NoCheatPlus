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
package fr.neatmonster.nocheatplus.actions;

import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.permissions.RegisteredPermission;

/**
 * A list of actions, that associates actions to thresholds. It allows to
 * retrieve all actions that match a certain threshold.
 * <hr>
 * TODO: refactor to an array of Actions entries (threshold + Action[]) + sort
 * that one.
 */
public class ActionList extends AbstractActionList<ViolationData, ActionList>{

    public static final ActionListFactory<ViolationData, ActionList> listFactory = new ActionListFactory<ViolationData, ActionList>() {

        @Override
        public ActionList getNewActionList(RegisteredPermission permissionSilent) {
            return new ActionList(permissionSilent);
        }

    }; 

    /**
     * 
     * @param permissionSilent
     *            The permission for bypassing log actions.
     */
    public ActionList(RegisteredPermission permissionSilent) {
        super(permissionSilent, listFactory);
    }

}
