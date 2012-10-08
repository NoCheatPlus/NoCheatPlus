package fr.neatmonster.nocheatplus.utilities;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.IBlockAccess;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

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

    /** The bounding box of the player. */
    private AxisAlignedBB           boundingBox;
    
    /** Y parameter for growing the bounding box with the isOnGround check.*/
    private double yOnGround = 0.001;

    /** The entity player. */
    private EntityPlayer            entity;

    /** The  block coordinates. */
    private int                     blockX, blockY, blockZ;
    
    /** The exact coordinates. */
    private double x,y,z;
    
    private float yaw, pitch;

    /** Bukkit world. */
    private World                   world;
    
    /** The worldServer. */
    private WorldServer             worldServer;
    
    private TypeIdCache idCache;

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
            inLava = worldServer.a(boundingBoxLava, net.minecraft.server.Material.LAVA);
        }
        return inLava;
    }

    /**
     * Checks if the player is in a liquid.
     * 
     * @return true, if the player is in a liquid
     */
    public boolean isInLiquid() {
        return isInLava() || isInWater();
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
            inWater = worldServer.a(boundingBoxWater, net.minecraft.server.Material.WATER, entity);
        }
        return inWater;
    }

    /**
     * Checks if the player is in web.
     * 
     * @return true, if the player is in web
     */
    public boolean isInWeb() {
        if (inWeb == null) {
            for (int blockX = Location.locToBlock(boundingBox.a + 0.001D); blockX <= Location.locToBlock(boundingBox.d - 0.001D); blockX++)
                for (int blockY = Location.locToBlock(boundingBox.b + 0.001D); blockY <= Location.locToBlock(boundingBox.e - 0.001D); blockY++)
                    for (int blockZ = Location.locToBlock(boundingBox.c + 0.001D); blockZ <= Location.locToBlock(boundingBox.f - 0.001D); blockZ++)
                        if (getTypeId(blockX, blockY, blockZ) == Material.WEB.getId())
                            inWeb = true;
            if (inWeb == null)
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
            AxisAlignedBB boundingBoxGround = boundingBox.clone();
            boundingBoxGround = boundingBoxGround.d(0D, -getyOnGround(), 0D);
            onGround = worldServer.getCubes(entity, boundingBoxGround).size() > 0;
        }
        return onGround;
    }

    /**
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        if (onIce == null)
            if (entity.getBukkitEntity().isSneaking() || entity.getBukkitEntity().isBlocking())
                onIce = getTypeId(blockX, Location.locToBlock(boundingBox.b - 0.1D), blockZ) == Material.ICE.getId();
            else
                onIce = getTypeIdBelow() == Material.ICE.getId();
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
     * Sets the player location object.
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

        typeId = typeIdBelow = null;
        aboveStairs = inLava = inWater = inWeb = onGround = onIce = onLadder = null;
        
        this.setyOnGround(yFreedom);
    }

	public double getyOnGround() {
		return yOnGround;
	}

	public void setyOnGround(final double yOnGround) {
		this.yOnGround = yOnGround;
		this.onGround = null;
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
	
	public boolean isDownStream(final double xDistance, final double zDistance){
        // x > 0 -> south, z > 0 -> west
        final int fromData = getData();
        
        if ((fromData & 0x8) == 0){
            // not falling.
            if ((xDistance > 0)){
                if (fromData < 7 && BlockProperties.isLiquid(getTypeId(blockX + 1, blockY, blockZ)) && getData(blockX + 1, blockY, blockZ) > fromData){
                    return true;
                }
                else if (fromData > 0  && BlockProperties.isLiquid(getTypeId(blockX - 1, blockY, blockZ)) && getData(blockX - 1, blockY, blockZ) < fromData){
                    // reverse direction.
                    return true;
                }
            } else if (xDistance < 0){
                if (fromData < 7 && BlockProperties.isLiquid(getTypeId(blockX - 1, blockY, blockZ)) && getData(blockX - 1, blockY, blockZ) > fromData){
                    return true;
                }
                else if (fromData > 0  && BlockProperties.isLiquid(getTypeId(blockX + 1, blockY, blockZ)) && getData(blockX + 1, blockY, blockZ) < fromData){
                    // reverse direction.
                    return true;
                }
            }
            if (zDistance > 0){
                if (fromData < 7 && BlockProperties.isLiquid(getTypeId(blockX, blockY, blockZ + 1)) && getData(blockX, blockY, blockZ + 1) > fromData){
                    return true;
                }
                else if (fromData > 0  && BlockProperties.isLiquid(getTypeId(blockX , blockY, blockZ - 1)) && getData(blockX, blockY, blockZ - 1) < fromData){
                    // reverse direction.
                    return true;
                }
            }
            else if (zDistance < 0 ){
                if (fromData < 7 && BlockProperties.isLiquid(getTypeId(blockX, blockY, blockZ - 1)) && getData(blockX, blockY, blockZ - 1) > fromData){
                    return true;
                }
                else if (fromData > 0  && BlockProperties.isLiquid(getTypeId(blockX , blockY, blockZ + 1)) && getData(blockX, blockY, blockZ + 1) < fromData){
                    // reverse direction.
                    return true;
                }
            }
        }
	    return false;
	}

	public final boolean isSameBlock(final PlayerLocation other) {
		// Maybe make block coordinate fields later.
		return blockX == other.getBlockX() && blockZ == other.getBlockZ() &&  blockY == other.getBlockY();
	}
	
	/**
	 * Uses id cache if present.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public final int getTypeId(final int x, final int y, final int z){
	    return idCache == null ? worldServer.getTypeId(x, y, z) : idCache.getTypeId(x, y, z);
	}

	/**
	 * Uses id cache if present.
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
    public final int getData(final int x, final int y, final int z){
        return idCache == null ? worldServer.getData(x, y, z) : idCache.getData(x, y, z);
    }
	   
	/**
	 * TODO: temp maybe
	 * @return
	 */
	public final IBlockAccess getBlockAccess() {
		return worldServer;
	}
	
	   /**
     * Set the id cache for faster id getting.
     * @param cache
     */
    public void setIdCache(final TypeIdCache cache) {
        this.idCache = cache;
    }
	
	/**
	 * Set some references to null.
	 */
	public void cleanup(){
	    entity = null;
	    world = null;
	    worldServer = null;
	    boundingBox = null;
	}

}
