package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

public class SelfHit extends Check {

	public SelfHit() {
		super(CheckType.FIGHT_SELFHIT);
	}
	
	public boolean check(final Player damager, final Player damaged, final FightData data, final FightConfig cc){
		if (!damager.getName().equals(damaged.getName())) return false;
		
		boolean cancel = false;
		// Treat self hitting as instant violation.
		data.selfHitVL.add(System.currentTimeMillis(), 1.0f);
		// NOTE: This lets VL decrease slightly over 30 seconds, one could also use a number, but  this is more tolerant.
		cancel = executeActions(damager, data.selfHitVL.score(0.99f), 1.0f, cc.selfHitActions);
		
		return cancel;
	}

}
