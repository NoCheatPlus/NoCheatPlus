package fr.neatmonster.nocheatplus.compat.cbreflect;

import org.bukkit.World;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.compat.bukkit.BlockCacheBukkit;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;

public class BlockCacheCBReflect extends BlockCacheBukkit {

    // TODO: Not sure if reflection can gain speed over Bukkit API anywhere (who wants to try?).

    protected final ReflectHelper helper;

    protected Object nmsWorld = null;

    public BlockCacheCBReflect(ReflectHelper reflectHelper, World world) {
        super(world);
        this.helper = reflectHelper;
    }

    @Override
    public void setAccess(World world) {
        super.setAccess(world);
        this.nmsWorld = world == null ? null : helper.getHandle(world);
    }

    @Override
    public double[] fetchBounds(int x, int y, int z) {
        try {
            return helper.nmsWorld_fetchBlockShape(this.nmsWorld, this.getTypeId(x, y, z), x, y, z);
        }
        catch (ReflectFailureException ex) {
            return super.fetchBounds(x, y, z);
        }
    }

    @Override
    public boolean standsOnEntity(Entity entity, double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        // TODO: Implement once relevant.
        return super.standsOnEntity(entity, minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    public void cleanup() {
        super.cleanup();
        this.nmsWorld = null;
    }

}
