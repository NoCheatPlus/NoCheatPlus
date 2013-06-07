package fr.neatmonster.nocheatplus.checks.combined;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

public class CombinedConfig extends ACheckConfig {
	
	/** The factory creating configurations. */
	public static final CheckConfigFactory factory = new CheckConfigFactory() {
		@Override
		public final ICheckConfig getConfig(final Player player) {
			return CombinedConfig.getConfig(player);
		}
	};

	private static final Map<String, CombinedConfig> worldsMap = new HashMap<String, CombinedConfig>();

	public static CombinedConfig getConfig(final Player player) {
		final String worldName = player.getWorld().getName();
		CombinedConfig cc = worldsMap.get(worldName);
		if (cc == null){
			cc = new CombinedConfig(ConfigManager.getConfigFile(worldName));
			worldsMap.put(worldName, cc);
		}
		return cc;
	}
	
	
	// Bedleave check.
	public final boolean        bedLeaveCheck;
	public final ActionList     bedLeaveActions;
	
	// Ender pearl
	public final boolean enderPearlCheck;
	public final boolean enderPearlPreventClickBlock;

	// Improbable check
	/** Do mind that this flag is not used by all components. */
	public final boolean        improbableCheck;
	public final float          improbableLevel;
	public final ActionList     improbableActions;
	
	// Invulnerable management.
	public final boolean                    invulnerableCheck;
    public final int                        invulnerableInitialTicksJoin;
    public final Set<DamageCause>           invulnerableIgnore = new HashSet<DamageCause>();
    public final Map<DamageCause, Integer>  invulnerableModifiers = new HashMap<DamageCause, Integer>();
    public final int                        invulnerableModifierDefault;
    public final boolean                    invulnerableTriggerAlways;
    public final boolean                    invulnerableTriggerFallDistance;
    
	public final boolean 		munchHausenCheck;
	public final ActionList 	munchHausenActions;
	
	// Last yaw tracking 
	public final float      yawRate;
	public final boolean    yawRateImprobable;
    public final float      yawRatePenaltyFactor;
    public final int        yawRatePenaltyMin;
    public final int        yawRatePenaltyMax;
	
	public CombinedConfig(final ConfigFile config) {
	    super(config, ConfPaths.COMBINED);
	    
		bedLeaveCheck = config.getBoolean(ConfPaths.COMBINED_BEDLEAVE_CHECK);
		bedLeaveActions = config.getOptimizedActionList(ConfPaths.COMBINED_BEDLEAVE_ACTIONS, Permissions.COMBINED_BEDLEAVE);

		enderPearlCheck = config.getBoolean(ConfPaths.COMBINED_ENDERPEARL_CHECK);
		enderPearlPreventClickBlock = config.getBoolean(ConfPaths.COMBINED_ENDERPEARL_PREVENTCLICKBLOCK);
		
		improbableCheck = config.getBoolean(ConfPaths.COMBINED_IMPROBABLE_CHECK);
		improbableLevel = (float) config.getDouble(ConfPaths.COMBINED_IMPROBABLE_LEVEL);
		improbableActions = config.getOptimizedActionList(ConfPaths.COMBINED_IMPROBABLE_ACTIONS, Permissions.COMBINED_IMPROBABLE);
		
	    invulnerableCheck = config.getBoolean(ConfPaths.COMBINED_INVULNERABLE_CHECK);
	    invulnerableInitialTicksJoin = config.getInt(ConfPaths.COMBINED_INVULNERABLE_INITIALTICKS_JOIN);
	    boolean error = false;
	    // Read ignored causes.
	    for (final String input : config.getStringList(ConfPaths.COMBINED_INVULNERABLE_IGNORE)){
	        final String normInput = input.trim().toUpperCase();
	        try{
	            invulnerableIgnore.add(DamageCause.valueOf(normInput.replace(' ', '_').replace('-', '_')));
	        }
	        catch (final Exception e){
	            error = true;
	            LogUtil.logWarning("[NoCheatPlus] Bad damage cause (combined.invulnerable.ignore): " + input);
	        }
	    }
	    // Read modifiers for causes.
	    Integer defaultMod = 0;
	    final ConfigurationSection sec = config.getConfigurationSection(ConfPaths.COMBINED_INVULNERABLE_MODIFIERS);
        for (final String input : sec.getKeys(false)){
            final int modifier = sec.getInt(input, 0);
            final String normInput = input.trim().toUpperCase();
            if (normInput.equals("ALL")){
                defaultMod = modifier;
                continue;
            }
            try{
                invulnerableModifiers.put(DamageCause.valueOf(normInput.replace(' ', '_').replace('-', '_')), modifier);
            }
            catch (final Exception e){
                error = true;
                LogUtil.logWarning("[NoCheatPlus] Bad damage cause (combined.invulnerable.modifiers): " + input);
            }
        }
        invulnerableModifierDefault = defaultMod;
	    if (error) LogUtil.logInfo("[NoCheatPlus] Damage causes can be: " + StringUtil.join(Arrays.asList(DamageCause.values()), ", "));
	    invulnerableTriggerAlways = config.getBoolean(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_ALWAYS);
	    invulnerableTriggerFallDistance = config.getBoolean(ConfPaths.COMBINED_INVULNERABLE_TRIGGERS_FALLDISTANCE);
	    
	    munchHausenCheck = config.getBoolean(ConfPaths.COMBINED_MUNCHHAUSEN_CHECK);
	    munchHausenActions = config.getOptimizedActionList(ConfPaths.COMBINED_MUNCHHAUSEN_ACTIONS, Permissions.COMBINED_MUNCHHAUSEN);
	    
		yawRate = config.getInt(ConfPaths.COMBINED_YAWRATE_RATE);
		yawRateImprobable = config.getBoolean(ConfPaths.COMBINED_YAWRATE_IMPROBABLE);
		yawRatePenaltyFactor = (float) config.getDouble(ConfPaths.COMBINED_YAWRATE_PENALTY_FACTOR);
		yawRatePenaltyMin = config.getInt(ConfPaths.COMBINED_YAWRATE_PENALTY_MIN);
		yawRatePenaltyMax = config.getInt(ConfPaths.COMBINED_YAWRATE_PENALTY_MAX);
	}

	@Override
	public boolean isEnabled(final CheckType checkType) {
		switch(checkType){
			case COMBINED_IMPROBABLE:
				return improbableCheck;
			case COMBINED_BEDLEAVE:
				return bedLeaveCheck;
			case COMBINED_MUNCHHAUSEN:
				return munchHausenCheck;
			default:
				return false;
		}
	}

	public static void clear() {
		worldsMap.clear();
	}

}
