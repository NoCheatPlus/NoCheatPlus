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
package fr.neatmonster.nocheatplus.components;


/**
 * Permission cache. Allow to query permissions (a defined set of permissions), to be registered and automatically be updated, according to registry.<br>
 * The permissions are not updated in real time but on certain events, to be specified by the registry.
 * 
 * @author mc_dev
 *
 */
public interface PermStateHolder {
	
	/**
	 * Get the default permissions that are guaranteed to be held here.
	 * @return
	 */
	public String[] getDefaultPermissions();
	
	/**
	 * Test a permission. If not available the result will be false, no updating of permissions is expected on calling this.
	 * @param player
	 * @param permission
	 * @return
	 */
	public boolean hasPermission(String player, String permission);
}
