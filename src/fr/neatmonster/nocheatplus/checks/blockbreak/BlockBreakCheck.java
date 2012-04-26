package fr.neatmonster.nocheatplus.checks.blockbreak;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.players.NCPPlayer;

/**
 * Abstract base class for BlockBreakChecks.
 */
public abstract class BlockBreakCheck extends Check {

    public BlockBreakCheck(final String name) {
        super("blockbreak." + name, BlockBreakConfig.class, BlockBreakData.class);
    }

    public abstract boolean check(final NCPPlayer player, final Object... args);

    public BlockBreakConfig getConfig(final NCPPlayer player) {
        return (BlockBreakConfig) player.getConfig(this);
    }

    public BlockBreakData getData(final NCPPlayer player) {
        return (BlockBreakData) player.getData(this);
    }
}
