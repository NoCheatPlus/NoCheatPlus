package fr.neatmonster.nocheatplus.actions.types.penalty.fight;

import org.bukkit.event.entity.EntityDamageEvent;

import fr.neatmonster.nocheatplus.actions.types.penalty.AbstractGenericPenalty;

/**
 * Basic fight specific penalty.
 * 
 * @author asofold
 *
 */
public abstract class FightPenaltyEntityDamage extends AbstractGenericPenalty<EntityDamageEvent> {

    public FightPenaltyEntityDamage() {
        super(EntityDamageEvent.class);
    }

}
