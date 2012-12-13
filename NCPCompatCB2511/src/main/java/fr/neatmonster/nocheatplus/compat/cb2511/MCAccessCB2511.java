package fr.neatmonster.nocheatplus.compat.cb2511;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.Block;
import net.minecraft.server.DamageSource;
import net.minecraft.server.EntityComplexPart;
import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.utilities.BlockCache;


public class MCAccessCB2511 implements MCAccess {
	
	/**
	 * Constructor to let it fail in time.
	 */
	public MCAccessCB2511(){
		getCommandMap();
	}

	@Override
	public String getMCVersion() {
		return "1.4";
	}

	@Override
	public String getServerVersionTag() {
		return "CB2511";
	}

	@Override
	public BlockCache getBlockCache(World world) {
		return new BlockCacheCB2511(world);
	}

	@Override
	public CommandMap getCommandMap() {
		return ((CraftServer) Bukkit.getServer()).getCommandMap();
	}

	@Override
	public double getHeight(final Entity entity) {
		final net.minecraft.server.Entity mcEntity = ((CraftEntity) entity).getHandle();
		final double entityHeight = Math.max(mcEntity.height, mcEntity.boundingBox.e - mcEntity.boundingBox.b);
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
	public boolean Block_i(final int id) {
		try{
			return Block.i(id);
		}
		catch(Throwable t){
			// Minecraft default value.
			return true;
		}
	}

	@Override
	public double getWidthOrLength(final Entity entity) {
		final net.minecraft.server.Entity mcEntity = ((CraftEntity) entity).getHandle();
		return Math.max(mcEntity.width, mcEntity.length);
	}

	@Override
	public Boolean isIllegalBounds(final Player player) {
		final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		if (entityPlayer.dead) return false;
		final AxisAlignedBB box = entityPlayer.boundingBox;
		if (!entityPlayer.isSleeping()){
			// This can not really test stance but height of bounding box.
			final double dY = Math.abs(box.e - box.b);
			if (dY > 1.8) return true; // dY > 1.65D || 
			if (dY < 0.1D) return true;
		}
		return null;
	}

	@Override
	public double getJumpAmplifier(final Player player) {
		final net.minecraft.server.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
	
		if (mcPlayer.hasEffect(MobEffectList.JUMP)) return mcPlayer.getEffect(MobEffectList.JUMP).getAmplifier();
		else return Double.MIN_VALUE;
	}

	@Override
	public double getFasterMovementAmplifier(final Player player) {
		final net.minecraft.server.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
		if (mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT)) return mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier();
		else return Double.MIN_VALUE;
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
	public void dealFallDamage(final Player player, final int damage) {
		((CraftPlayer) player).getHandle().damageEntity(DamageSource.FALL, damage);
	}

	@Override
	public boolean isComplexPart(final Entity entity) {
		return ((CraftEntity) entity).getHandle() instanceof EntityComplexPart;
	}

	@Override
	public boolean shouldBeZombie(final Player player) {
		final net.minecraft.server.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
		return !mcPlayer.dead && mcPlayer.getHealth() <= 0 ;
	}

	@Override
	public void setDead(final Player player, final int deathTicks) {
		final net.minecraft.server.EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
        mcPlayer.deathTicks = deathTicks;
        mcPlayer.dead = true;
	}

}
