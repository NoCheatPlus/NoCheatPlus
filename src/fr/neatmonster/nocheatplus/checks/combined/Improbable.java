package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;

/**
 * This check  combines different other checks frequency and occurrecnces into one count.
 * (Intended for static access by other checks.) 
 *
 * @author mc_dev
 *
 */
public class Improbable extends Check {

	private static Improbable instance = null;
	
	/**
	 * Return if t cancel.
	 * @param player
	 * @param weights
	 * @param now
	 * @return
	 */
	public static final boolean check(final Player player, final float weight, final long now){
		return instance.checkImprobable(player, weight, now);
	}
	
	
	////////////////////////////////////
	// Instance methods.
	///////////////////////////////////
	
	public Improbable() {
		super(CheckType.COMBINED_IMPROBABLE);
		instance = this;
	}

	private boolean checkImprobable(final Player player, final float weight, final long now) {
		if (!isEnabled(player)) return false;
		final CombinedData data = CombinedData.getData(player);
		final CombinedConfig cc = CombinedConfig.getConfig(player);
		data.improbableCount.add(now, weight);
		final float shortTerm = data.improbableCount.getScore(0);
		double violation = 0;
		boolean violated = false;
		if (shortTerm * 0.8f > cc.improbableLevel / 20.0){
			violation += shortTerm * 2d;
			violated = true;
		}
		final double full = data.improbableCount.getScore(1.0f);
		if (full > cc.improbableLevel){
			violation += full;
			violated = true;
		}
		boolean cancel = false;
//		System.out.println("IMPROBABLE("+player.getName()+"): " + shortTerm + " / " + full + " / " + violation);
		if (violated){
			// Execute actions
			data.improbableVL += violation / 10.0;
			cancel = executeActions(player, data.improbableVL, violation / 10.0, cc.improbableActions);
		}
		else
			data.improbableVL *= 0.95;
		return cancel;
	}

}
