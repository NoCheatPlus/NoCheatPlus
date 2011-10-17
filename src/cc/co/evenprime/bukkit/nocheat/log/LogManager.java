package cc.co.evenprime.bukkit.nocheat.log;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.config.Permissions;
import cc.co.evenprime.bukkit.nocheat.config.cache.ConfigurationCache;

/**
 * Manage logging throughout NoCheat. Messages may be logged directly to a
 * specific place or go through configuration/permissions to decide if and where
 * the message will be visible
 * 
 */
public class LogManager {

    public LogManager() {

    }

    /**
     * Check if and where the message should be logged to and then do it
     * 
     * @param level
     * @param message
     * @param cc
     */
    public void log(LogLevel level, String message, ConfigurationCache cc) {

        if(!cc.logging.active)
            return;

        if(cc.logging.fileLevel.matches(level)) {
            logToFile(level, ChatColor.stripColor(message), cc.logging.filelogger);
        }

        if(cc.logging.consoleLevel.matches(level)) {
            logToConsole(level, ChatColor.stripColor(message));
        }

        if(cc.logging.chatLevel.matches(level)) {
            logToChat(level, message);
        }
    }

    /**
     * Directly log to the server console, no checks
     * 
     * @param level
     * @param message
     */
    public void logToConsole(LogLevel level, String message) {
        Logger.getLogger("Minecraft").log(level.level, message);
    }

    /**
     * Directly log to the chat, no checks
     * 
     * @param level
     * @param message
     */
    private void logToChat(LogLevel level, String message) {
        for(Player player : Bukkit.getServer().getOnlinePlayers()) {
            if(player.hasPermission(Permissions.ADMIN_CHATLOG)) {
                player.sendMessage(message);
            }
        }
    }

    /**
     * Directly log to the file, no checks
     * 
     * @param level
     * @param message
     * @param fileLogger
     */
    private void logToFile(LogLevel level, String message, Logger fileLogger) {
        fileLogger.log(level.level, message);
    }
}
