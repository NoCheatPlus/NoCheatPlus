package fr.neatmonster.nocheatplus.compat.bukkit;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

import fr.neatmonster.nocheatplus.utilities.BlockCache;

public class BlockCacheBukkit extends BlockCache{

	protected World world;
	
	public BlockCacheBukkit(World world) {
		setAccess(world);
	}

	@Override
	public void setAccess(World world) {
		this.world = world;
	}

	@Override
	public int fetchTypeId(final int x, final int y, final int z) {
		// TODO: consider setting type id and data at once.
		return world.getBlockTypeIdAt(x, y, z);
	}

	@Override
	public int fetchData(final int x, final int y, final int z) {
		// TODO: consider setting type id and data at once.
		return world.getBlockAt(x, y, z).getData();
	}

	@Override
	public double[] fetchBounds(final int x, final int y, final int z){
		// minX, minY, minZ, maxX, maxY, maxZ
		// TODO: Want to maintain a list with manual entries or at least half / full blocks ?
		// Always return full bounds, needs extra adaption to BlockProperties (!).
		return new double[]{0f, 0f, 0f, 1f, 1f, 1f};
	}
	
	@Override
	public boolean standsOnEntity(final Entity entity, final double minX, final double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
		try{
			// TODO: Probably check other ids too before doing this ?
			for (final Entity other : entity.getNearbyEntities(2.0, 2.0, 2.0)){
				final EntityType type = other.getType();
				if (type != EntityType.BOAT && !(other instanceof Minecart)) continue;
				final Location loc = entity.getLocation();
				if (Math.abs(loc.getY() - minY) < 0.6){
					// TODO: A "better" estimate is possible, though some more tolerance would be good. 
					return true; 
				}
				else return false;
			}		
		}
		catch (Throwable t){
			// Ignore exceptions (Context: DisguiseCraft).
		}
		return false;
	}
    
}
