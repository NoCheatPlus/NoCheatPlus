package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.plugin.PluginManager;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.InfinitedurabilityListener;

public class InfinitedurabilityCheck extends Check {

    private String  logMessage;
    private String  kickMessage;
    private boolean kick;

    public InfinitedurabilityCheck(NoCheat plugin, NoCheatConfiguration config) {
        super(plugin, "infinitedurability", PermissionData.PERMISSION_INFINITEDURABILITY, config);
    }

    public void check(PlayerItemHeldEvent event) {

        if(skipCheck(event.getPlayer()))
            return;
        
        if(event.getNewSlot() == 9) {
            

            Player player = event.getPlayer();
            
            String logString = String.format(logMessage, player.getName());
            plugin.log(Level.SEVERE, logString);
            
            if(kick) {
                player.kickPlayer(kickMessage);
            }
        }
    }

    @Override
    public void configure(NoCheatConfiguration config) {

        try {
            setActive(config.getBooleanValue("active.infinitedurability"));
            logMessage = config.getStringValue("infinitedurability.logmessage");
            logMessage = logMessage.replace("[player]", "%1$s");

            kickMessage = config.getStringValue("infinitedurability.kickmessage");
            
            kick = config.getBooleanValue("infinitedurability.kick");
        } catch(ConfigurationException e) {
            setActive(false);
            e.printStackTrace();
        }
    }

    @Override
    protected void registerListeners() {
        PluginManager pm = Bukkit.getServer().getPluginManager();

        // Register listeners for itemdupe check
        Listener bogusitemsPlayerListener = new InfinitedurabilityListener(this);

        // Register listeners for itemdupe check
        pm.registerEvent(Event.Type.PLAYER_ITEM_HELD, bogusitemsPlayerListener, Priority.Lowest, plugin);
    }
}
