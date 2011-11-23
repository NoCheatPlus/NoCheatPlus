package cc.co.evenprime.bukkit.nocheat.player;

import java.lang.reflect.Method;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.GameMode;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.NoCheatPlayer;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;
import cc.co.evenprime.bukkit.nocheat.data.BaseData;

public class NoCheatPlayerImpl implements NoCheatPlayer {

    protected Player         player;
    protected final NoCheat  plugin;
    protected final BaseData data;
    protected long           lastUsedTime;

    // The method that's used to artifically "fast-forward" the player
    protected static Method  incAge = null;

    public NoCheatPlayerImpl(Player player, NoCheat plugin, BaseData data) {

        this.player = player;
        this.plugin = plugin;
        this.data = data;

        this.lastUsedTime = System.currentTimeMillis();
    }

    public void refresh(Player player) {
        this.player = player;
    }

    public boolean hasPermission(String permission) {
        if(permission == null) {
            System.out.println("NoCheat: Warning, asked for null permission");
            return false;
        }
        return player.hasPermission(permission);
    }

    public BaseData getData() {
        return data;
    }

    public Player getPlayer() {
        return player;
    }

    public ConfigurationCache getConfiguration() {
        return plugin.getConfig(player);
    }

    public String getName() {
        return player.getName();
    }

    public int getTicksLived() {
        return player.getTicksLived();
    }

    public void increaseAge(int ticks) {

        if(incAge == null) {
            player.setTicksLived(player.getTicksLived() + ticks);
            return;
        }

        EntityPlayer p = ((CraftPlayer) player).getHandle();

        for(int i = 0; i < ticks; i++) {
            try {
                incAge.invoke(p, true);
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public float getSpeedAmplifier() {
        EntityPlayer ep = ((CraftPlayer) player).getHandle();
        if(ep.hasEffect(MobEffectList.FASTER_MOVEMENT)) {
            // Taken directly from Minecraft code, should work
            return 1.0F + 0.2F * (float) (ep.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        } else {
            return 1.0F;
        }
    }

    public boolean isSprinting() {
        return player.isSprinting();
    }

    public void setLastUsedTime(long currentTimeInMilliseconds) {
        this.lastUsedTime = currentTimeInMilliseconds;
    }

    public boolean shouldBeRemoved(long currentTimeInMilliseconds) {
        if(lastUsedTime > currentTimeInMilliseconds) {
            // Should never happen, but if it does, fix it somewhat
            lastUsedTime = currentTimeInMilliseconds;
        }
        return lastUsedTime + 60000L < currentTimeInMilliseconds;
    }

    public boolean isCreative() {
        return player.getGameMode() == GameMode.CREATIVE;
    }
}
