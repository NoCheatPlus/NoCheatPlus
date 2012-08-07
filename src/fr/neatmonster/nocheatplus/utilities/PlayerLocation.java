package fr.neatmonster.nocheatplus.utilities;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.Block;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.Material;
import net.minecraft.server.WorldServer;

import org.bukkit.Location;
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

    /**
     * Another utility class used to manipulate differently booleans.
     */
    private class CustomBoolean {

        /** Is the boolean set? */
        private boolean isSet = false;

        /** What is its value? */
        private boolean value = false;

        /**
         * Gets the boolean.
         * 
         * @return the value
         */
        public boolean get() {
            return value;
        }

        /**
         * Checks if the boolean is set.
         * 
         * @return true, if the boolean is set
         */
        public boolean isSet() {
            return isSet;
        }

        /**
         * Sets the boolean.
         * 
         * @param value
         *            the value
         */
        public void set(final boolean value) {
            this.value = value;
            isSet = true;
        }
    }

    /** The original location. */
    private final Location      location;

    /** Is the player in lava? */
    private final CustomBoolean inLava      = new CustomBoolean();

    /** Is the player in water? */
    private final CustomBoolean inWater     = new CustomBoolean();

    /** Is the player is web? */
    private final CustomBoolean inWeb       = new CustomBoolean();

    /** Is the player on the ground? */
    private final CustomBoolean onGround    = new CustomBoolean();

    /** Is the player on ice? */
    private final CustomBoolean onIce       = new CustomBoolean();

    /** Is the player on ladder? */
    private final CustomBoolean onLadder    = new CustomBoolean();

    /** Is the player on ladder (ignoring unclimbable vines)? **/
    private final CustomBoolean onLadderBis = new CustomBoolean();

    /** Is the player on soul sand? */
    private final CustomBoolean onSoulSand  = new CustomBoolean();

    /** The bounding box of the player. */
    private final AxisAlignedBB boundingBox;

    /** The entity player. */
    private final EntityPlayer  entity;

    /** The x, y and z coordinates. */
    private final int           x, y, z;

    /** The world. */
    private final WorldServer   world;

    /**
     * Instantiates a new player location.
     * 
     * @param location
     *            the location
     * @param player
     *            the player
     */
    public PlayerLocation(final Location location, final Player player) {
        this.location = location;

        entity = ((CraftPlayer) player).getHandle();
        boundingBox = entity.boundingBox.clone().d(location.getX() - entity.locX, location.getY() - entity.locY,
                location.getZ() - entity.locZ);
        x = (int) Math.floor(location.getX());
        y = (int) Math.floor(boundingBox.b);
        z = (int) Math.floor(location.getZ());
        world = ((CraftWorld) location.getWorld()).getHandle();
    }

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
     * Checks if the player is in lava.
     * 
     * @return true, if the player is in lava
     */
    public boolean isInLava() {
        if (!inLava.isSet()) {
            AxisAlignedBB boundingBoxLava = boundingBox.clone();
            boundingBoxLava = boundingBoxLava.grow(-0.10000000149011612D, -0.40000000596046448D, -0.10000000149011612D);
            inLava.set(world.a(boundingBoxLava, Material.LAVA));
        }
        return inLava.get();
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
        if (!inWater.isSet()) {
            AxisAlignedBB boundingBoxWater = boundingBox.clone();
            boundingBoxWater = boundingBoxWater.grow(0.0D, -0.40000000596046448D, 0.0D);
            boundingBoxWater = boundingBoxWater.shrink(0.001D, 0.001D, 0.001D);
            inWater.set(world.a(boundingBoxWater, Material.WATER, entity));
        }
        return inWater.get();
    }

    /**
     * Checks if the player is in web.
     * 
     * @return true, if the player is in web
     */
    public boolean isInWeb() {
        if (!inWeb.isSet()) {
            for (int blockX = (int) Math.floor(boundingBox.a + 0.001D); blockX <= (int) Math
                    .floor(boundingBox.d - 0.001D); blockX++)
                for (int blockY = (int) Math.floor(boundingBox.b + 0.001D); blockY <= (int) Math
                        .floor(boundingBox.e - 0.001D); blockY++)
                    for (int blockZ = (int) Math.floor(boundingBox.c + 0.001D); blockZ <= (int) Math
                            .floor(boundingBox.f - 0.001D); blockZ++)
                        if (!inWeb.get() && world.getTypeId(blockX, blockY, blockZ) == Block.WEB.id)
                            inWeb.set(true);
            if (!inWeb.isSet())
                inWeb.set(false);
        }
        return inWeb.get();
    }

    /**
     * Checks if the player is on ground.
     * 
     * @return true, if the player is on ground
     */
    public boolean isOnGround() {
        if (!onGround.isSet()) {
            AxisAlignedBB boundingBoxGround = boundingBox.clone();
            boundingBoxGround = boundingBoxGround.d(0D, -0.001D, 0D);
            onGround.set(world.getCubes(entity, boundingBoxGround).size() > 0);
        }
        return onGround.get();
    }

    /**
     * Checks if the player is on ice.
     * 
     * @return true, if the player is on ice
     */
    public boolean isOnIce() {
        if (!onIce.isSet())
            if (entity.getBukkitEntity().isSneaking() || entity.getBukkitEntity().isBlocking())
                onIce.set(world.getTypeId(x, (int) Math.floor(boundingBox.b - 0.1D), z) == Block.ICE.id);
            else
                onIce.set(world.getTypeId(x, y - 1, z) == Block.ICE.id);
        return onIce.get();
    }

    /**
     * Checks if the player is on a ladder.
     * 
     * @return true, if the player is on a ladder
     */
    public boolean isOnLadder() {
        return isOnLadder(false);
    }

    /**
     * Checks if the player is on a ladder.
     * 
     * @param ignoreUnclimbableVines
     *            ignore unclimbable vines or not?
     * @return true, if the player is on a ladder
     */
    public boolean isOnLadder(final boolean ignoreUnclimbableVines) {
        if (ignoreUnclimbableVines) {
            if (!onLadderBis.isSet())
                if (world.getTypeId(x, y, z) == Block.LADDER.id)
                    onLadderBis.set(true);
                else if (world.getTypeId(x, y, z) == Block.VINE.id) {
                    final int data = world.getData(x, y, z);
                    if ((data & 1) != 0) {
                        final int id = world.getTypeId(x, y, z + 1);
                        if (id != 0 && Block.byId[id].c() && Block.byId[id].material.isSolid())
                            onLadderBis.set(true);
                    }
                    if (!onLadder.isSet() && (data & 2) != 0) {
                        final int id = world.getTypeId(x - 1, y, z);
                        if (id != 0 && Block.byId[id].c() && Block.byId[id].material.isSolid())
                            onLadderBis.set(true);
                    }
                    if (!onLadder.isSet() && (data & 4) != 0) {
                        final int id = world.getTypeId(x, y, z - 1);
                        if (id != 0 && Block.byId[id].c() && Block.byId[id].material.isSolid())
                            onLadderBis.set(true);
                    }
                    if (!onLadder.isSet() && (data & 8) != 0) {
                        final int id = world.getTypeId(x + 1, y, z);
                        if (id != 0 && Block.byId[id].c() && Block.byId[id].material.isSolid())
                            onLadderBis.set(true);
                    }
                }
            return onLadderBis.get();
        }
        if (!onLadder.isSet())
            onLadder.set(world.getTypeId(x, y, z) == Block.LADDER.id || world.getTypeId(x, y, z) == Block.VINE.id);
        return onLadder.get();
    }

    /**
     * Checks if the player is on soul sand.
     * 
     * @return true, if the player is on soul sand
     */
    public boolean isOnSoulSand() {
        if (!onSoulSand.isSet()) {
            AxisAlignedBB boundingBoxGround = boundingBox.clone();
            boundingBoxGround = boundingBoxGround.d(0D, -0.001D, 0D);
            for (final Object object : world.getCubes(entity, boundingBoxGround)) {
                final AxisAlignedBB aabbCube = (AxisAlignedBB) object;
                final int blockX = (int) Math.floor(aabbCube.a);
                final int blockY = (int) Math.floor(aabbCube.b);
                final int blockZ = (int) Math.floor(aabbCube.c);
                if (!onSoulSand.get() && world.getTypeId(blockX, blockY, blockZ) == Block.SOUL_SAND.id)
                    onSoulSand.set(true);
            }
            if (!onSoulSand.isSet())
                onSoulSand.set(false);
        }
        return onSoulSand.get();
    }
}
