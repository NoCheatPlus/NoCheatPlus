package fr.neatmonster.nocheatplus.utilities;

import java.util.Iterator;
import java.util.List;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.Entity;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IBlockAccess;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/*
 * MM"""""""`YM dP                                     
 * MM  mmmmm  M 88                                     
 * M'        .M 88 .d8888b. dP    dP .d8888b. 88d888b. 
 * MM  MMMMMMMM 88 88'  `88 88    88 88ooood8 88'  `88 
 * MM  MMMMMMMM 88 88.  .88 88.  .88 88.  ... 88       
 * MM  MMMMMMMM dP `88888P8 `8888P88 `88888P' dP       
 * MMMMMMMMMMMM                  .88                   
 *                           d8888P                    
 *                           
 * M""MMMMMMMM                              dP   oo                   
 * M  MMMMMMMM                              88                        
 * M  MMMMMMMM .d8888b. .d8888b. .d8888b. d8888P dP .d8888b. 88d888b. 
 * M  MMMMMMMM 88'  `88 88'  `"" 88'  `88   88   88 88'  `88 88'  `88 
 * M  MMMMMMMM 88.  .88 88.  ... 88.  .88   88   88 88.  .88 88    88 
 * M         M `88888P' `88888P' `88888P8   dP   dP `88888P' dP    dP 
 * MMMMMMMMMMM                                                        
 */
/**
 * An utility class used to know a lot of things for a player and a location given.
 */
public class PlayerLocation {

    /** Type id of the block at the position. */
    private Integer typeId;
    
    /** Type id of the block below. */
    private Integer typeIdBelow;
    
    private Integer data;

    /** Is the player above stairs? */
    private Boolean                 aboveStairs;

    /** Is the player in lava? */
    private Boolean                 inLava;

    /** Is the player in water? */
    private Boolean                 inWater;

    /** Is the player is web? */
    private Boolean                 inWeb;

    /** Is the player on the ground? */
    private Boolean                 onGround;

    /** Is the player on ice? */
    private Boolean                 onIce;

    /** Is the player on ladder? */
    private Boolean                 onLadder;
    
    /** Simple test if the exact position is passable. */
    private Boolean                 passable;
    
    /** Y parameter for growing the bounding box with the isOnGround check.*/
    private double yOnGround = 0.001;

    /** The  block coordinates. */
    private int                     blockX, blockY, blockZ;
    
    /** The exact coordinates. */
    private double x,y,z;
    
    private float yaw, pitch;
    
    // Members that need cleanup:
    
    /** The entity player. */
    private EntityPlayer            entity;
    
    /** The bounding box of the player. */
    private AxisAlignedBB           boundingBox;

    /** Bukkit world. */
    private World                   world;
    
    /** The worldServer. */
    private WorldServer             worldServer;
    
    /** Optional block property cache. */
    private BlockCache blockCache;

    /**
     * Gets the location.
     * 
     * @return the location
     */
    public Location getLocation() {
        return new Location(world, x, y, z);
    }

    /**
     * Gets the blockX.
     * 
     * @return the blockX
     */
    public double getX() {
        return x;
    }

    /**
     * Gets the boundY.
     * 
     * @return the boundY
     */
    public double getY() {
        return y;
    }
    
    /**
     * Gets the blockZ.
     * 
     * @return the blockZ
     */
    public double getZ() {
        return z;
    }

    /**
     * Gets the yaw.
     * 
     * @return the yaw
     */
    public float getYaw() {
        return yaw;
    }
    
    /**
     * Gets the pitch.
     * 
     * @return the pitch
     */
    public float getPitch() {
        return pitch;
    }
    
    public Vector getVector() {
        return new Vector(x, y, z);
    }
    
    public double getWidth(){
        return entity.width;
    }
    
    public int getBlockX(){
        return blockX;
    }
    
    public int getBlockY(){
        return blockY;
    }
    
    public int getBlockZ(){
        return blockZ;
    }
    
    /**
     * Compares block coordinates (not the world). 
     * @param other
     * @return
     */
    public final boolean isSameBlock(final PlayerLocation other) {
        return blockX == other.getBlockX() && blockZ == other.getBlockZ() &&  blockY == other.getBlockY();
    }
    
    /**
     * Compares exact coordinates (not the world).
     * @param loc
     * @return
     */
    public boolean isSamePos(final PlayerLocation loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }
    
