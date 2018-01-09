/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.compat.cb2545;

import java.util.Iterator;
import java.util.List;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_6.CraftWorld;
import org.bukkit.craftbukkit.v1_4_6.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.neatmonster.nocheatplus.utilities.map.BlockCache;
import net.minecraft.server.v1_4_6.AxisAlignedBB;
import net.minecraft.server.v1_4_6.IBlockAccess;
import net.minecraft.server.v1_4_6.Material;
import net.minecraft.server.v1_4_6.TileEntity;
import net.minecraft.server.v1_4_6.Vec3DPool;

public class BlockCacheCB2545 extends BlockCache {

    /** Box for one time use, no nesting, no extra storing this(!). */
    protected static final AxisAlignedBB useBox = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);

    protected net.minecraft.server.v1_4_6.World world;
    protected World bukkitWorld;

    private IBlockAccess iBlockAccess = new IBlockAccess() {

        @SuppressWarnings("deprecation")
        @Override
        public int getTypeId(final int x, final int y, final int z) {
            return BlockCacheCB2545.this.getType(x, y, z).getId();
        }


        @Override
        public int getData(final int x, final int y, final int z) {
            return BlockCacheCB2545.this.getData(x, y, z);
        }

        @Override
        public Material getMaterial(final int x, final int y, final int z) {
            return BlockCacheCB2545.this.world.getMaterial(x, y, z);
        }

        @Override
        public TileEntity getTileEntity(final int x, final int y, final int z) {
            return world.getTileEntity(x, y, z);
        }

        @Override
        public Vec3DPool getVec3DPool() {
            return world.getVec3DPool();
        }

        @Override
        public boolean isBlockFacePowered(final int arg0, final int arg1, final int arg2, final int arg3) {
            return world.isBlockFacePowered(arg0, arg1, arg2, arg3);
        }

        @Override
        public boolean t(final int x, final int y, final int z) {
            return world.t(x, y, z);
        }

    };

    public BlockCacheCB2545(World world) {
        setAccess(world);
    }

    @Override
    public BlockCache setAccess(World world) {
        bukkitWorld = world;
        if (world != null) {
            this.maxBlockY = world.getMaxHeight() - 1;
            this.world = ((CraftWorld) world).getHandle();
        } else {
            this.world = null;
        }
        return this;
    }

    @Override
    public org.bukkit.Material fetchTypeId(final int x, final int y, final int z) {
        return bukkitWorld.getBlockAt(x, y, z).getType();
    }

    @Override
    public int fetchData(final int x, final int y, final int z) {
        return world.getData(x, y, z);
    }

    @Override
    public double[] fetchBounds(final int x, final int y, final int z){

        // TODO: change api for this / use nodes (!)
        final int id = iBlockAccess.getTypeId(x, y, z);
        final net.minecraft.server.v1_4_6.Block block = net.minecraft.server.v1_4_6.Block.byId[id];
        if (block == null) return null;
        block.updateShape(iBlockAccess, x, y, z); // TODO: use THIS instead of world.

        // minX, minY, minZ, maxX, maxY, maxZ
        return new double[]{block.v(), block.x(), block.z(), block.w(),  block.y(),  block.A()};
    }

    @Override
    public boolean standsOnEntity(final Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
        try{
            // TODO: Probably check other ids too before doing this ?

            final net.minecraft.server.v1_4_6.Entity mcEntity  = ((CraftEntity) entity).getHandle();

            final AxisAlignedBB box = useBox.b(minX, minY, minZ, maxX, maxY, maxZ);
            @SuppressWarnings("rawtypes")
            final List list = world.getEntities(mcEntity, box);
            @SuppressWarnings("rawtypes")
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final net.minecraft.server.v1_4_6.Entity other = (net.minecraft.server.v1_4_6.Entity) iterator.next();
                final EntityType type = other.getBukkitEntity().getType();
                if (type != EntityType.BOAT && type != EntityType.MINECART) continue;
                final AxisAlignedBB otherBox = other.boundingBox;
                if (box.a > otherBox.d || box.d < otherBox.a || box.b > otherBox.e || box.e < otherBox.b || box.c > otherBox.f || box.f < otherBox.c) continue;
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

}
