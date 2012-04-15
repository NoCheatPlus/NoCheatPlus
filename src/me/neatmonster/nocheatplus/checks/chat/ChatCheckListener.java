package me.neatmonster.nocheatplus.checks.chat;

import java.util.LinkedList;
import java.util.List;

import me.neatmonster.nocheatplus.EventManager;
import me.neatmonster.nocheatplus.NoCheatPlus;
import me.neatmonster.nocheatplus.NoCheatPlusPlayer;
import me.neatmonster.nocheatplus.config.ConfigurationCacheStore;
import me.neatmonster.nocheatplus.config.Permissions;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.plugin.Plugin;

/**
 * Central location to listen to events that are
 * relevant for the chat checks
 * 
 */
public class ChatCheckListener implements Listener, EventManager {

    private final SpamCheck      spamCheck;
    private final SpamJoinsCheck spamJoinsCheck;
    private final ColorCheck     colorCheck;

    private final NoCheatPlus    plugin;

    public ChatCheckListener(final NoCheatPlus plugin) {

        this.plugin = plugin;

        spamCheck = new SpamCheck(plugin);
        spamJoinsCheck = new SpamJoinsCheck(plugin);
        colorCheck = new ColorCheck(plugin);
    }

    /**
     * We listen to PlayerChat events for obvious reasons
     * 
     * @param event
     *            The PlayerChat event
     */
    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void chat(final PlayerChatEvent event) {

        boolean cancelled = false;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final ChatConfig cc = ChatCheck.getConfig(player);
        final ChatData data = ChatCheck.getData(player);

        // Remember the original message
        data.message = event.getMessage();

        // Now do the actual checks

        // First the spam check
        if (cc.spamCheck && !player.hasPermission(Permissions.CHAT_SPAM))
            cancelled = spamCheck.check(player, data, cc);

        // Second the color check
        if (!cancelled && cc.colorCheck && !player.hasPermission(Permissions.CHAT_COLOR))
            cancelled = colorCheck.check(player, data, cc);

        // If one of the checks requested the event to be cancelled, do it
        if (cancelled)
            event.setCancelled(cancelled);
        else
            // In case one of the events modified the message, make sure that
            // the new message gets used
            event.setMessage(data.message);
    }

    /**
     * We listen to PlayerCommandPreprocess events because commands can be
     * used for spamming too.
     * 
     * @param event
     *            The PlayerCommandPreprocess Event
     */
    @EventHandler(
            priority = EventPriority.LOWEST)
    public void commandPreprocess(final PlayerCommandPreprocessEvent event) {

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final ChatConfig cc = ChatCheck.getConfig(player);

        // If the command is /plugins or /pl
        if ((event.getMessage().equalsIgnoreCase("/plugins")
                || event.getMessage().toLowerCase().startsWith("/plugins ")
                || event.getMessage().equalsIgnoreCase("/pl") || event.getMessage().toLowerCase().startsWith("/pl "))
                // Exception for 'PluginList'...
                && Bukkit.getPluginManager().getPlugin("PluginList") == null
                // ...and CommandHelper
                && Bukkit.getPluginManager().getPlugin("CommandHelper") == null && cc.hideNoCheatPlus) {
            // If the player isn't allowed to use this command
            if (!event.getPlayer().hasPermission("bukkit.command.plugins"))
                // Fake the permissions error message
                event.getPlayer().sendMessage(
                        ChatColor.RED + "I'm sorry, but you do not have permission to perform this command. "
                                + "Please contact the server administrators if you believe that this is in error.");
            else {
                // Fake the plugins list
                final StringBuilder pluginList = new StringBuilder();
                final Plugin[] plugins = Bukkit.getPluginManager().getPlugins();

                for (final Plugin plugin : plugins) {
                    // But make sure to hide NoCheatPlus from the plugins list
                    if (plugin.getName().equals("NoCheatPlus"))
                        continue;
                    if (pluginList.length() > 0) {
                        pluginList.append(ChatColor.WHITE);
                        pluginList.append(", ");
                    }

                    pluginList.append(plugin.isEnabled() ? ChatColor.GREEN : ChatColor.RED);
                    pluginList.append(plugin.getDescription().getName());
                }

                // Of course decrease the number of plugins
                event.getPlayer().sendMessage("Plugins (" + (plugins.length - 1) + "): " + pluginList.toString());
            }

            // Cancel the event, we have already replied to the player
            event.setCancelled(true);
        }

        // This type of event is derived from PlayerChatEvent, therefore
        // just treat it like that
        chat(event);
    }

    @Override
    public List<String> getActiveChecks(final ConfigurationCacheStore cc) {
        final LinkedList<String> s = new LinkedList<String>();

        final ChatConfig c = ChatCheck.getConfig(cc);
        if (c.spamCheck)
            s.add("chat.spam");
        if (c.spamJoinsCheck)
            s.add("chat.spamjoins");
        if (c.colorCheck)
            s.add("chat.color");
        return s;
    }

    @EventHandler(
            ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void login(final PlayerLoginEvent event) {

        // Only check new players (who has joined less than 10 minutes ago)
        if (System.currentTimeMillis() - event.getPlayer().getFirstPlayed() > 600000D)
            return;

        final NoCheatPlusPlayer player = plugin.getPlayer(event.getPlayer());
        final ChatConfig cc = ChatCheck.getConfig(player);
        final ChatData data = ChatCheck.getData(player);

        if (cc.spamJoinsCheck && spamJoinsCheck.check(player, data, cc))
            // If the player failed the check, disallow the login
            event.disallow(Result.KICK_OTHER, cc.spamJoinsKickMessage);
    }
}
