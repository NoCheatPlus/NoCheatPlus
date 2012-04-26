package fr.neatmonster.nocheatplus.checks.fight;

import java.util.Locale;

import net.minecraft.server.EntityPlayer;
import net.minecraft.server.MobEffectList;

import org.bukkit.Material;
import org.bukkit.craftbukkit.entity.CraftPlayer;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.CheckUtils;
import fr.neatmonster.nocheatplus.players.NCPPlayer;
import fr.neatmonster.nocheatplus.players.informations.Permissions;
import fr.neatmonster.nocheatplus.players.informations.Statistics.Id;
import fr.neatmonster.nocheatplus.utilities.locations.PreciseLocation;

public class CriticalCheck extends FightCheck {

    public CriticalCheck() {
        super("critical", Permissions.FIGHT_CRITICAL);
    }

    @Override
    public boolean check(final NCPPlayer player, final Object... args) {
        final FightConfig cc = getConfig(player);
        final FightData data = getData(player);

        boolean cancel = false;

        // We'll need the entity to do all the important stuff
        final EntityPlayer entity = ((CraftPlayer) data.damager).getHandle();

        // First we're going to check if the entity is on ladder
        // Get the type of the block the entity is currently on
        final int type = data.damager.getWorld().getBlockTypeIdAt((int) Math.floor(entity.locX),
                (int) Math.floor(entity.locY), (int) Math.floor(entity.locZ));
        // Check if this block if a ladder/vine or not
        final boolean isOnLadder = type == Material.LADDER.getId() || type == Material.VINE.getId();

        // Then we're going to check if the entity is in water
        // Get the entity's precise location
        final PreciseLocation location = new PreciseLocation();
        location.x = entity.locX;
        location.y = entity.locY;
        location.z = entity.locZ;
        // Check if the entity is in water
        final boolean isInWater = CheckUtils.isLiquid(CheckUtils.evaluateLocation(data.damager.getWorld(), location));

        // Check the hit was a critical hit or not (fallDistance > 0, entity in
        // the air, not on ladder, not in water and no blindness effect)
        if (entity.fallDistance > 0.0F && !entity.onGround && !isOnLadder && !isInWater
                && !entity.hasEffect(MobEffectList.BLINDNESS))
            // That was a critical hit, now check if the player has jumped and not
            // just a sent a packet to mislead the server
            if (data.damager.getFallDistance() < cc.criticalFallDistance
                    || Math.abs(data.damager.getVelocity().getY()) < cc.criticalVelocity) {

                final double deltaFallDistance = cc.criticalFallDistance - data.damager.getFallDistance()
                        / cc.criticalFallDistance;
                final double deltaVelocity = cc.criticalVelocity - Math.abs(data.damager.getVelocity().getY())
                        / cc.criticalVelocity;
                final double delta = deltaFallDistance > 0D ? deltaFallDistance
                        : 0D + deltaVelocity > 0D ? deltaVelocity : 0D;
                // Player failed the check, but this is influenced by lag,
                // so don't do it if there was lag
                if (!NoCheatPlus.skipCheck()) {
                    // Increment the violation level
                    data.criticalVL += delta;
                    // Increment the statisctics of the player
                    incrementStatistics(player, Id.FI_CRITICAL, delta);
                }

                // Execute whatever actions are associated with this check and the
                // violation level and find out if we should cancel the event
                cancel = executeActions(player, cc.criticalActions, data.criticalVL);
            }
        return cancel;
    }

    @Override
    public String getParameter(final ParameterName wildcard, final NCPPlayer player) {

        if (wildcard == ParameterName.VIOLATIONS)
            return String.format(Locale.US, "%d", getData(player).criticalVL);
        else
            return super.getParameter(wildcard, player);
    }

    @Override
    public boolean isEnabled(final FightConfig cc) {
        return cc.criticalCheck;
    }
}
