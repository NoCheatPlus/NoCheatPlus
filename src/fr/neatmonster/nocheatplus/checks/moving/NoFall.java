package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;

/*
 * M"""""""`YM          MM""""""""`M          dP dP 
 * M  mmmm.  M          MM  mmmmmmmM          88 88 
 * M  MMMMM  M .d8888b. M'      MMMM .d8888b. 88 88 
 * M  MMMMM  M 88'  `88 MM  MMMMMMMM 88'  `88 88 88 
 * M  MMMMM  M 88.  .88 MM  MMMMMMMM 88.  .88 88 88 
 * M  MMMMM  M `88888P' MM  MMMMMMMM `88888P8 dP dP 
 * MMMMMMMMMMM          MMMMMMMMMMMM                
 */
/**
 * A check to see if people cheat by tricking the server to not deal them fall damage.
 */
public class NoFall extends Check {

    /**
     * Instantiates a new no fall check.
     */
    public NoFall() {
        super(CheckType.MOVING_NOFALL);
    }

    /**
     * Checks a player.
     * 
     * @param player
     *            the player
     * @param from
     *            the from
     * @param to
     *            the to
     */
    public void check(final Player player, final PlayerLocation from, final PlayerLocation to) {
        final MovingConfig cc = MovingConfig.getConfig(player);
        final MovingData data = MovingData.getData(player);

        data.noFallWasOnGround = data.noFallOnGround;
        data.noFallOnGround = to.isOnGround();

        // If the player is on the ground, is falling into a liquid, in web or is on a ladder.
        if (from.isOnGround() && to.isOnGround() || to.isInLiquid() || to.isInWeb() || to.isOnLadder())
            data.noFallFallDistance = 0D;

        // If the player just touched the ground for the server.
        if (!data.noFallWasOnGround && data.noFallOnGround) {
            // If the difference between the fall distance recorded by Bukkit and NoCheatPlus is too big and the fall
            // distance bigger than 2.
            if (data.noFallFallDistance - player.getFallDistance() > 0.1D && (int) data.noFallFallDistance > 2) {
                // Add the difference to the violation level.
                data.noFallVL += data.noFallFallDistance - player.getFallDistance();

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(player, data.noFallVL, data.noFallFallDistance - player.getFallDistance(),
                        cc.noFallActions))
                    // Set the fall distance to its right value.
                    player.setFallDistance((float) data.noFallFallDistance);
            } else
                // Reward the player by lowering his violation level.
                data.noFallVL *= 0.95D;
        } else
            // Reward the player by lowering his violation level.
            data.noFallVL *= 0.95D;

        // The player has touched the ground somewhere, reset his fall distance.
        if (!data.noFallWasOnGround && data.noFallOnGround || data.noFallWasOnGround && !data.noFallOnGround)
            data.noFallFallDistance = 0D;

        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if (to.getY() > 0 && entityPlayer.locY > to.getY())
            data.noFallFallDistance += entityPlayer.locY - to.getY();
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.Check#getParameter(fr.neatmonster.nocheatplus.actions.ParameterName,
     * org.bukkit.entity.Player)
     */
    @Override
    public String getParameter(final ParameterName wildcard, final ViolationData violationData) {
        if (wildcard == ParameterName.FALL_DISTANCE)
            return String.format(Locale.US, "%.2f", MovingData.getData(violationData.player).noFallFallDistance);
        else
            return super.getParameter(wildcard, violationData);
    }
}
