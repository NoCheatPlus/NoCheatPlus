package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public interface NoCheatPlayer {

    public boolean hasPermission(String permission);

    public String getName();

    public Player getPlayer();

    public BaseData getData();

    public boolean isDead();
    
    public boolean isSprinting();

    public int getTicksLived();

    public ConfigurationCache getConfiguration();

    public float getSpeedAmplifier();

    public boolean isCreative();
}
