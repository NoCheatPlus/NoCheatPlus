package fr.neatmonster.nocheatplus.players;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckConfig;
import fr.neatmonster.nocheatplus.checks.CheckData;
import fr.neatmonster.nocheatplus.players.informations.ExecutionHistory;
import fr.neatmonster.nocheatplus.players.informations.Statistics;

public class NCPPlayer {
    public class ConfigByCheck {
        private final String                   worldName;

        private final Map<String, CheckConfig> configMap = new HashMap<String, CheckConfig>();

        public ConfigByCheck(final String worldName) {
            this.worldName = worldName;
        }

        public CheckConfig getConfig(final String group) {
            if (!configMap.containsKey(group))
                configMap.put(group, Check.newConfig(group, worldName));
            return configMap.get(group);
        }
    }

    private static final Map<String, NCPPlayer> players = new HashMap<String, NCPPlayer>();

    public static NCPPlayer getPlayer(final Player bukkitPlayer) {
        if (!players.containsKey(bukkitPlayer.getName()))
            players.put(bukkitPlayer.getName(), new NCPPlayer(bukkitPlayer));
        NCPPlayer player = players.get(bukkitPlayer.getName());
        if (player.getBukkitPlayer() != bukkitPlayer)
            players.put(bukkitPlayer.getName(), player = new NCPPlayer(bukkitPlayer));
        return player;
    }

    public static boolean hasPermission(final CommandSender sender, final String permission) {
        String subPermission = "";
        for (final String partOfPermission : permission.split("\\.")) {
            subPermission += (subPermission == "" ? "" : ".") + partOfPermission;
            if (sender.hasPermission(permission) || sender.hasPermission(permission + ".*"))
                return true;
        }
        return false;
    }

    private final String                     name;

    private Player                           bukkitPlayer;
    private final Map<String, ConfigByCheck> configMap  = new HashMap<String, ConfigByCheck>();
    private final Map<String, CheckData>     dataMap    = new HashMap<String, CheckData>();
    private final ExecutionHistory           history    = new ExecutionHistory();

    private final Statistics                 statistics = new Statistics();

    public NCPPlayer(final Player bukkitPlayer) {
        name = bukkitPlayer.getName();
        this.bukkitPlayer = bukkitPlayer;
    }

    public boolean canFly() {
        return bukkitPlayer.getGameMode() == GameMode.CREATIVE || bukkitPlayer.getAllowFlight();
    }

    public Map<String, Object> collectData() {
        final Map<String, Object> map = statistics.get();

        map.put("nocheatplus.version", NoCheatPlus.instance.getDescription().getVersion());

        return map;
    }

    public void dealFallDamage() {
        final EntityPlayer p = ((CraftPlayer) bukkitPlayer).getHandle();
        p.b(0D, true);
    }

    public Player getBukkitPlayer() {
        return bukkitPlayer;
    }

    public CheckConfig getConfig(final Check check) {
        return getConfig(check.getGroup());
    }

    public CheckConfig getConfig(final String group) {
        final String worldName = getWorld().getName();
        if (!configMap.containsKey(worldName))
            configMap.put(worldName, new ConfigByCheck(worldName));
        return configMap.get(worldName).getConfig(group);
    }

    public CheckData getData(final Check check) {
        return getData(check.getGroup());
    }

    public CheckData getData(final String group) {
        if (!dataMap.containsKey(group))
            dataMap.put(group, Check.newData(group));
        return dataMap.get(group);
    }

    public ExecutionHistory getExecutionHistory() {
        return history;
    }

    public float getJumpAmplifier() {
        final EntityPlayer ep = ((CraftPlayer) bukkitPlayer).getHandle();
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

    public Location getLocation() {
        return bukkitPlayer.getLocation();
    }

    public String getName() {
        return name;
    }

    public float getSpeedAmplifier() {
        final EntityPlayer ep = ((CraftPlayer) bukkitPlayer).getHandle();
        if (ep.hasEffect(MobEffectList.FASTER_MOVEMENT))
            // Taken directly from Minecraft code, should work
            return 1.0F + 0.2F * (ep.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier() + 1);
        else
            return 1.0F;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public World getWorld() {
        return bukkitPlayer.getWorld();
    }

    public boolean hasPermission(final String permission) {
        String subPermission = "";
        for (final String partOfPermission : permission.split("\\.")) {
            subPermission += (subPermission == "" ? "" : ".") + partOfPermission;
            if (bukkitPlayer.hasPermission(permission) || bukkitPlayer.hasPermission(permission + ".*"))
                return true;
        }
        return false;
    }

    public void refresh() {
        bukkitPlayer = Bukkit.getPlayer(name);
    }

    public void sendMessage(final String message) {
        bukkitPlayer.sendMessage(message);
    }
}
