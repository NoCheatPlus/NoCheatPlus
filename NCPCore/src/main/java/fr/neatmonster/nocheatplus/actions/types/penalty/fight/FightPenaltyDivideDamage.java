package fr.neatmonster.nocheatplus.actions.types.penalty.fight;

import org.bukkit.event.entity.EntityDamageEvent;

import fr.neatmonster.nocheatplus.compat.BridgeHealth;

public class FightPenaltyDivideDamage extends FightPenaltyEntityDamage {
    
    private final double divisor;

    public FightPenaltyDivideDamage(final double divisor) {
        super();
        this.divisor = divisor;
    }

    @Override
    protected void applyGenericEffects(final EntityDamageEvent event) {
        BridgeHealth.setDamage(event, BridgeHealth.getDamage(event) / divisor);
    }

}
