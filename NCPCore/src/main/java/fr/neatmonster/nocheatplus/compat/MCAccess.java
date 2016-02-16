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
     * @return MAYBE if undecided, YES or NO if decided.
     */
    public AlmostBoolean isBlockSolid(int id);

    /**
     * NMS Block static..
     * @param id
     * @return MAYBE if undecided, YES or NO if decided.
     */
    public AlmostBoolean isBlockLiquid(int id);

    /**
     * Does only check y bounds, returns false if dead. This is called by
     * PlayerLocation.hasIllegalStance(), PlayerLocation.hasIllegalCoords()
     * should always be checked first.
     * 
     * @param player
     * @return MAYBE if undecided, YES or NO if decided.
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

    /**
     * Generic speed modifier as a multiplier.
     * 
     * @param player
     * @return A multiplier for the allowed speed, excluding the sprint boost
     *         modifier (!). If not possible to determine, it should
     *         Double.MAX_VALUE.
     */
    public double getSpeedAttributeMultiplier(Player player);

    /**
     * Sprint boost modifier as a multiplier.
     * 
     * @param player
     * @return The sprint boost modifier as a multiplier. If not possible to
     *         determine, it should be Double.MAX_VALUE.
     */
    public double getSprintAttributeMultiplier(Player player);

    /**
     * 
     * @param player
     * @return Integer.MAX_VALUE if not available (!).
     */
    public int getInvulnerableTicks(Player player);

    public void setInvulnerableTicks(Player player, int ticks);

    public void dealFallDamage(Player player, double damage);

    /**
     * If dealFallDamage(Player, double) will fire a damage event.
     * @return
     */
    public AlmostBoolean dealFallDamageFiresAnEvent();

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
     * Usually sand and gravel. Not for fastest access.
     * @param type
     * @return
     */
    public boolean hasGravity(Material type);

    //	/**
    //	 * Correct the direction (yaw + pitch). If this can't be done lightly it should just do nothing. Check pitch and yaw before calling, use auxiliary methods from LocUtil.
    //	 * @param player
    //	 */
    //	public void correctDirection(Player player);


}
