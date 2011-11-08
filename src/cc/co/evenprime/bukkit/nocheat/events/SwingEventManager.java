package cc.co.evenprime.bukkit.nocheat.events;

import org.bukkit.event.Event;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.player.PlayerAnimationEvent;

import cc.co.evenprime.bukkit.nocheat.NoCheat;

/**
 * The only place that listens to and modifies player_move events if necessary
 * 
 * Get the event, decide which checks should work on it and in what order,
 * evaluate the check results and decide what to
 * 
 */
public class SwingEventManager extends EventManager {

    public SwingEventManager(NoCheat plugin) {

        super(plugin);

        registerListener(Event.Type.PLAYER_ANIMATION, Priority.Monitor, false);
    }

    @Override
    protected void handlePlayerAnimationEvent(PlayerAnimationEvent event, Priority priority) {
        plugin.getPlayer(event.getPlayer().getName()).getData().armswung = true;
    }
}
