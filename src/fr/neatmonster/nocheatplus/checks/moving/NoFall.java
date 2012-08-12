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
        
      	if (player.isInsideVehicle()){
      		// Emergency fix attempt:
      		data.clearFlyData();
      		player.setFallDistance(0.0f);
      		return;
      	}
        
        // If the player has just started falling, is falling into a liquid, in web or is on a ladder.
        if (to.isInLiquid() || to.isInWeb() || to.isOnLadder())
            // Reset his fall distance.
            data.noFallFallDistance = 0D;

        data.noFallFallDistance = data.noFallFallDistance; // TODO: ??

        // If the player just touched the ground for the server, but no for the client.
        if (!data.noFallWasOnGroundServer && data.noFallOnGroundServer
                && (data.noFallWasOnGroundClient || !data.noFallOnGroundClient)) {
            // Calculate the fall damages to be dealt.
            final int fallDamage = (int) data.noFallFallDistance - 2;
            if (fallDamage > 0) {
                // Add the fall distance to the violation level.
                data.noFallVL += data.noFallFallDistance;

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(player, data.noFallVL, cc.noFallActions))
                    // Deal the fall damages to the player.
                    player.damage(fallDamage);
            }
        }

        // If the player just touched the ground for the server.
        else if (!data.noFallWasOnGroundServer && data.noFallOnGroundServer) {
            // Calculate the difference between the fall distance calculated by the server and by the plugin.
            final double difference = (data.noFallFallDistance - player.getFallDistance()) / data.noFallFallDistance;

            // If the difference is too big and the fall distance calculated by the plugin should hurt the player.
            if (difference > 0.15D && (int) data.noFallFallDistance > 2) {
                // Add the difference to the violation level.
                data.noFallVL += data.noFallFallDistance - player.getFallDistance();

                // Execute the actions to find out if we need to cancel the event or not.
                if (executeActions(player, data.noFallVL, cc.noFallActions))
                    // Set the fall distance to its right value.
                    player.setFallDistance((float) data.noFallFallDistance);
            } else
                // Reward the player by lowering his violation level.
                data.noFallVL *= 0.95D;
        } else
            // Reward the player by lowering his violation level.
            data.noFallVL *= 0.95D;
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

    /**
     * Handle a movement packet to extract its precious information.
     * 
     * @param player
     *            the player
     * @param packet
     *            the packet
     */
    public void handlePacket(final EntityPlayer player, final Packet10Flying packet) {
        final MovingData data = MovingData.getData(player.getBukkitEntity());
        
        // Attempt to fix vehicle problems:
        if (player.getBukkitEntity().isInsideVehicle()) return;
        	
        data.noFallWasOnGroundClient = data.noFallOnGroundClient;
        data.noFallWasOnGroundServer = data.noFallOnGroundServer;
        data.noFallOnGroundClient = packet.g;
        final AxisAlignedBB boundingBoxGround = player.boundingBox.clone().d(packet.x - player.locX,
                packet.y - player.locY - 0.001D, packet.z - player.locZ);
        data.noFallOnGroundServer = player.world.getCubes(player, boundingBoxGround).size() > 0;
        if (packet.hasPos && data.noFallWasOnGroundServer && !data.noFallOnGroundServer)
            data.noFallFallDistance = 0D;
        else if (packet.hasPos && player.locY - packet.y > 0D)
            data.noFallFallDistance += player.locY - packet.y;
    }
}
