package fr.neatmonster.nocheatplus.compat.bukkit;


import java.util.HashSet;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.ComplexEntityPart;
import org.bukkit.entity.ComplexLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.BridgeHealth;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.utilities.BlockCache;
import fr.neatmonster.nocheatplus.utilities.BlockProperties;
import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import fr.neatmonster.nocheatplus.utilities.ReflectionUtil;

public class MCAccessBukkit implements MCAccess, BlockPropertiesSetup{

    // private AlmostBoolean entityPlayerAvailable = AlmostBoolean.MAYBE;

    /**
     * Constructor to let it fail.
     */
    public MCAccessBukkit() {
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
        return "1.4.6|1.4.7|1.5.x|1.6.x|1.7.x"; // 1.7.x is bold!
    }

    @Override
    public String getServerVersionTag() {
        return "Bukkit-API";
    }

    @Override
    public CommandMap getCommandMap() {
        try{
            return (CommandMap) ReflectionUtil.invokeMethodNoArgs(Bukkit.getServer(), "getCommandMap");
        } catch (Throwable t) {
            // Nasty.
            return null;
        }
    }

    @Override
    public BlockCache getBlockCache(final World world) {
        return new BlockCacheBukkit(world);
    }

    @Override
    public double getHeight(final Entity entity) {
        // TODO: Copy defaults like with widths.
        final double entityHeight = 1.0;
        if (entity instanceof LivingEntity) {
            return Math.max(((LivingEntity) entity).getEyeHeight(), entityHeight);
        } else {
            return entityHeight;
        }
    }

    @Override
    public AlmostBoolean isBlockSolid(final int id) {
        @SuppressWarnings("deprecation")
        final Material mat = Material.getMaterial(id); 
        if (mat == null) {
            return AlmostBoolean.MAYBE;
        }
        else {
            return AlmostBoolean.match(mat.isSolid());
        }
    }

    @Override
    public double getWidth(final Entity entity) {
        // TODO: Make readable from file for defaults + register individual getters where appropriate.
        // TODO: For height too. [Automatize most by spawning + checking?]
        // Values taken from 1.7.10.
        final EntityType type = entity.getType();
        switch(type){
        // TODO: case COMPLEX_PART:
        case ENDER_SIGNAL: // this.a(0.25F, 0.25F);
        case FIREWORK: // this.a(0.25F, 0.25F);
        case FISHING_HOOK: // this.a(0.25F, 0.25F);
        case DROPPED_ITEM: // this.a(0.25F, 0.25F);
        case SNOWBALL: // (projectile) this.a(0.25F, 0.25F);
            return 0.25;
        case CHICKEN: // this.a(0.3F, 0.7F);
        case SILVERFISH: // this.a(0.3F, 0.7F);
            return 0.3f;
        case SMALL_FIREBALL: // this.a(0.3125F, 0.3125F);
        case WITHER_SKULL: // this.a(0.3125F, 0.3125F);
            return 0.3125f;
        case GHAST: // this.a(4.0F, 4.0F);
        case SNOWMAN: // this.a(0.4F, 1.8F);
            return 0.4f;
        case ARROW: // this.a(0.5F, 0.5F);
        case BAT: // this.a(0.5F, 0.9F);
        case EXPERIENCE_ORB: // this.a(0.5F, 0.5F);
        case ITEM_FRAME: // hanging: this.a(0.5F, 0.5F);
        case PAINTING: // hanging: this.a(0.5F, 0.5F);
            return 0.5f;
        case PLAYER: // FAST RETURN
        case ZOMBIE:
        case PIG_ZOMBIE:
        case SKELETON:
        case CREEPER:
        case ENDERMAN:
        case OCELOT:
        case BLAZE:
        case VILLAGER:
        case WITCH:
        case WOLF:
            return 0.6f; // (Default entity width.)
        case CAVE_SPIDER: // this.a(0.7F, 0.5F);
            return 0.7f;
        case COW: // this.a(0.9F, 1.3F);
        case MUSHROOM_COW: // this.a(0.9F, 1.3F);
        case PIG: // this.a(0.9F, 0.9F);
        case SHEEP: // this.a(0.9F, 1.3F);
        case WITHER: // this.a(0.9F, 4.0F);
            return 0.9f;
        case SQUID: // this.a(0.95F, 0.95F);
            return 0.95f;
        case PRIMED_TNT: // this.a(0.98F, 0.98F);
            return 0.98f;
        case FIREBALL: // (EntityFireball) this.a(1.0F, 1.0F);
            return 1.0f;
        case IRON_GOLEM: // this.a(1.4F, 2.9F);
        case SPIDER: // this.a(1.4F, 0.9F);
            return 1.4f;
        case BOAT: // this.a(1.5F, 0.6F);
            return 1.5f;
        case ENDER_CRYSTAL: // this.a(2.0F, 2.0F);
            return 2.0f;
        case GIANT: // this.height *= 6.0F; this.a(this.width * 6.0F, this.length * 6.0F);
            return 3.6f; // (Better than nothing.)
        case ENDER_DRAGON: // this.a(16.0F, 8.0F);
            return 16.0f;
            // Variable size:
        case SLIME:
        case MAGMA_CUBE:
            if (entity instanceof Slime) {
                // setSize(i): this.a(0.6F * (float) i, 0.6F * (float) i);
                return 0.6f * ((Slime) entity).getSize();
            }
        default:
            break;
        }
        // Check by instance for minecarts (too many).
        if (entity instanceof Minecart) {
            return 0.98f; // this.a(0.98F, 0.7F);
        }
        // Latest Bukkit API.
        try {
            switch (type) {
            case LEASH_HITCH: // hanging: this.a(0.5F, 0.5F);
                return 0.5;
            case HORSE: // this.a(1.4F, 1.6F);
                return 1.4f;
            default:
                break;
            }
        } catch (Throwable t) {}
        // Default entity width.
        return 0.6f;

    }

