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
package fr.neatmonster.nocheatplus.compat.glowstone;

import net.glowstone.GlowServer;
import net.glowstone.entity.GlowPlayer;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.utilities.BlockCache;

public class MCAccessGlowstone extends MCAccessBukkit{

    // TODO: Glowstone: nodamageticks > 0 => damage(...) won't work (no updating).

    /**
     * Constructor to let it fail.
     */
    public MCAccessGlowstone() {
        super();
        getCommandMap();
        // TODO: Nail it down further.
    }

    @Override
    public String getMCVersion() {
        // Might work with earlier versions.
        return "1.8";
    }

    @Override
    public String getServerVersionTag() {
        // TODO: Consider version specific ?
        return "Glowstone";
    }

    @Override
    public CommandMap getCommandMap() {
        return ((GlowServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public BlockCache getBlockCache(final World world) {
        return new BlockCacheGlowstone(world);
    }

    @Override
    public void dealFallDamage(final Player player, final double damage) {
        // NOTE: Fires a damage event.
        ((GlowPlayer) player).damage(damage, DamageCause.FALL);
    }

    @Override
    public AlmostBoolean dealFallDamageFiresAnEvent() {
        return AlmostBoolean.YES; // Assumption (it's native access).
    }

}
