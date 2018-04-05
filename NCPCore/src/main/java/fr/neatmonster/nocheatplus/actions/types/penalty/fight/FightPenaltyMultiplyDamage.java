package fr.neatmonster.nocheatplus.actions.types.penalty.fight;

import org.bukkit.event.entity.EntityDamageEvent;

import fr.neatmonster.nocheatplus.compat.BridgeHealth;

/**
 * Multiply the final damage by a set amount.
 * 
 * @author asofold
 *
 */
public class FightPenaltyMultiplyDamage extends FightPenaltyEntityDamage {

    private final double multiplier;

    public FightPenaltyMultiplyDamage(final double multiplier) {
        super();
        this.multiplier = multiplier;
    }

    @Override
    protected void applyGenericEffects(final EntityDamageEvent event) {
        BridgeHealth.multiplyFinalDamage(event, multiplier);
    }

}
