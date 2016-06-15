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
package fr.neatmonster.nocheatplus.compat.registry;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

public class MCAccessConfig {

    public final boolean enableCBDedicated;
    public final boolean enableCBReflect;

    public MCAccessConfig(final ConfigFile config) {
        this.enableCBDedicated = config.getBoolean(ConfPaths.COMPATIBILITY_SERVER_CBDEDICATED_ENABLE);
        this.enableCBReflect = config.getBoolean(ConfPaths.COMPATIBILITY_SERVER_CBREFLECT_ENABLE);
    }

}
