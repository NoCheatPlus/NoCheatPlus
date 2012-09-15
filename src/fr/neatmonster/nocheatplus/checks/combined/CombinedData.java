package fr.neatmonster.nocheatplus.checks.combined;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.checks.access.ACheckData;
import fr.neatmonster.nocheatplus.checks.access.CheckDataFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckData;
import fr.neatmonster.nocheatplus.utilities.ActionFrequency;

public class CombinedData extends ACheckData {
	
	/** The factory creating data. */
	public static final CheckDataFactory factory = new CheckDataFactory() {
		@Override
		public final ICheckData getData(final Player player) {
			return CombinedData.getData(player);
		}

		@Override
		public ICheckData removeData(final String playerName) {
			return CombinedData.removeData(playerName);
		}
	};
                                                    
    private static final Map<String, CombinedData> playersMap = new HashMap<String, CombinedData>();

	public static CombinedData getData(final Player player) {
		final String playerName = player.getName(); 
		CombinedData data = playersMap.get(playerName);
		if (data == null){
			data = new CombinedData(player);
			playersMap.put(playerName, data);
		}
		return data;
	}
	
	public static ICheckData removeData(final String playerName) {
		return playersMap.remove(playerName);
	}

	public double improbableVL = 0;
	public double speedVL = 0;
	
	public final ActionFrequency improbableCount = new ActionFrequency(20, 3000);
	
	public final ActionFrequency speedCount = new ActionFrequency(20, 3000);
	
	public CombinedData(final Player player){
//		final CombinedConfig cc = CombinedConfig.getConfig(player);
		// TODO: Get some things from the config.
	}

}
