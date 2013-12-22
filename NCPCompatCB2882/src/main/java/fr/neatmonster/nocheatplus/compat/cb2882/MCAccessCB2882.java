package fr.neatmonster.nocheatplus.compat.cb2882;

import net.minecraft.server.v1_6_R3.AxisAlignedBB;
import net.minecraft.server.v1_6_R3.Block;
import net.minecraft.server.v1_6_R3.DamageSource;
import net.minecraft.server.v1_6_R3.EntityComplexPart;
import net.minecraft.server.v1_6_R3.EntityPlayer;
import net.minecraft.server.v1_6_R3.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.v1_6_R3.CraftServer;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class MCAccessCB2882 implements MCAccess{
	
	/**
	 * Constructor to let it fail.
	 */
	public MCAccessCB2882(){
		getCommandMap();
		ReflectionUtil.checkMembers("net.minecraft.server.v1_6_R3.", new String[]{"Entity" , "dead"});
		// block bounds, original: minX, maxX, minY, maxY, minZ, maxZ
		ReflectionUtil.checkMethodReturnTypesNoArgs(net.minecraft.server.v1_6_R3.Block.class, 
				new String[]{"u", "v", "w", "x", "y", "z"}, double.class);
		// TODO: Nail it down further.
	}

	@Override
	public String getMCVersion() {
		// 1_6_R3
		return "1.6.4";
	}

	@Override
	public String getServerVersionTag() {
		return "CB2882";
	}

	@Override
	public CommandMap getCommandMap() {
		return ((CraftServer) Bukkit.getServer()).getCommandMap();
	}

	@Override
	public BlockCache getBlockCache(final World world) {
		return new BlockCacheCB2882(world);
	}

	@Override
	public double getHeight(final Entity entity) {
		final net.minecraft.server.v1_6_R3.Entity mcEntity = ((CraftEntity) entity).getHandle();
		final double entityHeight = Math.max(mcEntity.length, Math.max(mcEntity.height, mcEntity.boundingBox.e - mcEntity.boundingBox.b));
		if (entity instanceof LivingEntity) {
			return Math.max(((LivingEntity) entity).getEyeHeight(), entityHeight);
		} else return entityHeight;
	}

	@Override
	public AlmostBoolean isBlockSolid(final int id) {
		final Block block = Block.byId[id];
		if (block == null || block.material == null) return AlmostBoolean.MAYBE;
		else return AlmostBoolean.match(block.material.isSolid());
	}

	@Override
	public AlmostBoolean isBlockLiquid(final int id) {
		final Block block = Block.byId[id];
		if (block == null || block.material == null) return AlmostBoolean.MAYBE;
		else return AlmostBoolean.match(block.material.isLiquid());
	}

	@Override
	public double getWidth(final Entity entity) {
		return ((CraftEntity) entity).getHandle().width;
	}

	@Override
	public AlmostBoolean isIllegalBounds(final Player player) {
		final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (entityPlayer.dead) return AlmostBoolean.NO;
		// TODO: Does this need a method call for the "real" box? Might be no problem during moving events, though.
		final AxisAlignedBB box = entityPlayer.boundingBox;
		if (!entityPlayer.isSleeping()){
			// This can not really test stance but height of bounding box.
			final double dY = Math.abs(box.e - box.b);
			if (dY > 1.8) return AlmostBoolean.YES; // dY > 1.65D || 
			if (dY < 0.1D && entityPlayer.length >= 0.1) return AlmostBoolean.YES;
		}
		return AlmostBoolean.MAYBE;
	}

	@Override
	public double getJumpAmplifier(final Player player) {
		final net.minecraft.server.v1_6_R3.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
	
		if (mcPlayer.hasEffect(MobEffectList.JUMP)) return mcPlayer.getEffect(MobEffectList.JUMP).getAmplifier();
		else return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double getFasterMovementAmplifier(final Player player) {
		final net.minecraft.server.v1_6_R3.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
		if (mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT)) return mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier();
		else return Double.NEGATIVE_INFINITY;
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
		final net.minecraft.server.v1_6_R3.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
		return !mcPlayer.dead && mcPlayer.getHealth() <= 0.0f ;
	}

	@Override
	public void setDead(final Player player, final int deathTicks) {
		final net.minecraft.server.v1_6_R3.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        mcPlayer.deathTicks = deathTicks;
        mcPlayer.dead = true;
	}
	
	@Override
	public long getKeepAliveTime(final Player player) {
		// TODO: Implement if possible.
		return Long.MIN_VALUE;
	}
	
	@Override
	public boolean hasGravity(final Material mat) {
		// TODO: Test/check.
		return mat.hasGravity();
	}
	
}
