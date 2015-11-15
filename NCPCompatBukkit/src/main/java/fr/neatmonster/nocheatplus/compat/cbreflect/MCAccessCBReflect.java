package fr.neatmonster.nocheatplus.compat.cbreflect;

import org.bukkit.World;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.bukkit.BlockCacheBukkit;
import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkitBase;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper;
import fr.neatmonster.nocheatplus.compat.cbreflect.reflect.ReflectHelper.ReflectFailureException;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.BlockCache;

public class MCAccessCBReflect extends MCAccessBukkitBase {

    protected final ReflectHelper helper;
    
    /** Generally supported Minecraft version (know for sure). */
    protected final boolean knownSupportedVersion;
    /** We know for sure that dealFallDamage will fire a damage event. */
    protected final boolean dealFallDamageFiresAnEvent;  

    public MCAccessCBReflect() throws ReflectFailureException {
        // TODO: Add unavailable stuff to features / missing (TBD).
        helper = new ReflectHelper();
        // Version Envelope tests (1.4.5-R1.0 ... 1.8.x is considered to be ok).
        final String mcVersion = ServerVersion.getMinecraftVersion();
        if (mcVersion == ServerVersion.UNKNOWN_VERSION) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.INIT, "The Minecraft version could not be detected, Compat-CB-Reflect might or might not work.");
            this.knownSupportedVersion = false;
        }
        else if (ServerVersion.compareVersions(mcVersion, "1.4.5") < 0) {
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.INIT, "The Minecraft version seems to be older than what Compat-CB-Reflect can support.");
            this.knownSupportedVersion = false;
        }
        else if (ServerVersion.compareVersions(mcVersion, "1.9") >= 0) {
            this.knownSupportedVersion = false;
            NCPAPIProvider.getNoCheatPlusAPI().getLogManager().warning(Streams.INIT, "The Minecraft version seems to be more recent than the one Compat-CB-Reflect has been built with - this might work, but there could be incompatibilities.");
        } else {
            this.knownSupportedVersion = true;
        }
        // Fall damage / event. TODO: Tests between 1.8 and 1.7.2. How about spigot vs. CB?
        if (mcVersion == ServerVersion.UNKNOWN_VERSION || ServerVersion.compareVersions(mcVersion, "1.8") < 0) {
            dealFallDamageFiresAnEvent = false;
        } else {
            // Assume higher versions to fire an event.
            dealFallDamageFiresAnEvent = true;
        }
    }

    @Override
    public String getMCVersion() {
        // Potentially all :p.
        return "1.4.5-1.8.8|?";
    }

    @Override
    public String getServerVersionTag() {
        return "CB-Reflect";
    }

    @Override
    public BlockCache getBlockCache(World world) {
        try {
            return new BlockCacheCBReflect(helper, world);
        }
        catch (ReflectFailureException ex) {
            return new BlockCacheBukkit(world);
        }
    }

    @Override
    public boolean shouldBeZombie(Player player) {
        try {
            Object handle = helper.getHandle(player);
            return !helper.nmsPlayer_dead(handle) && helper.nmsPlayer_getHealth(handle) <= 0.0; 
        }
        catch (ReflectFailureException ex) {
            // Fall back to Bukkit.
            return super.shouldBeZombie(player);
        }
    }

    @Override
    public void setDead(Player player, int deathTicks) {
        try {
            Object handle = helper.getHandle(player);
            helper.nmsPlayer_dead(handle, true);
            helper.nmsPlayer_deathTicks(handle, deathTicks);
        }
        catch (ReflectFailureException ex) {
            super.setDead(player, deathTicks);
        }
    }

    @Override
    public AlmostBoolean dealFallDamageFiresAnEvent() {
        if (!dealFallDamageFiresAnEvent) {
            return AlmostBoolean.NO;
        }
        return AlmostBoolean.match(this.helper.canDealFallDamage());
    }

    @Override
    public void dealFallDamage(final Player player, final double damage) {
        try {
            helper.dealFallDamage(player, damage);
        }
        catch (ReflectFailureException ex) {
            // TODO: Fire an event ?
            super.dealFallDamage(player, damage);
        }
    }

    @Override
    public int getInvulnerableTicks(final Player player) {
        try {
            return helper.getInvulnerableTicks(player);
        }
        catch (ReflectFailureException ex) {
            return super.getInvulnerableTicks(player);
        }
    }

    @Override
    public void setInvulnerableTicks(final Player player, final int ticks) {
        try {
            helper.setInvulnerableTicks(player, ticks);
        }
        catch (ReflectFailureException ex) {
            super.setInvulnerableTicks(player, ticks);
        }
    }

    @Override
    public double getSpeedAttributeMultiplier(Player player) {
        try {
            return helper.getSpeedAttributeMultiplier(player, true);
        }
        catch (ReflectFailureException ex) {
            return super.getSpeedAttributeMultiplier(player);
        }
    }

    @Override
    public double getSprintAttributeMultiplier(Player player) {
        try {
            return helper.getSprintAttributeMultiplier(player);
        }
        catch (ReflectFailureException ex) {
            return super.getSprintAttributeMultiplier(player);
        }
    }

    @Override
    public AlmostBoolean isBlockSolid(final int id) {
        try {
            return helper.isBlockSolid(id);
        }
        catch (ReflectFailureException ex) {
            return super.isBlockSolid(id);
        }
    }

    @Override
    public AlmostBoolean isBlockLiquid(final int id) {
        try {
            return helper.isBlockLiquid(id);
        }
        catch (ReflectFailureException ex) {
            return super.isBlockLiquid(id);
        }
    }


    // TODO: ---- Missing (better to implement these) ----

    //    @Override
    //    public double getHeight(final Entity entity) {
    //        final net.minecraft.server.v1_8_R3.Entity mcEntity = ((CraftEntity) entity).getHandle();
    //        AxisAlignedBB boundingBox = mcEntity.getBoundingBox();
    //        final double entityHeight = Math.max(mcEntity.length, Math.max(mcEntity.getHeadHeight(), boundingBox.e - boundingBox.b));
    //        if (entity instanceof LivingEntity) {
    //            return Math.max(((LivingEntity) entity).getEyeHeight(), entityHeight);
    //        } else return entityHeight;
    //    }

    //    @Override
    //    public double getWidth(final Entity entity) {
    //        return ((CraftEntity) entity).getHandle().width;
    //    }

    //    @Override
    //    public AlmostBoolean isIllegalBounds(final Player player) {
    //        final EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
    //        if (entityPlayer.dead) {
    //            return AlmostBoolean.NO;
    //        }
    //        // TODO: Does this need a method call for the "real" box? Might be no problem during moving events, though.
    //        final AxisAlignedBB box = entityPlayer.getBoundingBox();
    //        if (!entityPlayer.isSleeping()) {
    //            // This can not really test stance but height of bounding box.
    //            final double dY = Math.abs(box.e - box.b);
    //            if (dY > 1.8) {
    //                return AlmostBoolean.YES; // dY > 1.65D || 
    //            }
    //            if (dY < 0.1D && entityPlayer.length >= 0.1) {
    //                return AlmostBoolean.YES;
    //            }
    //        }
    //        return AlmostBoolean.MAYBE;
    //    }


    // ---- Missing (probably ok with Bukkit only) ----

    //    @Override
    //    public double getJumpAmplifier(final Player player) {
    //        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
    //        if (mcPlayer.hasEffect(MobEffectList.JUMP)) {
    //            return mcPlayer.getEffect(MobEffectList.JUMP).getAmplifier();
    //        }
    //        else {
    //            return Double.NEGATIVE_INFINITY;
    //        }
    //    }

    //    @Override
    //    public double getFasterMovementAmplifier(final Player player) {
    //        final EntityPlayer mcPlayer = ((CraftPlayer) player).getHandle();
    //        if (mcPlayer.hasEffect(MobEffectList.FASTER_MOVEMENT)) {
    //            return mcPlayer.getEffect(MobEffectList.FASTER_MOVEMENT).getAmplifier();
    //        }
    //        else {
    //            return Double.NEGATIVE_INFINITY;
    //        }
    //    }

    //    @Override
    //    public boolean isComplexPart(final Entity entity) {
    //        return ((CraftEntity) entity).getHandle() instanceof EntityComplexPart;
    //    }

    // (getCommandMap already uses reflection, but could be more speedy.).

}
