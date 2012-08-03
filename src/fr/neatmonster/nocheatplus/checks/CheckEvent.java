package fr.neatmonster.nocheatplus.checks;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/*
 * MM'""""'YMM dP                         dP       MM""""""""`M                              dP   
 * M' .mmm. `M 88                         88       MM  mmmmmmmM                              88   
 * M  MMMMMooM 88d888b. .d8888b. .d8888b. 88  .dP  M`      MMMM dP   .dP .d8888b. 88d888b. d8888P 
 * M  MMMMMMMM 88'  `88 88ooood8 88'  `"" 88888"   MM  MMMMMMMM 88   d8' 88ooood8 88'  `88   88   
 * M. `MMM' .M 88    88 88.  ... 88.  ... 88  `8b. MM  MMMMMMMM 88 .88'  88.  ... 88    88   88   
 * MM.     .dM dP    dP `88888P' `88888P' dP   `YP MM        .M 8888P'   `88888P' dP    dP   dP   
 * MMMMMMMMMMM                                     MMMMMMMMMMMM                                   
 */
/**
 * An event that is triggered by NoCheatPlus' API.
 */
public class CheckEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();

    /**
     * Gets the handler list.
     * 
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /** The player who has triggered the check. */
    private final Player player;

    /** Is the event cancelled? */
    private boolean      cancel = false;

    /**
     * Instantiates a new check event.
     * 
     * @param player
     *            the player
     */
    public CheckEvent(final Player player) {
        this.player = player;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.Event#getHandlers()
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the player who has triggered the event.
     * 
     * @return the player
     */
    public Player getPlayer() {
        return player;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.Cancellable#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /* (non-Javadoc)
     * @see org.bukkit.event.Cancellable#setCancelled(boolean)
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }
}
