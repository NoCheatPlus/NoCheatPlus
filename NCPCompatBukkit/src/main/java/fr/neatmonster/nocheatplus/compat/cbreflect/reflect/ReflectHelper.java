package fr.neatmonster.nocheatplus.compat.cbreflect.reflect;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.AttribUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * More handy high level methods throwing one type of exception.
 * @author asofold
 *
 */
public class ReflectHelper {

    // TODO: Many possible exceptions are not yet caught (...).
    // TODO: Some places: should actually try-catch and fail() instead of default values and null return values.

    /** Failure to use / apply [ / setup ? ]. */
    public static class ReflectFailureException extends RuntimeException {

        /**
         * 
         */
        private static final long serialVersionUID = -3934791920291782604L;

        public ReflectFailureException() {
            super();
        }

        public ReflectFailureException(ClassNotFoundException ex) {
            super(ex);
        }

        // TODO: Might add a sub-error enum/code/thing support.

    }

    protected final ReflectBase reflectBase;

    protected final ReflectBlockPosition reflectBlockPosition;
    protected final ReflectBlock reflectBlock;
    protected final ReflectMaterial reflectMaterial;
    protected final ReflectWorld reflectWorld;

    protected final ReflectDamageSource reflectDamageSource;
    protected final ReflectEntity reflectEntity;
    protected final ReflectPlayer reflectPlayer;

    protected final ReflectGenericAttributes reflectGenericAttributes;
    protected final ReflectAttributeInstance reflectAttributeInstance;
    protected final ReflectAttributeModifier reflectAttributeModifier;
    protected final boolean hasAttributes;