    /**
     * Compares exact coordinates (not the world).
     * @param loc
     * @return
     */
    public boolean isSamePos(final Location loc) {
        return x == loc.getX() && z == loc.getZ() && y == loc.getY();
    }

    /**
     * Checks if the player is above stairs.
     * 
     * @return true, if the player above on stairs
     */
    public boolean isAboveStairs() {
        if (aboveStairs == null)
            aboveStairs = BlockProperties.isStairs(getTypeIdBelow().intValue());
        return aboveStairs;
    }

    /**
     * Checks if the player is in lava.
     * 
     * @return true, if the player is in lava
     */
    public boolean isInLava() {
        if (inLava == null) {
            AxisAlignedBB boundingBoxLava = boundingBox.clone();
            boundingBoxLava = boundingBoxLava.grow(-0.10000000149011612D, -0.40000000596046448D, -0.10000000149011612D);
            inLava = BlockProperties.collides(getBlockAccess(), boundingBoxLava, BlockProperties.F_LAVA);
        }
        return inLava;
    }
    
    /**
     * Checks if the player is in water.
     * 
     * @return true, if the player is in water
     */
    public boolean isInWater() {
        if (inWater == null) {
            AxisAlignedBB boundingBoxWater = boundingBox.clone();
            boundingBoxWater = boundingBoxWater.grow(0.0D, -0.40000000596046448D, 0.0D);
            boundingBoxWater = boundingBoxWater.shrink(0.001D, 0.001D, 0.001D);
            inWater = BlockProperties.collides(getBlockAccess(), boundingBoxWater, BlockProperties.F_WATER);
        }
        return inWater;
    }

    /**
     * Checks if the player is in a liquid.
     * 
     * @return true, if the player is in a liquid
     */
    public boolean isInLiquid() {
        // TODO: optimize (check liquid first and only if liquid check further)
        return isInLava() || isInWater();
    }
    

    /**
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        if (onIce == null){
            final org.bukkit.entity.Player entity = this.entity.getBukkitEntity();
            if (entity.isSneaking() || entity.isBlocking())
                onIce = getTypeId(blockX, Location.locToBlock(boundingBox.b - 0.1D), blockZ) == Material.ICE.getId();
            else
                onIce = getTypeIdBelow().intValue() == Material.ICE.getId();
        }
        return onIce;
    }

    /**
     * Checks if the player is on a ladder or vine.
     * 
     * @return If so.
     */
    public boolean isOnLadder() {
        if (onLadder == null){
            final int typeId = getTypeId();
            onLadder = typeId == Material.LADDER.getId() || typeId == Material.VINE.getId();
        }
        return onLadder;
    }
    
    /**
     * Checks if the player is above a ladder or vine.<br>
     * Does not save back value to field.
     * 
     * @return If so.
     */
    public boolean isAboveLadder() {
        final int typeId = getTypeIdBelow();
        return typeId == Material.LADDER.getId() || typeId == Material.VINE.getId();
    }

    /**
     * Checks if the player is in web.
     * 
     * @return true, if the player is in web
     */
    public boolean isInWeb() {
        final int webId = Material.WEB.getId();
        if (inWeb == null) {
            for (int blockX = Location.locToBlock(boundingBox.a + 0.001D); blockX <= Location.locToBlock(boundingBox.d - 0.001D); blockX++){
                for (int blockY = Location.locToBlock(boundingBox.b + 0.001D); blockY <= Location.locToBlock(boundingBox.e - 0.001D); blockY++){
                    for (int blockZ = Location.locToBlock(boundingBox.c + 0.001D); blockZ <= Location.locToBlock(boundingBox.f - 0.001D); blockZ++){
                        if (getTypeId(blockX, blockY, blockZ) == webId){
                            inWeb = true;
                            return true;
                        }
                    }
                }
            }
            inWeb = false;
        }
        return inWeb;
    }

