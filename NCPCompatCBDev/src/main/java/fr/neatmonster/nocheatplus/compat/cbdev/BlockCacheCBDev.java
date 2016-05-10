package fr.neatmonster.nocheatplus.compat.cbdev;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_9_R2.AxisAlignedBB;
import net.minecraft.server.v1_9_R2.BlockPosition;
import net.minecraft.server.v1_9_R2.EntityBoat;
import net.minecraft.server.v1_9_R2.EntityShulker;
import net.minecraft.server.v1_9_R2.EnumDirection;
import net.minecraft.server.v1_9_R2.IBlockAccess;
import net.minecraft.server.v1_9_R2.IBlockData;
import net.minecraft.server.v1_9_R2.TileEntity;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_9_R2.CraftWorld;
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftEntity;
import org.bukkit.entity.Entity;

import fr.neatmonster.nocheatplus.utilities.BlockCache;

public class BlockCacheCBDev extends BlockCache implements IBlockAccess {

    protected net.minecraft.server.v1_9_R2.WorldServer world;
    protected World bukkitWorld;

    public BlockCacheCBDev(World world) {
        setAccess(world);
    }

    @Override
    public void setAccess(World world) {
        if (world != null) {
            this.maxBlockY = world.getMaxHeight() - 1;
            this.world = ((CraftWorld) world).getHandle();
            this.bukkitWorld = world;
        } else {
            this.world = null;
            this.bukkitWorld = null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public int fetchTypeId(final int x, final int y, final int z) {
        return bukkitWorld.getBlockTypeIdAt(x, y, z);
    }

    @SuppressWarnings("deprecation")
    @Override
    public int fetchData(final int x, final int y, final int z) {
        return bukkitWorld.getBlockAt(x, y, z).getData();
    }

    @Override
    public double[] fetchBounds(final int x, final int y, final int z){
        final int id = getTypeId(x, y, z);		
        final net.minecraft.server.v1_9_R2.Block block = net.minecraft.server.v1_9_R2.Block.getById(id);
        if (block == null) {
            // TODO: Convention for null blocks -> full ?
            return null;
        }
        final BlockPosition pos = new BlockPosition(x, y, z);
        // TODO: Deprecation warning below (reason / substitute?).
        final AxisAlignedBB bb = block.a(getType(pos), this, pos);
        if (bb == null) {
            return new double[] {0.0, 0.0, 0.0, 1.0, 1.0, 1.0}; // Special case.
            //return null;
        }
        // minX, minY, minZ, maxX, maxY, maxZ
        return new double[]{bb.a, bb.b, bb.c, bb.d,  bb.e, bb.f};
    }

    @Override
    public boolean standsOnEntity(final Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
        try{
            // TODO: Find some simplification!

            final net.minecraft.server.v1_9_R2.Entity mcEntity  = ((CraftEntity) entity).getHandle();

            final AxisAlignedBB box = new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
            @SuppressWarnings("rawtypes")
            final List list = world.getEntities(mcEntity, box);
            @SuppressWarnings("rawtypes")
            final Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final net.minecraft.server.v1_9_R2.Entity other = (net.minecraft.server.v1_9_R2.Entity) iterator.next();
                if (mcEntity == other || !(other instanceof EntityBoat) && !(other instanceof EntityShulker)) { // && !(other instanceof EntityMinecart)) continue;
                    continue;
                }
                if (minY >= other.locY && minY - other.locY <= 0.7){
                    return true;
                }
                // Still check this for some reason.
                final AxisAlignedBB otherBox = other.getBoundingBox();
                if (box.a > otherBox.d || box.d < otherBox.a || box.b > otherBox.e || box.e < otherBox.b || box.c > otherBox.f || box.f < otherBox.c) {
                    continue;
                }
                else {
                    return true;
                }
            }
        }
        catch (Throwable t){
            // Ignore exceptions (Context: DisguiseCraft).
        }
        return false;
    }

    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.utilities.BlockCache#cleanup()
     */
    @Override
    public void cleanup() {
        super.cleanup();
        world = null;
        bukkitWorld = null;
    }

    @Override
    public int getBlockPower(BlockPosition pos, EnumDirection dir) {
        return world.getBlockPower(pos, dir);
    }

    @Override
    public TileEntity getTileEntity(BlockPosition pos) {
        return world.getTileEntity(pos);
    }

    @Override
    public IBlockData getType(BlockPosition pos) {
        // TODO: Can this be cached ?
        return world.getType(pos);
    }

    @Override
    public boolean isEmpty(BlockPosition pos) {
        // TODO: Can (and should) this be cached ?
        return world.isEmpty(pos);
    }

}
