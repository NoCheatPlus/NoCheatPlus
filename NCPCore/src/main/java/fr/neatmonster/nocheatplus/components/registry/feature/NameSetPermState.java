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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Store set of player names by defaultPermissions they have to get players with a certain permission.<br>
 * TODO: Might later detach to an interface (+SimpleNameSetPermStateHolder).
 * @author mc_dev
 *
 */
public class NameSetPermState implements PermStateReceiver{
	
	/** Map permission to player names (all exact case). */
	protected final HashMap<String, Set<String>> playerSets = new HashMap<String, Set<String>>();
	
	protected String[] defaultPermissions;
	
	public NameSetPermState(String... permissions){
		this.defaultPermissions = permissions;
	}
	
	@Override
	public String[] getDefaultPermissions() {
		return defaultPermissions;
	}

	@Override
	public boolean hasPermission(final String player,  final String permission) {
		final Set<String> names = playerSets.get(permission);
		if (names == null) return false;
		return (names.contains(player));
	}
	
	@Override
	public void setPermission(final String player,  final String permission, boolean state) {
		Set<String> names = playerSets.get(permission);
		if (names == null){
			if (!state) return;
			names = new LinkedHashSet<String>(20);
			playerSets.put(permission, names);
		}
		if (state) names.add(player);
		else names.remove(player);
	}


	@Override
	public void removePlayer(final String player) {
		// TODO: Something more efficient ? [mostly used with few permissions, though].
		final Iterator<Entry<String, Set<String>>> it = playerSets.entrySet().iterator();
		while (it.hasNext()){
			final Entry<String, Set<String>> entry = it.next();
			final Set<String> set = entry.getValue();
			set.remove(player);
			if (set.isEmpty()) it.remove();
		}
	}
	
	/**
	 * Get a set with the players that hold the permission.
	 * @param permission
	 * @return
	 */
	public Set<String> getPlayers(final String permission){
		return playerSets.get(permission);
	}
	
	public void addDefaultPermissions(Collection<String> permissions){
		Collection<String> newDefaults = new HashSet<String>();
		newDefaults.addAll(Arrays.asList(this.defaultPermissions));
		newDefaults.addAll(permissions);
	}
	
	public void setDefaultPermissions(Collection<String> permissions){
		defaultPermissions = new String[permissions.size()];
		permissions.toArray(defaultPermissions);
	}

}
