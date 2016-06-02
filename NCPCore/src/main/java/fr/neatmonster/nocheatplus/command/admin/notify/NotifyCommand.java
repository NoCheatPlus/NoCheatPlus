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
package fr.neatmonster.nocheatplus.command.admin.notify;

import org.bukkit.plugin.java.JavaPlugin;

import fr.neatmonster.nocheatplus.command.BaseCommand;
import fr.neatmonster.nocheatplus.permissions.Permissions;

/**
 * Toggle notifications on and off.
 * @author mc_dev
 *
 */
public class NotifyCommand extends BaseCommand {

	public NotifyCommand(JavaPlugin plugin) {
		super(plugin, "notify", Permissions.COMMAND_NOTIFY, new String[]{"alert", "alerts"});
		addSubCommands(
			new NotifyOffCommand(plugin),
			new NotifyOnCommand(plugin)
			);
	}
	
}
