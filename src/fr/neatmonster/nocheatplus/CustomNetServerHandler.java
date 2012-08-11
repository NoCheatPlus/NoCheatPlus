package fr.neatmonster.nocheatplus;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.INetworkManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet10Flying;

/*
 * MM'""""'YMM                     dP                       M"""""""`YM            dP   
 * M' .mmm. `M                     88                       M  mmmm.  M            88   
 * M  MMMMMooM dP    dP .d8888b. d8888P .d8888b. 88d8b.d8b. M  MMMMM  M .d8888b. d8888P 
 * M  MMMMMMMM 88    88 Y8ooooo.   88   88'  `88 88'`88'`88 M  MMMMM  M 88ooood8   88   
 * M. `MMM' .M 88.  .88       88   88   88.  .88 88  88  88 M  MMMMM  M 88.  ...   88   
 * MM.     .dM `88888P' `88888P'   dP   `88888P' dP  dP  dP M  MMMMM  M `88888P'   dP   
 * MMMMMMMMMMM                                              MMMMMMMMMMM                 
 * 
 * MP""""""`MM                                              M""MMMMM""MM                         dP dP                   
 * M  mmmmm..M                                              M  MMMMM  MM                         88 88                   
 * M.      `YM .d8888b. 88d888b. dP   .dP .d8888b. 88d888b. M         `M .d8888b. 88d888b. .d888b88 88 .d8888b. 88d888b. 
 * MMMMMMM.  M 88ooood8 88'  `88 88   d8' 88ooood8 88'  `88 M  MMMMM  MM 88'  `88 88'  `88 88'  `88 88 88ooood8 88'  `88 
 * M. .MMM'  M 88.  ... 88       88 .88'  88.  ... 88       M  MMMMM  MM 88.  .88 88    88 88.  .88 88 88.  ... 88       
 * Mb.     .dM `88888P' dP       8888P'   `88888P' dP       M  MMMMM  MM `88888P8 dP    dP `88888P8 dP `88888P' dP       
 * MMMMMMMMMMM                                              MMMMMMMMMMMM                                                 
 */
/**
 * A custom NetServerHandler used to intercept packets sent by the player.
 */
public class CustomNetServerHandler extends NetServerHandler {

    /** The distance the player has falled. */
    public double  fallDistance;

    /** Is the player on the ground? Client-side value. */
    public boolean onGroundClient;

    /** Is the player on the ground? Server-side value. */
    public boolean onGroundServer;

    /** Was the player on the ground? Client-side value. */
    public boolean wasOnGroundClient;

    /** Was the player on the ground? Server-side value. */
    public boolean wasOnGroundServer;

    /**
     * Instantiates a new custom net server handler.
     * 
     * @param minecraftserver
     *            the minecraftserver
     * @param inetworkmanager
     *            the inetworkmanager
     * @param entityplayer
     *            the entityplayer
     */
    public CustomNetServerHandler(final MinecraftServer minecraftserver, final INetworkManager inetworkmanager,
            final EntityPlayer entityplayer) {
        super(minecraftserver, inetworkmanager, entityplayer);
    }

    /* (non-Javadoc)
     * @see net.minecraft.server.NetServerHandler#a(net.minecraft.server.Packet10Flying)
     */
    @Override
    public void a(final Packet10Flying packet) {
        wasOnGroundClient = onGroundClient;
        wasOnGroundServer = onGroundServer;
        onGroundClient = packet.g;
        final AxisAlignedBB boundingBoxGround = player.boundingBox.clone().d(packet.x - player.locX,
                packet.y - player.locY - 0.001D, packet.z - player.locZ);
        onGroundServer = player.world.getCubes(player, boundingBoxGround).size() > 0;
        if (packet.hasPos && wasOnGroundServer && !onGroundServer)
            fallDistance = 0D;
        else if (packet.hasPos && player.locY - packet.y > 0D)
            fallDistance += player.locY - packet.y;
        super.a(packet);
    }
}
