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
package fr.neatmonster.nocheatplus.utilities.map;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.NCPAPIProvider;
import fr.neatmonster.nocheatplus.compat.Bridge1_9;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.vanilla.VanillaBlocksFactory;
import fr.neatmonster.nocheatplus.components.registry.event.IHandle;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.RawConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogManager;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.PotionUtil;
import fr.neatmonster.nocheatplus.utilities.StringUtil;
import fr.neatmonster.nocheatplus.utilities.collision.BlockPositionContainer;
import fr.neatmonster.nocheatplus.utilities.collision.ICollidePassable;
import fr.neatmonster.nocheatplus.utilities.collision.PassableAxisTracing;
import fr.neatmonster.nocheatplus.utilities.collision.PassableRayTracing;
import fr.neatmonster.nocheatplus.utilities.location.PlayerLocation;

// TODO: Auto-generated Javadoc
/**
 * Properties of blocks.
 * 
 * Likely to be added:
 * - reading (all) properties from files.
 * - reading (all) the default properties from a file too.
 *
 */
@SuppressWarnings("deprecation")
public class BlockProperties {

    /**
     * The Enum ToolType.
     *
     * @author mc_dev
     * @deprecated Will be replaced by a generic way to define tools.
     */
    public static enum ToolType{

        /** The none. */
        NONE,

        /** The sword. */
        SWORD,

        /** The shears. */
        SHEARS,

        /** The spade. */
        SPADE,

        /** The axe. */
        AXE,

        /** The pickaxe. */
        PICKAXE,
        //		HOE,
    }

    /**
     * The Enum MaterialBase.
     *
     * @author mc_dev
     * @deprecated Will be replaced by a generic way to define tools.
     */
    public static enum MaterialBase{

        /** The none. */
        NONE(0, 1f),

        /** The wood. */
        WOOD(1, 2f),

        /** The stone. */
        STONE(2, 4f),

        /** The iron. */
        IRON(3, 6f),

        /** The diamond. */
        DIAMOND(4, 8f),

        /** The gold. */
        GOLD(5, 12f);
        /** Index for array. */
        public final int index;

        /** The break multiplier. */
        public final float breakMultiplier;

        /**
         * Instantiates a new material base.
         *
         * @param index
         *            the index
         * @param breakMultiplier
         *            the break multiplier
         */
        private MaterialBase(int index, float breakMultiplier) {
            this.index = index;
            this.breakMultiplier = breakMultiplier;
        }

        /**
         * Gets the by id.
         *
         * @param id
         *            the id
         * @return the by id
         */
        public static final MaterialBase getById(final int id) {
            for (final MaterialBase base : MaterialBase.values()) {
                if (base.index == id) {
                    return base;
                }
            }
            throw new IllegalArgumentException("Bad id: " + id);
        }
    }

    /**
     * Properties of a tool.
     * 
     * @deprecated Will be replaced by a generic way to define tools.
     */
    public static class ToolProps{

        /** The tool type. */
        public final ToolType toolType;

        /** The material base. */
        public final MaterialBase materialBase;

        /**
         * Instantiates a new tool props.
         *
         * @param toolType
         *            the tool type
         * @param materialBase
         *            the material base
         */
        public ToolProps(ToolType toolType, MaterialBase materialBase) {
            this.toolType = toolType;
            this.materialBase = materialBase;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "ToolProps("+toolType + "/"+materialBase+")";
        }

        /**
         * Validate.
         */
        public void validate() {
            if (toolType == null) {
                throw new IllegalArgumentException("ToolType must not be null.");
            }
            if (materialBase == null) {
                throw new IllegalArgumentException("MaterialBase must not be null");
            }
        }
    }

    /**
     * Properties of a block.
     * 
     * @deprecated Will be replaced by a generic way to define tools.
     */
    public static class BlockProps{

        /** The tool. */
        public final ToolProps tool;

        /** The breaking times. */
        public final long[] breakingTimes;

        /** The hardness. */
        public final float hardness;
        /** Factor 2 = 2 times faster. */
        public final float efficiencyMod;

        /**
         * Instantiates a new block props.
         *
         * @param tool
         *            the tool
         * @param hardness
         *            the hardness
         */
        public BlockProps(ToolProps tool, float hardness) {
            this(tool, hardness, 1);
        }

        /**
         * Instantiates a new block props.
         *
         * @param tool
         *            the tool
         * @param hardness
         *            the hardness
         * @param efficiencyMod
         *            the efficiency mod
         */
        public BlockProps(ToolProps tool, float hardness, float efficiencyMod) {
            this.tool = tool;
            this.hardness = hardness;
            breakingTimes = new long[6];
            for (int i = 0; i < 6; i++) {
                final float multiplier;
                if (tool.materialBase == null) {
                    multiplier = 1f;
                }
                else if (i < tool.materialBase.index) {
                    multiplier = 1f;
                }
                else {
                    multiplier = MaterialBase.getById(i).breakMultiplier * 3.33f;
                }
                breakingTimes[i] = (long) (1000f * 5f * hardness / multiplier);
            }
            this.efficiencyMod = efficiencyMod;
        }

        /**
         * Instantiates a new block props.
         *
         * @param tool
         *            the tool
         * @param hardness
         *            the hardness
         * @param breakingTimes
         *            the breaking times
         */
        public BlockProps(ToolProps tool, float hardness, long[] breakingTimes) {
            this(tool, hardness, breakingTimes, 1f);
        }

        /**
         * Instantiates a new block props.
         *
         * @param tool
         *            the tool
         * @param hardness
         *            the hardness
         * @param breakingTimes
         *            the breaking times
         * @param efficiencyMod
         *            the efficiency mod
         */
        public BlockProps(ToolProps tool, float hardness, long[] breakingTimes, float efficiencyMod) {
            this.tool = tool;
            this.breakingTimes = breakingTimes;
            this.hardness = hardness;
            this.efficiencyMod = efficiencyMod;
        }

        /* (non-Javadoc)
         * @see java.lang.Object#toString()
         */
        public String toString() {
            return "BlockProps(" + hardness + " / " + tool.toString() + " / " + Arrays.toString(breakingTimes) + ")";
        }

        /**
         * Validate.
         */
        public void validate() {
            if (breakingTimes == null) {
                throw new IllegalArgumentException("Breaking times must not be null.");
            }
            if (breakingTimes.length != 6) {
                throw new IllegalArgumentException("Breaking times length must match the number of available tool types (6).");
            }
            if (tool == null)  {
                throw new IllegalArgumentException("Tool must not be null.");
            }
            tool.validate();
        }
    }

    /** Liquid height if no solid/full blocks are above. */
    protected static final double LIQUID_HEIGHT_LOWERED = 80000002;

    /** The Constant maxBlocks. */
    protected static final int maxBlocks = 4096; 

    /** Properties by block id, might be extended to 4096 later for custom blocks.*/
    protected static final BlockProps[] blocks = new BlockProps[maxBlocks];

    /** Map for the tool properties. */
    protected static Map<Integer, ToolProps> tools = new LinkedHashMap<Integer, ToolProps>(50, 0.5f);

    /** Breaking time for indestructible materials. */
    public static final long indestructible = Long.MAX_VALUE;

    /** Default tool properties (inappropriate tool). */
    public static final ToolProps noTool = new ToolProps(ToolType.NONE, MaterialBase.NONE);

    /** The Constant woodSword. */
    public static final ToolProps woodSword = new ToolProps(ToolType.SWORD, MaterialBase.WOOD);

    /** The Constant woodSpade. */
    public static final ToolProps woodSpade = new ToolProps(ToolType.SPADE, MaterialBase.WOOD);

