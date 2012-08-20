package fr.neatmonster.nocheatplus.packets;

import net.minecraft.server.NetHandler;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet11PlayerPosition;

/*
 * M"""""""`YM MM'""""'YMM MM"""""""`YM MM"""""""`YM                   dP                  dP   d88  d88  
 * M  mmmm.  M M' .mmm. `M MM  mmmmm  M MM  mmmmm  M                   88                  88    88   88  
 * M  MMMMM  M M  MMMMMooM M'        .M M'        .M .d8888b. .d8888b. 88  .dP  .d8888b. d8888P  88   88  
 * M  MMMMM  M M  MMMMMMMM MM  MMMMMMMM MM  MMMMMMMM 88'  `88 88'  `"" 88888"   88ooood8   88    88   88  
 * M  MMMMM  M M. `MMM' .M MM  MMMMMMMM MM  MMMMMMMM 88.  .88 88.  ... 88  `8b. 88.  ...   88    88   88  
 * M  MMMMM  M MM.     .dM MM  MMMMMMMM MM  MMMMMMMM `88888P8 `88888P' dP   `YP `88888P'   dP   d88P d88P 
 * MMMMMMMMMMM MMMMMMMMMMM MMMMMMMMMMMM MMMMMMMMMMMM                                                      
 * 
 * MM"""""""`YM dP                                     MM"""""""`YM                   oo   dP   oo                   
 * MM  mmmmm  M 88                                     MM  mmmmm  M                        88                        
 * M'        .M 88 .d8888b. dP    dP .d8888b. 88d888b. M'        .M .d8888b. .d8888b. dP d8888P dP .d8888b. 88d888b. 
 * MM  MMMMMMMM 88 88'  `88 88    88 88ooood8 88'  `88 MM  MMMMMMMM 88'  `88 Y8ooooo. 88   88   88 88'  `88 88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88.  .88 88.  ... 88       MM  MMMMMMMM 88.  .88       88 88   88   88 88.  .88 88    88 
 * MM  MMMMMMMM dP `88888P8 `8888P88 `88888P' dP       MM  MMMMMMMM `88888P' `88888P' dP   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMMM                  .88                   MMMMMMMMMMMM                                                  
 *                           d8888P                                                                                  
 */
/**
 * A custom Packet11PlayerPosition.
 */
public class NCPPacket11PlayerPosition extends Packet11PlayerPosition {

    /* (non-Javadoc)
     * @see net.minecraft.server.Packet10Flying#handle(net.minecraft.server.NetHandler)
     */
    @Override
    public void handle(final NetHandler netHandler) {
        if (netHandler instanceof NetServerHandler && hasPos) {
            final NetServerHandler nsh = (NetServerHandler) netHandler;
            final double deltaX = Math.max(Math.abs(x), Math.abs(nsh.player.motX));
            final double deltaY = Math.max(Math.abs(y), Math.abs(nsh.player.motY));
            final double deltaZ = Math.max(Math.abs(z), Math.abs(nsh.player.motZ));
            final double delta = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
            if (delta > 100D) {
                nsh.player.locX = x;
                nsh.player.locY = y;
                nsh.player.locZ = z;
            }
        }
        super.handle(netHandler);
    }
}
