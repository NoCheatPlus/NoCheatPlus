package fr.neatmonster.nocheatplus.checks.moving;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.moving.model.ModelFlying;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.versions.Bugs;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;

/**
 * Configurations specific for the moving checks. Every world gets one of these assigned to it.
 */
public class MovingConfig extends ACheckConfig {

    /** The factory creating configurations. */
    public static final CheckConfigFactory factory = new CheckConfigFactory() {
        @Override
        public final ICheckConfig getConfig(final Player player) {
            return MovingConfig.getConfig(player);
        }

        @Override
        public void removeAllConfigs() {
            clear(); // Band-aid.
        }
    };

    /** The map containing the configurations per world. */
    private static final Map<String, MovingConfig> worldsMap = new HashMap<String, MovingConfig>();

    /**
     * Clear all the configurations.
     */
    public static void clear() {
        worldsMap.clear();
    }

    /**
     * Gets the configuration for a specified player .
     * <br>NOTE: Currently only per-world configs are implemented. This method might or might not get removed some day.
     * 
     * @param player
     *            the player
     * @return the configuration
     */
    public static MovingConfig getConfig(final Player player) {
        return getConfig(player.getWorld().getName());
    }

    /**
     * Get a per-world config.
     * @param worldName Exact case world name.
     * @return
     */
    public static MovingConfig getConfig(final String worldName) {
        final MovingConfig cc = worldsMap.get(worldName); 
        if (cc != null){
            return cc;
        }
        final MovingConfig ccNew = new MovingConfig(ConfigManager.getConfigFile(worldName));
        worldsMap.put(worldName, ccNew);
        return ccNew;
    }


    public final boolean    ignoreCreative;
    public final boolean    ignoreAllowFlight;

    public final boolean    creativeFlyCheck;
    public final Map<GameMode, ModelFlying> flyingModels = new HashMap<GameMode, ModelFlying>();
    public final ActionList creativeFlyActions;

    public final boolean    morePacketsCheck;
    /** Assumed number of packets per second under ideal conditions. */
    public final float      morePacketsEPSIdeal;
    /** The maximum number of packets per second that we accept. */
    public final float      morePacketsEPSMax;
    public final int        morePacketsEPSBuckets;
    public final float		morePacketsBurstPackets;
    public final double		morePacketsBurstDirect;
    public final double		morePacketsBurstEPM;
    public final ActionList morePacketsActions;

    public final boolean    morePacketsVehicleCheck;
    public final ActionList morePacketsVehicleActions;

    public final boolean    noFallCheck;
    /** Deal damage instead of Minecraft, whenever a player is judged to be on ground. */
    public final boolean    noFallDealDamage;
    /** Reset data on violation, i.e. a player taking fall damage without being on ground. */
    public final boolean    noFallViolationReset;
    /** Reset data on tp. */
    public final boolean 	noFallTpReset;
    /** Reset if in vehicle. */
    public final boolean noFallVehicleReset;
    /** Reset fd to 0  if on ground (dealdamage only). */
    public final boolean noFallAntiCriticals;
    public final ActionList noFallActions;

    public final boolean    passableCheck;
    public final boolean	passableRayTracingCheck;
    public final boolean	passableRayTracingBlockChangeOnly;
    // TODO: passableAccuracy: also use if not using ray-tracing
    public final ActionList passableActions;
    public final boolean    passableUntrackedTeleportCheck;
    public final boolean    passableUntrackedCommandCheck;
    public final boolean    passableUntrackedCommandTryTeleport;
    public final SimpleCharPrefixTree passableUntrackedCommandPrefixes = new SimpleCharPrefixTree();