    /** The Constant woodPickaxe. */
    public static final ToolProps woodPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.WOOD);

    /** The Constant woodAxe. */
    public static final ToolProps woodAxe = new ToolProps(ToolType.AXE, MaterialBase.WOOD);

    /** The Constant stonePickaxe. */
    public static final ToolProps stonePickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.STONE);

    /** The Constant ironPickaxe. */
    public static final ToolProps ironPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.IRON);

    /** The Constant diamondPickaxe. */
    public static final ToolProps diamondPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.DIAMOND);

    /** Times for instant breaking. */
    public static final long[] instantTimes = secToMs(0);

    /** The Constant leafTimes. */
    public static final long[] leafTimes = secToMs(0.3);

    /** The glass times. */
    public static long[] glassTimes = secToMs(0.45);

    /** The Constant gravelTimes. */
    public static final long[] gravelTimes = secToMs(0.9, 0.45, 0.25, 0.15, 0.15, 0.1);

    /** The rails times. */
    public static long[] railsTimes = secToMs(1.05, 0.55, 0.3, 0.2, 0.15, 0.1);

    /** The Constant woodTimes. */
    public static final long[] woodTimes = secToMs(3, 1.5, 0.75, 0.5, 0.4, 0.25);

    /** The Constant indestructibleTimes. */
    private static final long[] indestructibleTimes = new long[] {indestructible, indestructible, indestructible, indestructible, indestructible, indestructible}; 

    /** Instantly breakable. */ 
    public static final BlockProps instantType = new BlockProps(noTool, 0, instantTimes);

    /** The Constant glassType. */
    public static final BlockProps glassType = new BlockProps(noTool, 0.3f, glassTimes, 2f);

    /** The Constant gravelType. */
    public static final BlockProps gravelType = new BlockProps(woodSpade, 0.6f, gravelTimes);
    /** Stone type blocks. */
    public static final BlockProps stoneType = new BlockProps(woodPickaxe, 1.5f);

    /** The Constant woodType. */
    public static final BlockProps woodType = new BlockProps(woodAxe, 2, woodTimes);

    /** The Constant brickType. */
    public static final BlockProps brickType = new BlockProps(woodPickaxe, 2);

    /** The Constant coalType. */
    public static final BlockProps coalType = new BlockProps(woodPickaxe, 3);

    /** The Constant goldBlockType. */
    public static final BlockProps goldBlockType = new BlockProps(woodPickaxe, 3, secToMs(15, 7.5, 3.75, 0.7, 0.55, 1.2));

    /** The Constant ironBlockType. */
    public static final BlockProps ironBlockType = new BlockProps(woodPickaxe, 5, secToMs(25, 12.5, 2.0, 1.25, 0.95, 2.0));

    /** The Constant diamondBlockType. */
    public static final BlockProps diamondBlockType = new BlockProps(woodPickaxe, 5, secToMs(25, 12.5, 6.0, 1.25, 0.95, 2.0));

    /** The Constant hugeMushroomType. */
    public static final BlockProps hugeMushroomType = new BlockProps(woodAxe, 0.2f, secToMs(0.3, 0.15, 0.1, 0.05, 0.05, 0.05));

    /** The Constant leafType. */
    public static final BlockProps leafType = new BlockProps(noTool, 0.2f, leafTimes);

    /** The Constant sandType. */
    public static final BlockProps sandType = new BlockProps(woodSpade, 0.5f, secToMs(0.75, 0.4, 0.2, 0.15, 0.1, 0.1));

    /** The Constant leverType. */
    public static final BlockProps leverType = new BlockProps(noTool, 0.5f, secToMs(0.75));

    /** The Constant sandStoneType. */
    public static final BlockProps sandStoneType = new BlockProps(woodPickaxe, 0.8f);

    /** The Constant chestType. */
    public static final BlockProps chestType = new BlockProps(woodAxe, 2.5f, secToMs(3.75, 1.9, 0.95, 0.65, 0.5, 0.35));

    /** The Constant woodDoorType. */
    public static final BlockProps woodDoorType = new BlockProps(woodAxe, 3.0f, secToMs(4.5, 2.25, 1.15, 0.75, 0.6, 0.4));

    /** The Constant dispenserType. */
    public static final BlockProps dispenserType = new BlockProps(woodPickaxe, 3.5f);

    /** The Constant ironDoorType. */
    public static final BlockProps ironDoorType = new BlockProps(woodPickaxe, 5);

    /** The Constant indestructibleType. */
    public static final BlockProps indestructibleType = new BlockProps(noTool, -1f, indestructibleTimes);

    /** Returned if unknown. */
    private static BlockProps defaultBlockProps = instantType;

    /** The Constant instantMat. */
    protected static final Material[] instantMat = new Material[]{
            // Named in wiki.
            Material.CROPS,
            Material.TRIPWIRE_HOOK, Material.TRIPWIRE,
            Material.TORCH,
            Material.TNT,
            Material.SUGAR_CANE_BLOCK,
            Material.SAPLING,
            Material.RED_ROSE, Material.YELLOW_FLOWER,
            Material.REDSTONE_WIRE, 
            Material.REDSTONE_TORCH_ON, Material.REDSTONE_TORCH_OFF,
            Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF,
            Material.PUMPKIN_STEM,
            Material.NETHER_WARTS,
            Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.MELON_STEM,
            Material.WATER_LILY,
            Material.LONG_GRASS,
            Material.FIRE,
            Material.DEAD_BUSH,
            //
            Material.CROPS,

            // 1.4
            Material.COMMAND,
            Material.FLOWER_POT,
            Material.CARROT,
            Material.POTATO,
    };

    /** The rt ray. */
    private static ICollidePassable rtRay = null;

    /** The rt axis. */
    private static ICollidePassable rtAxis = null;

    /** The block cache. */
    private static WrapBlockCache wrapBlockCache = null; 

    /** The p loc. */
    private static PlayerLocation pLoc = null;

    /** The Constant blockFlags. */
    protected static final long[] blockFlags = new long[maxBlocks];

    /** Flag position for stairs. */
    public static final long F_STAIRS               = 0x1;

    /** The Constant F_LIQUID. */
    public static final long F_LIQUID               = 0x2;
    // TODO: maybe remove F_SOLID use (unless for setting F_GROUND on init).
    /** Minecraft isSolid result. Used for setting ground flag - Subject to change / rename.*/
    public static final long F_SOLID                = 0x4;
    /** Compatibility flag: regard this block as passable always. */
    public static final long F_IGN_PASSABLE         = 0x8;

    /** The Constant F_WATER. */
    public static final long F_WATER                = 0x10;

    /** The Constant F_LAVA. */
    public static final long F_LAVA                 = 0x20;
    /** Override bounding box: 1.5 blocks high, like fences.<br>
     *  NOTE: This might have relevance for passable later.
     */
    public static final long F_HEIGHT150             = 0x40;
    /** The player can stand on these, sneaking or not. */
    public static final long F_GROUND               = 0x80; // TODO: 
    /** Override bounding box: 1 block height.<br>
     * NOTE: This should later be ignored by passable, rather.
     */
    public static final long F_HEIGHT100            = 0x100;
    /** Climbable like ladder and vine (allow to land on without taking damage). */
    public static final long F_CLIMBABLE            = 0x200;
    /** The block can change shape. This is most likely not 100% accurate... */
    public static final long F_VARIABLE             = 0x400;
    //    /** The block has full bounds (0..1), inaccurate! */
    //    public static final int F_FULL   		= 0x800;
    /** Block has full xz-bounds. */
    public static final long F_XZ100                = 0x800;

    /** This flag indicates that even though a passable workaround, everything above passable height is still ground. */
    public static final long F_GROUND_HEIGHT        = 0x1000;

    /** 
     * The height is assumed to decrease from 1.0 with increasing data value from 0 to 0x7, with 0x7 being the lowest.
     * (repeating till 0x15)). 0x8 means falling/full block. This is meant to model flowing water/lava. <br>
     * However the hit-box for collision checks  will be set to 0.5 height or 1.0 height only.
     */
    public static final long F_HEIGHT_8SIM_DEC      = 0x2000;

    /**
     * The height is assumed to increase with data value up to 0x7, repeating up to 0x15.<br>
     * However the hit-box for collision checks  will be set to 0.5 height or 1.0 height only,<br>
     * as with the 1.4.x snow levels.
     */
    public static final long F_HEIGHT_8SIM_INC      = 0x4000;


    /**
     * The height increases with data value (8 heights).<br>
     * This is for MC 1.5 snow levels.
     */
    public static final long F_HEIGHT_8_INC         = 0x8000;

    /** All rail types a minecart can move on. */
    public static final long F_RAILS                = 0x10000;

    /** ICE. */
    public static final long F_ICE                  = 0x20000;

    /** LEAVES. */
    public static final long F_LEAVES               = 0x40000;

    /** THIN FENCE (glass panes, iron fence). */
    public static final long F_THIN_FENCE           = 0x80000;

    /** Meta-flag to indicate that the (max.-) edges should mean a collision, can be passed to collidesBlock. */
    public static final long F_COLLIDE_EDGES        = 0x100000;

    /** Thick fence (default wooden fence). */
    public static final long F_THICK_FENCE          = 0x200000;

    /** Fence gate style with 0x04 being fully passable. */
    public static final long F_PASSABLE_X4          = 0x400000;

    // TODO: Separate no fall damage flag ? [-> on ground could return "dominating" flags, or extra flags]
    /** Like slime block: bounce back 25% of fall height without taking fall damage [TODO: Check/adjust]. */
    public static final long F_BOUNCE25             = 0x800000;

    /**
     * The facing direction is described by the lower 3 data bits in order of
     * NSWE, starting at and defaulting to 2, which includes invalid states.
     * Main purpose is ladders, no guarantees on defaults for other blocks yet.
     */
    public static final long F_FACING_LOW3D2_NSWE     = 0x1000000;

    /**
     * The direction the block is attached to is described by the lower 2 bits
     * in order of SNEW.
     */
    public static final long F_ATTACHED_LOW2_SNEW       = 0x2000000;

    /**
     * The hacky way to force sfNoLowJump when the block at from has this flag.
     */
    public static final long F_ALLOW_LOWJUMP            = 0x4000000;

    /** One eighth block height (0.125). */
    public static final long F_HEIGHT8_1            = 0x8000000;

    // TODO: When flags are out, switch to per-block classes :p.

    // Special case activation flags.
    /** Trap door is climbable with ladder underneath, both facing distinct. */
    private static boolean specialCaseTrapDoorAboveLadder = false;

    /**
     * Map flag to names.
     */
    private static final Map<Long, String> flagNameMap = new LinkedHashMap<Long, String>();
    /**
     * Map flag name to flag, both names starting with F_... and the name without F_.
     */
    private static final Map<String, Long> nameFlagMap = new LinkedHashMap<String, Long>();

    /** The Constant useLoc. */
    private static final Location useLoc = new Location(null, 0, 0, 0);

    static{
        // Use reflection to get a flag -> name mapping and vice versa.
        for (Field field : BlockProperties.class.getDeclaredFields()) {
            String name = field.getName();
            if (name.startsWith("F_")) {
                try {
                    Long value = field.getLong(BlockProperties.class);
                    flagNameMap.put(value, name.substring(2));
                    nameFlagMap.put(name, value);
                    nameFlagMap.put(name.substring(2), value);
                } catch (IllegalArgumentException e) {
                } catch (IllegalAccessException e) {
                }
            }
        }
    }

    /** Penalty factor for block break duration if under water. */
    protected static float breakPenaltyInWater = 4f;
    /** Penalty factor for block break duration if not on ground. */
    protected static float breakPenaltyOffGround = 4f;

    /**
     * Initialize blocks and tools properties. This can be called at any time
     * during runtime.
     *
     * @param mcAccess
     *            If mcAccess implements BlockPropertiesSetup,
     *            mcAccess.setupBlockProperties will be called directly after
     *            basic initialization but before the configuration is applied.
     * @param worldConfigProvider
     *            the world config provider
     */
    public static void init(final IHandle<MCAccess> mcAccess, final WorldConfigProvider<?> worldConfigProvider) {
        wrapBlockCache = new WrapBlockCache();
        rtRay = new PassableRayTracing();
        rtAxis = new PassableAxisTracing();
        pLoc = new PlayerLocation(mcAccess, null);
        final Set<String> blocksFeatures = new LinkedHashSet<String>(); // getClass().getName() or some abstract.
        try{
            initTools(mcAccess, worldConfigProvider);
            initBlocks(mcAccess, worldConfigProvider);
            blocksFeatures.add("BlocksMC1_4");
            // Extra hand picked setups.
            try{
                blocksFeatures.addAll(new VanillaBlocksFactory().setupVanillaBlocks(worldConfigProvider));
            }
            catch(Throwable t) {
                StaticLog.logSevere("Could not initialize vanilla blocks: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                StaticLog.logSevere(t);
            }
            // Allow mcAccess to setup block properties.
            if (mcAccess instanceof BlockPropertiesSetup) {
                try{
                    ((BlockPropertiesSetup) mcAccess).setupBlockProperties(worldConfigProvider);
                    blocksFeatures.add(mcAccess.getClass().getSimpleName());
                }
                catch(Throwable t) {
                    StaticLog.logSevere("McAccess.setupBlockProperties (" + mcAccess.getClass().getSimpleName() + ") could not execute properly: " + t.getClass().getSimpleName() + " - " + t.getMessage());
                    StaticLog.logSevere(t);
                }
            }
            // TODO: Add registry for further BlockPropertiesSetup instances.
        }
        catch(Throwable t) {
            StaticLog.logSevere(t);
        }
        // Override feature tags for blocks.
        NCPAPIProvider.getNoCheatPlusAPI().setFeatureTags("blocks", blocksFeatures);
    }

    /**
     * Inits the tools.
     *
     * @param mcAccess
     *            the mc access
     * @param worldConfigProvider
     *            the world config provider
     */
    private static void initTools(final IHandle<MCAccess> mcAccess, final WorldConfigProvider<?> worldConfigProvider) {
        tools.clear();
        tools.put(268, new ToolProps(ToolType.SWORD, MaterialBase.WOOD));
        tools.put(269, new ToolProps(ToolType.SPADE, MaterialBase.WOOD));
        tools.put(270, new ToolProps(ToolType.PICKAXE, MaterialBase.WOOD));
        tools.put(271, new ToolProps(ToolType.AXE, MaterialBase.WOOD));

        tools.put(272, new ToolProps(ToolType.SWORD, MaterialBase.STONE));
        tools.put(273, new ToolProps(ToolType.SPADE, MaterialBase.STONE));
        tools.put(274, new ToolProps(ToolType.PICKAXE, MaterialBase.STONE));
        tools.put(275, new ToolProps(ToolType.AXE, MaterialBase.STONE));

        tools.put(256, new ToolProps(ToolType.SPADE, MaterialBase.IRON));
        tools.put(257, new ToolProps(ToolType.PICKAXE, MaterialBase.IRON));
        tools.put(258, new ToolProps(ToolType.AXE, MaterialBase.IRON));
        tools.put(267, new ToolProps(ToolType.SWORD, MaterialBase.IRON));

        tools.put(276, new ToolProps(ToolType.SWORD, MaterialBase.DIAMOND));
        tools.put(277, new ToolProps(ToolType.SPADE, MaterialBase.DIAMOND));
        tools.put(278, new ToolProps(ToolType.PICKAXE, MaterialBase.DIAMOND));
        tools.put(279, new ToolProps(ToolType.AXE, MaterialBase.DIAMOND));

        tools.put(283, new ToolProps(ToolType.SWORD, MaterialBase.GOLD));
        tools.put(284, new ToolProps(ToolType.SPADE, MaterialBase.GOLD));
        tools.put(285, new ToolProps(ToolType.PICKAXE, MaterialBase.GOLD));
        tools.put(286, new ToolProps(ToolType.AXE, MaterialBase.GOLD));

        tools.put(359, new ToolProps(ToolType.SHEARS, MaterialBase.NONE));
    }

    /**
     * Inits the blocks.
     *
     * @param mcAccessHandle
     *            the mc access handle
     * @param worldConfigProvider
     *            the world config provider
     */
    private static void initBlocks(final IHandle<MCAccess> mcAccessHandle, final WorldConfigProvider<?> worldConfigProvider) {
        final MCAccess mcAccess = mcAccessHandle.getHandle();
        // Reset tool props.
        Arrays.fill(blocks, null);
        // Initialize block flags
        // Generic initialization.
        for (int i = 0; i < maxBlocks; i++) {
            blockFlags[i] = 0;

            if (mcAccess.isBlockLiquid(i).decide()) {
                // TODO: do not set F_GROUND for liquids ?
                blockFlags[i] |= F_LIQUID;
                if (mcAccess.isBlockSolid(i).decide()) blockFlags[i] |= F_SOLID;
            }
            else if (mcAccess.isBlockSolid(i).decide()) {
                blockFlags[i] |= F_SOLID | F_GROUND;
            }

        }

        // Stairs.
        for (final Material mat : new Material[] { Material.NETHER_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, 
                Material.SMOOTH_STAIRS, Material.BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.WOOD_STAIRS, 
                Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS }) {
            blockFlags[mat.getId()] |= F_STAIRS | F_HEIGHT100 | F_XZ100 | F_GROUND | F_GROUND_HEIGHT; // Set ground too, to be sure.
        }

        // Step (ground + full width).
        for (final Material mat : new Material[]{
                Material.STEP, Material.WOOD_STEP,
        }) {
            blockFlags[mat.getId()] |= F_GROUND | F_XZ100;
        }

        // Rails
        for (final Material mat : new Material[] { 
                Material.RAILS, Material.DETECTOR_RAIL, Material.POWERED_RAIL,
        }) {
            blockFlags[mat.getId()] |= F_RAILS;
        }

        // WATER.
        for (final Material mat : new Material[]{
                Material.STATIONARY_WATER, Material.WATER,
        }) {
            blockFlags[mat.getId()] |= F_LIQUID | F_HEIGHT_8SIM_DEC | F_WATER;
        }

        // LAVA.
        for (final Material mat : new Material[]{
                Material.LAVA, Material.STATIONARY_LAVA,
        }) {
            blockFlags[mat.getId()] |= F_LIQUID | F_HEIGHT_8SIM_DEC | F_LAVA;
        }

        // Snow (1.4.x)
        blockFlags[Material.SNOW.getId()] |= F_HEIGHT_8SIM_INC;

        // Climbable
        for (final Material mat : new Material[]{
                Material.VINE, Material.LADDER,
        }) {
            blockFlags[mat.getId()] |= F_CLIMBABLE;
        }

        // Workarounds.
        // Ground (can stand on).
        for (final Material mat : new Material[]{
                Material.WATER_LILY, Material.LADDER,
                Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
                Material.COCOA, Material.SNOW, Material.BREWING_STAND,
                Material.PISTON_MOVING_PIECE, Material.PISTON_EXTENSION,
                Material.STEP, Material.WOOD_STEP,
        }) {
            blockFlags[mat.getId()] |= F_GROUND;
        }

        // Full block height.
        for (final Material mat : new Material[]{
                //        		Material.ENDER_PORTAL_FRAME,
                Material.BREWING_STAND,
                Material.PISTON_EXTENSION,
                Material.SOIL, // Server reports the visible shape 0.9375, client moves on full block height.
        }) {
            blockFlags[mat.getId()] |= F_HEIGHT100;
        }

        // Full width/xz-bounds.
        for (final Material mat : new Material[]{
                Material.PISTON_EXTENSION,
                Material.ENDER_PORTAL_FRAME
        }) {
            blockFlags[mat.getId()] |= F_XZ100;
        }

        // ICE
        blockFlags[Material.ICE.getId()] |= F_ICE;

        // Not ground (!).
        for (final Material mat : new Material[]{
                Material.WALL_SIGN, Material.SIGN_POST,
        }) {
            // TODO: Might keep solid since it is meant to be related to block shapes rather ("original mc value").
            blockFlags[mat.getId()] &= ~(F_GROUND | F_SOLID);
        }

        // Ignore for passable.
        for (final Material mat : new Material[]{
                // More strictly needed.
                Material.WOOD_PLATE, Material.STONE_PLATE, 
                Material.WALL_SIGN, Material.SIGN_POST,
                Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF,
                Material.BREWING_STAND,
                // Compatibility.
                Material.LADDER, 
                // Somewhat needed (xz-bounds vary, not critical to pass through).
                Material.CAKE_BLOCK,
                // Workarounds.
                //				Material.COCOA,
        }) {
            blockFlags[mat.getId()] |= F_IGN_PASSABLE;
        }

        // ? Extra flag for COCOA, ANVIL: depends on data value (other issue)

        // Fences, 1.5 block high.
        for (final Material mat : new Material[]{
                Material.FENCE, Material.FENCE_GATE,
                Material.NETHER_FENCE, Material.COBBLE_WALL,
        }) {
            blockFlags[mat.getId()] |= F_HEIGHT150 | F_VARIABLE | F_THICK_FENCE;
        }

        // F_PASSABLE_X4
        for (final Material mat : new Material[]{
                Material.FENCE_GATE,
                Material.TRAP_DOOR, // TODO: Players can stand on - still passable past 1.9?
        }) {
            blockFlags[mat.getId()] |= F_PASSABLE_X4; // TODO: Flag is abused for other checks, need another one.
        }

        // F_FACING_LOW3D2_NSWE
        for (final Material mat : new Material[]{
                Material.LADDER
        }) {
            blockFlags[mat.getId()] |= F_FACING_LOW3D2_NSWE;
        }

        // F_FACING_LOW2_SNEW
        for (final Material mat : new Material[]{
                Material.TRAP_DOOR,
        }) {
            blockFlags[mat.getId()] |= F_ATTACHED_LOW2_SNEW;
        }

        // Thin fences (iron fence, glass panes).
        for (final Material mat : new Material[]{
                Material.IRON_FENCE, Material.THIN_GLASS,
        }) {
            blockFlags[mat.getId()] |= F_THIN_FENCE | F_VARIABLE;
        }

        // Flexible ground (height):
        for (final Material mat : new Material[]{
                // Strictly needed (multiple boxes otherwise).
                Material.PISTON_EXTENSION,
                Material.BREWING_STAND,
                Material.ENDER_PORTAL_FRAME,
                // XZ-bounds issues.
                Material.CAKE_BLOCK,
                // Already worked around with isPassableWorkaround (kept for dev-reference).
                //				Material.ANVIL,
                //				Material.SKULL, Material.FLOWER_POT,
                //				Material.DRAGON_EGG,
                //				Material.COCOA,
                // Issues standing on with F_PASSABLE_X4.
                Material.TRAP_DOOR,
                // 1.10.2 +- client uses the reported height.
                Material.SOIL,
        }) {
            blockFlags[mat.getId()] |= F_GROUND_HEIGHT;
        }
        // Set block break properties.
        // Instantly breakable.
        for (final Material mat : instantMat) {
            blocks[mat.getId()] = instantType;
        }
        // TODO: Bed is special.
        // Leaf type
        for (Material mat : new Material[]{ 
                Material.LEAVES, Material.BED_BLOCK}) {
            blocks[mat.getId()] = leafType;
        }
        blockFlags[Material.LEAVES.getId()] |= F_LEAVES;
        // Huge mushroom type (...)
        for (Material mat : new Material[]{ 
                Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2,
                Material.VINE, Material.COCOA}) {
            blocks[mat.getId()] = hugeMushroomType;
        }

        blocks[Material.SNOW.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(0.5, 0.1, 0.05, 0.05, 0.05, 0.05));
        blocks[Material.SNOW_BLOCK.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(1, 0.15, 0.1, 0.05, 0.05, 0.05));
        for (Material mat : new Material[]{ 
                Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF,
                Material.GLOWSTONE, Material.GLASS,
        }) {
            blocks[mat.getId()] = glassType;
        }
        blocks[Material.THIN_GLASS.getId()] = glassType;
        blocks[Material.NETHERRACK.getId()] = new BlockProps(woodPickaxe, 0.4f, secToMs(2, 0.3, 0.15, 0.1, 0.1, 0.05));
        blocks[Material.LADDER.getId()] = new BlockProps(noTool, 0.4f, secToMs(0.6), 2.5f);
        blocks[Material.CACTUS.getId()] = new BlockProps(noTool, 0.4f, secToMs(0.6));
        blocks[Material.WOOD_PLATE.getId()] = new BlockProps(woodAxe, 0.5f, secToMs(0.75, 0.4, 0.2, 0.15, 0.1, 0.1));
        blocks[Material.STONE_PLATE.getId()] = new BlockProps(woodPickaxe, 0.5f, secToMs(2.5, 0.4, 0.2, 0.15, 0.1, 0.07));
        blocks[Material.SAND.getId()] = sandType;
        blocks[Material.SOUL_SAND.getId()] = sandType;
        for (Material mat: new Material[]{Material.LEVER, Material.PISTON_BASE, 
                Material.PISTON_EXTENSION, Material.PISTON_STICKY_BASE,
                Material.STONE_BUTTON, Material.PISTON_MOVING_PIECE}) {
            blocks[mat.getId()] = leverType;
        }
        //		blocks[Material.ICE.getId()] = new BlockProps(woodPickaxe, 0.5f, secToMs(2.5, 0.4, 0.2, 0.15, 0.1, 0.1));
        blocks[Material.ICE.getId()] = new BlockProps(woodPickaxe, 0.5f, secToMs(0.7, 0.35, 0.18, 0.12, 0.09, 0.06 ));
        blocks[Material.DIRT.getId()] = sandType;
        blocks[Material.CAKE_BLOCK.getId()] = leverType;
        blocks[Material.BREWING_STAND.getId()] = new BlockProps(woodPickaxe, 0.5f, secToMs(2.5, 0.4, 0.2, 0.15, 0.1, 0.1));
        blocks[Material.SPONGE.getId()] = new BlockProps(noTool, 0.6f, secToMs(0.9));
        for (Material mat : new Material[]{
                Material.MYCEL, Material.GRAVEL, Material.GRASS, Material.SOIL,
                Material.CLAY,
        }) {
            blocks[mat.getId()] = gravelType;
        }
        for (Material mat : new Material[]{
                Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL,
        }) {
            blocks[mat.getId()] = new BlockProps(woodPickaxe, 0.7f, railsTimes);
        }
        blocks[Material.MONSTER_EGGS.getId()] = new BlockProps(noTool, 0.75f, secToMs(1.15)); 
        blocks[Material.WOOL.getId()] = new BlockProps(noTool, 0.8f, secToMs(1.2), 3f);
        blocks[Material.SANDSTONE.getId()] = sandStoneType;
        blocks[Material.SANDSTONE_STAIRS.getId()] = sandStoneType;
        for (Material mat : new Material[]{
                Material.STONE, Material.SMOOTH_BRICK, Material.SMOOTH_STAIRS,
        }) {
            blocks[mat.getId()] =  stoneType;
        }
        blocks[Material.NOTE_BLOCK.getId()] = new BlockProps(woodAxe, 0.8f, secToMs(1.2, 0.6, 0.3, 0.2, 0.15, 0.1));
        final BlockProps pumpkinType = new BlockProps(woodAxe, 1, secToMs(1.5, 0.75, 0.4, 0.25, 0.2, 0.15));
        blocks[Material.WALL_SIGN.getId()] = pumpkinType;
        blocks[Material.SIGN_POST.getId()] = pumpkinType;
        blocks[Material.PUMPKIN.getId()] = pumpkinType;
        blocks[Material.JACK_O_LANTERN.getId()] = pumpkinType;
        blocks[Material.MELON_BLOCK.getId()] = new BlockProps(noTool, 1, secToMs(1.45), 3);
        blocks[Material.BOOKSHELF.getId()] = new BlockProps(woodAxe, 1.5f, secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.2));
        for (Material mat : new Material[]{
                Material.WOOD_STAIRS, Material.WOOD, Material.WOOD_STEP, Material.LOG,
                Material.FENCE, Material.FENCE_GATE, Material.JUKEBOX,
                Material.JUNGLE_WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS,
                Material.BIRCH_WOOD_STAIRS, 
                Material.WOOD_DOUBLE_STEP, // ?
                // double slabs ?
        }) {
            blocks[mat.getId()] =  woodType;
        }
        for (Material mat : new Material[]{
                Material.COBBLESTONE_STAIRS, Material.COBBLESTONE, 
                Material.NETHER_BRICK, Material.NETHER_BRICK_STAIRS, Material.NETHER_FENCE,
                Material.CAULDRON, Material.BRICK, Material.BRICK_STAIRS,
                Material.MOSSY_COBBLESTONE, Material.BRICK, Material.BRICK_STAIRS,
                Material.STEP, Material.DOUBLE_STEP, // ?

        }) {
            blocks[mat.getId()] =  brickType;
        }
        blocks[Material.WORKBENCH.getId()] = chestType;
        blocks[Material.CHEST.getId()] = chestType;
        blocks[Material.WOODEN_DOOR.getId()] = woodDoorType;
        blocks[Material.TRAP_DOOR.getId()] = woodDoorType;
        for (Material mat : new Material[]{
                Material.ENDER_STONE, Material.COAL_ORE,

        }) {
            blocks[mat.getId()] =  coalType;
        }
        blocks[Material.DRAGON_EGG.getId()] = new BlockProps(noTool, 3f, secToMs(4.5)); // Former: coalType.
        final long[] ironTimes = secToMs(15, 15, 1.15, 0.75, 0.6, 15);
        final BlockProps ironType = new BlockProps(stonePickaxe, 3, ironTimes);
        for (Material mat : new Material[]{
                Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.IRON_ORE,
        }) {
            blocks[mat.getId()] =  ironType;
        }
        final long[] diamondTimes = secToMs(15, 15, 15, 0.75, 0.6, 15);
        final BlockProps diamondType = new BlockProps(ironPickaxe, 3, diamondTimes);
        for (Material mat : new Material[]{
                Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
                Material.EMERALD_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
        }) {
            blocks[mat.getId()] =  diamondType;
        }
        blocks[Material.GOLD_BLOCK.getId()] =  goldBlockType;
        blocks[Material.FURNACE.getId()] = dispenserType;
        blocks[Material.BURNING_FURNACE.getId()] = dispenserType;
        blocks[Material.DISPENSER.getId()] = dispenserType;
        blocks[Material.WEB.getId()] = new BlockProps(woodSword, 4, secToMs(20, 0.4, 0.4, 0.4, 0.4, 0.4));

        for (Material mat : new Material[]{
                Material.MOB_SPAWNER, Material.IRON_DOOR_BLOCK,
                Material.IRON_FENCE, Material.ENCHANTMENT_TABLE, 
                Material.EMERALD_BLOCK,
        }) {
            blocks[mat.getId()] = ironDoorType;
        }
        blocks[Material.IRON_BLOCK.getId()] = ironBlockType;
        blocks[Material.DIAMOND_BLOCK.getId()] = diamondBlockType;
        blocks[Material.ENDER_CHEST.getId()] = new BlockProps(woodPickaxe, 22.5f);
        blocks[Material.OBSIDIAN.getId()] = new BlockProps(diamondPickaxe, 50, secToMs(250, 125, 62.5, 41.6, 9.4, 20.8));

        // More 1.4 (not insta).
        // TODO: Either move all to an extra setup class, or integrate above.
        blocks[Material.BEACON.getId()] = new BlockProps(noTool, 25f, secToMs(4.45)); // TODO
        blocks[Material.COBBLE_WALL.getId()] = brickType;
        blockFlags[Material.COBBLE_WALL.getId()] |= F_HEIGHT150;
        blocks[Material.WOOD_BUTTON.getId()] = leverType;
        blocks[Material.SKULL.getId()] = new BlockProps(noTool, 8.5f, secToMs(1.45)); // TODO
        blockFlags[Material.SKULL.getId()] |= F_GROUND;
        blocks[Material.ANVIL.getId()] = new BlockProps(woodPickaxe, 5f); // TODO
        blockFlags[Material.FLOWER_POT.getId()] |= F_GROUND;

        // Indestructible.
        for (Material mat : new Material[]{
                Material.AIR, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
                Material.PORTAL, Material.LAVA, Material.WATER, Material.BEDROCK,
                Material.STATIONARY_LAVA, Material.STATIONARY_WATER,
        }) {
            blocks[mat.getId()] = indestructibleType; 
        }
        blocks[95] = indestructibleType; // Locked chest (prevent crash with 1.7).
    }

    /**
     * Dump blocks.
     *
     * @param all
     *            the all
     */
    public static void dumpBlocks(boolean all) {
        final LogManager logManager = NCPAPIProvider.getNoCheatPlusAPI().getLogManager();
        List<String> missing = new LinkedList<String>();
        List<String> allBlocks = new LinkedList<String>();
        if (all) {
            allBlocks.add("Dump block properties for fastbreak check:");
            allBlocks.add("--- Present entries -------------------------------");
        }
        List<String> tags = new ArrayList<String>();
        for (int i = 0; i < blocks.length; i++) {
            String mat;
            try{
                Material temp = Material.getMaterial(i);
                if (!temp.isBlock()) {
                    continue;
                }
                mat = temp.toString();
            }
            catch(Exception e) {
                mat = "?";
            }
            tags.clear();
            addFlagNames(blockFlags [i], tags);
            String tagsJoined = tags.isEmpty() ? "" : " / " + StringUtil.join(tags, "+");
            if (blocks[i] == null) {
                if (mat.equals("?")) {
                    continue;
                }
                missing.add("* MISSING "+i + "(" + mat + tagsJoined + ") ");
            }
            else {
                if (all) {
                    allBlocks.add(i + ": (" + mat + tagsJoined + ") " + blocks[i].toString());
                }
            }
        }
        if (all) {
            logManager.info(Streams.DEFAULT_FILE, StringUtil.join(allBlocks, "\n"));
        }
        if (!missing.isEmpty()) {
            missing.add(0, "--- Missing entries -------------------------------");
            missing.add(0, "The block breaking data is incomplete, default to allow instant breaking:");
            logManager.warning(Streams.INIT, StringUtil.join(missing,  "\n"));
        }
    }

    /**
     * Add all flag names for existing default flags to the list.
     *
     * @param flags
     *            the flags
     * @param tags
     *            Flag names will be added to this list (not with the
     *            "F_"-prefix).
     */
    public static void addFlagNames(final long flags, final Collection<String> tags) {
        String tag = flagNameMap.get(flags);
        if (tag != null) {
            tags.add(tag);
            return;
        }
        for (final Long flag : flagNameMap.keySet()) {
            if ((flags & flag) != 0) {
                tags.add(flagNameMap.get(flag));
            }
        }
    }

    /**
     * Return a collection containing all names of the flags.
     *
     * @param flags
     *            the flags
     * @return the flag names
     */
    public static Collection<String> getFlagNames(final Long flags) {
        final ArrayList<String> tags = new ArrayList<String>(flagNameMap.size());
        if (flags == null) {
            return tags;
        }
        addFlagNames(flags, tags);
        return tags;
    }

    /**
     * Convenience method to parse a flag.
     *
     * @param input
     *            the input
     * @return the long
     * @throws InputMismatchException
     *             if failed to parse.
     */
    public static long parseFlag(final String input) {
        final String ucInput = input.trim().toUpperCase();
        final Long flag = nameFlagMap.get(ucInput);
        if (flag != null) {
            return flag.longValue();
        }
        try{
            final Long altFlag = Long.parseLong(input);
            return altFlag;
        } catch (NumberFormatException e) {}
        // TODO: This very exception type?
        throw new InputMismatchException();
    }

    /**
     * Sec to ms.
     *
     * @param s1
     *            the s1
     * @param s2
     *            the s2
     * @param s3
     *            the s3
     * @param s4
     *            the s4
     * @param s5
     *            the s5
     * @param s6
     *            the s6
     * @return the long[]
     */
    public static long[] secToMs(final double s1, final double s2, final double s3, final double s4, final double s5, final double s6) {
        return new long[] { (long) (s1 * 1000d), (long) (s2 * 1000d), (long) (s3 * 1000d), (long) (s4 * 1000d), (long) (s5 * 1000d), (long) (s6 * 1000d) };
    }

    /**
     * Sec to ms.
     *
     * @param s1
     *            the s1
     * @return the long[]
     */
    public static long[] secToMs(final double s1) {
        final long v = (long) (s1 * 1000d);
        return new long[]{v, v, v, v, v, v};
    }

    /**
     * Gets the tool props.
     *
     * @param stack
     *            the stack
     * @return the tool props
     */
    public static ToolProps getToolProps(final ItemStack stack) {
        if (stack == null) {
            return noTool;
        }
        else {
            return getToolProps(stack.getTypeId());
        }
    }

    /**
     * Gets the tool props.
     *
     * @param mat
     *            the mat
     * @return the tool props
     */
    public static ToolProps getToolProps(final Material mat) {
        if (mat == null) {
            return noTool;
        }
        else {
            return getToolProps(mat.getId());
        }
    }

    /**
     * Gets the tool props.
     *
     * @param id
     *            the id
     * @return the tool props
     */
    public static ToolProps getToolProps(final Integer id) {
        final ToolProps props = tools.get(id);
        if (props == null) {
            return noTool;
        }
        else {
            return props;
        }
    }

    /**
     * Gets the block props.
     *
     * @param stack
     *            the stack
     * @return the block props
     */
    public static BlockProps getBlockProps(final ItemStack stack) {
        if (stack == null) {
            return defaultBlockProps;
        }
        else {
            return getBlockProps(stack.getTypeId());
        }
    }

    /**
     * Gets the block props.
     *
     * @param mat
     *            the mat
     * @return the block props
     */
    public static BlockProps getBlockProps(final Material mat) {
        if (mat == null) {
            return defaultBlockProps;
        }
        else {
            return getBlockProps(mat.getId());
        }
    }

    /**
     * Gets the block props.
     *
     * @param blockId
     *            the block id
     * @return the block props
     */
    public static BlockProps getBlockProps(final int blockId) {
        if (blockId <0 || blockId >= blocks.length || blocks[blockId] == null) {
            return defaultBlockProps;
        }
        else {
            return blocks[blockId];
        }
    }

    /**
     * Convenience method.
     *
     * @param BlockType
     *            the block type
     * @param player
     *            the player
     * @return the breaking duration
     */
    public static long getBreakingDuration(final Material BlockType, final Player player) {
        return getBreakingDuration(BlockType.getId(), player);
    }

    /**
     * Convenience method.
     *
     * @param blockId
     *            the block id
     * @param player
     *            the player
     * @return the breaking duration
     */
    public static long getBreakingDuration(final int blockId, final Player player) {
        final long res = getBreakingDuration(blockId, 
                Bridge1_9.getItemInMainHand(player), player.getInventory().getHelmet(), 
                player, player.getLocation(useLoc));
        useLoc.setWorld(null);
        return res;
    }

    /**
     * TODO: repair signature some day (rid of PlayerLocation).
     *
     * @param blockId
     *            the block id
     * @param itemInHand
     *            May be null.
     * @param helmet
     *            May be null.
     * @param player
     *            the player
     * @param location
     *            The normal location of a player.
     * @return the breaking duration
     */
    public static long getBreakingDuration(final int blockId, final ItemStack itemInHand, final ItemStack helmet, final Player player, final Location location) {
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, 0.3);
        // On ground.
        final boolean onGround = pLoc.isOnGround();
        // Head in water.
        final int bx = pLoc.getBlockX();
        final int bz = pLoc.getBlockZ();
        final double y = pLoc.getY() + player.getEyeHeight();
        final int by = Location.locToBlock(y);
        final int headId = blockCache.getTypeId(bx, by, bz);
        final long headFlags = blockFlags[headId];
        final boolean inWater;
        if ((headFlags & F_WATER) == 0) {
            inWater = false;
        }
        else {
            // Check real bounding box collision.
            // (Not sure which to use here.)
            final int data8 = (blockCache.getData(bx, by, bz) & 0xF) % 8;
            final double level;
            if ((data8 & 8) != 0) {
                level = 1.0;
            }
            else {
                level = 1.0 - 0.125 * (1.0 + data8);
            }
            inWater = y - by < level; // <= ? ...
        }
        blockCache.cleanup();
        pLoc.cleanup();
        // Haste (faster digging).
        final double haste = PotionUtil.getPotionEffectAmplifier(player, PotionEffectType.FAST_DIGGING);
        // TODO: haste: int / double !?
        return getBreakingDuration(blockId, itemInHand, onGround, inWater, helmet != null && helmet.containsEnchantment(Enchantment.WATER_WORKER), haste == Double.NEGATIVE_INFINITY ? 0 : 1 + (int) haste);
    }

    /**
     * Get the normal breaking duration, including enchantments, and tool
     * properties.
     *
     * @param blockId
     *            the block id
     * @param itemInHand
     *            the item in hand
     * @param onGround
     *            the on ground
     * @param inWater
     *            the in water
     * @param aquaAffinity
     *            the aqua affinity
     * @param haste
     *            the haste
     * @return the breaking duration
     */
    public static long getBreakingDuration(final int blockId, final ItemStack itemInHand, final boolean onGround, final boolean inWater, final boolean aquaAffinity, final int haste) {
        // TODO: more configurability / load from file for blocks (i.e. set for shears etc.
        if (isAir(itemInHand)) {
            return getBreakingDuration(blockId, getBlockProps(blockId), noTool, onGround, inWater, aquaAffinity, 0);
        }
        else {
            int efficiency = 0;
            if (itemInHand.containsEnchantment(Enchantment.DIG_SPEED)) {
                efficiency = itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
            }
            return getBreakingDuration(blockId, getBlockProps(blockId), getToolProps(itemInHand.getTypeId()), onGround, inWater, aquaAffinity, efficiency, haste);
        }
    }

    /**
     * Gets the breaking duration.
     *
     * @param blockId
     *            the block id
     * @param blockProps
     *            the block props
     * @param toolProps
     *            the tool props
     * @param onGround
     *            the on ground
     * @param inWater
     *            the in water
     * @param aquaAffinity
     *            the aqua affinity
     * @param efficiency
     *            the efficiency
     * @param haste
     *            Amplifier of haste potion effect (assume > 0 for effect there
     *            at all, so 1 is haste I, 2 is haste II).
     * @return the breaking duration
     */
    public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency, int haste) {
        final long dur = getBreakingDuration(blockId, blockProps, toolProps, onGround, inWater, aquaAffinity, efficiency);
        return haste > 0 ? (long) (Math.pow(0.8, haste) * dur) : dur;
    }

    /**
     * Gets the breaking duration.
     *
     * @param blockId
     *            the block id
     * @param blockProps
     *            the block props
     * @param toolProps
     *            the tool props
     * @param onGround
     *            the on ground
     * @param inWater
     *            the in water
     * @param aquaAffinity
     *            the aqua affinity
     * @param efficiency
     *            the efficiency
     * @return the breaking duration
     */
    public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency) {
        boolean isValidTool = isValidTool(blockId, blockProps, toolProps, efficiency);
        if (efficiency > 0) {
            // Workaround until something better is found..
            // TODO: Re-evaluate.
            if (isLeaves(blockId) || blockProps == glassType) {
                if (efficiency == 1) {
                    return 100;
                }
                else {
                    return 0; // insta break.
                }
            }
            else if (blockProps == chestType) {
                // TODO: The no tool time might be reference anyway for some block types.
                return (long) ((double )blockProps.breakingTimes[0] / 5f / efficiency);
            }
        }

        long duration;

        if (isValidTool) {
            // appropriate tool
            duration = blockProps.breakingTimes[toolProps.materialBase.index];
            if (efficiency > 0) {
                duration = (long) (duration / blockProps.efficiencyMod);
            }
        }
        else {
            // Inappropriate tool.
            duration = blockProps.breakingTimes[0];
            // Swords are always appropriate.
            if (toolProps.toolType == ToolType.SWORD) {
                duration = (long) ((float) duration / 1.5f);
            }
        }

        // Specialties:
        if (toolProps.toolType == ToolType.SHEARS) {
            // (Note: shears are not in the block props, anywhere)
            // Treat these extra (partly experimental):
            if (blockId == Material.WEB.getId()) {
                duration = 400;
                isValidTool = true;
            }
            else if (blockId == Material.WOOL.getId()) {
                duration = 240;
                isValidTool = true;
            }
            else if (isLeaves(blockId)) {
                duration = 20;
                isValidTool = true;
            }
            else if (blockId == Material.VINE.getId()) {
                duration = 300;
                isValidTool = true;
            }
        }
        // (sword vs web already counted)
        else if (blockId == Material.VINE.getId() && toolProps.toolType == ToolType.AXE) {
            isValidTool = true;
            if (toolProps.materialBase == MaterialBase.WOOD || toolProps.materialBase == MaterialBase.STONE) {
                duration = 100;
            }
            else {
                duration = 0;
            }
        }

        if (isValidTool || blockProps.tool.toolType == ToolType.NONE) {
            float mult = 1f;
            if (inWater && !aquaAffinity) {
                mult *= breakPenaltyInWater;
            }
            if (!onGround) {
                mult *= breakPenaltyOffGround;
            }
            duration = (long) (mult * duration);

            // Efficiency level.
            if (efficiency > 0) {
                // Workarounds ...
                if (blockId == Material.WOODEN_DOOR.getId() && toolProps.toolType != ToolType.AXE) {
                    // Heck [Cleanup pending]...
                    switch (efficiency) {
                        case 1:
                            return (long) (mult * 1500);
                        case 2:
                            return (long) (mult * 750);
                        case 3:
                            return (long) (mult * 450);
                        case 4:
                            return (long) (mult * 250);
                        case 5:
                            return (long) (mult * 150);
                    }
                }
                // This seems roughly correct.
                for (int i = 0; i < efficiency; i++) {
                    duration /= 1.33; // Matches well with obsidian.
                }
                // Formula from MC wiki.
                // TODO: Formula from mc wiki does not match well (too fast for obsidian).
                //				duration /= (1.0 + 0.5 * efficiency);

                // More Workarounds:
                // TODO: Consider checking a generic workaround (based on duration, assuming some dig packets lost, proportional to duration etc.).
                if (toolProps.materialBase == MaterialBase.WOOD) {
                    if (toolProps.toolType == ToolType.PICKAXE && (blockProps == ironDoorType || blockProps == dispenserType)) {
                        // Special correction.
                        // TODO: Uncomfortable: hide this in the blocks by some flags / other type of workarounds !
                        if (blockProps == dispenserType) {
                            duration = (long) (duration / 1.5 - (efficiency - 1) * 60);
                        }
                        else if (blockProps == ironDoorType) {
                            duration = (long) (duration / 1.5 - (efficiency - 1) * 100);
                        }
                    }
                    else if (blockId == Material.LOG.getId()) {
                        duration -= efficiency >= 4 ? 250 : 400;
                    }
                    else if (blockProps.tool.toolType == toolProps.toolType) {
                        duration -= 250;
                    }
                    else {
                        duration -= efficiency * 30;
                    }

                }
                else if (toolProps.materialBase == MaterialBase.STONE) {
                    if (blockId == Material.LOG.getId()) {
                        duration -= 100;
                    }
                }
            }
        }
        // Post/legacy workarounds for efficiency tools ("improper").
        if (efficiency > 0 && !isValidTool) {
            if (!isValidTool && blockId == Material.MELON_BLOCK.getId()) {
                // Fall back to pre-1.8 behavior.
                // 450, 200 , 100 , 50 , 0
                duration = Math.min(duration, 450 / (long) Math.pow(2, efficiency - 1)); 
            }
        }
        return Math.max(0, duration);
    }

    /**
     * Check if the tool is officially appropriate for the block id, counting in
     * efficiency enchantments.
     *
     * @param blockId
     *            the block id
     * @param blockProps
     *            the block props
     * @param toolProps
     *            the tool props
     * @param efficiency
     *            the efficiency
     * @return true, if is valid tool
     */
    public static boolean isValidTool(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final int efficiency) {
        boolean isValidTool = blockProps.tool.toolType == toolProps.toolType;

        if (!isValidTool && efficiency > 0) {
            // Efficiency makes the tool.
            // (wood, sand, gravel, ice)
            if (blockId == Material.SNOW.getId()) {
                return toolProps.toolType == ToolType.SPADE;
            }
            if (blockId == Material.WOOL.getId()) {
                return true;
            }
            if (blockId == Material.WOODEN_DOOR.getId()) {
                return true;
            }
            if (blockProps.hardness <= 2 
                    && (blockProps.tool.toolType == ToolType.AXE 
                    || blockProps.tool.toolType == ToolType.SPADE
                    || (blockProps.hardness < 0.8 && (blockId != Material.NETHERRACK.getId() && blockId != Material.SNOW.getId() && blockId != Material.SNOW_BLOCK.getId() && blockId != Material.STONE_PLATE.getId())))) {
                // Also roughly.
                return true;
            }
        }
        return isValidTool;
    }

    /**
     * Access API for setting tool properties.<br>
     * NOTE: No guarantee that this harmonizes with internals and workarounds,
     * currently.
     *
     * @param itemId
     *            the item id
     * @param toolProps
     *            the tool props
     */
    public static void setToolProps(int itemId, ToolProps toolProps) {
        if (toolProps == null) {
            throw new NullPointerException("ToolProps must not be null");
        }
        toolProps.validate();
        // No range check.
        tools.put(itemId, toolProps);
    }

    /**
     * Access API to set a blocks properties. NOTE: No guarantee that this
     * harmonizes with internals and workarounds, currently.
     *
     * @param blockId
     *            the block id
     * @param blockProps
     *            the block props
     */
    public static void setBlockProps(int blockId, BlockProps blockProps) {
        if (blockProps == null) {
            throw new NullPointerException("BlockProps must not be null");
        }
        blockProps.validate();
        if (blockId < 0 || blockId >= blocks.length) {
            throw new IllegalArgumentException("The blockId is outside of supported range: " + blockId);
        }
        blocks[blockId] = blockProps;
    }

    /**
     * Checks if is valid tool.
     *
     * @param blockType
     *            the block type
     * @param itemInHand
     *            the item in hand
     * @return true, if is valid tool
     */
    public static boolean isValidTool(final Material blockType, final ItemStack itemInHand) {
        return isValidTool(blockType.getId(), itemInHand);
    }

    /**
     * Checks if is valid tool.
     *
     * @param blockId
     *            the block id
     * @param itemInHand
     *            the item in hand
     * @return true, if is valid tool
     */
    public static boolean isValidTool(final int blockId, final ItemStack itemInHand) {
        final BlockProps blockProps = getBlockProps(blockId);
        final ToolProps toolProps = getToolProps(itemInHand);
        final int efficiency = itemInHand == null ? 0 : itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
        return isValidTool(blockId, blockProps, toolProps, efficiency);
    }


    /**
     * Gets the default block props.
     *
     * @return the default block props
     */
    public static BlockProps getDefaultBlockProps() {
        return defaultBlockProps;
    }

    /**
     * Feeding null will cause an npe - will validate.
     *
     * @param blockProps
     *            the new default block props
     */
    public static void setDefaultBlockProps(BlockProps blockProps) {
        blockProps.validate();
        BlockProperties.defaultBlockProps = blockProps;
    }

    /**
     * Simple checking method, heavy. No isIllegal check.
     *
     * @param player
     *            the player
     * @param location
     *            the location
     * @param yOnGround
     *            the y on ground
     * @return true, if is in liquid
     */
    public static boolean isInLiquid(final Player player, final Location location, final double yOnGround) {
        // Bit fat workaround, maybe put the object through from check listener ?
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, yOnGround);
        final boolean res = pLoc.isInLiquid();
        blockCache.cleanup();
        pLoc.cleanup();
        return res;
    }

    /**
     * Simple checking method, heavy. No isIllegal check.
     *
     * @param player
     *            the player
     * @param location
     *            the location
     * @param yOnGround
     *            the y on ground
     * @return true, if is in web
     */
    public static boolean isInWeb(final Player player, final Location location, final double yOnGround) {
        // Bit fat workaround, maybe put the object through from check listener ?
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, yOnGround);
        final boolean res = pLoc.isInWeb();
        blockCache.cleanup();
        pLoc.cleanup();
        return res;
    }

    /**
     * Simple checking method, heavy. No isIllegal check.
     *
     * @param player
     *            the player
     * @param location
     *            the location
     * @param yOnGround
     *            the y on ground
     * @return true, if is on ground
     */
    public static boolean isOnGround(final Player player, final Location location, final double yOnGround) {
        // Bit fat workaround, maybe put the object through from check listener ?
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, yOnGround);
        final boolean res = pLoc.isOnGround();
        blockCache.cleanup();
        pLoc.cleanup();
        return res;
    }

    /**
     * Simple checking method, heavy. No isIllegal check.
     *
     * @param player
     *            the player
     * @param location
     *            the location
     * @param yOnGround
     *            the y on ground
     * @return true, if is on ground or reset cond
     */
    public static boolean isOnGroundOrResetCond(final Player player, final Location location, final double yOnGround) {
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, yOnGround);
        final boolean res = pLoc.isOnGroundOrResetCond();
        blockCache.cleanup();
        pLoc.cleanup();
        return res;
    }

    /**
     * Simple checking method, heavy. No isIllegal check.
     *
     * @param player
     *            the player
     * @param location
     *            the location
     * @param yOnGround
     *            the y on ground
     * @return true, if is reset cond
     */
    public static boolean isResetCond(final Player player, final Location location, final double yOnGround) {
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(location.getWorld());
        pLoc.setBlockCache(blockCache);
        pLoc.set(location, player, yOnGround);
        final boolean res = pLoc.isResetCond();
        blockCache.cleanup();
        pLoc.cleanup();
        return res;
    }

    /**
     * Straw-man-method to hide warnings. Rather intended for display in
     * debug/alert messages.
     *
     * @param blockType
     *            the block type
     * @return the id
     */
    public static int getId(final Material blockType) {
        return blockType.getId();
    }

    /**
     * Get the (legacy) data value for the block.
     *
     * @param block
     *            the block
     * @return the data
     */
    public static int getData(final Block block) {
        return block.getData();
    }

    /**
     * Straw-man method to hide warnings.
     *
     * @param id
     *            the id
     * @return the material
     */
    public static Material getMaterial(final int id) {
        return Material.getMaterial(id);
    }

    /**
     * Gets the b lock flags.
     *
     * @param id
     *            the id
     * @return the b lock flags
     * @deprecated Typo in method name.
     */
    public static final long getBLockFlags(final int id) {
        return blockFlags[id];
    }

    /**
     * Gets the block flags.
     *
     * @param blockType
     *            the block type
     * @return the block flags
     */
    public static final long getBlockFlags(final Material blockType) {
        return getBlockFlags(blockType.getId());
    }

    /**
     * Gets the block flags.
     *
     * @param id
     *            the id
     * @return the block flags
     */
    public static final long getBlockFlags(final int id) {
        return blockFlags[id];
    }

    /**
     * Sets the block flags.
     *
     * @param blockType
     *            the block type
     * @param flags
     *            the flags
     */
    public static final void setBlockFlags(final Material blockType, final long flags) {
        setBlockFlags(blockType.getId(), flags);
    }

    /**
     * Sets the block flags.
     *
     * @param id
     *            the id
     * @param flags
     *            the flags
     */
    public static final void setBlockFlags(final int id, final long flags) {
        blockFlags[id] = flags;
    }

    /**
     * Auxiliary check for if this block is climbable and allows climbing up.
     * Does not account for jumping off ground etc.
     *
     * @param cache
     *            the cache
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if successful
     */
    public static final boolean canClimbUp(final BlockCache cache, final int x, final int y, final int z) {
        final int id = cache.getTypeId(x, y, z);
        if ((blockFlags[id] & F_CLIMBABLE) == 0) {
            return false;
        }
        if (id == Material.LADDER.getId()) {
            return true;
        }
        // The direct way is a problem (backwards compatibility to before 1.4.5-R1.0).
        if ((blockFlags[cache.getTypeId(x + 1, y, z)] & F_SOLID) != 0) {
            return true;
        }
        if ((blockFlags[cache.getTypeId(x - 1, y, z)] & F_SOLID) != 0) {
            return true;
        }
        if ((blockFlags[cache.getTypeId(x, y, z + 1)] & F_SOLID) != 0) {
            return true;
        }
        if ((blockFlags[cache.getTypeId(x, y, z - 1)] & F_SOLID) != 0) {
            return true;
        }
        return false;
    }

    /**
     * Checks if is climbable.
     *
     * @param id
     *            the id
     * @return true, if is climbable
     */
    public static final boolean isClimbable(final int id) {
        return (blockFlags[id] & F_CLIMBABLE) != 0;
    }

    /**
     * Climbable material that needs to be attached to a block, to allow players
     * to climb up.<br>
     * Currently only applies to vines. There is no flag for such, yet.
     *
     * @param id
     *            the id
     * @return true, if is attached climbable
     */
    public static final boolean isAttachedClimbable(final int id) {
        return id == Material.VINE.getId();
    }

    /**
     * Checks if is stairs.
     *
     * @param id
     *            the id
     * @return true, if is stairs
     */
    public static final boolean isStairs(final int id) {
        return (blockFlags[id] & F_STAIRS) != 0;
    }

    /**
     * Checks if is liquid.
     *
     * @param blockType
     *            the block type
     * @return true, if is liquid
     */
    public static final boolean isLiquid(final Material blockType) {
        return isLiquid(blockType.getId());
    }

    /**
     * Checks if is liquid.
     *
     * @param id
     *            the id
     * @return true, if is liquid
     */
    public static final boolean isLiquid(final int id) {
        return (blockFlags[id] & F_LIQUID) != 0;
    }

    /**
     * Checks if is ice.
     *
     * @param id
     *            the id
     * @return true, if is ice
     */
    public static final boolean isIce(final int id) {
        return (blockFlags[id] & F_ICE) != 0;
    }

    /**
     * Checks if is leaves.
     *
     * @param id
     *            the id
     * @return true, if is leaves
     */
    public static final boolean isLeaves(final int id) {
        return (blockFlags[id] & F_LEAVES) != 0;
    }

    /**
     * Might hold true for liquids too.
     *
     * @param blockType
     *            the block type
     * @return true, if is solid
     */
    public static final boolean isSolid(final Material blockType) {
        return isSolid(blockType.getId());
    }

    /**
     * Might hold true for liquids too.
     *
     * @param id
     *            the id
     * @return true, if is solid
     */
    public static final boolean isSolid(final int id) {
        return (blockFlags[id] & F_SOLID) != 0;
    }

    /**
     * Might hold true for liquids too.
     *
     * @param blockType
     *            the block type
     * @return true, if is ground
     */
    public static final boolean isGround(final Material blockType) {
        return isGround(blockType.getId());
    }

    /**
     * Might hold true for liquids too.
     *
     * @param id
     *            the id
     * @return true, if is ground
     */
    public static final boolean isGround(final int id) {
        return (blockFlags[id] & F_GROUND) != 0;
    }

    /**
     * Convenience method including null checks.
     *
     * @param type
     *            the type
     * @return true, if is air
     */
    public static final boolean isAir(Material type) {
        return type == null || type == Material.AIR;
    }

    /**
     * Convenience method including null checks.
     *
     * @param stack
     *            the stack
     * @return true, if is air
     */
    public static final boolean isAir(ItemStack stack) {
        return stack == null || isAir(stack.getType());
    }

    /**
     * Get the facing direction as BlockFace, unified approach with attached to
     * = opposite of facing. Likely the necessary flags are just set where it's
     * been needed so far.
     *
     * @param flags
     *            the flags
     * @param data
     *            the data
     * @return Return null, if facing can not be determined.
     */
    public static final BlockFace getFacing(final long flags, final int data) {
        if ((flags & F_FACING_LOW3D2_NSWE) != 0L) {
            switch(data & 7) {
                case 3:
                    return BlockFace.SOUTH;
                case 4:
                    return BlockFace.WEST;
                case 5:
                    return BlockFace.EAST;
                default: // 2 and invalid states.
                    return BlockFace.NORTH;
            }

        }
        else if ((flags & F_ATTACHED_LOW2_SNEW) != 0L) {
            switch (data & 3) {
                case 0:
                    return BlockFace.NORTH; // Attached to BlockFace.SOUTH;
                case 1:
                    return BlockFace.SOUTH; // Attached to BlockFace.NORTH;
                case 2:
                    return BlockFace.WEST; // Attached to BlockFace.EAST;
                case 3:
                    return BlockFace.EAST; // Attached to BlockFace.WEST;
            }
        }
        return null;
    }

    /**
     * Special case: Trap door above ladder attached to lower and of block, same
     * facing. Thus the trap door can be climbed up like a ladder.<br>
     * Suggested fast precondition checks are for nearby flags covering this and
     * below:
     * <ul>
     * <li>F_PASSABLE_X4 (trap door at these coordinates).</li>
     * <li>F_CLIMBABLE (ladder below).</li>
     * </ul>
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is trap door above ladder special case
     */
    public static final boolean isTrapDoorAboveLadderSpecialCase(final BlockCache access, final int x, final int y, final int z) {
        // Special case activation.
        if (!isSpecialCaseTrapDoorAboveLadder()) {
            return false;
        }
        // Basic flags and facing for trap door.
        final long flags1 = blockFlags[access.getTypeId(x, y, z)];
        if ((flags1 & F_PASSABLE_X4) == 0) {
            return false;
        }
        // TODO: Really confine to trap door types (add a flag or something else)?
        final int data1 = access.getData(x, y, z);
        // (Trap door may be attached to top or bottom, regardless.)
        // Trap door must be open (really?).
        if ((data1 & 0x04) != 0x04) {
            return false;
        }
        // Need the facing direction.
        final BlockFace face1 = getFacing(flags1, data1);
        if (face1 == null) {
            return false;
        }
        // Basic flags and facing for ladder.
        final int belowId = access.getTypeId(x, y - 1, z);
        // Really confine to ladder here.
        if (belowId != getId(Material.LADDER)) {
            return false;
        }
        final long flags2 = blockFlags[belowId];
        // (Type has already been checked.)
        //if ((flags2 & F_CLIMBABLE) == 0) {
        //    return false;
        //}
        final int data2 = access.getData(x, y - 1, z);
        final BlockFace face2 = getFacing(flags2, data2);
        // Compare faces.
        if (face1 != face2) {
            return false;
        }
        return true;
    }

    /**
     * Just check if a position is not inside of a block that has a bounding
     * box.<br>
     * This is an inaccurate check, it also returns false for doors etc.
     *
     * @param blockType
     *            the block type
     * @return true, if is passable
     */
    public static final boolean isPassable(final Material blockType) {
        return isPassable(blockType.getId());
    }

    /**
     * Just check if a block type is fully passable by default.<br>
     * This is an inaccurate check, it also returns false for doors etc.
     *
     * @param id
     *            the id
     * @return true, if is passable
     */
    public static final boolean isPassable(final int id) {
        final long flags = blockFlags[id];
        // TODO: What with non-solid blocks that are not passable ?
        if ((flags & (F_LIQUID | F_IGN_PASSABLE)) != 0) {
            return true;
        }
        else {
            return (flags & F_SOLID) == 0;
        }
    }

    /**
     * All rail types a minecart can move on.
     *
     * @param id
     *            the id
     * @return true, if is rails
     */
    public static final boolean isRails(final int id) {
        return (blockFlags[id] & F_RAILS) != 0; 
    }

    /**
     * Test if the id is rails and if data means ascending.
     *
     * @param id
     *            the id
     * @param data
     *            the data
     * @return true, if is ascending rails
     */
    public static final boolean isAscendingRails(final int id, final int data) {
        // TODO: Configurable magic.
        return isRails(id) && (data & 7) > 1;
    }

    /**
     * Test if a position can be passed through (collidesBlock + passable test,
     * no fences yet).<br>
     * NOTE: This is experimental.
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @return true, if is passable
     */
    public static final boolean isPassable(final BlockCache access, final double x, final double y, final double z, final int id) {
        // Simple exclusion check first.
        if (isPassable(id)) {
            return true;
        }
        // Check if the position is inside of a bounding box.
        final int bx = Location.locToBlock(x);
        final int by = Location.locToBlock(y);
        final int bz = Location.locToBlock(z);
        final double[] bounds = access.getBounds(bx, by, bz);
        if (bounds == null || !collidesBlock(access, x, y, z, x, y, z, bx, by, bz, id, bounds, blockFlags[id])) {
            return true;
        }

        final double fx = x - bx;
        final double fy = y - by;
        final double fz = z - bz;
        // TODO: Check f_itchy if/once exists.
        // Check workarounds (blocks with bigger collision box but passable on some spots).
        if (!isPassableWorkaround(access, bx, by, bz, fx, fy, fz, id, 0, 0, 0, 0)) {
            // Not passable.
            return false;
        }
        return true;
    }

    /**
     * Checking the block below to account for fences and such. This must be
     * called extra to isPassable(...).
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is passable h150
     */
    public static final boolean isPassableH150(final BlockCache access, final double x, final double y, final double z) {
        // Check for fences.
        final int by = Location.locToBlock(y) - 1;
        final double fy = y - by;
        if (fy >= 1.5) {
            return true;
        }
        final int bx = Location.locToBlock(x);
        final int bz = Location.locToBlock(z);
        final int belowId = access.getTypeId(bx, by, bz);
        final long belowFlags = blockFlags[belowId]; 
        if ((belowFlags & F_HEIGHT150) == 0 || isPassable(belowId)) {
            return true;
        }
        final double[] belowBounds = access.getBounds(bx, by, bz);
        if (belowBounds == null) {
            return true;
        }
        if (!collidesBlock(access, x, y, z, x, y, z, bx, by, bz, belowId, belowBounds, belowFlags)) {
            return true;
        }
        final double fx = x - bx;
        final double fz = z - bz;
        return isPassableWorkaround(access, bx, by, bz, fx, fy, fz, belowId, 0, 0, 0, 0);
    }

    /**
     * Check if passable, including blocks with height 1.5.
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @return true, if is passable exact
     */
    public static final boolean isPassableExact(final BlockCache access, final double x, final double y, final double z, final int id) {
        return isPassable(access, x, y, z, id) && isPassableH150(access, x, y, z);
    }

    /**
     * Check if passable, including blocks with height 1.5.
     *
     * @param access
     *            the access
     * @param loc
     *            the loc
     * @return true, if is passable exact
     */
    public static final boolean isPassableExact(final BlockCache access, final Location loc) {
        return isPassableExact(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }


    /**
     * Requires the hit-box of the block is hit (...): this checks for special
     * blocks properties such as glass panes and similar.<br>
     * Ray-tracing version for passable-workarounds.
     *
     * @param access
     *            the access
     * @param bx
     *            Block-coordinates.
     * @param by
     *            the by
     * @param bz
     *            the bz
     * @param fx
     *            Offset from block-coordinates in [0..1].
     * @param fy
     *            the fy
     * @param fz
     *            the fz
     * @param id
     *            Type-id of the block.
     * @param dX
     *            Total ray distance for coordinated (see dT).
     * @param dY
     *            the d y
     * @param dZ
     *            the d z
     * @param dT
     *            Time to cover from given position in [0..1], relating to dX,
     *            dY, dZ.
     * @return true, if is passable workaround
     */
    public static final boolean isPassableWorkaround(final BlockCache access, final int bx, final int by, final int bz, final double fx, final double fy, final double fz, final int id, final double dX, final double dY, final double dZ, final double dT) {
        // Note: Since this is only called if the bounding box collides, out-of-bounds checks should not be necessary.
        // TODO: Add a flag if a workaround exists (!), might store the type of workaround extra (generic!), or extra flags.
        final long flags = blockFlags[id];
        if ((flags & F_STAIRS) != 0) {
            if ((access.getData(bx, by, bz) & 0x4) != 0) {
                if (Math.max(fy, fy + dY * dT) < 0.5) {
                    return true;
                }
            }
            else {
                // what with >= 1?
                if (Math.min(fy, fy + dY * dT) >= 0.5) {
                    return true;
                }
            }
        }
        else if (id == Material.SOUL_SAND.getId()) {
            if (Math.min(fy, fy + dY * dT) >= 0.875) {
                return true; // 0.125
            }
        }
        else if ((flags & F_PASSABLE_X4) != 0 && (access.getData(bx, by, bz) & 0x4) != 0) {
            // (Allow checking further entries.)
            return true; 
        }
        else if ((flags & F_THICK_FENCE) != 0) {
            if (!collidesFence(fx, fz, dX, dZ, dT, 0.125)) {
                return true;
            }
        }
        else if ((flags & F_THIN_FENCE) != 0) {
            if (!collidesFence(fx, fz, dX, dZ, dT, 0.05)) {
                return true;
            }
        }
        else if (id == Material.CAKE_BLOCK.getId()) {
            if (Math.min(fy, fy + dY * dT) >= 0.4375) {
                return true; // 0.0625 = 0.125 / 2
            }
        }
        else if (id == Material.CAULDRON.getId()) {
            if (Math.min(fy, fy + dY * dT) >= 0.3125) {
                // Check for moving through walls or floor.
                // TODO: Maybe this is too exact...
                return isInsideCenter(fx, fz, dX, dZ, dT, 0.125);
            }
        }
        else if (id == Material.CACTUS.getId()) {
            if (Math.min(fy, fy + dY * dT) >= 0.9375) {
                return true;
            }
            return !collidesCenter(fx, fz, dX, dZ, dT, 0.0625);
        }
        else if (id == Material.PISTON_EXTENSION.getId()) {
            if (Math.min(fy, fy + dY * dT) >= 0.625) {
                return true;
            }
        }
        else if ((flags & F_GROUND_HEIGHT) != 0 
                && getGroundMinHeight(access, bx, by, bz, id, access.getBounds(bx, by, bz), flags) <= Math.min(fy, fy + dY * dT)) {
            return true;
        }
        // Nothing found.
        return false;
    }

    /**
     * XZ-collision check for (bounds / pseudo-ray) with fence type blocks
     * (fences, glass panes), margin configurable.
     *
     * @param fx
     *            the fx
     * @param fz
     *            the fz
     * @param dX
     *            the d x
     * @param dZ
     *            the d z
     * @param dT
     *            the d t
     * @param d
     *            Distance to the fence middle to keep (see code of
     *            isPassableworkaround for reference).
     * @return true, if successful
     */
    public static boolean collidesFence(final double fx, final double fz, final double dX, final double dZ, final double dT, final double d) {
        final double dFx = 0.5 - fx;
        final double dFz = 0.5 - fz;
        if (Math.abs(dFx) > d && Math.abs(dFz) > d) {
            // Check moving between quadrants.
            final double dFx2 = 0.5 - (fx + dX * dT);
            final double dFz2 = 0.5 - (fz + dZ * dT);
            if (Math.abs(dFx2) > d && Math.abs(dFz2) > d) {
                if (dFx * dFx2 > 0.0 && dFz * dFz2 > 0.0) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Collision for x-z ray / bounds. Use this to check if a box is really
     * outside.
     *
     * @param fx
     *            the fx
     * @param fz
     *            the fz
     * @param dX
     *            the d x
     * @param dZ
     *            the d z
     * @param dT
     *            the d t
     * @param inset
     *            the inset
     * @return False if no collision with the center bounds.
     */
    public static final boolean collidesCenter(final double fx, final double fz, final double dX, final double dZ, final double dT, final double inset) {
        final double low = inset;
        final double high = 1.0 - inset;
        final double xEnd = fx + dX * dT;
        if (xEnd < low && fx < low) {
            return false;
        }
        else if (xEnd >= high && fx >= high) {
            return false;
        }
        final double zEnd = fz + dZ * dT;
        if (zEnd < low && fz < low) {
            return false;
        }
        else if (zEnd >= high && fz >= high) {
            return false;
        }
        return true;
    }

    /**
     * Collision for x-z ray / bounds. Use this to check if a box is really
     * inside.
     *
     * @param fx
     *            the fx
     * @param fz
     *            the fz
     * @param dX
     *            the d x
     * @param dZ
     *            the d z
     * @param dT
     *            the d t
     * @param inset
     *            the inset
     * @return True if the box is really inside of the center bounds.
     */
    public static final boolean isInsideCenter(final double fx, final double fz, final double dX, final double dZ, final double dT, final double inset) {
        final double low = inset;
        final double high = 1.0 - inset;
        final double xEnd = fx + dX * dT;
        if (xEnd < low || fx < low) {
            return false;
        }
        else if (xEnd >= high || fx >= high) {
            return false;
        }
        final double zEnd = fz + dZ * dT;
        if (zEnd < low || fz < low) {
            return false;
        }
        else if (zEnd >= high || fz >= high) {
            return false;
        }
        return true;
    }

    /**
     * Reference block height for on-ground judgment: player must be at this or
     * greater height to stand on this block.<br>
     * <br>
     * TODO: Check naming convention, might change to something with max ...
     * volatile! <br>
     * This might return 0 or somewhat arbitrary values for some blocks that
     * don't have full bounds (!), might return 0 for blocks with the
     * F_GROUND_HEIGHT flag, unless they are treated individually here.
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @param bounds
     *            the bounds
     * @param flags
     *            Flags for this block.
     * @return the ground min height
     */
    public static double getGroundMinHeight(final BlockCache access, final int x, final int y, final int z, final int id, final double[] bounds, final long flags) {
        // TODO: Check which ones are really needed !
        if ((flags & F_HEIGHT_8SIM_INC) != 0) {
            final int data = (access.getData(x, y, z) & 0xF) % 8;
            if (data < 3) {
                return 0;
            }
            else {
                return 0.5;
            }
        }
        else if ((flags & F_HEIGHT_8_INC) != 0) {
            final int data = (access.getData(x, y, z) & 0xF) % 8;
            return 0.125 * (double) data;
        }
        // Height 100 is ignored (!).
        else if ((flags & F_HEIGHT150) != 0) {
            return 1.5;
        }
        else if ((flags & F_STAIRS) != 0) {
            if ((access.getData(x, y, z) & 0x4) != 0) {
                return 1.0;
            }
            else {
                // what with >= 1?
                return 0.5;
            }
        }
        else if ((flags & F_THICK_FENCE) != 0) {
            return Math.min(1.0, bounds[4]);
        }
        else if (id == Material.SOUL_SAND.getId()) {
            return 0.875;
        }
        //		else if (id == Material.CAKE_BLOCK.getId()) {
        //			return 0.4375;
        //		}
        else if (id == Material.CAULDRON.getId()) {
            // TODO: slightly over 0.
            return 0.3125;
        }
        else if (id == Material.CACTUS.getId()) {
            return 0.9375;
        }
        else if (id == Material.PISTON_EXTENSION.getId()) {
            return 0.625;
        }
        else if (id == Material.ENDER_PORTAL_FRAME.getId()) {
            // Allow moving as if no eye was inserted.
            return 0.8125;
        }
        else if ((flags & F_GROUND_HEIGHT) != 0) {
            // Default height is used.
            if (id == Material.SOIL.getId()) {
                return bounds[4];
            }
            // Assume open gates/trapdoors/things to only allow standing on to, if at all.
            if ((flags & F_PASSABLE_X4) != 0 && (access.getData(x, y, z) & 0x04) != 0) {
                return bounds[4];
            }
            // All blocks that are not treated individually are ground all through.
            return 0;
        }
        else {
            // Nothing found.
            // TODO: Consider using Math.min(1.0, bounds[4]) for compatibility rather?
            return bounds[4];
        }
    }

    /**
     * Convenience method for debugging purposes.
     *
     * @param loc
     *            the loc
     * @return true, if is passable
     */
    public static final boolean isPassable(final PlayerLocation loc) {
        return isPassable(loc.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), loc.getTypeId());
    }

    /**
     * Convenience method.
     *
     * @param loc
     *            the loc
     * @return true, if is passable
     */
    public static final boolean isPassable(final Location loc) {
        return isPassable(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ());
    }

    /**
     * Convenience method.
     *
     * @param world
     *            the world
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if is passable
     */
    public static final boolean isPassable(final World world, final double x, final double y, final double z) {
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(world);
        boolean res = isPassable(blockCache, x, y, z, blockCache.getTypeId(x, y, z));
        blockCache.cleanup();
        return res;
    }

    /**
     * Normal ray tracing.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @return true, if is passable
     */
    public static final boolean isPassable(final Location from, final Location to) {
        return isPassable(rtRay, from, to);
    }

    /**
     * Axis-wise checking, like the client does.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @return true, if is passable axis wise
     */
    public static final boolean isPassableAxisWise(final Location from, final Location to) {
        return isPassable(rtAxis, from, to);
    }

    /**
     * Checks if is passable.
     *
     * @param rt
     *            the rt
     * @param from
     *            the from
     * @param to
     *            the to
     * @return true, if is passable
     */
    private static boolean isPassable(final ICollidePassable rt, final Location from, final Location to) {
        final BlockCache blockCache = wrapBlockCache.getBlockCache();
        blockCache.setAccess(from.getWorld());
        rt.setMaxSteps(60); // TODO: Configurable ?
        rt.setBlockCache(blockCache);
        rt.set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
        rt.loop();
        final boolean collides = rt.collides();
        blockCache.cleanup();
        rt.cleanup();
        return !collides;
    }

    /**
     * Convenience method to allow using an already fetched or prepared
     * IBlockAccess.
     *
     * @param access
     *            the access
     * @param loc
     *            the loc
     * @return true, if is passable
     */
    public static final boolean isPassable(final BlockCache  access, final Location loc)
    {
        return isPassable(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
    }

    /**
     * API access to read extra properties from files.
     *
     * @param config
     *            the config
     * @param pathPrefix
     *            the path prefix
     */
    public static void applyConfig(final RawConfigFile config, final String pathPrefix) {

        // Allow instant breaking.
        for (final String input : config.getStringList(pathPrefix + ConfPaths.SUB_ALLOWINSTANTBREAK)) {
            final Integer id = RawConfigFile.parseTypeId(input);
            if (id == null || id < 0 || id >= 4096) {
                StaticLog.logWarning("Bad block id (" + pathPrefix + ConfPaths.SUB_ALLOWINSTANTBREAK + "): " + input);
            }
            else {
                setBlockProps(id, instantType);
            }
        }

        // Override block flags.
        ConfigurationSection section = config.getConfigurationSection(pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS);
        if (section != null) {
            final Map<String, Object> entries = section.getValues(false);
            boolean hasErrors = false;
            for (final Entry<String, Object> entry : entries.entrySet()) {
                final String key = entry.getKey();
                final Integer id = RawConfigFile.parseTypeId(key);
                if (id == null || id < 0 || id >= 4096) {
                    StaticLog.logWarning("Bad block id (" + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + "): " + key);
                    continue;
                }
                final Object obj = entry.getValue();
                if (!(obj instanceof String)) {
                    StaticLog.logWarning("Bad flags at " + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + " for key: " + key);
                    hasErrors = true;
                    continue;
                }
                final Collection<String> split = StringUtil.split((String) obj, ' ', ',', '/', '|', '+', ';', '\t');
                long flags = 0;
                boolean error = false;
                for (String input : split) {
                    input = input.trim();
                    if (input.isEmpty()) {
                        continue;
                    }
                    else if (input.equalsIgnoreCase("default")) {
                        flags |= blockFlags[id];
                        continue;
                    }
                    try{
                        flags |= parseFlag(input);
                    } catch(InputMismatchException e) {
                        StaticLog.logWarning("Bad flag at " + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + " for key " + key + " (skip setting flags for this block): " + input);
                        error = true;
                        hasErrors = true;
                        break;
                    }
                }
                if (error) {
                    continue;
                }
                blockFlags[id] = flags;
            }
            if (hasErrors) {
                StaticLog.logInfo("Overriding block-flags was not entirely successful, all available flags: \n" + StringUtil.join(flagNameMap.values(), "|"));
            }
        }
    }

    /**
     * Test if the bounding box overlaps with a block of given flags (does not
     * check the blocks bounding box).
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param flags
     *            Block flags (@see
     *            fr.neatmonster.nocheatplus.utilities.BlockProperties).
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags) {
        return hasAnyFlags(access, Location.locToBlock(minX), Location.locToBlock(minY), Location.locToBlock(minZ), Location.locToBlock(maxX), Location.locToBlock(maxY), Location.locToBlock(maxZ), flags);
    }


    /**
     * Test if the bounding box overlaps with a block of given flags (does not
     * check the blocks bounding box).
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param flags
     *            Block flags (@see
     *            fr.neatmonster.nocheatplus.utilities.BlockProperties).
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final BlockCache access,final int minX, int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final long flags) {
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    if ((blockFlags[access.getTypeId(x, y, z)] & flags) != 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Test if the box collide with any block that matches the flags somehow.
     * Convenience method.
     *
     * @param access
     *            the access
     * @param bounds
     *            'Classic' bounds as returned for blocks (minX, minY, minZ,
     *            maxX, maxY, maxZ).
     * @param flags
     *            The flags to match.
     * @return true, if successful
     */
    public static final boolean collides(final BlockCache access, double[] bounds,  final long flags) {
        return collides(access, bounds[0], bounds[1], bounds[2], bounds[3], bounds[4], bounds[5], flags);
    }

    /**
     * Test if the box collide with any block that matches the flags somehow.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param flags
     *            The flags to match.
     * @return true, if successful
     */
    public static final boolean collides(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags) {
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        // At least find fences etc. if searched for.
        // TODO: F_HEIGHT150 could also be ground etc., more consequent might be to always use or flag it.
        final int iMinY = Location.locToBlock(minY - ((flags & F_HEIGHT150) != 0 ? 0.5625 : 0));
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    final int id = access.getTypeId(x, y, z);
                    final long cFlags = blockFlags[id];
                    if ((cFlags & flags) != 0) {
                        // Might collide.
                        final double[] bounds = access.getBounds(x, y, z);
                        if (bounds != null) {
                            if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, cFlags)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Convenience method for Material instead of block id.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param mat
     *            the mat
     * @return true, if successful
     */
    public static final boolean collidesId(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final Material mat) {
        return collidesId(access, minX, minY, minZ, maxX, maxY, maxZ, mat.getId());
    }

    /**
     * Check if a block with the given id is overlapping with the bounds, this
     * does not check the blocks actual bounds (All bounds of the block are seen
     * as 0...1, for exact box match use collidesBlock).
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param id
     *            the id
     * @return true, if successful
     */
    public static final boolean collidesId(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int id) {
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY - ((blockFlags[id] & F_HEIGHT150) != 0 ? 0.5625 : 0));
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    if (id == access.getTypeId(x, y, z)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the given bounding box collides with a block of the given id,
     * taking into account the actual bounds of the block.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param id
     *            the id
     * @return true, if successful
     */
    public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int id) {
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY - ((blockFlags[id] & F_HEIGHT150) != 0 ? 0.5625 : 0));
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    if (id == access.getTypeId(x, y, z)) {
                        final double[] bounds = access.getBounds(x, y, z);
                        if (bounds != null) {
                            if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, blockFlags[id])) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Check if the bounds collide with the block that has the given type id at
     * the given position. <br>
     * Delegates: This method signature is not used internally anymore.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @return true, if successful
     */
    public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int x, final int y, final int z, final int id) {
        // TODO: use internal block data unless delegation wanted?
        final double[] bounds = access.getBounds(x,y,z);
        if (bounds == null) {
            return false; // TODO: policy ?
        }
        final long flags = blockFlags[id];
        return collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags);
    }

    /**
     * Check if the bounds collide with the block for the given type id at the
     * given position. This does not check workarounds for ground_height nor
     * passable.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @param bounds
     *            Not null: bounds of the block at x, y, z.
     * @param flags
     *            Block flags for the block at x, y, z. Mix in F_COLLIDE_EDGES
     *            to disallow the "high edges" of blocks.
     * @return true, if successful
     */
    public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int x, final int y, final int z, final int id, final double[] bounds, final long flags) {
        final double bminX, bminZ, bminY;
        final double bmaxX, bmaxY, bmaxZ;
        // TODO: Consider a quick shortcut checks flags == F_NORMAL_GROUND
        if ((flags & F_STAIRS) != 0) { // TODO: make this a full block flag ?
            // Mainly for on ground style checks, would not go too well with passable.
            // TODO: change this to something like F_FULLBOX probably.
            bminX = bminY = bminZ = 0D;
            bmaxX = bmaxY = bmaxZ = 1D;
        }
        else {
            // xz-bounds
            if ((flags & F_XZ100) != 0) {
                bminX = bminZ = 0;
                bmaxX = bmaxZ = 1;
            }
            else {
                bminX = bounds[0]; // block.v(); // minX
                bminZ = bounds[2]; // block.z(); // minZ
                bmaxX = bounds[3]; //block.w(); // maxX
                bmaxZ = bounds[5]; //block.A(); // maxZ
            }
            // y-bounds
            if ((flags & F_HEIGHT_8SIM_INC) != 0) {
                // TODO: remove / solve differently ?
                bminY = 0;
                final int data = (access.getData(x, y, z) & 0xF) % 8;
                //        		bmaxY = (double) (1 +  data) / 8.0;
                bmaxY = data < 3 ? 0 : 0.5;
            }
            else if ((flags & F_HEIGHT_8_INC) != 0) {
                bminY = 0;
                final int data = (access.getData(x, y, z) & 0xF) % 8;
                bmaxY = 0.125 * data;
            }
            else if ((flags & F_HEIGHT150) != 0) {
                bminY = 0;
                bmaxY = 1.5;
            }
            else if ((flags & F_HEIGHT100) != 0) {
                bminY = 0;
                bmaxY = 1.0;
            }
            else if ((flags & F_HEIGHT_8SIM_DEC) != 0) {
                bminY = 0;
                final int data = access.getData(x, y, z);
                if ((data & 0x8) == 0) {
                    // This box works for jumping over flowing water.
                    //            		bmaxY = (0.8 - (double) ((data & 0xF) % 8) / 9.8);
                    final int data8 = (data & 0xF) % 8;
                    if (data8 > 4) {
                        // TODO: With block breaking and data8 == 7 this would be too high.
                        bmaxY = 0.5;
                    }
                    else {
                        //bmaxY = 1.0; // - (double) data8 / 9.0;
                        bmaxY = shouldLiquidBelowBeFullHeight(access, x, y + 1, z) ? 1.0 : LIQUID_HEIGHT_LOWERED;
                    }
                }
                else {
                    //bmaxY = 1.0;
                    bmaxY = shouldLiquidBelowBeFullHeight(access, x, y + 1, z) ? 1.0 : LIQUID_HEIGHT_LOWERED;
                }
            }
            else if ((flags & F_HEIGHT8_1) != 0) {
                bminY = 0.0;
                bmaxY = 0.125;
            }
            else if (id == Material.ENDER_PORTAL_FRAME.getId()) {
                // TODO: Test
                // TODO: Other concepts ...
                bminY = 0;
                if ((access.getData(x, y, z) & 0x04) != 0) {
                    bmaxY = 1.0;
                } else {
                    bmaxY = 0.8125;
                }
            }
            else {
                bminY = bounds[1]; // minY
                bmaxY = bounds[4]; // maxY
            }
        }
        // Clearly outside of bounds.
        if (minX > bmaxX + x || maxX < bminX + x
                || minY > bmaxY + y || maxY < bminY + y
                || minZ > bmaxZ + z || maxZ < bminZ + z) {
            return false;
        }
        // Hitting the max-edges (if allowed).
        final boolean allowEdge = (flags & F_COLLIDE_EDGES) == 0;
        if (minX == bmaxX + x && (bmaxX < 1.0 || allowEdge)
                || minY == bmaxY + y && (bmaxY < 1.0 || allowEdge)
                || minZ == bmaxZ + z && (bmaxZ < 1.0 || allowEdge)) {
            return false;
        }
        // Collision.
        return true;
    }

    /**
     * Determine if the liquid block below has full height or not (provided it
     * is max. level).
     *
     * @param access
     *            the access
     * @param x
     *            Coordinates of the block above the liquid block in question.
     * @param y
     *            the y
     * @param z
     *            the z
     * @return true, if successful
     */
    public static boolean shouldLiquidBelowBeFullHeight(final BlockCache access, final int x, final int y, final int z) {
        final int id = access.getTypeId(x, y, z);
        if (isLiquid(id)) {
            return true;
        }
        if (!isSolid(id)) {
            return false;
        }
        final double[] bounds = getCorrectedBounds(x, y, z, id, access.getBounds(x, y, z));
        // TODO: Implement corrected bounds.
        // TODO: Fences ~ test.
        return bounds == null ? true : (bounds[1] == 0.0);
    }

    /**
     * Attempt to return the exact outside bounds, corrected by flags and other.
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @return If changed, a copy is returned, otherwise the original bounds
     *         returned by the BlockCache instance.
     * @deprecated Not yet for real (only used in certain checks/contexts).
     */
    public static double[] getCorrectedBounds(final BlockCache access, final int x, final int y, final int z) {
        return getCorrectedBounds(x, y, z, access.getTypeId(x, y, z), access.getBounds(x, y, z));
    }

    /**
     * Attempt to return the exact outside bounds, corrected by flags and other.
     *
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param id
     *            the id
     * @param bounds
     *            the bounds
     * @return If changed, a copy is returned, otherwise the original array as
     *         given.
     * @deprecated Not yet for real (only used in certain checks/contexts).
     */
    public static double[] getCorrectedBounds(final int x, final int y, final int z, final int id, final double[] bounds) {
        if (bounds == null) {
            return null;
        }
        //final long flags = blockFlags[id];
        // TODO: Consider to change to adaptBounds and to store the adapted bounds already.
        // TODO: IMPLEMENT special bounds, at least: Step + stairs (dy=0.5 for both) + snow.
        return bounds;
    }

    /**
     * Like isOnGround, just with minimum and maximum coordinates in arbitrary
     * order.
     *
     * @param access
     *            the access
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @param xzMargin
     *            Subtracted from minima and added to maxima.
     * @param yBelow
     *            Subtracted from the minimum of y.
     * @param yAbove
     *            Added to the maximum of y.
     * @return true, if is on ground shuffled
     */
    public static final boolean isOnGroundShuffled(final BlockCache access, final double x1, double y1, final double z1, final double x2, final double y2, final double z2, final double xzMargin, final double yBelow, final double yAbove) {
        return isOnGroundShuffled(access, x1, y1, z1, x2, y2, z2, xzMargin, yBelow, yAbove, 0L);
    }

    /**
     * Similar to collides(... , F_GROUND), but also checks the block above
     * (against spider).<br>
     * NOTE: This does not return true if stuck, to check for that use
     * collidesBlock for the players location.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @return true, if is on ground
     */
    public static final boolean isOnGround(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        return isOnGround(access, minX, minY, minZ, maxX, maxY, maxZ, 0L);
    }

    /**
     * Like isOnGround, just with minimum and maximum coordinates in arbitrary
     * order.
     *
     * @param access
     *            the access
     * @param x1
     *            the x1
     * @param y1
     *            the y1
     * @param z1
     *            the z1
     * @param x2
     *            the x2
     * @param y2
     *            the y2
     * @param z2
     *            the z2
     * @param xzMargin
     *            Subtracted from minima and added to maxima.
     * @param yBelow
     *            Subtracted from the minimum of y.
     * @param yAbove
     *            Added to the maximum of y.
     * @param ignoreFlags
     *            the ignore flags
     * @return true, if is on ground shuffled
     */
    public static final boolean isOnGroundShuffled(final BlockCache access, final double x1, double y1, final double z1, final double x2, final double y2, final double z2, final double xzMargin, final double yBelow, final double yAbove, final long ignoreFlags) {
        return isOnGround(access, Math.min(x1, x2) - xzMargin, Math.min(y1, y2) - yBelow, Math.min(z1, z2) - xzMargin, Math.max(x1, x2) + xzMargin, Math.max(y1, y2) + yAbove, Math.max(z1, z2) + xzMargin, ignoreFlags);
    }

    /**
     * Similar to collides(... , F_GROUND), but also checks the block above
     * (against spider).<br>
     * NOTE: This does not return true if stuck, to check for that use
     * collidesBlock for the players location.
     *
     * @param access
     *            the access
     * @param minX
     *            Bounding box coordinates...
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            Meant to be the foot-level.
     * @param maxZ
     *            the max z
     * @param ignoreFlags
     *            Blocks with these flags are not counted as ground.
     * @return true, if is on ground
     */
    public static final boolean isOnGround(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long ignoreFlags) {
        final int maxBlockY = access.getMaxBlockY();
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY - 0.5626);
        if (iMinY > maxBlockY) {
            // TODO: This can be wrong for high yOnGround (0.0626 and higher).
            return false;
        }
        final int iMaxY = Math.min(Location.locToBlock(maxY), maxBlockY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        //    	NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** isOnGround check size: " + ((iMaxX - iMinX + 1) * (iMaxY - iMinY + 1) * (iMaxZ - iMinZ + 1)));
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {

                // TODO: Might move above block check right here.
                // TODO: Given workaround fixes, could go for the much-simplified-top-down-loop once more.

                for (int y = iMaxY; y >= iMinY; y --) {

                    // TODO: Remember the state of the last block below instead of checking the block above.
                    // TODO: Alternatively optimize: block-above-check: use loop up to iMaxY...

                    final int id = access.getTypeId(x, y, z);
                    final long flags = blockFlags[id];


                    // TODO: LIQUID could be a quick return as well.
                    // (IGN_PASSABLE might still allow standing on.)
                    if ((flags & F_GROUND) == 0 || (flags & ignoreFlags) != 0) {
                        continue;
                    }

                    // Might collide.
                    final double[] bounds = access.getBounds(x, y, z);
                    if (bounds == null) {
                        // TODO: Safety check, uncertain.
                        return true;
                    }
                    if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags)) {
                        continue;
                    }

                    // TODO: Make this one work (passable workaround).
                    // Check if the block can be passed through with the bounding box (disregard the ignore flag).
                    if (isPassableWorkaround(access, x, y, z, minX - x, minY - y, minZ - z, id, maxX - minX, maxY - minY, maxZ - minZ, 1.0)) {
                        // Spider !
                        // Not nice but...
                        // TODO: GROUND_HEIGHT: would have to check passable workaround again ?
                        // TODO: height >= ?
                        // TODO: Another concept is needed for the stand-on-passable !
                        // TODO: Add getMinGroundHeight, getMaxGroundHeight.
                        if ((flags & F_GROUND_HEIGHT) == 0 ||  getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - y) {
                            // Don't break, though could for some cases (?), since a block below still can be ground.
                            continue;
                        }
                    }

                    // Don't count as ground if a block contains the foot.
                    // height >= ?
                    if (getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - y) {
                        // Within block, this x and z is no candidate for ground.
                        if (isFullBounds(bounds)) {
                            break;
                        }
                        else {
                            continue; 
                        }
                    }

                    if (maxY - y < 1.0) {
                        // No need to check the block above (half slabs, stairs).
                        return true;
                    }

                    // Check if the block above allows this to be ground. 

                    if (y >= maxBlockY) {
                        // Only air above.
                        return true;
                    }

                    boolean variable = (flags & F_VARIABLE) != 0;

                    // TODO: Keep an eye on this one for exploits.
                    if (y != iMaxY && !variable) {
                        // Ground found and the block above is passable, no need to check above.
                        return true;
                    }
                    // TODO: Else if variable : continue ?
                    // TODO: Highest block is always the foot position, even if just below 1.0, a return true would be ok?

                    final int aboveId = access.getTypeId(x, y + 1, z);
                    final long aboveFlags = blockFlags[aboveId];
                    if ((aboveFlags & F_IGN_PASSABLE) != 0) {
                        // Ignore these (Note for above block check before ground property).
                        // TODO: Should this always apply ?
                        return true;
                    }

                    if ((aboveFlags & F_GROUND) == 0 || (aboveFlags & F_LIQUID) != 0 || (aboveFlags & ignoreFlags) != 0) {
                        return true;
                    }

                    variable |= (aboveFlags & F_VARIABLE) != 0;
                    // Check if it is the same id (walls!) and similar.
                    if (!variable && id == aboveId) {
                        // Exclude stone walls "quickly", can not stand on.
                        if (isFullBounds(bounds)) {
                            break;
                        }
                        else {
                            continue;
                        }
                    }

                    // Check against spider type hacks.
                    final double[] aboveBounds = access.getBounds(x, y + 1, z);
                    if (aboveBounds == null) {
                        return true;
                    }

                    // TODO: 1.49 might be obsolete !
                    if (!collidesBlock(access, minX, minY, minZ, maxX, Math.max(maxY, 1.49 + y), maxZ, x, y + 1, z, aboveId, aboveBounds, aboveFlags)) {
                        return true;
                    }

                    // Check passable workaround without checking ignore flag.
                    if (isPassableWorkaround(access, x, y + 1, z, minX - x, minY - (y + 1), minZ - z, id, maxX - minX, maxY - minY, maxZ - minZ, 1.0)) {
                        return true;
                    }

                    if (isFullBounds(aboveBounds)) {
                        // Can not be ground at this x - z position.
                        break;
                    }

                    // TODO: Is this variable workaround still necessary ? Has this not been tested above already (passable workaround!)
                    // TODO: This might be seen as a violation for many block types.
                    // TODO: More distinction necessary here.
                    if (variable) {
                        // Simplistic hot fix attempt for same type + same shape.
                        // TODO: Needs passable workaround check.
                        if (isSameShape(bounds, aboveBounds)) {
                            // Can not stand on (rough heuristics).
                            // TODO: Test with cactus.
                            break;
                            // continue;
                        }
                        else {
                            return true;
                        }
                    }

                    // Not regarded as ground, 
                    continue;
                }
            }
        }
        return false;
    }

    /**
     * All dimensions 0 ... 1, no null checks.
     *
     * @param bounds
     *            Block bounds: minX, minY, minZ, maxX, maxY, maxZ
     * @return true, if is full bounds
     */
    public static final boolean isFullBounds(final double[] bounds) {
        for (int i = 0; i < 3; i++) {
            if (bounds[i] != 0.0 || bounds[i + 3] != 1.0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the bounds are the same. No null checks.
     *
     * @param bounds1
     *            the bounds1
     * @param bounds2
     *            the bounds2
     * @return Block bounds: minX, minY, minZ, maxX, maxY, maxZ
     */
    public static final boolean isSameShape(final double[] bounds1, final double[] bounds2) {
        // TODO: further exclude simple full shape blocks, or confine to itchy block types
        // TODO: make flags for it.
        // Allow as ground for differing shapes.
        for (int i = 0; i <  6; i++) {
            if (bounds1[i] != bounds2[i]) {
                // Simplistic.
                return false;
            }
        }
        return true;
    }

    /**
     * Check if the move given by from and to is leading downstream. Currently
     * only the x-z move is regarded, no envelope/consistency checks are
     * performed here, such as checking if the block is liquid at all, nor if
     * the move makes sense. performed here.
     *
     * @param from
     *            the from
     * @param to
     *            the to
     * @return true, if is down stream
     */
    public static final boolean isDownStream(final PlayerLocation from, final PlayerLocation to) {
        return isDownStream(from.getBlockCache(), from.getBlockX(), from.getBlockY(), from.getBlockZ(), from.getData(), to.getX() - from.getX(), to.getZ() - from.getZ());
    }

    /**
     * Check if a move determined by xDistance and zDistance is leading down
     * stream.
     *
     * @param access
     *            the access
     * @param x
     *            the x
     * @param y
     *            the y
     * @param z
     *            the z
     * @param data
     *            the data
     * @param dX
     *            the d x
     * @param dZ
     *            the d z
     * @return true, if is down stream
     */
    public static final boolean isDownStream(final BlockCache access, final int x, final int y, final int z, final int data, 
            final double dX, final double dZ) {
        // x > 0 -> south, z > 0 -> west
        if ((data & 0x8) == 0) {
            // not falling.
            if ((dX > 0)) {
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) > data) {
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) < data) {
                    // reverse direction.
                    return true;
                }
            } else if (dX < 0) {
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) > data) {
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) < data) {
                    // reverse direction.
                    return true;
                }
            }
            if (dZ > 0) {
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x, y, z + 1)) && access.getData(x, y, z + 1) > data) {
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x , y, z - 1)) && access.getData(x, y, z - 1) < data) {
                    // reverse direction.
                    return true;
                }
            }
            else if (dZ < 0 ) {
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x, y, z - 1)) && access.getData(x, y, z - 1) > data) {
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x , y, z + 1)) && access.getData(x, y, z + 1) < data) {
                    // reverse direction.
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Collect all flags of blocks touched by the bounds, this does not check
     * versus the blocks bounding box.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @return the long
     */
    public static final long collectFlagsSimple(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ) {
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY);
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        //    	NCPAPIProvider.getNoCheatPlusAPI().getLogManager().debug(Streams.TRACE_FILE, "*** collect flags check size: " + ((iMaxX - iMinX + 1) * (iMaxY - iMinY + 1) * (iMaxZ - iMinZ + 1)));
        long flags = 0;
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    flags |= blockFlags[access.getTypeId(x, y, z)];
                }
            }
        }
        return flags;
    }

    /**
     * Penalty factor for block break duration if under water.
     *
     * @return the break penalty in water
     */
    public static float getBreakPenaltyInWater() {
        return breakPenaltyInWater;
    }

    /**
     * Penalty factor for block break duration if under water.
     *
     * @param breakPenaltyInWater
     *            the new break penalty in water
     */
    public static void setBreakPenaltyInWater(float breakPenaltyInWater) {
        BlockProperties.breakPenaltyInWater = breakPenaltyInWater;
    }

    /**
     * Penalty factor for block break duration if not on ground.
     *
     * @return the break penalty off ground
     */
    public static float getBreakPenaltyOffGround() {
        return breakPenaltyOffGround;
    }

    /**
     * Penalty factor for block break duration if not on ground.
     *
     * @param breakPenaltyOffGround
     *            the new break penalty off ground
     */
    public static void setBreakPenaltyOffGround(float breakPenaltyOffGround) {
        BlockProperties.breakPenaltyOffGround = breakPenaltyOffGround;
    }

    /**
     * Cleanup. Call init() to re-initialize.
     */
    public static void cleanup() {
        pLoc.cleanup();
        pLoc = null;
        wrapBlockCache.cleanup();
        wrapBlockCache = null;
        // TODO: might empty mappings...
    }

    /**
     * Checks if is passable ray.
     *
     * @param access
     *            the access
     * @param blockX
     *            Block location.
     * @param blockY
     *            the block y
     * @param blockZ
     *            the block z
     * @param oX
     *            Origin / offset from block location.
     * @param oY
     *            the o y
     * @param oZ
     *            the o z
     * @param dX
     *            Direction (multiplied by dT to get end point of move).
     * @param dY
     *            the d y
     * @param dZ
     *            the d z
     * @param dT
     *            the d t
     * @return true, if is passable ray
     */
    public static final boolean isPassableRay(final BlockCache access, final int blockX, final int blockY, final int blockZ, final double oX, final double oY, final double oZ, final double dX, final double dY, final double dZ, final double dT) {
        final int id = access.getTypeId(blockX, blockY, blockZ);
        if (BlockProperties.isPassable(id)) {
            return true;
        }
        double[] bounds = access.getBounds(blockX, blockY, blockZ);
        if (bounds == null) {
            return true;
        }

        // Simplified check: Only collision of bounds of the full move is checked.
        final double minX, maxX;
        if (dX < 0.0) {
            minX = dX * dT + oX + blockX;
            maxX = oX + blockX;
        }
        else {
            maxX = dX * dT + oX + blockX;
            minX = oX + blockX;
        }
        final double minY, maxY;
        if (dY < 0.0) {
            minY = dY * dT + oY + blockY;
            maxY = oY + blockY;
        }
        else {
            maxY = dY * dT + oY + blockY;
            minY = oY + blockY;
        }
        final double minZ, maxZ;
        if (dZ < 0.0) {
            minZ = dZ * dT + oZ + blockZ;
            maxZ = oZ + blockZ;
        }
        else {
            maxZ = dZ * dT + oZ + blockZ;
            minZ = oZ + blockZ;
        }
        if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, blockX, blockY, blockZ, id, bounds, blockFlags[id] | F_COLLIDE_EDGES)) {
            // TODO: Might check for fence too, here.
            return true;
        }

        // TODO: Actual ray-collision checking?

        // Check for workarounds.
        // TODO: check f_itchy once exists.
        if (BlockProperties.isPassableWorkaround(access, blockX, blockY, blockZ, oX, oY, oZ, id, dX, dY, dZ, dT)) {
            return true;
        }
        // Does collide (most likely).
        // (Could allow start-end if passable + check first collision time or some estimate.)
        return false;
    }

    /**
     * Check passability with an arbitrary bounding box vs. a block.
     *
     * @param access
     *            the access
     * @param blockX
     *            the block x
     * @param blockY
     *            the block y
     * @param blockZ
     *            the block z
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @return true, if is passable box
     */
    public static final boolean isPassableBox(final BlockCache access, 
            final int blockX, final int blockY, final int blockZ,
            final double minX, final double minY, final double minZ,
            final double maxX, final double maxY, final double maxZ) {
        // TODO: This mostly is copy and paste from isPassableRay.
        final int id = access.getTypeId(blockX, blockY, blockZ);
        if (BlockProperties.isPassable(id)) {
            return true;
        }
        double[] bounds = access.getBounds(blockX, blockY, blockZ);
        if (bounds == null) {
            return true;
        }
        // (Coordinates are already passed in an ordered form.)
        if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, blockX, blockY, blockZ, id, bounds, blockFlags[id] | F_COLLIDE_EDGES)) {
            return true;
        }

        // Check for workarounds.
        // TODO: Adapted to use the version initially intended for ray-tracing. Should have an explicit thing for the box, and let the current ray-tracing variant use that, until THEY implement something real.
        // TODO: check f_itchy once exists.
        if (BlockProperties.isPassableWorkaround(access, blockX, blockY, blockZ, minX - blockX, minY - blockY, minZ - blockZ, id, maxX - minX, maxY - minY, maxZ - minZ, 1.0)) {
            return true;
        }
        // Does collide (most likely).
        return false;
    }

    /**
     * Check if the bounding box collides with a block (passable + accounting
     * for workarounds).
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @return true, if is passable box
     */
    public static final boolean isPassableBox(final BlockCache access, 
            final double minX, final double minY, final double minZ,
            final double maxX, final double maxY, final double maxZ) {
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY);
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    if (!isPassableBox(access, x, y, z, minX, minY, minZ, maxX, maxY, maxZ)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Add the block coordinates that are colliding via a isPassableBox check
     * for the given bounds to the given container.
     *
     * @param access
     *            the access
     * @param minX
     *            the min x
     * @param minY
     *            the min y
     * @param minZ
     *            the min z
     * @param maxX
     *            the max x
     * @param maxY
     *            the max y
     * @param maxZ
     *            the max z
     * @param results
     *            the results
     * @return the int
     */
    public static final int collectInitiallyCollidingBlocks(final BlockCache access, 
            final double minX, final double minY, final double minZ,
            final double maxX, final double maxY, final double maxZ,
            final BlockPositionContainer results) {
        int added = 0;
        final int iMinX = Location.locToBlock(minX);
        final int iMaxX = Location.locToBlock(maxX);
        final int iMinY = Location.locToBlock(minY);
        final int iMaxY = Location.locToBlock(maxY);
        final int iMinZ = Location.locToBlock(minZ);
        final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++) {
            for (int z = iMinZ; z <= iMaxZ; z++) {
                for (int y = iMinY; y <= iMaxY; y++) {
                    if (!isPassableBox(access, x, y, z, minX, minY, minZ, maxX, maxY, maxZ)) {
                        results.addBlockPosition(x, y, z);
                        ++added;
                    }
                }
            }
        }
        return added;
    }

    /**
     * Test for special case activation: trap door is climbable above ladder
     * with distinct facing.
     *
     * @return true, if is special case trap door above ladder
     */
    public static boolean isSpecialCaseTrapDoorAboveLadder() {
        return specialCaseTrapDoorAboveLadder;
    }

    /**
     * Set special case activation: trap door is climbable above ladder with
     * distinct facing.
     *
     * @param specialCaseTrapDoorAboveLadder
     *            the new special case trap door above ladder
     */
    public static void setSpecialCaseTrapDoorAboveLadder(boolean specialCaseTrapDoorAboveLadder) {
        BlockProperties.specialCaseTrapDoorAboveLadder = specialCaseTrapDoorAboveLadder;
    }

}
