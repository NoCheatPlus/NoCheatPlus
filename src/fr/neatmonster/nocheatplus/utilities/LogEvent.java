package fr.neatmonster.nocheatplus.utilities;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class LogEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    public static HandlerList getHandlerList() {
        return handlers;
    }

    private String message;
    private String prefix;

    private boolean toConsole, toChat, toFile;

    public LogEvent(final String prefix, final String message, final boolean toConsole, final boolean toChat,
            final boolean toFile) {
        this.prefix = prefix;
        this.message = message;
        this.toConsole = toConsole;
        this.toChat = toChat;
        this.toFile = toFile;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public String getMessage() {
        return message;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setMessage(final String message) {
        this.message = message;
    }

    public void setPrefix(final String prefix) {
        this.prefix = prefix;
    }

    public void setToChat(final boolean toChat) {
        this.toChat = toChat;
    }

    public void setToConsole(final boolean toConsole) {
        this.toConsole = toConsole;
    }

    public void setToFile(final boolean toFile) {
        this.toFile = toFile;
    }

    public boolean toChat() {
        return toChat;
    }

    public boolean toConsole() {
        return toConsole;
    }

    public boolean toFile() {
        return toFile;
    }
}
