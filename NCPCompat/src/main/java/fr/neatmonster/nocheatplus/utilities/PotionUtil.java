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
	 * @return Double.MIN_VALUE if not present, otherwise the maximal amplifier.
	 */
	public static final double getPotionEffectAmplifier(final Player player, final PotionEffectType type) {
		if (!player.hasPotionEffect(type)) return Double.MIN_VALUE;
		final Collection<PotionEffect> effects = player.getActivePotionEffects();
		double max = Double.MIN_VALUE;
		for (final PotionEffect effect : effects){
			if (effect.getType() == type){
				max = Math.max(max, effect.getAmplifier());
			}
		}
		return max;
	}

}
