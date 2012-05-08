package fr.neatmonster.nocheatplus.checks;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * The event created when actions are executed.
 */
public abstract class CheckEvent extends Event implements Cancellable {

    /** The Constant handlers. */
    private static final HandlerList handlers = new HandlerList();

    /**
     * Gets the handler list.
     * 
     * @return the handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }

    /** The check which has triggered this event. */
    private final Check     check;

    /** The player who has triggered this event. */
    private final NCPPlayer player;

    /** The actions which are going to be executed if the event isn't cancelled. */
    private ActionList      actions;

    /** The violation level of the player for the specified check. */
    private double          vL;

    /** The boolean used to know if the event is cancelled. */
    private boolean         cancel = false;

    /**
     * Instantiates a new check event.
     * 
     * @param check
     *            the check
     * @param player
     *            the player
     * @param actions
     *            the actions
     * @param vL
     *            the vL
     */
    public CheckEvent(final Check check, final NCPPlayer player, final ActionList actions, final double vL) {
        this.check = check;
        this.player = player;
        this.actions = actions;
        this.vL = vL;
    }

    /**
     * Gets the actions which are going to be executed if the event isn't cancelled.
     * 
     * @return the actions
     */
    public ActionList getActions() {
        return actions;
    }

    /**
     * Gets the check which has triggered this event.
     * 
     * @return the check
     */
    public Check getCheck() {
        return check;
    }

    /*
     * (non-Javadoc)
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
    public NCPPlayer getPlayer() {
        return player;
    }

    /**
     * Gets the violation level of the player for the specified check.
     * 
     * @return the vL
     */
    public double getVL() {
        return vL;
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.event.Cancellable#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Sets the actions which are going to be executed if the event isn't cancelled.
     * 
     * @param actions
     *            the new actions
     */
    public void setActions(final ActionList actions) {
        this.actions = actions;
    }

    /*
     * (non-Javadoc)
     * @see org.bukkit.event.Cancellable#setCancelled(boolean)
     */
    @Override
    public void setCancelled(final boolean cancel) {
        this.cancel = cancel;
    }

    /**
     * Sets the violation level of the player for the specified check.
     * 
     * @param vL
     *            the new vL
     */
    public void setVL(final double vL) {
        this.vL = vL;
    }
}
