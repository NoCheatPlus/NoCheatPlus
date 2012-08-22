package fr.neatmonster.nocheatplus.packets;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.server.Packet;

/*
 * MM"""""""`YM                   dP                  dP            
 * MM  mmmmm  M                   88                  88            
 * M'        .M .d8888b. .d8888b. 88  .dP  .d8888b. d8888P .d8888b. 
 * MM  MMMMMMMM 88'  `88 88'  `"" 88888"   88ooood8   88   Y8ooooo. 
 * MM  MMMMMMMM 88.  .88 88.  ... 88  `8b. 88.  ...   88         88 
 * MM  MMMMMMMM `88888P8 `88888P' dP   `YP `88888P'   dP   `88888P' 
 * MMMMMMMMMMMM                                                     
 * 
 * M""MMM""MMM""M                   dP                                                          dP 
 * M  MMM  MMM  M                   88                                                          88 
 * M  MMP  MMP  M .d8888b. 88d888b. 88  .dP  .d8888b. 88d888b. .d8888b. dP    dP 88d888b. .d888b88 
 * M  MM'  MM' .M 88'  `88 88'  `88 88888"   88'  `88 88'  `88 88'  `88 88    88 88'  `88 88'  `88 
 * M  `' . '' .MM 88.  .88 88       88  `8b. 88.  .88 88       88.  .88 88.  .88 88    88 88.  .88 
 * M    .d  .dMMM `88888P' dP       dP   `YP `88888P8 dP       `88888P' `88888P' dP    dP `88888P8 
 * MMMMMMMMMMMMMM                                                                                  
 */
/**
 * The packets workaround.
 */
public class PacketsWorkaround {

    /** The old classes. */
    private static Map<Integer, Class<?>> oldClasses = new HashMap<Integer, Class<?>>();

    /**
     * Disable the packets workaround.
     */
    public static void disable() {
        for (final int packetId : oldClasses.keySet())
            replace(packetId, oldClasses.get(packetId));
    }

    /**
     * Enable the packets workaround.
     */
    public static void enable() {
        oldClasses.put(11, replace(11, NCPPacket11PlayerPosition.class));
        oldClasses.put(13, replace(13, NCPPacket13PlayerLookMove.class));
    }

    /**
     * Replace a packet class.
     * 
     * @param packetId
     *            the packet id
     * @param newClass
     *            the new class
     * @return the class
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static Class<?> replace(final int packetId, final Class<?> newClass) {
        final Class<?> oldClass = (Class<?>) Packet.l.d(packetId);
        Packet.l.a(packetId, newClass);
        try {
            final Field aField = Packet.class.getDeclaredField("a");
            aField.setAccessible(true);
            final Map a = (Map) aField.get(null);
            a.put(newClass, packetId);
        } catch (final Exception e) {
            e.printStackTrace();
        }
        return oldClass;
    }
}