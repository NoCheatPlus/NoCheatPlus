package fr.neatmonster.nocheatplus.compat.bukkit;


import java.util.HashSet;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PotionUtil;

public class MCAccessBukkit implements MCAccess, BlockPropertiesSetup{
	
	/**
	 * Constructor to let it fail.
	 */
	public MCAccessBukkit(){
		// TODO: Add more that might fail if not supported ?
		Material.AIR.isSolid();
		Material.AIR.isOccluding();
		Material.AIR.isTransparent();
		// TODO: Deactivate checks that might not work. => MCAccess should have availability method, NCP deactivates check on base of that.
	}

	@Override
	public String getMCVersion() {
		// Bukkit API.
		// TODO: maybe output something else.
		return "1.4.6|1.4.7|1.5.x|1.6.1|1.6.2|?";
	}

	@Override
	public String getServerVersionTag() {
		return "Bukkit-API";
	}

	@Override
	public CommandMap getCommandMap() {
		return null;
	}

	@Override
	public BlockCache getBlockCache(final World world) {
		return new BlockCacheBukkit(world);
	}

	@Override
	public double getHeight(final Entity entity) {
		final double entityHeight = 1.0;
		if (entity instanceof LivingEntity) {
			return Math.max(((LivingEntity) entity).getEyeHeight(), entityHeight);
		} else return entityHeight;
	}

	@Override
	public AlmostBoolean isBlockSolid(final int id) {
		final Material mat = Material.getMaterial(id); 
		if (mat == null) return AlmostBoolean.MAYBE;
		else return AlmostBoolean.match(mat.isSolid());
	}

	@Override
	public AlmostBoolean isBlockLiquid(final int id) {
		final Material mat = Material.getMaterial(id); 
		if (mat == null) return AlmostBoolean.MAYBE;
		switch (mat) {
			case STATIONARY_LAVA:
			case STATIONARY_WATER:
			case WATER:
			case LAVA:
				return AlmostBoolean.YES;
			default:
				return AlmostBoolean.NO;
		}
	}

	@Override
	public double getWidth(final Entity entity) {
		// TODO
		return 0.6f;
	}

	@Override
	public AlmostBoolean isIllegalBounds(final Player player) {
		if (player.isDead()) return AlmostBoolean.NO;
		if (!player.isSleeping()){ // TODO: ignored sleeping ?
			// TODO: This can test like ... nothing !
			// (Might not be necessary.)
		}
		return AlmostBoolean.MAYBE;
	}

	@Override
	public double getJumpAmplifier(final Player player) {
		return PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.JUMP);
	}

	@Override
	public double getFasterMovementAmplifier(final Player player) {
		return PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.SPEED);
	}

	@Override
	public int getInvulnerableTicks(final Player player) {
		// TODO: Ahhh...
		return player.getNoDamageTicks();
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setInvulnerableTicks(final Player player, final int ticks) {
		// TODO: Ahhh...
		player.setLastDamageCause(new EntityDamageEvent(player, DamageCause.CUSTOM, 500));
		player.setNoDamageTicks(ticks);
	}

	@Override
	public void dealFallDamage(final Player player, final int damage) {
		// TODO: account for armor, other.
		player.damage(damage);
	}

	@Override
	public boolean isComplexPart(final Entity entity) {
		return entity instanceof ComplexEntityPart || entity instanceof ComplexLivingEntity;
	}

	@Override
	public boolean shouldBeZombie(final Player player) {
		// Not sure :) ...
		return player.getHealth() <= 0 && !player.isDead();
	}

	@Override
	public void setDead(final Player player, final int deathTicks) {
		// TODO: Test / kick ? ...
		player.setHealth(0);
		player.damage(1);
	}

	@Override
	public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {
		// TODO: (?) Set some generic properties matching what BlockCache.getShape returns.
		final Set<Integer> fullBlocks = new HashSet<Integer>();
		for (final Material mat : new Material[]{
				Material.GLASS, Material.GLOWSTONE, Material.ICE, Material.LEAVES,
				Material.COMMAND, Material.BEACON,
				Material.PISTON_BASE,
		}){
			fullBlocks.add(mat.getId());
		}
		for (final Material mat : Material.values()){
			if (!mat.isBlock()) continue;
			final int id = mat.getId();
			if (id < 0 || id >= 4096 || fullBlocks.contains(id)) continue;
			if (!mat.isOccluding() || !mat.isSolid() || mat.isTransparent()){
				// Uncertain bounding-box, allow passing through.
				long flags = BlockProperties.F_IGN_PASSABLE;
				if ((BlockProperties.isSolid(id) || BlockProperties.isGround(id)) && !BlockProperties.isLiquid(id)){
					// Block can be ground, so allow standing on any height.
					flags |= BlockProperties.F_GROUND_HEIGHT;
				}
				BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
			}
		}
		// Blocks that are reported to be full and solid, but which are not.
		for (final Material mat : new Material[]{
				Material.ENDER_PORTAL_FRAME,
		}){
			final int id = mat.getId();
			final long flags = BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT;
			BlockProperties.setBlockFlags(id, BlockProperties.getBlockFlags(id) | flags);
		}
	}

	@Override
	public long getKeepAliveTime(final Player player) {
		// TODO: Implement if possible.
		return Long.MIN_VALUE;
	}
	
	@Override
	public boolean hasGravity(final Material mat) {
		try{
			return mat.hasGravity();
		}
		catch(Throwable t){
			switch(mat){
			case SAND:
			case GRAVEL:
				return true;
			default:
				return false;
			}
		}
	}
	
}
