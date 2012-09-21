package fr.neatmonster.nocheatplus.utilities;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
import org.bukkit.Material;
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
    
    /** The original location. */
    private Location                location;

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

    /** The x, y and z coordinates. */
    private int                     x, y, z;

    /** The world. */
    private WorldServer             world;

    /**
     * Gets the location.
     * 
     * @return the location
     */
    public Location getLocation() {
        return location;
    }

    /**
     * Gets the pitch.
     * 
     * @return the pitch
     */
    public float getPitch() {
        return location.getPitch();
    }

    /**
     * Gets the x.
     * 
     * @return the x
     */
    public double getX() {
        return location.getX();
    }

    /**
     * Gets the y.
     * 
     * @return the y
     */
    public double getY() {
        return location.getY();
    }

    /**
     * Gets the yaw.
     * 
     * @return the yaw
     */
    public float getYaw() {
        return location.getYaw();
    }

    /**
     * Gets the z.
     * 
     * @return the z
     */
    public double getZ() {
        return location.getZ();
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
            inLava = world.a(boundingBoxLava, net.minecraft.server.Material.LAVA);
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
            inWater = world.a(boundingBoxWater, net.minecraft.server.Material.WATER, entity);
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
            for (int blockX = (int) Math.floor(boundingBox.a + 0.001D); blockX <= (int) Math
                    .floor(boundingBox.d - 0.001D); blockX++)
                for (int blockY = (int) Math.floor(boundingBox.b + 0.001D); blockY <= (int) Math
                        .floor(boundingBox.e - 0.001D); blockY++)
                    for (int blockZ = (int) Math.floor(boundingBox.c + 0.001D); blockZ <= (int) Math
                            .floor(boundingBox.f - 0.001D); blockZ++)
                        if (world.getTypeId(blockX, blockY, blockZ) == Material.WEB.getId())
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
            onGround = world.getCubes(entity, boundingBoxGround).size() > 0;
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
                onIce = world.getTypeId(x, (int) Math.floor(boundingBox.b - 0.1D), z) == Material.ICE.getId();
            else
                onIce = getTypeIdBelow() == Material.ICE.getId();
        return onIce;
    }

    /**
     * Checks if the player is on a ladder.
     * 
     * @return true, if the player is on a ladder
     */
    public boolean isOnLadder() {
        if (onLadder == null){
        	final int typeId = getTypeId();
        	onLadder = typeId == Material.LADDER.getId() || typeId == Material.VINE.getId();
        }
        return onLadder;
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
        this.location = location;

        entity = ((CraftPlayer) player).getHandle();
        boundingBox = entity.boundingBox.clone().d(location.getX() - entity.locX, location.getY() - entity.locY,
                location.getZ() - entity.locZ);
        x = (int) Math.floor(location.getX());
        y = (int) Math.floor(boundingBox.b);
        z = (int) Math.floor(location.getZ());
        world = ((CraftWorld) location.getWorld()).getHandle();

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
		if (typeId == null) typeId = world.getTypeId(x, y, z);
		return typeId;
	}


	public Integer getTypeIdBelow() {
		if (typeIdBelow == null) typeIdBelow = world.getTypeId(x, y - 1, z);
		return typeIdBelow;
	}

}
