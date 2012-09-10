package fr.neatmonster.nocheatplus.checks.combined;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.types.ActionList;
import fr.neatmonster.nocheatplus.checks.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.players.Permissions;

public class CombinedConfig extends ACheckConfig {
	
	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return CombinedConfig.getConfig(player);
		}
	};

	private static final Map<String, CombinedConfig> worldsMap = new HashMap<String, CombinedConfig>();

	protected static CombinedConfig getConfig(final Player player) {
		final String worldName = player.getWorld().getName();
		CombinedConfig cc = worldsMap.get(worldName);
		if (cc == null){
			cc = new CombinedConfig(ConfigManager.getConfigFile(worldName));
			worldsMap.put(worldName, cc);
		}
		return cc;
	}

	public final boolean improbableCheck;
	public final float improbableLevel;
	public final ActionList improbableActions;
	
	public CombinedConfig(final ConfigFile config) {
		improbableCheck = config.getBoolean(ConfPaths.COMBINED_IMPROBABLE_CHECK, false);
		improbableLevel = (float) config.getDouble(ConfPaths.COMBINED_IMPROBABLE_LEVEL, 300);
		improbableActions = config.getActionList(ConfPaths.COMBINED_IMPROBABLE_ACTIONS, Permissions.COMBINED_IMPROBABLE);
	}

	@Override
	public boolean isEnabled(final CheckType checkType) {
		switch(checkType){
		case COMBINED_IMPROBABLE:
			return improbableCheck;
		}
		return false;
	}

	public static void clear() {
		worldsMap.clear();
	}

}
