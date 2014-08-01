package fr.neatmonster.nocheatplus.checks.fight;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.Check;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.TickTask;

/**
 * 
 * @author mc_dev
 *
 */
public class FastHeal extends Check {
	public FastHeal(){
		super(CheckType.FIGHT_FASTHEAL);
	}
	
	public boolean check(final Player player){
		final long time = System.currentTimeMillis();
		final FightConfig cc = FightConfig.getConfig(player);
		final FightData data = FightData.getData(player);
		boolean cancel = false;
		if (time < data.fastHealRefTime || time - data.fastHealRefTime >= cc.fastHealInterval){
			// Reset.
			data.fastHealVL *= 0.96;
			// Only add a predefined amount to the buffer.
			// TODO: Confine regain-conditions further? (e.g. if vl < 0.1)
			data.fastHealBuffer = Math.min(cc.fastHealBuffer, data.fastHealBuffer + 50L);
		}
		else{
			// Violation.
			final double correctedDiff = ((double) time - data.fastHealRefTime) * TickTask.getLag(cc.fastHealInterval, true);
			// TODO: Consider using a simple buffer as well (to get closer to the correct interval).
			// TODO: Check if we added a buffer.
			if (correctedDiff < cc.fastHealInterval){
				data.fastHealBuffer -= (cc.fastHealInterval - correctedDiff);
				if (data.fastHealBuffer <= 0){
					final double violation = ((double) cc.fastHealInterval - correctedDiff) / 1000.0;
					data.fastHealVL += violation;
					if (executeActions(player, data.fastHealVL, violation, cc.fastHealActions)){
						cancel = true;
					}
				}
			}
		}
		
		if (cc.debug && player.hasPermission(Permissions.ADMINISTRATION_DEBUG)){
			player.sendMessage("Regain health(SATIATED): " + (time - data.fastHealRefTime) + " ms "+ "(buffer=" + data.fastHealBuffer + ")" +" , cancel=" + cancel);
		}
		
		data.fastHealRefTime = time;
		
		return cancel;
	}
}