    public ReflectHelper() throws ReflectFailureException {
        // TODO: Allow some to not work?
        try {
            this.reflectBase = new ReflectBase();
            ReflectBlockPosition reflectBlockPosition = null;
            try {
                reflectBlockPosition = new ReflectBlockPosition(this.reflectBase);
            }
            catch (ClassNotFoundException ex) {}
            this.reflectBlockPosition = reflectBlockPosition;
            this.reflectBlock = new ReflectBlock(this.reflectBase, this.reflectBlockPosition);
            this.reflectMaterial = new ReflectMaterial(this.reflectBase);
            this.reflectWorld = new ReflectWorld(this.reflectBase);

            this.reflectDamageSource = new ReflectDamageSource(this.reflectBase);
            this.reflectEntity = new ReflectEntity(this.reflectBase, this.reflectDamageSource);
            this.reflectPlayer = new ReflectPlayer(this.reflectBase, this.reflectDamageSource);
            ReflectGenericAttributes reflectGenericAttributes = null;
            ReflectAttributeInstance reflectAttributeInstance = null;
            ReflectAttributeModifier reflectAttributeModifier = null;
            boolean hasAttributes = false;
            try {
                reflectGenericAttributes = new ReflectGenericAttributes(this.reflectBase);
                reflectAttributeInstance = new ReflectAttributeInstance(this.reflectBase);
                reflectAttributeModifier = new ReflectAttributeModifier(this.reflectBase);
                hasAttributes = true; // TODO: null checks (...).
            }
            catch (ClassNotFoundException ex) {}
            this.reflectGenericAttributes = reflectGenericAttributes;
            this.reflectAttributeInstance = reflectAttributeInstance;
            this.reflectAttributeModifier = reflectAttributeModifier;
            this.hasAttributes = hasAttributes;
        }
        catch (ClassNotFoundException ex) {
            throw new ReflectFailureException(ex);
        }
        if (ConfigManager.getConfigFile().getBoolean(ConfPaths.LOGGING_EXTENDED_STATUS)) {
            List<String> parts = new LinkedList<String>();
            for (Field rootField : this.getClass().getDeclaredFields()) {
                boolean accessible = rootField.isAccessible();
                if (!accessible) {
                    rootField.setAccessible(true);
                }
                Object obj = ReflectionUtil.get(rootField, this, null);
                if (obj == null) {
                    parts.add("(Not available: " + rootField.getName() + ")");
                    continue;
                }
                else if (rootField.getName().startsWith("reflect")) {
                    Class<?> clazz = obj.getClass();
                    // TODO: Skip attributes silently before 1.6.1 (and not unknown version).
                    for (Field field : clazz.getFields()) {
                        if (ReflectionUtil.get(field, obj, null) == null) {
                            parts.add(clazz.getName() + "." + field.getName());
                        }
                    }
                }
                if (!accessible) {
                    rootField.setAccessible(false);
                }
            }
            if (!parts.isEmpty()) {
                parts.add(0, "CompatCBReflect: The following properties could not be set:");
                NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.INIT, StringUtil.join(parts, "\n"));
            }
        }
    }

    /**
     * Quick fail with exception.
     */
    private void fail() {
        throw new ReflectFailureException();
    }

    public Object getHandle(Player player) {
        // TODO: CraftPlayer check (isAssignableFrom)?
        if (this.reflectPlayer.obcGetHandle == null) {
            fail();
        }
        Object handle = ReflectionUtil.invokeMethodNoArgs(this.reflectPlayer.obcGetHandle, player);
        if (handle == null) {
            fail();
        }
        return handle;
    }

    public double nmsPlayer_getHealth(Object handle) {
        if (this.reflectPlayer.nmsGetHealth == null) {
            fail();
        }
        return ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectPlayer.nmsGetHealth, handle)).doubleValue();
    }

    public boolean nmsPlayer_dead(Object handle) {
        if (this.reflectPlayer.nmsDead == null) {
            fail();
        }
        return ReflectionUtil.getBoolean(this.reflectPlayer.nmsDead, handle, true);
    }

    /**
     * Set the value for the dead field.
     * @param handle
     * @param value
     */
    public void nmsPlayer_dead(Object handle, boolean value) {
        if (this.reflectPlayer.nmsDead == null || !ReflectionUtil.set(this.reflectPlayer.nmsDead, handle, value)) {
            fail();
        }
    }

    /**
     * Set the value for the dead field.
     * @param handle
     * @param value
     */
    public void nmsPlayer_deathTicks(Object handle, int value) {
        if (this.reflectPlayer.nmsDeathTicks == null || !ReflectionUtil.set(this.reflectPlayer.nmsDeathTicks, handle, value)) {
            fail();
        }
    }

    public boolean canDealFallDamage() {
        return this.reflectPlayer.nmsDamageEntity != null && this.reflectDamageSource.nmsFALL != null;
    }

    public void dealFallDamage(Player player, double damage) {
        if (this.reflectDamageSource.nmsFALL == null) {
            fail();
        }
        Object handle = getHandle(player);
        nmsPlayer_dealDamage(handle, this.reflectDamageSource.nmsFALL, damage);
    }

    public void nmsPlayer_dealDamage(Object handle, Object damage_source, double damage) {
        if (this.reflectPlayer.nmsDamageEntity == null) {
            fail();
        }
        if (this.reflectPlayer.nmsDamageEntityInt) {
            ReflectionUtil.invokeMethod(this.reflectPlayer.nmsDamageEntity, handle, damage_source, (int) damage);
        } else {
            ReflectionUtil.invokeMethod(this.reflectPlayer.nmsDamageEntity, handle, damage_source, (float) damage);
        }
    }

    public int getInvulnerableTicks(Player player) {
        if (this.reflectPlayer.nmsInvulnerableTicks == null) {
            fail();
        }
        Object handle = getHandle(player);
        return ReflectionUtil.getInt(this.reflectPlayer.nmsInvulnerableTicks, handle, player.getNoDamageTicks() / 2);
    }

    public void setInvulnerableTicks(Player player, int ticks) {
        if (this.reflectPlayer.nmsInvulnerableTicks == null) {
            fail();
        }
        Object handle = getHandle(player);
        if (!ReflectionUtil.set(this.reflectPlayer.nmsInvulnerableTicks, handle, ticks)) {
            fail();
        }
    }

    /**
     * Get the speed attribute (MOVEMENT_SPEED) for a player.
     * @param handle EntityPlayer
     * @return AttributeInstance
     */
    public Object getMovementSpeedAttributeInstance(Player player) {
        if (!hasAttributes || this.reflectPlayer.nmsGetAttributeInstance == null || this.reflectGenericAttributes.nmsMOVEMENT_SPEED == null) {
            fail();
        }
        return ReflectionUtil.invokeMethod(this.reflectPlayer.nmsGetAttributeInstance, getHandle(player), this.reflectGenericAttributes.nmsMOVEMENT_SPEED);
    }

    /**
     * 
     * @param player
     * @param removeSprint If to calculate away the sprint boost modifier.
     * @return
     */
    public double getSpeedAttributeMultiplier(Player player, boolean removeSprint) {
        if (!hasAttributes || this.reflectAttributeInstance.nmsGetValue == null || 
                this.reflectAttributeInstance.nmsGetBaseValue == null) {
            fail();
        }
        Object attributeInstance = getMovementSpeedAttributeInstance(player);
        double val = ((Double) ReflectionUtil.invokeMethodNoArgs(this.reflectAttributeInstance.nmsGetValue, attributeInstance)).doubleValue() / ((Double) ReflectionUtil.invokeMethodNoArgs(this.reflectAttributeInstance.nmsGetBaseValue, attributeInstance)).doubleValue();
        if (!removeSprint) {
            return val;
        }
        else {
            return val / nmsAttributeInstance_getAttributeModifierMultiplier(attributeInstance);
        }
    }

    public double getSprintAttributeMultiplier(Player player) {

        if (!hasAttributes || this.reflectAttributeModifier.nmsGetOperation == null || this.reflectAttributeModifier.nmsGetValue == null) {
            fail();
        }
        Object attributeInstance = getMovementSpeedAttributeInstance(player);
        if (attributeInstance == null) {
            fail();
        }
        return nmsAttributeInstance_getAttributeModifierMultiplier(attributeInstance);
    }

    /**
     * (Not an existing method.)
     * @param attributeInstance
     */
    public double nmsAttributeInstance_getAttributeModifierMultiplier(Object attributeInstance) {
        if (this.reflectAttributeInstance.nmsGetAttributeModifier == null) {
            fail();
        }
        // TODO: Need to fall back in case of errors.
        Object mod = ReflectionUtil.invokeMethod(this.reflectAttributeInstance.nmsGetAttributeModifier, attributeInstance, AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return 1.0;
        }
        else {
            return AttribUtil.getMultiplier((Integer) ReflectionUtil.invokeMethodNoArgs(this.reflectAttributeModifier.nmsGetOperation, mod), (Double) ReflectionUtil.invokeMethodNoArgs(this.reflectAttributeModifier.nmsGetValue, mod));
        }
    }

    public Object getHandle(World world) {
        if (this.reflectWorld.obcGetHandle == null) {
            fail();
        }
        Object handle = ReflectionUtil.invokeMethodNoArgs(this.reflectWorld.obcGetHandle, world);
        if (handle == null) {
            fail();
        }
        return handle;
    }

    public Object nmsBlockPosition(int x, int y, int z) {
        if (!this.reflectBlock.useBlockPosition || this.reflectBlockPosition.new_nmsBlockPosition == null) {
            fail();
        }
        Object blockPos = ReflectionUtil.newInstance(this.reflectBlockPosition.new_nmsBlockPosition, x, y, z);
        if (blockPos == null) {
            fail();
        }
        return blockPos;
    }

    /**
     * 
     * @param id
     * @return Block instance (could be null).
     */
    public Object nmsBlock_getById(int id) {
        if (this.reflectBlock.nmsGetById == null) {
            fail();
        }
        return ReflectionUtil.invokeMethod(this.reflectBlock.nmsGetById, null, id);
    }

    public Object nmsBlock_getMaterial(Object block) {
        if (this.reflectBlock.nmsGetMaterial == null) {
            fail();
        }
        return ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMaterial, block);
    }

    public void nmsBlock_updateShape(Object block, Object iBlockAccess, int x, int y, int z) {
        if (this.reflectBlock.nmsUpdateShape == null) {
            fail();
        }
        if (this.reflectBlock.useBlockPosition) {
            ReflectionUtil.invokeMethod(this.reflectBlock.nmsUpdateShape, block, iBlockAccess, nmsBlockPosition(x, y, z));
        } else {
            ReflectionUtil.invokeMethod(this.reflectBlock.nmsUpdateShape, block, iBlockAccess, x, y, z);
        }
    }

    public boolean nmsMaterial_isSolid(Object material) {
        if (this.reflectMaterial.nmsIsSolid == null) {
            fail();
        }
        return (Boolean) ReflectionUtil.invokeMethodNoArgs(this.reflectMaterial.nmsIsSolid, material);
    }

    public boolean nmsMaterial_isLiquid(Object material) {
        if (this.reflectMaterial.nmsIsLiquid == null) {
            fail();
        }
        return (Boolean) ReflectionUtil.invokeMethodNoArgs(this.reflectMaterial.nmsIsLiquid, material);
    }

    public AlmostBoolean isBlockSolid(int id) {
        Object obj = nmsBlock_getById(id);
        if (obj == null) {
            return AlmostBoolean.MAYBE;
        }
        obj = nmsBlock_getMaterial(obj);
        if (obj == null) {
            return AlmostBoolean.MAYBE;
        }
        return AlmostBoolean.match(nmsMaterial_isSolid(obj));
    }

    public AlmostBoolean isBlockLiquid(int id) {
        Object obj = nmsBlock_getById(id);
        if (obj == null) {
            return AlmostBoolean.MAYBE;
        }
        obj = nmsBlock_getMaterial(obj);
        if (obj == null) {
            return AlmostBoolean.MAYBE;
        }
        return AlmostBoolean.match(nmsMaterial_isLiquid(obj));
    }

    /**
     * (Not a method in world types.)
     * @param nmsWorld
     * @param typeId
     * @param x
     * @param y
     * @param z
     * @return double[6] minX, minY, minZ, maxX, maxY, maxZ. Returns null for cases like air/unspecified.
     */
    public double[] nmsWorld_fetchBlockShape(Object nmsWorld, int id, int x, int y, int z) {
        if (this.reflectBlock.nmsGetMinX == null) { // Use nmsGetMinX as reference for all six methods (!).
            fail();
        }
        Object block = nmsBlock_getById(id);
        if (block == null) {
            return null;
        }
        nmsBlock_updateShape(block, nmsWorld, x, y, z);
        // TODO: The methods could return null [better try-catch here].
        return new double[] {
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMinX, block)).doubleValue(),
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMinY, block)).doubleValue(),
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMinZ, block)).doubleValue(),
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMaxX, block)).doubleValue(),
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMaxY, block)).doubleValue(),
                ((Number) ReflectionUtil.invokeMethodNoArgs(this.reflectBlock.nmsGetMaxZ, block)).doubleValue(),
        };
    }

}
