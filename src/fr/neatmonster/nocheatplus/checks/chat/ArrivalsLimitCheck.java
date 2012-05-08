package fr.neatmonster.nocheatplus.checks.chat;

import java.util.Arrays;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * A check used to limit the number of new players allowed to join in a specified time frame
 * 
 */
public class ArrivalsLimitCheck extends ChatCheck {

    public class ArrivalsLimitCheckEvent extends ChatEvent {

        public ArrivalsLimitCheckEvent(final ArrivalsLimitCheck check, final NCPPlayer player,
                final ActionList actions, final double vL) {
            super(check, player, actions, vL);
        }
    }

    // Used to know if the cooldown is enabled and since when
    private boolean  cooldown          = false;

    private long     cooldownStartTime = 0L;

    // Used to remember the latest joins;
    private long[]   joinsTimes        = null;

    private String[] joinsPlayers      = null;

    public ArrivalsLimitCheck() {
        super("arrivalslimit");
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final ChatConfig cc = getConfig(player);

        // Initialize the joins array
        if (joinsTimes == null)
            joinsTimes = new long[cc.arrivalsLimitPlayersLimit];
        if (joinsPlayers == null)
            joinsPlayers = new String[cc.arrivalsLimitPlayersLimit];

        boolean cancel = false;

        // If the new players cooldown is over
        if (cooldown && System.currentTimeMillis() - cooldownStartTime > cc.arrivalsLimitCooldownDelay) {
            // Stop the new players cooldown
            cooldown = false;
            cooldownStartTime = 0L;
        }

        // If the new players cooldown is active...
        else if (cooldown)
            // Kick the player who joined
            cancel = executeActions(player, cc.arrivalsLimitActions, 0);
        else if (System.currentTimeMillis() - joinsTimes[0] < cc.arrivalsLimitTimeframe) {
            // ...if more than limit new players have joined in less than limit time
            // Enable the new players cooldown
            cooldown = true;
            cooldownStartTime = System.currentTimeMillis();
            // Kick the player who joined
            cancel = executeActions(player, cc.arrivalsLimitActions, 0);
        }

        // Fill the joining times array
        if (!Arrays.asList(joinsPlayers).contains(player.getName())) {
            for (int i = 0; i < cc.arrivalsLimitPlayersLimit - 1; i++) {
                joinsTimes[i] = joinsTimes[i + 1];
                joinsPlayers[i] = joinsPlayers[i + 1];
            }
            joinsTimes[cc.arrivalsLimitPlayersLimit - 1] = System.currentTimeMillis();
            joinsPlayers[cc.arrivalsLimitPlayersLimit - 1] = player.getName();
        }

        return cancel;
    }

    @Override
    protected boolean executeActions(final NCPPlayer player, final ActionList actionList, final double violationLevel) {
        final ArrivalsLimitCheckEvent event = new ArrivalsLimitCheckEvent(this, player, actionList, violationLevel);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled())
            return super.executeActions(player, event.getActions(), event.getVL());
        return false;
    }
}
