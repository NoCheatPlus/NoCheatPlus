package fr.neatmonster.nocheatplus.logging.debug;

import org.bukkit.Location;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PlayerLocation;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.TrigUtil;

/**
 * Some auxiliary static-access methods.
 * @author mc_dev
 *
 */
public class DebugUtil {

    // TODO: Add useLoc1 and useLoc2.

    /**
     * Just the coordinates.
     * @param loc
     * @return
     */
    public static String formatLocation(final Location loc) {
        StringBuilder b = new StringBuilder(128);
        addLocation(loc, b);
        return b.toString();
    }

    /**
     * Just the coordinates.
     * @param from
     * @param to
     * @return
     */
    public static String formatMove(Location from, Location to) {
        StringBuilder builder = new StringBuilder(128);
        DebugUtil.addMove(from, to, null, builder);
        return builder.toString();
    }

    public static void addLocation(final double x, final double y, final double z, final StringBuilder builder){
        builder.append(x + ", " + y + ", " + z);
    }

    public static void addLocation(final Location loc, final StringBuilder builder){
        addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
    }

    public static void addLocation(final PlayerLocation loc, final StringBuilder builder){
        addLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
    }

    public static void addFormattedLocation(final double x, final double y, final double z, final StringBuilder builder){
        builder.append(StringUtil.fdec3.format(x) + ", " + StringUtil.fdec3.format(y) + ", " + StringUtil.fdec3.format(z));
    }

    public static void addFormattedLocation(final Location loc, final StringBuilder builder){
        addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
    }

    public static void addFormattedLocation(final PlayerLocation loc, final StringBuilder builder){
        addFormattedLocation(loc.getX(), loc.getY(), loc.getZ(), builder);
    }


    /**
     * With line break between from and to.
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param toX
     * @param toY
     * @param toZ
     * @param builder
     */
    public static void addMove(final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ, final StringBuilder builder){
        builder.append("from: ");
        addLocation(fromX, fromY, fromZ, builder);
        builder.append("\nto: ");
        addLocation(toX, toY, toZ, builder);
    }

    /**
     * No line breaks, max. 3 digits after comma.
     * @param fromX
     * @param fromY
     * @param fromZ
     * @param toX
     * @param toY
     * @param toZ
     * @param builder
     */
    public static void addFormattedMove(final double fromX, final double fromY, final double fromZ, final double toX, final double toY, final double toZ, final StringBuilder builder){
        addFormattedLocation(fromX, fromY, fromZ, builder);
        builder.append(" -> ");
        addFormattedLocation(toX, toY, toZ, builder);
    }

    /**
     * 3 decimal digits after comma (StringUtil.fdec3). No leading new line.
     * @param from
     * @param to
     * @param loc Reference location for from, usually Player.getLocation(). May be null.
     * @param builder
     * @return
     */
    public static void addFormattedMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
        if (loc != null && !from.isSamePos(loc)){
            builder.append("(");
            addFormattedLocation(loc, builder);
            builder.append(") ");
        }
        addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);	
    }

    /**
     * Add exact coordinates, multiple lines. No leading new line.
     * @param from
     * @param to
     * @param loc Reference location for from, usually Player.getLocation().
     * @param builder
     */
    public static void addMove(final PlayerLocation from, final PlayerLocation to, final Location loc, final StringBuilder builder){
        if (loc != null && !from.isSamePos(loc)){
            builder.append("Location: ");
            addLocation(loc, builder);
            builder.append("\n");
        }
        addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);	
    }

    /**
     * 3 decimal digits after comma (StringUtil.fdec3). No leading new line.
     * @param from
     * @param to
     * @param loc Reference location for from, usually Player.getLocation().
     * @param builder
     * @return
     */
    public static void addFormattedMove(final Location from, final Location to, final Location loc, final StringBuilder builder){
        if (loc != null && !TrigUtil.isSamePos(from, loc)){
            builder.append("(");
            addFormattedLocation(loc, builder);
            builder.append(") ");
        }
        addFormattedMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);		}

    /**
     * Add exact coordinates, multiple lines. No leading new line.
     * @param from
     * @param to
     * @param loc Reference location for from, usually Player.getLocation().
     * @param builder
     */
    public static void addMove(final Location from, final Location to, final Location loc, final StringBuilder builder){
        if (loc != null && !TrigUtil.isSamePos(from, loc)){
            builder.append("Location: ");
            addLocation(loc, builder);
            builder.append("\n");
        }
        addMove(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ(), builder);
    }

    public static void addBlockBelowInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
        addBlockInfo(builder, loc.getBlockCache(), loc.getBlockX(), loc.getBlockY() - 1, loc.getBlockZ(), tag + " below");
    }

    public static void addBlockInfo(final StringBuilder builder, final PlayerLocation loc, final String tag) {
        addBlockInfo(builder, loc.getBlockCache(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), tag);
    }

    /**
     * Add information about id, data, shape to the builder. Starts with a space
     * always.
     * 
     * @param builder
     * @param blockCache
     * @param x
     *            Block coordinates.
     * @param y
     * @param z
     * @param messagePrefix
     *            May be null.
     */
    public static void addBlockInfo(final StringBuilder builder, final BlockCache blockCache, final int x, final int y, final int z, final String messagePrefix) {
        if (messagePrefix != null && ! messagePrefix.isEmpty()) {
            builder.append(messagePrefix);
        }
        builder.append(" id=");
        final int id = blockCache.getTypeId(x, y, z);
        builder.append(id);
        builder.append(" data=");
        builder.append(blockCache.getData(x, y, z));
        final double[] bounds = blockCache.getBounds(x, y, z);
        if (bounds != null) {
            final double minHeight = BlockProperties.getGroundMinHeight(blockCache, x, y, z, id, bounds, BlockProperties.getBlockFlags(id));
            builder.append(" shape=[");
            builder.append(bounds[0]);
            builder.append(", ");
            builder.append(bounds[1]);
            builder.append(", ");
            builder.append(bounds[2]);
            builder.append(", ");
            builder.append(bounds[3]);
            builder.append(", ");
            builder.append(minHeight == bounds[4] ? minHeight : (minHeight + ".." + bounds[4]));
            builder.append(", ");
            builder.append(bounds[5]);
            builder.append("]");
        }
    }

    /**
     * Debug to TRACE_FILE. Meant for temporary use, use CheckUtils.debug for
     * permanent inserts.
     * 
     * @return The given returnValue.
     */
    public static boolean debug(String message, boolean returnValue) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, message);
        return returnValue;
    }

    /**
     * Debug to TRACE_FILE. Meant for temporary use, use CheckUtils.debug for
     * permanent inserts.
     * 
     * @param message
     * @return
     */
    public static void debug(String message) {
        NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, message);
    }

    public static String toJava(final double[] doubles) {
        final StringBuilder builder = new StringBuilder(20 + doubles.length * 20);
        toJava(doubles, builder);
        return builder.toString();
    }

    /**
     * 
     * @param doubles
     * @param builder
     */
    public static void toJava(final double[] doubles, final StringBuilder builder) {
        if (doubles == null) {
            builder.append("null");
            return;
        }
        builder.append("new double[] {");
        if (doubles.length > 0) {
            builder.append(doubles[0]);
        }
        for (int i = 1; i < doubles.length; i++) {
            builder.append(", " + doubles[i]);
        }
        builder.append("}");
    }

    // TODO: method to log raytracing/recorder directly with prefix message.

}
