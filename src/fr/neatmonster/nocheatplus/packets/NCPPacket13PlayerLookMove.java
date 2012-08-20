package fr.neatmonster.nocheatplus.packets;

import net.minecraft.server.NetHandler;
import net.minecraft.server.NetServerHandler;
import net.minecraft.server.Packet13PlayerLookMove;

/*
 * M"""""""`YM MM'""""'YMM MM"""""""`YM MM"""""""`YM                   dP                  dP   d88  d8888b. 
 * M  mmmm.  M M' .mmm. `M MM  mmmmm  M MM  mmmmm  M                   88                  88    88      `88 
 * M  MMMMM  M M  MMMMMooM M'        .M M'        .M .d8888b. .d8888b. 88  .dP  .d8888b. d8888P  88   aaad8' 
 * M  MMMMM  M M  MMMMMMMM MM  MMMMMMMM MM  MMMMMMMM 88'  `88 88'  `"" 88888"   88ooood8   88    88      `88 
 * M  MMMMM  M M. `MMM' .M MM  MMMMMMMM MM  MMMMMMMM 88.  .88 88.  ... 88  `8b. 88.  ...   88    88      .88 
 * M  MMMMM  M MM.     .dM MM  MMMMMMMM MM  MMMMMMMM `88888P8 `88888P' dP   `YP `88888P'   dP   d88P d88888P 
 * MMMMMMMMMMM MMMMMMMMMMM MMMMMMMMMMMM MMMMMMMMMMMM                                                         
 * 
 * MM"""""""`YM dP                                     M""MMMMMMMM                   dP       M"""""`'"""`YM                            
 * MM  mmmmm  M 88                                     M  MMMMMMMM                   88       M  mm.  mm.  M                            
 * M'        .M 88 .d8888b. dP    dP .d8888b. 88d888b. M  MMMMMMMM .d8888b. .d8888b. 88  .dP  M  MMM  MMM  M .d8888b. dP   .dP .d8888b. 
 * MM  MMMMMMMM 88 88'  `88 88    88 88ooood8 88'  `88 M  MMMMMMMM 88'  `88 88'  `88 88888"   M  MMM  MMM  M 88'  `88 88   d8' 88ooood8 
 * MM  MMMMMMMM 88 88.  .88 88.  .88 88.  ... 88       M  MMMMMMMM 88.  .88 88.  .88 88  `8b. M  MMM  MMM  M 88.  .88 88 .88'  88.  ... 
 * MM  MMMMMMMM dP `88888P8 `8888P88 `88888P' dP       M         M `88888P' `88888P' dP   `YP M  MMM  MMM  M `88888P' 8888P'   `88888P' 
 * MMMMMMMMMMMM                  .88                   MMMMMMMMMMM                            MMMMMMMMMMMMMM                            
 *                           d8888P                                                                                                     
 */
/**
 * A custom NCPPacket13PlayerLookMove.
 */
public class NCPPacket13PlayerLookMove extends Packet13PlayerLookMove {

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