    public final boolean    survivalFlyCheck;
    public final int        survivalFlyBlockingSpeed;
    public final int        survivalFlySneakingSpeed;
    public final int        survivalFlySpeedingSpeed;
    public final int        survivalFlySprintingSpeed;
    public final int        survivalFlySwimmingSpeed;
    public final int        survivalFlyWalkingSpeed;
    public final boolean    survivalFlyCobwebHack;
    public final boolean    survivalFlyAccountingH;
    public final boolean    survivalFlyAccountingV;
    public final boolean    sfSetBackPolicyVoid;
    public final boolean    sfSetBackPolicyFallDamage;
    public final double     sfStepHeight;
    public final long       survivalFlyVLFreeze;
    public final ActionList survivalFlyActions;

    public final boolean 	sfHoverCheck;
    public final int 		sfHoverTicks;
    public final int		sfHoverLoginTicks;
    public final boolean    sfHoverFallDamage;
    public final double		sfHoverViolation;

    // Special tolerance values:
    /** This is not strictly ticks, but packets, for now.*/
    public final int 		velocityGraceTicks;
    public final int		velocityActivationCounter;
    public final int		velocityActivationTicks;
    public final boolean	velocityStrictInvalidation;
    public final double     noFallyOnGround;
    public final double     yOnGround;

    // General things.
    public final boolean ignoreStance;
    public final boolean tempKickIllegal;
    public final boolean loadChunksOnJoin;
    public final long sprintingGrace;
    public final boolean assumeSprint;
    public final int speedGrace;
    public final boolean enforceLocation;

    // Vehicles
    public final boolean vehicleEnforceLocation;
    public final boolean vehiclePreventDestroyOwn;

    // Trace
    public final int traceSize;
    public final double traceMergeDist;


