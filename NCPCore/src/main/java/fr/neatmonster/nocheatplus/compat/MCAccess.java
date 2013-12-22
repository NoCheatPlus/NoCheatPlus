package fr.neatmonster.nocheatplus.compat;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.utilities.BlockCache;


/**
 * Compatibility interface to get properties for Bukkit instances that need access of CraftBukkit or Minecraft classes.<br>
 * NOTE: All methods returning AlmostBoolean must never return null, unless stated otherwise.<br>
 * NOTE: Expect API changes in the near future!<br>
 * NOTE: If an instance implements BlockPropertiesSetup, the setup method will be called after basic initialization but before configuration is applied.<br>
 * <hr>
 * TODO: Make minimal.
 * @author mc_dev
 *
 */
public interface MCAccess {
	
	/**
	 * Simple version identifiers, if several must be separated by '|' like "1.4.2|1.4.4|1.4.5", to indicate multiple sub-versions supported use "1.5.x", use "?" to indicate general future support.
	 * @return
	 */
	public String getMCVersion();
	
	/**
	 * Server version tag, like CB 2511.
	 * @return
	 */
	public String getServerVersionTag();
	
	/**
	 * Get the servers command map.
	 * @return May return null if not supported.
	 */
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
	 * Return some width.
	 * @param entity
	 * @return
	 */
	public double getWidth(Entity entity);
	
	/**
	 * NMS Block static.
	 * @param id
	 * @return
	 */
	public AlmostBoolean isBlockSolid(int id);
	
	/**
	 * NMS Block static..
	 * @param id
	 * @return
	 */
	public AlmostBoolean isBlockLiquid(int id);

	/**
	 * Does only check y bounds, returns false if dead. this is half a check as auxiliary means for PlayerLocation.isIllegal.
	 * @param player
	 * @return null If undecided, true / false if decided.
	 */
	public AlmostBoolean isIllegalBounds(Player player);
	
	/**
	 * 
	 * @param player
	 * @return Double.NEGATIVE_INFINITY if not present.
	 */
	public double getJumpAmplifier(Player player);
	
	/**
	 * 
	 * @return Double.NEGATIVE_INFINITY if not present.
	 */
	public double getFasterMovementAmplifier(Player player);

	public int getInvulnerableTicks(Player player);

	public void setInvulnerableTicks(Player player, int ticks);

	public void dealFallDamage(Player player, double damage);

	/**
	 * This may well be removed, if possible to check with Bukkit.
	 * @param damaged
	 * @return
	 */
	public boolean isComplexPart(Entity damaged);

	/**
	 * Tests if player is not set to dead but has no health.
	 * @param player
	 * @return
	 */
	public boolean shouldBeZombie(Player player);

	/**
	 * Ensure the player is really taken out: Set flag + death ticks.
	 * 
	 * TODO: Check if still necessary + make knowledge-base entries for what to check.
	 * 
	 * @param player
	 * @param deathTicks
	 */
	public void setDead(Player player, int deathTicks);
	
	/**
	 * Get timestamp of the keep-alive field (not only updated on keep-alive packets).
	 * @param player The player for which to get the time.
	 * @return Long.MIN_VALUE if not possible.
	 */
	public long getKeepAliveTime(Player player);
	
	/**
	 * Usually sand and gravel. Not for fastest access.
	 * @param type
	 * @return
	 */
	public boolean hasGravity(Material type);
		
}
