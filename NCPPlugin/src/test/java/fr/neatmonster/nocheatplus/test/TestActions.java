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
package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.actions.Action;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.DefaultConfig;

public class TestActions {

    @Test
    public void testOptimizedLogActionEmpty() {
        final ConfigFile config = new DefaultConfig();
        config.set("actions", "log:dummy:0:0:icf");
        config.set("strings.dummy", "dummy");
        config.set(ConfPaths.LOGGING_ACTIVE, false);
        ActionList actionList = config.getOptimizedActionList("actions", "dummy");
        Action<ViolationData, ActionList>[] actions = actionList.getActions(0.0);
        if (actions.length != 0) {
            fail("Wrong number of actions.");
        }
    }

}
