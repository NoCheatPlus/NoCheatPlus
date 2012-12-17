package fr.neatmonster.nocheatplus.compat;

import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.BlockCache;


/**
 * Compatibility interface to get properties for Bukkit instances that need access of CraftBukkit or Minecraft classes.<br>
 * NOTE: All methods returning AlmostBoolean must never return null, unless stated otherwise.<br>
 * NOTE: Expect API changes in the near future!<br>
 * <hr>
 * TODO: Make minimal (do we need WorldServer yet)?
 * @author mc_dev
 *
 */
public interface MCAccess {
	
	/**
	 * Simple version identifier like 1.4.
	 * @return
	 */
	public String getMCVersion();
	
	/**
	 * Server version tag, like CB 2511.
	 * @return
	 */
	public String getServerVersionTag();
	
	public CommandMap getCommandMap();
	
	/**
	 * Get a BlockCache implementation.
	 * @param world May be null to store an instance of BlockCache for future use.
	 * @return
	 */
	public BlockCache getBlockCache(World world);
	
	/**
	 * Get height of an entity (attack relevant, the maximal "thing" found).
	 */
	public double  getHeight(Entity entity);
	
	/**
	 * Return maximum.
	 * @param entity
	 * @return
	 */
	public double getWidthOrLength(Entity entity);
	
	
	public AlmostBoolean isBlockSolid(int id);
	
	public AlmostBoolean isBlockLiquid(int id);
	
	/**
	 * Hiding the API access here.<br>
	 * // TODO: Replace by independent method.
	 * TODO: Find description of this and use block properties from here, as well as a speaking method name.<br>
	 * Assumption: This is something like "can stand on this type of block".
	 * @param id
	 * @return
	 */
	public boolean Block_i(int id);

	/**
	 * Does only check y bounds, returns false if dead. this is half a check as auxiliary means for PlayerLocation.isIllegal.
	 * @param player
	 * @return null If undecided, true / false if decided.
	 */
	public AlmostBoolean isIllegalBounds(Player player);
	
	/**
	 * 
	 * @param player
	 * @return Double.MIN_VALUE if not present.
	 */
	public double getJumpAmplifier(Player player);
	
	/**
	 * 
	 * @return Double.MIN_VALUE if not present.
	 */
	public double getFasterMovementAmplifier(Player player);

	public int getInvulnerableTicks(Player player);

	public void setInvulnerableTicks(Player player, int ticks);

	public void dealFallDamage(Player player, int damage);

	public boolean isComplexPart(Entity damaged);

	/**
	 * Tests if player is not set to dead but has no health.
	 * @param player
	 * @return
	 */
	public boolean shouldBeZombie(Player player);

	/**
	 * Set flag + death ticks.
	 * @param player
	 * @param i
	 */
	public void setDead(Player player, int deathTicks);
		
}
