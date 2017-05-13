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
package fr.neatmonster.nocheatplus.command.admin.log;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.admin.log.counters.CountersCommand;
import fr.neatmonster.nocheatplus.command.admin.log.stream.StreamCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

public class LogCommand extends BaseCommand{

    public LogCommand(JavaPlugin plugin) {
        super(plugin, "log", Permissions.COMMAND_LOG);
        addSubCommands(
                new CountersCommand(plugin),
                new StreamCommand(plugin)
                );
    }

}
