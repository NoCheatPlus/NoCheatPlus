package fr.neatmonster.nocheatplus.checks;

import org.bukkit.event.Listener;

import fr.neatmonster.nocheatplus.players.NCPPlayer;

public abstract class CheckListener implements Listener {
    private final String group;

    public CheckListener(final String group) {
        this.group = group;
    }

    public CheckConfig getConfig(final NCPPlayer player) {
        return player.getConfig(group);
    }

    public CheckData getData(final NCPPlayer player) {
        return player.getData(group);
    }
}