    @Override
    public AlmostBoolean isBlockLiquid(final int id) {
        @SuppressWarnings("deprecation")
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
    public AlmostBoolean isIllegalBounds(final Player player) {
        if (player.isDead()) {
            return AlmostBoolean.NO;
        }
        if (!player.isSleeping()) { // TODO: ignored sleeping ?
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

    @Override
    public void setInvulnerableTicks(final Player player, final int ticks) {
        // TODO: Not really.
        player.setLastDamageCause(BridgeHealth.getEntityDamageEvent(player, DamageCause.CUSTOM, 500.0));
        player.setNoDamageTicks(ticks);
    }

    @Override
    public void dealFallDamage(final Player player, final double damage) {
        // TODO: Document in knowledge base.
        // TODO: Account for armor, other.
        // TODO: use setLastDamageCause here ?
        BridgeHealth.damage(player, damage);
    }

    @Override
    public boolean isComplexPart(final Entity entity) {
        return entity instanceof ComplexEntityPart || entity instanceof ComplexLivingEntity;
    }

    @Override
    public boolean shouldBeZombie(final Player player) {
        // Not sure :) ...
        return BridgeHealth.getHealth(player) <= 0.0 && !player.isDead();
    }

    @Override
    public void setDead(final Player player, final int deathTicks) {
        // TODO: Test / kick ? ...
        BridgeHealth.setHealth(player, 0.0);
        // TODO: Might try stuff like setNoDamageTicks.
        BridgeHealth.damage(player, 1.0);
    }

    @Override
    public void setupBlockProperties(final WorldConfigProvider<?> worldConfigProvider) {
        // Note deprecation suppression: These ids should be unique for a server run, that should be ok for setting up generic properties.
        // TODO: (?) Set some generic properties matching what BlockCache.getShape returns.
        final Set<Material> fullBlocks = new HashSet<Material>();
        for (final Material mat : new Material[]{
                // TODO: Ice !? / Packed ice !?
                Material.GLASS, Material.GLOWSTONE, Material.ICE, Material.LEAVES,
                Material.COMMAND, Material.BEACON,
                Material.PISTON_BASE,
        }) {
            fullBlocks.add(mat);
        }
        for (final Material mat : Material.values()) {
            if (!mat.isBlock()) {
                continue;
            }
            if (fullBlocks.contains(mat)) {
                continue;
            }
            if (!mat.isOccluding() || !mat.isSolid() || mat.isTransparent()) {
                // Uncertain bounding-box, allow passing through.
                long flags = BlockProperties.F_IGN_PASSABLE;
                if ((BlockProperties.isSolid(mat) || BlockProperties.isGround(mat)) && !BlockProperties.isLiquid(mat)) {
                    // Block can be ground, so allow standing on any height.
                    flags |= BlockProperties.F_GROUND_HEIGHT;
                }
                BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
            }
        }
        // Blocks that are reported to be full and solid, but which are not.
        for (final Material mat : new Material[]{
                Material.ENDER_PORTAL_FRAME,
        }) {
            final long flags = BlockProperties.F_IGN_PASSABLE | BlockProperties.F_GROUND_HEIGHT;
            BlockProperties.setBlockFlags(mat, BlockProperties.getBlockFlags(mat) | flags);
        }
    }

    @Override
    public boolean hasGravity(final Material mat) {
        try{
            return mat.hasGravity();
        }
        catch(Throwable t) {
            // Backwards compatibility.
            switch(mat) {
            case SAND:
            case GRAVEL:
                return true;
            default:
                return false;
            }
        }
    }

    //	@Override
    //	public void correctDirection(Player player) {
    //		// TODO: Consider using reflection (detect CraftPlayer, access EntityPlayer + check if possible (!), use flags for if valid or invalid.)
    //	}

}
