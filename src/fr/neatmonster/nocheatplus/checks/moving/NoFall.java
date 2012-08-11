package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Locale;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Packet10Flying;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;

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
    public void check(final EntityPlayer player, final Packet10Flying packet) {
        final Player bukkitPlayer = player.getBukkitEntity();
        final MovingConfig cc = MovingConfig.getConfig(bukkitPlayer);
        final MovingData data = MovingData.getData(bukkitPlayer);

        // Check the player is now on the ground (for the client and for the server).
        final boolean onGroundClient = packet.g;
        final AxisAlignedBB boundingBoxGround = player.boundingBox.clone().d(packet.x - player.locX,
                packet.y - player.locY - 0.001D, packet.z - player.locZ);
        final boolean onGroundServer = player.world.getCubes(player, boundingBoxGround).size() > 0;

        // If the packet has position information.
        if (packet.hasPos)
            // If the player has just started falling.
            if (data.noFallWasOnGroundServer && !onGroundServer)
                // Reset his fall distance.
                data.noFallFallDistance = 0D;
            else {
                final int id = player.world.getTypeId((int) Math.ceil(packet.x), (int) Math.ceil(packet.y),
                        (int) Math.ceil(packet.z));

                // If the player is falling into water.
                if (id > 7 && id < 12)
                    // Reset his fall distance.
                    data.noFallFallDistance = 0D;
                else if (player.locY - packet.y > 0D)
                    // Add the distance to the fall distance.
                    data.noFallFallDistance += player.locY - packet.y;
            }

        // If the player just touched the ground for the server, but no for the client.
        if (!data.noFallWasOnGroundServer && onGroundServer && (data.noFallWasOnGroundClient || !onGroundClient)) {
            // Calculate the fall damages to be dealt.
            final int fallDamage = (int) data.noFallFallDistance - 2;
            if (fallDamage > 1) {
                // Add the fall distance to the violation level.
                data.noFallVL += data.noFallFallDistance;

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(bukkitPlayer, data.noFallVL, cc.noFallActions))
                    // Deal the fall damages to the player.
                    bukkitPlayer.damage(fallDamage);
            }
        }

        // If the player just touched the ground for the server.
        else if (!data.noFallWasOnGroundServer && onGroundServer) {
            // Calculate the difference between the fall distance calculated by the server and by the plugin.
            final int difference = (int) data.noFallFallDistance - (int) bukkitPlayer.getFallDistance();

            // If the difference is too big and the fall distance calculated by the plugin should hurt the player.
            if (difference > 1 && (int) data.noFallFallDistance - 3 > 0) {
                // Add the difference to the violation level.
                data.noFallVL += data.noFallFallDistance - bukkitPlayer.getFallDistance();

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(bukkitPlayer, data.noFallVL, cc.noFallActions))
                    // Set the fall distance to its right value.
                    bukkitPlayer.setFallDistance((float) data.noFallFallDistance);
            }
        }

        // Remember if the player was on ground for the client and for the server.
        data.noFallWasOnGroundClient = onGroundClient;
        data.noFallWasOnGroundServer = onGroundServer;
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