    /**
     * Checks if the player is on ground.
     * 
     * @return true, if the player is on ground
     */
    public boolean isOnGround() {
        if (onGround == null) {
            onGround = BlockProperties.isOnGround(getBlockAccess(), boundingBox.a, boundingBox.b - yOnGround, boundingBox.c, boundingBox.d, boundingBox.b + 0.25, boundingBox.f);
            if (!onGround){
                // TODO: Probably check other ids too before doing this ?
                // TODO: clean this up, use other checking method, detach it to blockproperties ?
                double d0 = 0.25D;
                AxisAlignedBB axisalignedbb = boundingBox.clone();
                axisalignedbb = axisalignedbb.d(0D, -getyOnGround(), 0D);
                @SuppressWarnings("rawtypes")
                List list = worldServer.getEntities(entity, axisalignedbb.grow(d0, d0, d0));
                @SuppressWarnings("rawtypes")
                Iterator iterator = list.iterator();
                while (iterator.hasNext()) {
                    final Entity entity1 = (Entity) iterator.next();
                    final EntityType type = entity.getBukkitEntity().getType();
                    if (type != EntityType.BOAT && type != EntityType.MINECART) continue;
                    AxisAlignedBB axisalignedbb1 = entity1.E();
                    if (axisalignedbb1 != null && axisalignedbb1.a(axisalignedbb)) {
                        onGround = true;
                        return true;
                    }
                    axisalignedbb1 = entity.g(entity1);
                    if (axisalignedbb1 != null && axisalignedbb1.a(axisalignedbb)) {
                        onGround = true;
                        return true;
                    }
                }
            }
        }
        return onGround;
    }
    
    public double getyOnGround() {
        return yOnGround;
    }

    public void setyOnGround(final double yOnGround) {
        this.yOnGround = yOnGround;
        this.onGround = null;
    }
    
    /**
     * Simple at the spot passability test, no bounding boxes.
     * @return
     */
    public boolean isPassable(){
        if (passable == null) passable = BlockProperties.isPassable(getBlockAccess(), x, y, z, getTypeId());
        return passable;
    }
    
    /**
     * Convenience method: delegate to BlockProperties.isDoppwnStream .
     * @param xDistance
     * @param zDistance
     * @return
     */
    public boolean isDownStream(final double xDistance, final double zDistance){
        return BlockProperties.isDownStream(getBlockAccess(), blockX, blockY, blockZ, getData(), xDistance, zDistance);
    }

	public Integer getTypeId() {
		if (typeId == null) typeId = getTypeId(blockX, blockY, blockZ);
		return typeId;
	}


	public Integer getTypeIdBelow() {
		if (typeIdBelow == null) typeIdBelow = getTypeId(blockX, blockY - 1, blockZ);
		return typeIdBelow;
	}
	
	public Integer getData(){
	    if (data == null) data = getData(blockX, blockY, blockZ);
	    return data;
	}
	
	/**
	 * Uses id cache if present.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public final int getTypeId(final int x, final int y, final int z){
	    return blockCache == null ? worldServer.getTypeId(x, y, z) : blockCache.getTypeId(x, y, z);
	}

	/**
	 * Uses id cache if present.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
    public final int getData(final int x, final int y, final int z){
        return blockCache == null ? worldServer.getData(x, y, z) : blockCache.getData(x, y, z);
    }
    
    public WorldServer getWorldServer() {
        return worldServer;
    }
    
    /**
     * Set the id cache for faster id getting.
     * @param cache
     */
     public void setBlockCache(final BlockCache cache) {
         this.blockCache = cache;
     }
        
    /**
    *
    * @return
    */
   public final IBlockAccess getBlockAccess() {
       return blockCache == null ? worldServer : blockCache;
   }
   
   /**
    * Sets the player location object.
    * 
    * @param location
    *            the location
    * @param player
    *            the player
    */
   public void set(final Location location, final Player player){
       set(location, player, 0.001);
   }
    
    /**
     * Sets the player location object. Does not set or reset blockCache.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     */
    public void set(final Location location, final Player player, final double yFreedom) {

        entity = ((CraftPlayer) player).getHandle();
        boundingBox = entity.boundingBox.clone().d(location.getX() - entity.locX, location.getY() - entity.locY,
                location.getZ() - entity.locZ);
        blockX = location.getBlockX();
        blockY = location.getBlockY();
        blockZ = location.getBlockZ();
        x = location.getX();
        y = location.getY();
        z = location.getZ();
        yaw = location.getYaw();
        pitch = location.getPitch();
        world = location.getWorld();
        worldServer = ((CraftWorld) world).getHandle();

        typeId = typeIdBelow = data = null;
        aboveStairs = inLava = inWater = inWeb = onGround = onIce = onLadder = passable = null;
        
        // TODO: consider blockCache.setAccess.
        
        this.setyOnGround(yFreedom);
    }
    
    /**
     * Set some references to null.
     */
    public void cleanup(){
        entity = null;
        world = null;
        worldServer = null;
        boundingBox = null;
        blockCache = null;
    }

}
