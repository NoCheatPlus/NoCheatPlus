package fr.neatmonster.nocheatplus.checks.combined;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ParameterName;
import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ViolationData;
import fr.neatmonster.nocheatplus.utilities.TickTask;

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
	public static final boolean check(final Player player, final float weight, final long now, final String tags){
		return instance.checkImprobable(player, weight, now, tags);
	}
	
	/**
	 * Feed the check but no violations processing (convenience method).
	 * @param player
	 * @param weight
	 * @param now
	 */
	public static final void feed(final Player player, final float weight, final long now){
		CombinedData.getData(player).improbableCount.add(now, weight);
	}
	
	////////////////////////////////////
	// Instance methods.
	///////////////////////////////////
	
	public Improbable() {
		super(CheckType.COMBINED_IMPROBABLE);
		instance = this;
	}

	private boolean checkImprobable(final Player player, final float weight, final long now, final String tags) {
		if (!isEnabled(player)) return false;
		final CombinedData data = CombinedData.getData(player);
		final CombinedConfig cc = CombinedConfig.getConfig(player);
		data.improbableCount.add(now, weight);
		final float shortTerm = data.improbableCount.bucketScore(0);
		double violation = 0;
		boolean violated = false;
		if (shortTerm * 0.8f > cc.improbableLevel / 20.0){
			final float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration(), true) : 1f;
			if (shortTerm / lag > cc.improbableLevel / 20.0){
				violation += shortTerm * 2d / lag;
				violated = true;
			}
		}
		final double full = data.improbableCount.score(1.0f);
		if (full > cc.improbableLevel){
			final float lag = cc.lag ? TickTask.getLag(data.improbableCount.bucketDuration() * data.improbableCount.numberOfBuckets(), true) : 1f;
			if (full / lag > cc.improbableLevel){
				violation += full / lag;
				violated = true;
			}
		}
		boolean cancel = false;
		if (violated){
			// Execute actions
			data.improbableVL += violation / 10.0;
			final ViolationData vd = new ViolationData(this, player, data.improbableVL, violation, cc.improbableActions);
			if (tags != null && !tags.isEmpty()) vd.setParameter(ParameterName.TAGS, tags);
			cancel = executeActions(vd);
		}
		else
			data.improbableVL *= 0.95;
		return cancel;
	}

}
