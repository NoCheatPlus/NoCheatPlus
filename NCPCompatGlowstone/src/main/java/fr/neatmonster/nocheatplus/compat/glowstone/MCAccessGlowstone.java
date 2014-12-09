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
