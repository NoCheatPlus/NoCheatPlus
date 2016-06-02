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
package fr.neatmonster.nocheatplus.command.admin.reset;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.command.admin.reset.counters.CountersCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Reset stuff, e.g. statistics counters.
 * @author dev1mc
 *
 */
public class ResetCommand extends BaseCommand{

	public ResetCommand(JavaPlugin plugin) {
		super(plugin, "reset", Permissions.COMMAND_RESET);
		addSubCommands(
			new CountersCommand(plugin)
			);
	}

}
