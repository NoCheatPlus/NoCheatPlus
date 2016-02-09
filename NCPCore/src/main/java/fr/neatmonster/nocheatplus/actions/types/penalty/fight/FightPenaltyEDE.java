package fr.neatmonster.nocheatplus.actions.types.penalty.fight;

import org.bukkit.event.entity.EntityDamageByEntityEvent;

import fr.neatmonster.nocheatplus.actions.types.penalty.AbstractGenericPenalty;

/**
 * Fight penalties usually use EntityDamageByEntityEvent.
 * @author asofold
 *
 */
public abstract class FightPenaltyEDE extends AbstractGenericPenalty<EntityDamageByEntityEvent> {

    public FightPenaltyEDE() {
        super(EntityDamageByEntityEvent.class);
    }

}
