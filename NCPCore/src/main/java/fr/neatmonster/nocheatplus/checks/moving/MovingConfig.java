/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.neatmonster.nocheatplus.checks.moving;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import fr.neatmonster.nocheatplus.actions.ActionList;
import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.checks.access.ACheckConfig;
import fr.neatmonster.nocheatplus.checks.access.CheckConfigFactory;
import fr.neatmonster.nocheatplus.checks.access.ICheckConfig;
import fr.neatmonster.nocheatplus.checks.moving.magic.Magic;
import fr.neatmonster.nocheatplus.checks.moving.model.ModelFlying;
import fr.neatmonster.nocheatplus.checks.moving.player.PlayerSetBackMethod;
import fr.neatmonster.nocheatplus.command.CommandUtil;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.versions.Bugs;
import fr.neatmonster.nocheatplus.compat.versions.ServerVersion;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.permissions.Permissions;
import fr.neatmonster.nocheatplus.utilities.ColorUtil;
import fr.neatmonster.nocheatplus.utilities.ds.prefixtree.SimpleCharPrefixTree;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;

/**
 * Configurations specific for the moving checks. Every world gets one of these
 * assigned to it.
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
     * Gets the configuration for a specified player . <br>
     * NOTE: Currently only per-world configs are implemented. This method might
     * or might not get removed some day.
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
     * 
     * @param worldName
     *            Exact case world name.
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
    private final Map<GameMode, ModelFlying> flyingModelGameMode = new HashMap<GameMode, ModelFlying>();
    private final ModelFlying flyingModelElytra;
    private final ModelFlying flyingModelLevitation;
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

    public final boolean    noFallCheck;
    /**
     * Deal damage instead of Minecraft, whenever a player is judged to be on
     * ground.
     */
    public final boolean    noFallDealDamage;
    public final boolean    noFallSkipAllowFlight;
    /**
     * Reset data on violation, i.e. a player taking fall damage without being
     * on ground.
     */
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
    public final boolean    sfSlownessSprintHack;
    /**
     * If true, will allow moderate bunny hop without lift off. Applies for
     * normal speed on 1.6.4 and probably below.
     */
    public final boolean    sfGroundHop;
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
    /** Horizontal buffer (rather sf), after failure leniency. */
    public final double hBufMax;
    /**
     * Number of moving packets until which a velocity entry must be activated,
     * in order to not be removed.
     */
    public final int		velocityActivationCounter;
    /** Server ticks until invalidating queues velocity. */
    public final int		velocityActivationTicks;
    public final boolean	velocityStrictInvalidation;
    public final double     noFallyOnGround;
    public final double     yOnGround;

    // General things.
    /**
     * If to allow splitting moves, due to player.getLocation reflecting
     * something else than from/to.
     */
    public final boolean splitMoves;
    public final boolean ignoreStance;
    public final boolean tempKickIllegal;
    public final boolean loadChunksOnJoin;
    public final boolean loadChunksOnMove;
    public final boolean loadChunksOnTeleport;
    public final boolean loadChunksOnWorldChange;
    public final long sprintingGrace;
    public final boolean assumeSprint;
    public final int speedGrace;
    public final boolean enforceLocation;
    public final boolean trackBlockMove;
    public final PlayerSetBackMethod playerSetBackMethod;

    // Vehicles
    public final boolean vehicleEnforceLocation;
    public final boolean vehiclePreventDestroyOwn;
    public final boolean scheduleVehicleSetBacks;

    public final Set<EntityType> ignoredVehicles = new HashSet<EntityType>();

    public final boolean    vehicleMorePacketsCheck;
    public final ActionList vehicleMorePacketsActions;

    public final boolean vehicleEnvelopeActive;
    public final HashMap<EntityType, Double> vehicleEnvelopeHorizontalSpeedCap = new HashMap<EntityType, Double>();
    public final ActionList vehicleEnvelopeActions;

    // Trace
    public final int traceMaxAge;
    public final int traceMaxSize;

    // Messages.
    public final String msgKickIllegalMove;
    public final String msgKickIllegalVehicleMove;

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
        final ModelFlying defaultModel = new ModelFlying("gamemode.creative", config, ConfPaths.MOVING_CREATIVEFLY_MODEL + "creative.", new ModelFlying());
        for (final GameMode gameMode : GameMode.values()) {
            flyingModelGameMode.put(gameMode, new ModelFlying("gamemode." + gameMode.name().toLowerCase(), config, ConfPaths.MOVING_CREATIVEFLY_MODEL + (gameMode.name().toLowerCase()) + ".", defaultModel));
        }
        flyingModelLevitation = new ModelFlying("potion.levitation", config, ConfPaths.MOVING_CREATIVEFLY_MODEL + "levitation.", defaultModel);
        flyingModelLevitation.setScaleLevitationEffect(true); // Pirate.
        flyingModelElytra = new ModelFlying("jetpack.elytra", config, ConfPaths.MOVING_CREATIVEFLY_MODEL + "elytra.", defaultModel);
        creativeFlyActions = config.getOptimizedActionList(ConfPaths.MOVING_CREATIVEFLY_ACTIONS, Permissions.MOVING_CREATIVEFLY);

        morePacketsCheck = config.getBoolean(ConfPaths.MOVING_MOREPACKETS_CHECK);
        morePacketsEPSIdeal = config.getInt(ConfPaths.MOVING_MOREPACKETS_EPSIDEAL);
        morePacketsEPSMax = Math.max(morePacketsEPSIdeal, config.getInt(ConfPaths.MOVING_MOREPACKETS_EPSMAX));
        morePacketsEPSBuckets = 2 * Math.max(1, Math.min(60, config.getInt(ConfPaths.MOVING_MOREPACKETS_SECONDS)));
        morePacketsBurstPackets = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_EPM);
        morePacketsBurstDirect = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_DIRECT);
        morePacketsBurstEPM = config.getInt(ConfPaths.MOVING_MOREPACKETS_BURST_EPM);
        morePacketsActions = config.getOptimizedActionList(ConfPaths.MOVING_MOREPACKETS_ACTIONS, Permissions.MOVING_MOREPACKETS);

        noFallCheck = config.getBoolean(ConfPaths.MOVING_NOFALL_CHECK);
        noFallDealDamage = config.getBoolean(ConfPaths.MOVING_NOFALL_DEALDAMAGE);
        noFallSkipAllowFlight = config.getBoolean(ConfPaths.MOVING_NOFALL_SKIPALLOWFLIGHT);
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
        sfSlownessSprintHack = config.getAlmostBoolean(ConfPaths.MOVING_SURVIVALFLY_SLOWNESSSPRINTHACK, AlmostBoolean.MAYBE).decideOptimistically();
        sfGroundHop = config.getBoolean(ConfPaths.MOVING_SURVIVALFLY_GROUNDHOP, ServerVersion.compareMinecraftVersion("1.7") == -1);
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

        hBufMax = config.getDouble(ConfPaths.MOVING_SURVIVALFLY_HBUFMAX);
        velocityActivationCounter = config.getInt(ConfPaths.MOVING_VELOCITY_ACTIVATIONCOUNTER);
        velocityActivationTicks = config.getInt(ConfPaths.MOVING_VELOCITY_ACTIVATIONTICKS);
        velocityStrictInvalidation = config.getBoolean(ConfPaths.MOVING_VELOCITY_STRICTINVALIDATION);
        yOnGround = config.getDouble(ConfPaths.MOVING_YONGROUND, Magic.Y_ON_GROUND_MIN, Magic.Y_ON_GROUND_MAX, Magic.Y_ON_GROUND_DEFAULT); // sqrt(1/256), see: NetServerHandler.
        noFallyOnGround = config.getDouble(ConfPaths.MOVING_NOFALL_YONGROUND, Magic.Y_ON_GROUND_MIN, Magic.Y_ON_GROUND_MAX, yOnGround);

        AlmostBoolean refSplitMoves = config.getAlmostBoolean(ConfPaths.MOVING_SPLITMOVES, AlmostBoolean.MAYBE);
        //splitMoves = refSplitMoves == AlmostBoolean.MAYBE ? ServerVersion.compareMinecraftVersion("1.9") == -1 : refSplitMoves.decide();
        splitMoves = refSplitMoves.decideOptimistically();
        // TODO: Ignore the stance, once it is known that the server catches such.
        AlmostBoolean refIgnoreStance = config.getAlmostBoolean(ConfPaths.MOVING_IGNORESTANCE, AlmostBoolean.MAYBE);
        ignoreStance = refIgnoreStance == AlmostBoolean.MAYBE ? ServerVersion.compareMinecraftVersion("1.8") >= 0 : refIgnoreStance.decide();
        tempKickIllegal = config.getBoolean(ConfPaths.MOVING_TEMPKICKILLEGAL);
        loadChunksOnJoin = config.getBoolean(ConfPaths.MOVING_LOADCHUNKS_JOIN);
        loadChunksOnMove = config.getBoolean(ConfPaths.MOVING_LOADCHUNKS_MOVE);
        loadChunksOnTeleport = config.getBoolean(ConfPaths.MOVING_LOADCHUNKS_TELEPORT);
        loadChunksOnWorldChange = config.getBoolean(ConfPaths.MOVING_LOADCHUNKS_WORLDCHANGE);
        sprintingGrace = Math.max(0L, (long) (config.getDouble(ConfPaths.MOVING_SPRINTINGGRACE) * 1000.0)); // Config: seconds.
        assumeSprint = config.getBoolean(ConfPaths.MOVING_ASSUMESPRINT);
        speedGrace = Math.max(0, (int) Math.round(config.getDouble(ConfPaths.MOVING_SPEEDGRACE) * 20.0)); // Config: seconds
        AlmostBoolean ref = config.getAlmostBoolean(ConfPaths.MOVING_ENFORCELOCATION, AlmostBoolean.MAYBE);
        if (ref == AlmostBoolean.MAYBE) {
            enforceLocation = Bugs.shouldEnforceLocation();
        } else {
            enforceLocation = ref.decide();
        }
        // TODO: Rename overall flag to trackBlockChanges. Create a sub-config rather.
        trackBlockMove = config.getBoolean(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_ACTIVE) 
                && (config.getBoolean(ConfPaths.COMPATIBILITY_BLOCKS_CHANGETRACKER_PISTONS
                        // TODO: || other activation flags.
                        ));
        final PlayerSetBackMethod playerSetBackMethod = PlayerSetBackMethod.fromString(
                "extern.fromconfig", config.getString(ConfPaths.MOVING_SETBACK_METHOD));
        if (playerSetBackMethod.doesThisMakeSense()) {
            // (Might info/warn if legacy is used without setTo and without SCHEDULE and similar?)
            this.playerSetBackMethod = playerSetBackMethod;
        }
        else if (ServerVersion.compareMinecraftVersion("1.9") < 0) {
            this.playerSetBackMethod = PlayerSetBackMethod.LEGACY;
        }
        else {
            // Latest.
            this.playerSetBackMethod = PlayerSetBackMethod.MODERN;
        }

        traceMaxAge = config.getInt(ConfPaths.MOVING_TRACE_MAXAGE, 200);
        traceMaxSize = config.getInt(ConfPaths.MOVING_TRACE_MAXSIZE, 200);

        ref = config.getAlmostBoolean(ConfPaths.MOVING_VEHICLE_ENFORCELOCATION, AlmostBoolean.MAYBE);
        vehicleEnforceLocation = ref.decideOptimistically(); // Currently rather enabled.
        vehiclePreventDestroyOwn = config.getBoolean(ConfPaths.MOVING_VEHICLE_PREVENTDESTROYOWN);
        scheduleVehicleSetBacks = config.getAlmostBoolean(ConfPaths.MOVING_VEHICLE_SCHEDULESETBACKS, AlmostBoolean.MAYBE).decide();
        vehicleMorePacketsCheck = config.getBoolean(ConfPaths.MOVING_VEHICLE_MOREPACKETS_CHECK);
        vehicleMorePacketsActions = config.getOptimizedActionList(ConfPaths.MOVING_VEHICLE_MOREPACKETS_ACTIONS, Permissions.MOVING_MOREPACKETS);
        ref = config.getAlmostBoolean(ConfPaths.MOVING_VEHICLE_ENVELOPE_ACTIVE, AlmostBoolean.MAYBE);
        vehicleEnvelopeActive = ref == AlmostBoolean.MAYBE ? ServerVersion.compareMinecraftVersion("1.9") >= 0 : ref.decide();
        config.readDoubleValuesForEntityTypes(ConfPaths.MOVING_VEHICLE_ENVELOPE_HSPEEDCAP, vehicleEnvelopeHorizontalSpeedCap, 4.0, true);
        vehicleEnvelopeActions = config.getOptimizedActionList(ConfPaths.MOVING_VEHICLE_ENVELOPE_ACTIONS, Permissions.MOVING_VEHICLE_ENVELOPE);
        // Ignored vehicle types (ignore mostly, no checks run).
        List<String> types;
        if (config.get(ConfPaths.MOVING_VEHICLE_IGNOREDVEHICLES) == null) { // Hidden setting for now.
            // Use defaults.
            types = Arrays.asList("arrow", "spectral_arrow", "tipped_arrow");
        }
        else {
            types = config.getStringList(ConfPaths.MOVING_VEHICLE_IGNOREDVEHICLES);
        }
        for (String stype : types) {
            try {
                EntityType type = EntityType.valueOf(stype.toUpperCase());
                if (type != null) {
                    ignoredVehicles.add(type);
                }
            }
            catch (IllegalArgumentException e) {}
        }


        // Messages.
        msgKickIllegalMove = ColorUtil.replaceColors(config.getString(ConfPaths.MOVING_MESSAGE_ILLEGALPLAYERMOVE));
        msgKickIllegalVehicleMove = ColorUtil.replaceColors(config.getString(ConfPaths.MOVING_MESSAGE_ILLEGALVEHICLEMOVE));
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
            case MOVING_VEHICLE_MOREPACKETS:
                return vehicleMorePacketsCheck;
            case MOVING_VEHICLE_ENVELOPE:
                return vehicleEnvelopeActive;
            case MOVING_CREATIVEFLY:
                return creativeFlyCheck;
            default:
                return true;
        }
    }

    public ModelFlying getModelFlying(final Player player, final PlayerLocation fromLocation) {
        final GameMode gameMode = player.getGameMode();
        final ModelFlying modelGameMode = flyingModelGameMode.get(gameMode);
        switch(gameMode) {
            case SURVIVAL:
            case ADVENTURE:
                // Check for jetpack/potion first.
                break;
            default:
                return modelGameMode;
        }
        // TODO: Short touch of water/vines (/ground?) is possible - use past moves or in-medium-count.
        final boolean isUsingElytra = Bridge1_9.isWearingElytra(player);
        if (player.isFlying() 
                || !isUsingElytra && !ignoreAllowFlight && player.getAllowFlight()) {
            return modelGameMode;
        }
        // TODO: ORDER IS RANDOM GUESSING. Is mixtures possible?
        if (fromLocation.isInLiquid()) {
            // TODO: INCONSISTENT.
            return modelGameMode;
        }
        if (!Double.isInfinite(Bridge1_9.getLevitationAmplifier(player))) {
            return flyingModelLevitation;
        }
        if (fromLocation.isOnGroundOrResetCond()) {
            // TODO: INCONSISTENT.
            return modelGameMode;
        }
        if (isUsingElytra) { // Defensive: don't demand isGliding.
            return flyingModelElytra;
        }
        // Default by game mode.
        return modelGameMode;
    }

}
