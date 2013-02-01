package fr.neatmonster.nocheatplus.utilities;

import java.util.Collection;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Could not help it.
 * @author mc_dev
 *
 */
public class PotionUtil {

	/**
	 * Get amplifier for a potion effect.
	 * @param player
	 * @param type
	 * @return Double.NEGATIVE_INFINITY if not present, otherwise the maximal amplifier.
	 */
	public static final double getPotionEffectAmplifier(final Player player, final PotionEffectType type) {
		if (!player.hasPotionEffect(type)) return Double.NEGATIVE_INFINITY; // TODO: Might not win anything.
		final Collection<PotionEffect> effects = player.getActivePotionEffects();
		double max = Double.NEGATIVE_INFINITY;
		for (final PotionEffect effect : effects){
			if (effect.getType().equals(type)){
				max = Math.max(max, effect.getAmplifier());
			}
		}
		return max;
	}

}
