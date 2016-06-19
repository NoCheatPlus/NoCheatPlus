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
package fr.neatmonster.nocheatplus.components.registry.feature;

/**
 * Component to listen to plugin/onDisable.
 * 
 * @author asofold
 *
 */
public interface IDisableListener {

    /**
     * Called in the plugin in onDisable in reversed order of registration,
     * before all components get unregistered. This is meant for general data
     * cleanup, there may be extra registry cleanup stages for data sources and
     * checks later on.
     */
    public void onDisable();

}