    /**
     * Instantiates a new moving configuration.
     * 
     * @param config
     *            the data
     */
    public MovingConfig(final ConfigFile config) {
        super(config, ConfPaths.MOVING);

        ignoreCreative = config.getBoolean(ConfPaths.MOVING_CREATIVEFLY_IGNORECREATIVE);
        ignoreAllowFlight = config.getBoolean(ConfPaths.MOVING_CREATIVEFLY_IGNOREALLOWFLIGHT);

        creativeFlyCheck = config.getBoolean(ConfPaths.MOVING_CREATIVEFLY_CHECK);
        final ModelFlying defaultModel = new ModelFlying(config, ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative.", new ModelFlying());
        for (final GameMode gameMode : GameMode.values()) {
            flyingModels.put(gameMode, new ModelFlying(config, ConfPaths.MOVING_CREATIVEFLY_MODEL + (gameMode.name().toLowerCase()) + ".", defaultModel));
        }
        creativeFlyActions = config.getOptimizedActionList(ConfPaths.MOVING_CREATIVEFLY_ACTIONS, Permissions.MOVING_CREATIVEFLY);

        morePacketsCheck = config.getBoolean(ConfPaths.MOVING_MOREPACKETS_CHECK);
        morePacketsEPSIdeal = config.getInt(ConfPaths.MOVING_MOREPACKETS_EPSIDEAL);
        morePacketsEPSMax = Math.max(morePacketsEPSIdeal, config.getInt(ConfPaths.MOVING_MOREPACKETS_EPSMAX));
        morePacketsEPSBuckets = 2 * Math.max(1, Math.min(60, config.getInt(ConfPaths.MOVING_MOREPACKETS_SECONDS)));
        morePacketsBurstPackets = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_EPM);
        morePacketsBurstDirect = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_DIRECT);
        morePacketsBurstEPM = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_EPM);
        morePacketsActions = config.getOptimizedActionList(ConfPaths.MOVING_MOREPACKETS_ACTIONS, Permissions.MOVING_MOREPACKETS);

        morePacketsVehicleCheck = config.getBoolean(ConfPaths.MOVING_MOREPACKETSVEHICLE_CHECK);
        morePacketsVehicleActions = config.getOptimizedActionList(ConfPaths.MOVING_MOREPACKETSVEHICLE_ACTIONS,
                Permissions.MOVING_MOREPACKETS);

        noFallCheck = config.getBoolean(ConfPaths.MOVING_NOFALL_CHECK);
        noFallDealDamage = config.getBoolean(ConfPaths.MOVING_NOFALL_DEALDAMAGE);
        noFallViolationReset = config.getBoolean(ConfPaths.MOVING_NOFALL_RESETONVL);
        noFallTpReset = config.getBoolean(ConfPaths.MOVING_NOFALL_RESETONTP);
        noFallVehicleReset = config.getBoolean(ConfPaths.MOVING_NOFALL_RESETONVEHICLE);
        noFallAntiCriticals = config.getBoolean(ConfPaths.MOVING_NOFALL_ANTICRITICALS);
        noFallActions = config.getOptimizedActionList(ConfPaths.MOVING_NOFALL_ACTIONS, Permissions.MOVING_NOFALL);

        passableCheck = config.getBoolean(ConfPaths.MOVING_PASSABLE_CHECK);
        passableRayTracingCheck = config.getBoolean(ConfPaths.MOVING_PASSABLE_RAYTRACING_CHECK);
        passableRayTracingBlockChangeOnly = config.getBoolean(ConfPaths.MOVING_PASSABLE_RAYTRACING_BLOCKCHANGEONLY);
        passableActions = config.getOptimizedActionList(ConfPaths.MOVING_PASSABLE_ACTIONS, Permissions.MOVING_PASSABLE);
        passableUntrackedTeleportCheck = config.getBoolean(ConfPaths.MOVING_PASSABLE_UNTRACKED_TELEPORT_ACTIVE);
        passableUntrackedCommandCheck = config.getBoolean(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_ACTIVE);
        passableUntrackedCommandTryTeleport = config.getBoolean(ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_TRYTELEPORT);
        CommandUtil.feedCommands(passableUntrackedCommandPrefixes, config, ConfPaths.MOVING_PASSABLE_UNTRACKED_CMD_PREFIXES, true);

        survivalFlyCheck = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_CHECK);
        // Default values are specified here because this settings aren't showed by default into the configuration file.
        survivalFlyBlockingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_BLOCKINGSPEED, 100);
        survivalFlySneakingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_SNEAKINGSPEED, 100);
        survivalFlySpeedingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_SPEEDINGSPEED, 200);
        survivalFlySprintingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_SPRINTINGSPEED, 100);
        survivalFlySwimmingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_SWIMMINGSPEED, 100);
        survivalFlyWalkingSpeed = config.getInt(ConfPaths.MOVING_SURVIVALFLY_WALKINGSPEED, 100);
        survivalFlyCobwebHack = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_COBWEBHACK, true);
        survivalFlyAccountingH = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_HACC, false);
        survivalFlyAccountingV = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_EXTENDED_VACC);
        sfSetBackPolicyFallDamage = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_SETBACKPOLICY_FALLDAMAGE);
        sfSetBackPolicyVoid = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_SETBACKPOLICY_VOIDTOVOID);
        final double sfStepHeight = config.getDouble(ConfPaths.MOVING_SURVIVALFLY_STEPHEIGHT, Double.MAX_VALUE);
        if (sfStepHeight == Double.MAX_VALUE) {
            final String ref;
            if (Bukkit.getVersion().toLowerCase().indexOf("spigot") != -1) {
                // Assume 1.8 clients being supported.
                ref = "1.7.10";
            } else {
                ref = "1.8";
            }
            this.sfStepHeight = ServerVersion.select(ref, 0.5, 0.6, 0.6, 0.5).doubleValue();
        } else {
            this.sfStepHeight = sfStepHeight;
        }
        survivalFlyVLFreeze = config.getLong(ConfPaths.MOVING_SURVIVALFLY_VLFREEZE, 2000L);
        survivalFlyActions = config.getOptimizedActionList(ConfPaths.MOVING_SURVIVALFLY_ACTIONS, Permissions.MOVING_SURVIVALFLY);

        sfHoverCheck = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_HOVER_CHECK);
        sfHoverTicks = config.getInt(ConfPaths.MOVING_SURVIVALFLY_HOVER_TICKS);
        sfHoverLoginTicks = Math.max(0, config.getInt(ConfPaths.MOVING_SURVIVALFLY_HOVER_LOGINTICKS));
        sfHoverFallDamage = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_HOVER_FALLDAMAGE);
        sfHoverViolation = config.getDouble(ConfPaths.MOVING_SURVIVALFLY_HOVER_SFVIOLATION);

        velocityGraceTicks = config.getInt(ConfPaths.MOVING_VELOCITY_GRACETICKS);
        velocityActivationCounter = config.getInt(ConfPaths.MOVING_VELOCITY_ACTIVATIONCOUNTER);
        velocityActivationTicks = config.getInt(ConfPaths.MOVING_VELOCITY_ACTIVATIONTICKS);
        velocityStrictInvalidation = config.getBoolean(ConfPaths.MOVING_VELOCITY_STRICTINVALIDATION);
        yOnGround = config.getDouble(ConfPaths.MOVING_YONGROUND, 0.001, 2.0, 0.0626); // sqrt(1/256), see: NetServerHandler.
        noFallyOnGround = config.getDouble(ConfPaths.MOVING_NOFALL_YONGROUND, 0.001, 2.0, yOnGround);

        // TODO: Ignore the stance, once it is known that the server catches such.
        ignoreStance = config.getAlmostBoolean(ConfPaths.MOVING_IGNORESTANCE, AlmostBoolean.NO).decide();
        tempKickIllegal = config.getBoolean(ConfPaths.MOVING_TEMPKICKILLEGAL);
        loadChunksOnJoin = config.getBoolean(ConfPaths.MOVING_LOADCHUNKS_JOIN);
        sprintingGrace = Math.max(0L, (long) (config.getDouble(ConfPaths.MOVING_SPRINTINGGRACE) * 1000.0)); // Config: seconds.
        assumeSprint = config.getBoolean(ConfPaths.MOVING_ASSUMESPRINT);
        speedGrace = Math.max(0, (int) Math.round(config.getDouble(ConfPaths.MOVING_SPEEDGRACE) * 20.0)); // Config: seconds
        AlmostBoolean ref = config.getAlmostBoolean(ConfPaths.MOVING_ENFORCELOCATION, AlmostBoolean.MAYBE);
        if (ref == AlmostBoolean.MAYBE) {
            enforceLocation = Bugs.shouldEnforceLocation();
        } else {
            enforceLocation = ref.decide();
        }

        ref = config.getAlmostBoolean(ConfPaths.MOVING_VEHICLES_ENFORCELOCATION, AlmostBoolean.MAYBE);
        vehicleEnforceLocation = ref.decideOptimistically(); // Currently rather enabled.
        vehiclePreventDestroyOwn = config.getBoolean(ConfPaths.MOVING_VEHICLES_PREVENTDESTROYOWN);

        traceSize = config.getInt(ConfPaths.MOVING_TRACE_SIZE);
        traceMergeDist = config.getDouble(ConfPaths.MOVING_TRACE_MERGEDIST);

    }


    /* (non-Javadoc)
     * @see fr.neatmonster.nocheatplus.checks.ICheckConfig#isEnabled(fr.neatmonster.nocheatplus.checks.CheckType)
     */
    @Override
    public final boolean isEnabled(final CheckType checkType) {
        switch (checkType) {
            case MOVING_NOFALL:
                return noFallCheck;
            case MOVING_SURVIVALFLY:
                return survivalFlyCheck;
            case MOVING_PASSABLE:
                return passableCheck;
            case MOVING_MOREPACKETS:
                return morePacketsCheck;
            case MOVING_MOREPACKETSVEHICLE:
                return morePacketsVehicleCheck;
            case MOVING_CREATIVEFLY:
                return creativeFlyCheck;
            default:
                return true;
        }
    }
}
