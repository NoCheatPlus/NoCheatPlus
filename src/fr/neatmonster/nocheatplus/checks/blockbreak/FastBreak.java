package fr.neatmonster.nocheatplus.checks.blockbreak;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/*
 * MM""""""""`M                     dP   M#"""""""'M                             dP       
 * MM  mmmmmmmM                     88   ##  mmmm. `M                            88       
 * M'      MMMM .d8888b. .d8888b. d8888P #'        .M 88d888b. .d8888b. .d8888b. 88  .dP  
 * MM  MMMMMMMM 88'  `88 Y8ooooo.   88   M#  MMMb.'YM 88'  `88 88ooood8 88'  `88 88888"   
 * MM  MMMMMMMM 88.  .88       88   88   M#  MMMM'  M 88       88.  ... 88.  .88 88  `8b. 
 * MM  MMMMMMMM `88888P8 `88888P'   dP   M#       .;M dP       `88888P' `88888P8 dP   `YP 
 * MMMMMMMMMMMM                          M#########M                                      
 */
/**
 * A check used to verify if the player isn't breaking his blocks too quickly.
 */
public class FastBreak extends Check {

    /** The minimum time that needs to be elapsed between two block breaks for a player in creative mode. */
    private static final long CREATIVE  = 95L;

    /** The minimum time that needs to be elapsed between two block breaks for a player in survival mode. */
    private static final long SURVIVAL  = 45L;

    /** The multiplicative constant used when incrementing the limit. */
    private static final int  TOLERANCE = 10;

    /**
     * Instantiates a new fast break check.
     */
    public FastBreak() {
        super(CheckType.BLOCKBREAK_FASTBREAK);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param block
     *            the block
     * @return true, if successful
     */
    public boolean check(final Player player, final Block block) {
        final BlockBreakConfig cc = BlockBreakConfig.getConfig(player);
        final BlockBreakData data = BlockBreakData.getData(player);

        boolean cancel = false;

        // First, check the game mode of the player and choose the right limit.
        long timeLimit = Math.round(cc.fastBreakInterval / 100D * SURVIVAL);
        if (player.getGameMode() == GameMode.CREATIVE)
            timeLimit = Math.round(cc.fastBreakInterval / 100D * CREATIVE);

        // This is the experimental mode (incrementing the limit according to the violation level).
        long elapsedTimeLimit = timeLimit;
        if (cc.fastBreakExperimental)
            elapsedTimeLimit = Math.min(timeLimit + Math.round(data.fastBreakVL / TOLERANCE), CREATIVE);

        // The elapsed time is the difference between the last damage time and the last break time.
        final long elapsedTime = data.fastBreakDamageTime - data.fastBreakBreakTime;
        if (elapsedTime < elapsedTimeLimit && data.fastBreakBreakTime > 0L && data.fastBreakDamageTime > 0L
                && (player.getItemInHand().getType() != Material.SHEARS || block.getType() != Material.LEAVES)) {
            // If the buffer has been consumed.
            if (data.fastBreakBuffer == 0) {
                // Increment the violation level (but using the original limit).
                data.fastBreakVL += Math.max(timeLimit - elapsedTime, 0D);

                // Cancel the event if needed.
                cancel = executeActions(player, data.fastBreakVL, Math.max(timeLimit - elapsedTime, 0D),
                        cc.fastBreakActions);
            } else
                // Remove one from the buffer.
                data.fastBreakBuffer--;
        } else {
            // If the buffer isn't full.
            if (data.fastBreakBuffer < cc.fastBreakBuffer)
                // Add one to the buffer.
                data.fastBreakBuffer++;

            // Reduce the violation level, the player was nice with blocks.
            data.fastBreakVL *= 0.9D;
        }

        // Remember the block breaking time.
        data.fastBreakBreakTime = System.currentTimeMillis();

        return cancel;
    }
}
