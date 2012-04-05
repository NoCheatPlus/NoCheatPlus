package me.neatmonster.nocheatplus;

import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.data.DataStore;
import me.neatmonster.nocheatplus.data.ExecutionHistory;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class NoCheatPlusPlayerImpl implements NoCheatPlusPlayer {

    private Player                  player;
    private final NoCheatPlus       plugin;
    private final DataStore         data;
    private ConfigurationCacheStore config;
    private long                    lastUsedTime;
    private final ExecutionHistory  history;

    public NoCheatPlusPlayerImpl(final Player player, final NoCheatPlus plugin) {

        this.player = player;
        this.plugin = plugin;
        data = new DataStore();
        history = new ExecutionHistory();

        lastUsedTime = System.currentTimeMillis();
    }

    @Override
    public void dealFallDamage() {
        final EntityPlayer p = ((CraftPlayer) player).getHandle();
        p.b(0D, true);

    }

    @Override
    public ConfigurationCacheStore getConfigurationStore() {
        return config;
    }

    @Override
    public DataStore getDataStore() {
        return data;
    }

    @Override
    public ExecutionHistory getExecutionHistory() {
        return history;
    }

    @Override
    public float getJumpAmplifier() {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        if (ep.hasEffect(MobEffectList.JUMP)) {
            final int amp = ep.getEffect(MobEffectList.JUMP).getAmplifier();
            // Very rough estimates only
            if (amp > 20)
                return 1.5F * (ep.getEffect(MobEffectList.JUMP).getAmplifier() + 1);
            else
                return 1.2F * (ep.getEffect(MobEffectList.JUMP).getAmplifier() + 1);
        } else
            return 1.0F;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public float getSpeedAmplifier() {
        final EntityPlayer ep = ((CraftPlayer) player).getHandle();
        if (ep.hasEffect(MobEffectList.FASTER_MOVEMENT))
            // Taken directly from Minecraft code, should work
            return 1.0F + 0.2F * (ep.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        else
            return 1.0F;
    }

    @Override
    public int getTicksLived() {
        return player.getTicksLived();
    }

    @Override
    public boolean hasPermission(final String permission) {
        if (permission == null)
            // System.out.println("NoCheatPlus: Warning, asked for null permission");
            return false;
        return player.hasPermission(permission);
    }

    @Override
    public boolean isCreative() {
        return player.getGameMode() == GameMode.CREATIVE || player.getAllowFlight();
    }

    @Override
    public boolean isDead() {
        return player.getHealth() <= 0 || player.isDead();
    }

    @Override
    public boolean isSprinting() {
        return player.isSprinting();
    }

    public void refresh(final Player player) {
        this.player = player;
        config = plugin.getConfig(player);
    }

    public void setLastUsedTime(final long currentTimeInMilliseconds) {
        lastUsedTime = currentTimeInMilliseconds;
    }

    public boolean shouldBeRemoved(final long currentTimeInMilliseconds) {
        if (lastUsedTime > currentTimeInMilliseconds)
            // Should never happen, but if it does, fix it somewhat
            lastUsedTime = currentTimeInMilliseconds;
        return lastUsedTime + 60000L < currentTimeInMilliseconds;
    }
}
