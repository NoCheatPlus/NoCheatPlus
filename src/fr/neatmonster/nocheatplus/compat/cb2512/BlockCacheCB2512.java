package fr.neatmonster.nocheatplus.compat.cb2512;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_4_5.AxisAlignedBB;
import net.minecraft.server.v1_4_5.IBlockAccess;
import net.minecraft.server.v1_4_5.Material;
import net.minecraft.server.v1_4_5.TileEntity;
import net.minecraft.server.v1_4_5.Vec3DPool;

import org.bukkit.World;
import org.bukkit.craftbukkit.v1_4_5.CraftWorld;
import org.bukkit.craftbukkit.v1_4_5.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import fr.neatmonster.nocheatplus.utilities.BlockCache;

public class BlockCacheCB2512 extends BlockCache implements IBlockAccess{ // TODO: let it implement IBlockAccess !
	
	/** Box for one time use, no nesting, no extra storing this(!). */
	protected static final AxisAlignedBB useBox = AxisAlignedBB.a(0, 0, 0, 0, 0, 0);

	protected net.minecraft.server.v1_4_5.World world;
	
	public BlockCacheCB2512(World world) {
		setAccess(world);
	}

	@Override
	public void setAccess(World world) {
		this.world = ((CraftWorld) world).getHandle();
	}

	@Override
	public int fetchTypeId(final int x, final int y, final int z) {
		return world.getTypeId(x, y, z);
	}

	@Override
	public int fetchData(final int x, final int y, final int z) {
		return world.getData(x, y, z);
	}

	@Override
	public double[] fetchBounds(final int x, final int y, final int z){
		
		// TODO: change api for this / use nodes (!)
		final int id = getTypeId(x, y, z);		
		final net.minecraft.server.v1_4_5.Block block = net.minecraft.server.v1_4_5.Block.byId[id];
		block.updateShape(this, x, y, z); // TODO: use THIS instead of world.
		
		// minX, minY, minZ, maxX, maxY, maxZ
		return new double[]{block.v(), block.x(), block.z(), block.w(),  block.y(),  block.A()};
	}
	
	@Override
	public boolean standsOnEntity(final Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
		try{
			// TODO: Probably check other ids too before doing this ?
			
			final net.minecraft.server.v1_4_5.Entity mcEntity  = ((CraftEntity) entity).getHandle();
			
			final AxisAlignedBB box = useBox.b(minX, minY, minZ, maxX, maxY, maxZ);
			@SuppressWarnings("rawtypes")
			final List list = world.getEntities(mcEntity, box);
			@SuppressWarnings("rawtypes")
			Iterator iterator = list.iterator();
			while (iterator.hasNext()) {
				final net.minecraft.server.v1_4_5.Entity other = (net.minecraft.server.v1_4_5.Entity) iterator.next();
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

	@Override
	public Material getMaterial(final int x, final int y, final int z) {
		return world.getMaterial(x, y, z);
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
    
}
