package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class ReflectBase {

    // TODO: Envelope check, enum for what envelope level (within expected version range, before / after).

    public final String obcPackageName;

    public final String nmsPackageName;

    public ReflectBase() {
        final Server server = Bukkit.getServer();
        // TODO: Confine even more closely (detect v... package part, sequence of indices).
        // obc
        Class<?> clazz = server.getClass();
        String name = clazz.getPackage().getName();
        if (name.equals("org.bukkit.craftbukkit") || name.indexOf("org.") == 0 && name.indexOf(".bukkit.") != -1 && name.indexOf(".craftbukkit.") != -1) {
            obcPackageName = name;
        } else {
            obcPackageName = null;
        }
        // nms
        Object obj = ReflectionUtil.invokeMethodNoArgs(server, "getHandle");
        clazz = obj.getClass();
        name = clazz.getPackage().getName();
        if (name.equals("net.minecraft.server") || name.indexOf("net.") == 0 && name.indexOf(".minecraft.") != -1 && name.indexOf(".server.") != -1) {
            nmsPackageName = name;
        } else {
            nmsPackageName = null;
        }
    }

}
