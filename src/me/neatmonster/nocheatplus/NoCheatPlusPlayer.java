package me.neatmonster.nocheatplus;

import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;
import me.neatmonster.nocheatplus.data.ExecutionHistory;

import org.bukkit.entity.Player;

public interface NoCheatPlusPlayer {

    public void dealFallDamage();

    public ConfigurationCacheStore getConfigurationStore();

    public DataStore getDataStore();

    public ExecutionHistory getExecutionHistory();

    public float getJumpAmplifier();

    public String getName();

    public Player getPlayer();

    public float getSpeedAmplifier();

    public int getTicksLived();

    public boolean hasPermission(String permission);

    public boolean isCreative();

    public boolean isDead();

    public boolean isSprinting();

    public void sendMessage(String message);

}
