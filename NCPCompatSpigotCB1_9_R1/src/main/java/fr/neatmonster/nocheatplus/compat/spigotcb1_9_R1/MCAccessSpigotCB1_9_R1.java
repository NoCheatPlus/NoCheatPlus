package fr.neatmonster.nocheatplus.compat.spigotcb1_9_R1;

import net.minecraft.server.v1_9_R1.AttributeInstance;
import net.minecraft.server.v1_9_R1.AttributeModifier;
import net.minecraft.server.v1_9_R1.AxisAlignedBB;
import net.minecraft.server.v1_9_R1.Block;
import net.minecraft.server.v1_9_R1.BlockPosition;
import net.minecraft.server.v1_9_R1.DamageSource;
import net.minecraft.server.v1_9_R1.EntityComplexPart;
import net.minecraft.server.v1_9_R1.EntityPlayer;
import net.minecraft.server.v1_9_R1.GenericAttributes;
import net.minecraft.server.v1_9_R1.IBlockAccess;
import net.minecraft.server.v1_9_R1.IBlockData;
import net.minecraft.server.v1_9_R1.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_9_R1.CraftServer;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.AttribUtil;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class MCAccessSpigotCB1_9_R1 implements MCAccess {

    private final MobEffectList JUMP;
    private final MobEffectList FASTER_MOVEMENT;

    /**
     * Test for availability in constructor.
     */
    public MCAccessSpigotCB1_9_R1() {
        //        try {
        getCommandMap();
        if (ReflectionUtil.getMethod(Block.class, "a", IBlockData.class, IBlockAccess.class, BlockPosition.class).getReturnType() != AxisAlignedBB.class) {
            throw new RuntimeException();
        }
        if (ReflectionUtil.getConstructor(BlockPosition.class, int.class, int.class, int.class) == null) {
            throw new RuntimeException();
        }
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.EntityLiving.class, 
                new String[]{"getHeadHeight"}, float.class);
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.EntityPlayer.class, 
                new String[]{"getHealth"}, float.class);
        ReflectionUtil.checkMembers(net.minecraft.server.v1_9_R1.AxisAlignedBB.class, double.class,
                "a", "b", "c", "d", "e", "f");
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.AttributeInstance.class, 
                new String[]{"b"}, double.class);
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.AttributeModifier.class, 
                new String[]{"c"}, int.class);
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.AttributeModifier.class, 
                new String[]{"d"}, double.class);
        ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_9_R1.Material.class, 
                new String[]{"isSolid", "isLiquid"}, boolean.class);
        // TODO: Confine the following by types as well.
        ReflectionUtil.checkMembers("net.minecraft.server.v1_9_R1.", 
                new String[] {"Entity" , "length", "width", "locY"});
        ReflectionUtil.checkMembers("net.minecraft.server.v1_9_R1.", 
                new String[] {"EntityPlayer" , "dead", "deathTicks", "invulnerableTicks"});

        // obc: getHandle() for CraftWorld, CraftPlayer, CraftEntity.
        // nms: Several: AxisAlignedBB, WorldServer
        // nms: Block.getById(int), BlockPosition(int, int, int), WorldServer.getEntities(Entity, AxisAlignedBB)
        // nms: AttributeInstance.a(UUID), EntityComplexPart, EntityPlayer.getAttributeInstance(IAttribute).

        //        } catch(Throwable t) {
        //            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().severe(Streams.INIT, t);
        //            throw new RuntimeException("NO WERK");
        //        }
        JUMP = MobEffectList.getByName("jump_boost");
        if (JUMP == null) {
            throw new RuntimeException();
        }
        FASTER_MOVEMENT = MobEffectList.getByName("speed");
        if (FASTER_MOVEMENT == null) {
            throw new RuntimeException();
        }
    }

    @Override
    public String getMCVersion() {
        // 1.9-1.9.3 (1_9_R1)
        return "1.9-1.9.3";
    }

    @Override
    public String getServerVersionTag() {
        return "Spigot-CB-1.9_R1";
    }

    @Override
    public CommandMap getCommandMap() {
        return ((CraftServer) Bukkit.getServer()).getCommandMap();
    }

    @Override
    public BlockCache getBlockCache(final World world) {
        return new BlockCacheSpigotCB1_9_R1(world);
    }

    @Override
    public double getHeight(final Entity entity) {
        final net.minecraft.server.v1_9_R1.Entity mcEntity = ((CraftEntity) entity).getHandle();
        AxisAlignedBB boundingBox = mcEntity.getBoundingBox();
        final double entityHeight = Math.max(mcEntity.length, Math.max(mcEntity.getHeadHeight(), boundingBox.e - boundingBox.b));
        if (entity instanceof LivingEntity) {
            return Math.max(((LivingEntity) entity).getEyeHeight(), entityHeight);
        } else return entityHeight;
    }

    private net.minecraft.server.v1_9_R1.Material getMaterial(int blockId) {
        final Block block = Block.getById(blockId);
        if (block == null) {
            return null;
        }
        // (Currently no update state, since we don't have any position.)
        return block.q(block.getBlockData());
    }

    @Override
    public AlmostBoolean isBlockSolid(final int id) {
        final net.minecraft.server.v1_9_R1.Material material = getMaterial(id);
        if (material == null) {
            return AlmostBoolean.MAYBE;
        }
        else {
            return AlmostBoolean.match(material.isSolid());
        }
    }

    @Override
    public AlmostBoolean isBlockLiquid(final int id) {
        final net.minecraft.server.v1_9_R1.Material material = getMaterial(id);
        if (material == null) {
            return AlmostBoolean.MAYBE;
        }
        else {
            return AlmostBoolean.match(material.isLiquid());
        }
    }

    @Override
    public double getWidth(final Entity entity) {
        return ((CraftEntity) entity).getHandle().width;
    }

    @Override
    public AlmostBoolean isIllegalBounds(final Player player) {
        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        if (entityPlayer.dead) {
            return AlmostBoolean.NO;
        }
        // TODO: Does this need a method call for the "real" box? Might be no problem during moving events, though.
        final AxisAlignedBB box = entityPlayer.getBoundingBox();
        if (!entityPlayer.isSleeping()) {
            // This can not really test stance but height of bounding box.
            final double dY = Math.abs(box.e - box.b);
            if (dY > 1.8) {
                return AlmostBoolean.YES; // dY > 1.65D || 
            }
            if (dY < 0.1D && entityPlayer.length >= 0.1) {
                return AlmostBoolean.YES;
            }
        }
        return AlmostBoolean.MAYBE;
    }

    @Override
    public double getJumpAmplifier(final Player player) {
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        if (mcPlayer.hasEffect(JUMP)) {
            return mcPlayer.getEffect(JUMP).getAmplifier();
        }
        else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public double getFasterMovementAmplifier(final Player player) {
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        if (mcPlayer.hasEffect(FASTER_MOVEMENT)) {
            return mcPlayer.getEffect(FASTER_MOVEMENT).getAmplifier();
        }
        else {
            return Double.NEGATIVE_INFINITY;
        }
    }

    @Override
    public double getSpeedAttributeMultiplier(Player player) {
        final AttributeInstance attr = ((CraftLivingEntity) player).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED);
        final double val = attr.getValue() / attr.b();
        final AttributeModifier mod = attr.a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return val;
        } else {
            return val / AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

    @Override
    public double getSprintAttributeMultiplier(Player player) {
        final AttributeModifier mod = ((CraftLivingEntity) player).getHandle().getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).a(AttribUtil.ID_SPRINT_BOOST);
        if (mod == null) {
            return 1.0;
        } else {
            return AttribUtil.getMultiplier(mod.c(), mod.d());
        }
    }

    @Override
    public int getInvulnerableTicks(final Player player) {
        return ((CraftPlayer) player).getHandle().invulnerableTicks;
    }

    @Override
    public void setInvulnerableTicks(final Player player, final int ticks) {
        ((CraftPlayer) player).getHandle().invulnerableTicks = ticks;
    }

    @Override
    public void dealFallDamage(final Player player, final double damage) {
        ((CraftPlayer) player).getHandle().damageEntity(DamageSource.FALL, (float) damage);
    }

    @Override
    public boolean isComplexPart(final Entity entity) {
        return ((CraftEntity) entity).getHandle() instanceof EntityComplexPart;
    }

    @Override
    public boolean shouldBeZombie(final Player player) {
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        return !mcPlayer.dead && mcPlayer.getHealth() <= 0.0f ;
    }

    @Override
    public void setDead(final Player player, final int deathTicks) {
        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        mcPlayer.deathTicks = deathTicks;
        mcPlayer.dead = true;
    }

    @Override
    public boolean hasGravity(final Material mat) {
        return mat.hasGravity();
    }

    @Override
    public AlmostBoolean dealFallDamageFiresAnEvent() {
        return AlmostBoolean.YES;
    }

    //  @Override
    //  public void correctDirection(final Player player) {
    //      final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
    //      // Main direction.
    //      mcPlayer.yaw = LocUtil.correctYaw(mcPlayer.yaw);
    //      mcPlayer.pitch = LocUtil.correctPitch(mcPlayer.pitch);
    //      // Consider setting the lastYaw here too.
    //  }

}
