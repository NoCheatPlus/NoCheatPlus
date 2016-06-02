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
package fr.neatmonster.nocheatplus.checks.net.protocollib;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.reflect.StructureModifier;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.net.NetConfig;
import fr.neatmonster.nocheatplus.checks.net.NetConfigCache;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

public class SoundDistance extends BaseAdapter {

    // TODO: Will not be effective with 512 radius, if they add the patch by @Amranth.
    // TODO: For lower distances more packets might need to be intercepted.

    /** Partly by debugging, partly from various sources, possibly including wrong spelling. */
    private static final Set<String> effectNames = new HashSet<String>(Arrays.asList(
            // Weather
            "ambient.weather.thunder",
            // Wither
            "wither-spawn-sound-radius",
            "mob.wither.spawn",
            "mob.wither.shoot",
            "mob.wither.idle",
            "mob.wither.hurt",
            "mob.wither.death",
            // Enderdragon
            "dragon-death-sound-radius",
            "mob.enderdragon.wings",
            "mob.enderdragon.grow",
            "mob.enderdragon.growl",
            "mob.enderdragon.hit",
            "mob.enderdragon.end",
            "game.neutral.die" // Enderdragon 1.8.7 (debug).
            ));

    private final Integer idSoundEffectCancel = counters.registerKey("packet.sound.cancel");
    private final NetConfigCache configs;
    private final Location useLoc = new Location(null, 0, 0, 0);

    public SoundDistance(Plugin plugin) {
        super(plugin, ListenerPriority.LOW, PacketType.Play.Server.NAMED_SOUND_EFFECT);
        this.configs = (NetConfigCache) CheckType.NET.getConfigFactory(); // TODO: DataManager.getConfig(NetConfigCache.class);
        this.checkType = CheckType.NET_SOUNDDISTANCE;
    }

    @Override
    public void onPacketSending(final PacketEvent event) {
        final PacketContainer packetContainer = event.getPacket();

        // Compare sound effect name.
        final String soundName = packetContainer.getStrings().read(0);
        if (!effectNames.contains(soundName)) {
            return;
        }

        final Player player = event.getPlayer();
        final NetConfig cc = configs.getConfig(player.getWorld());
        if (!cc.soundDistanceActive) {
            return;
        }

        // Compare distance of player to the weather location.
        final Location loc = player.getLocation(useLoc);
        final StructureModifier<Integer> ints = packetContainer.getIntegers();
        final double dSq = TrigUtil.distanceSquared(ints.read(0) / 8, ints.read(2) / 8, loc.getX(), loc.getZ());
        //        if (data.debug) {
        //            debug(player, "SoundDistance(" + soundName + "): " + StringUtil.fdec1.format(Math.sqrt(dSq)));
        //        }
        if (dSq > cc.soundDistanceSq) {
            event.setCancelled(true);
            counters.add(idSoundEffectCancel, 1);
        }
        useLoc.setWorld(null);
    }

}
