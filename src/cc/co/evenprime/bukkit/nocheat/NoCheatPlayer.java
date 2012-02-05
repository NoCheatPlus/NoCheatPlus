package cc.co.evenprime.bukkit.nocheat;

import org.bukkit.entity.Player;
import cc.co.evenprime.bukkit.nocheat.config.ConfigurationCacheStore;
import cc.co.evenprime.bukkit.nocheat.data.DataStore;
import cc.co.evenprime.bukkit.nocheat.data.ExecutionHistory;

public interface NoCheatPlayer {

    public boolean hasPermission(String permission);

    public String getName();

    public Player getPlayer();

    public DataStore getDataStore();

    public boolean isDead();

    public boolean isSprinting();

    public int getTicksLived();

    public ConfigurationCacheStore getConfigurationStore();

    public float getSpeedAmplifier();

    public float getJumpAmplifier();

    public boolean isCreative();

    public ExecutionHistory getExecutionHistory();

}
