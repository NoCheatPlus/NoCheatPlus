package fr.neatmonster.nocheatplus.checks.fight;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * Abstract base class for Fight checks, provides some convenience
 * methods for access to data and config that's relevant to this checktype
 */
public abstract class FightCheck extends Check {

    public final String permission;

    public FightCheck(final String name, final String permission) {
        super("fight." + name, FightConfig.class, FightData.class);

        this.permission = permission;
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public FightConfig getConfig(final NCPPlayer player) {
        return (FightConfig) player.getConfig(this);
    }

    public FightData getData(final NCPPlayer player) {
        return (FightData) player.getData(this);
    }

    public abstract boolean isEnabled(final FightConfig cc);
}
