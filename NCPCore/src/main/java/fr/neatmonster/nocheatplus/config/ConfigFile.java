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
package fr.neatmonster.nocheatplus.config;

import org.bukkit.configuration.MemorySection;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.actions.ActionFactoryFactory;
import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.ViolationData;

/**
 * A special configuration class created to handle the loading/saving of actions lists. This is for normal use with the plugin.
 */
public class ConfigFile extends ConfigFileWithActions<ViolationData, ActionList> {

    @Override
    public void setActionFactory() {
        setActionFactory(NCPAPIProvider.getNoCheatPlusAPI().getActionFactoryFactory());
    }

    public void setActionFactory(final ActionFactoryFactory actionFactoryFactory) {
        setActionFactory(actionFactoryFactory.newActionFactory(((MemorySection) this.get(ConfPaths.STRINGS)).getValues(false)));
    }

}