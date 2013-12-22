package fr.neatmonster.nocheatplus.utilities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.InputMismatchException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.compat.blocks.BlockPropertiesSetup;
import fr.neatmonster.nocheatplus.compat.blocks.init.vanilla.VanillaBlocksFactory;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.RawConfigFile;
import fr.neatmonster.nocheatplus.config.WorldConfigProvider;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Properties of blocks.
 * 
 * Likely to be added:
 * - reading (all) properties from files.
 * - reading (all) the default properties from a file too.
 *
 */
public class BlockProperties {
	
	/**
	 * @deprecated Will be replaced by a generic way to define tools.
	 * @author mc_dev
	 *
	 */
	public static enum ToolType{
		NONE,
		SWORD,
		SHEARS,
		SPADE,
		AXE,
		PICKAXE,
//		HOE,
	}
	
	/**
	 * @deprecated Will be replaced by a generic way to define tools.
	 * @author mc_dev
	 *
	 */
	public static enum MaterialBase{
		NONE(0, 1f),
		WOOD(1, 2f),
		STONE(2, 4f),
		IRON(3, 6f),
		DIAMOND(4, 8f),
		GOLD(5, 12f);
		/** Index for array. */
		public final int index;
		public final float breakMultiplier;
		private MaterialBase(int index, float breakMultiplier){
			this.index = index;
			this.breakMultiplier = breakMultiplier;
		}
		public static final MaterialBase getById(final int id){
			for (final MaterialBase base : MaterialBase.values()){
				if (base.index == id) return base;
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
		public final ToolType toolType;
		public final MaterialBase materialBase;
		public ToolProps(ToolType toolType, MaterialBase materialBase){
			this.toolType = toolType;
			this.materialBase = materialBase;
		}
		public String toString(){
			return "ToolProps("+toolType + "/"+materialBase+")";
		}
		public void validate() {
			if (toolType == null) throw new IllegalArgumentException("ToolType must not be null.");
			if (materialBase == null) throw new IllegalArgumentException("MaterialBase must not be null");
		}
	}
	
	/**
	 * Properties of a block.
	 * 
	 * @deprecated Will be replaced by a generic way to define tools.
	 */
	public static class BlockProps{
		public final ToolProps tool;
		public final long[] breakingTimes;
		public final float hardness;
		/** Factor 2 = 2 times faster. */
		public final float efficiencyMod;
		public BlockProps(ToolProps tool, float hardness){
			this(tool, hardness, 1);
		}
		public BlockProps(ToolProps tool, float hardness, float efficiencyMod){
			this.tool = tool;
			this.hardness = hardness;
			breakingTimes = new long[6];
			for (int i = 0; i < 6; i++) {
				final float multiplier;
				if (tool.materialBase == null)
					multiplier = 1f;
				else if (i < tool.materialBase.index)
					multiplier = 1f;
				else
					multiplier = MaterialBase.getById(i).breakMultiplier * 3.33f;
				breakingTimes[i] = (long) (1000f * 5f * hardness / multiplier);
			}
			this.efficiencyMod = efficiencyMod;
		}
		public BlockProps(ToolProps tool, float hardness, long[] breakingTimes){
			this(tool, hardness, breakingTimes, 1f);
		}
		public BlockProps(ToolProps tool, float hardness, long[] breakingTimes, float efficiencyMod){
			this.tool = tool;
			this.breakingTimes = breakingTimes;
			this.hardness = hardness;
			this.efficiencyMod = efficiencyMod;
		}
		public String toString(){
			return "BlockProps(" + hardness + " / " + tool.toString() + " / " + Arrays.toString(breakingTimes) + ")";
		}
		public void validate() {
			if (breakingTimes == null) throw new IllegalArgumentException("Breaking times must not be null.");
			if (breakingTimes.length != 6) throw new IllegalArgumentException("Breaking times length must match the number of available tool types (6).");
			if (tool == null)  throw new IllegalArgumentException("Tool must not be null.");
			tool.validate();
		}
	}
	
	protected static final int maxBlocks = 4096; 
	
	/** Properties by block id, might be extended to 4096 later for custom blocks.*/
	protected static final BlockProps[] blocks = new BlockProps[maxBlocks];
	
	/** Map for the tool properties. */
	protected static Map<Integer, ToolProps> tools = new LinkedHashMap<Integer, ToolProps>(50, 0.5f);
	
	/** Breaking time for indestructible materials. */
	public static final long indestructible = Long.MAX_VALUE;
	
	/** Default tool properties (inappropriate tool). */
	public static final ToolProps noTool = new ToolProps(ToolType.NONE, MaterialBase.NONE);
	
	public static final ToolProps woodSword = new ToolProps(ToolType.SWORD, MaterialBase.WOOD);
	
	public static final ToolProps woodSpade = new ToolProps(ToolType.SPADE, MaterialBase.WOOD);
	
	public static final ToolProps woodPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.WOOD);
	
	public static final ToolProps woodAxe = new ToolProps(ToolType.AXE, MaterialBase.WOOD);
	
	public static final ToolProps stonePickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.STONE);

	public static final ToolProps ironPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.IRON);
	
	public static final ToolProps diamondPickaxe = new ToolProps(ToolType.PICKAXE, MaterialBase.DIAMOND);
	
	/** Times for instant breaking. */
	public static final long[] instantTimes = secToMs(0);
	
	public static final long[] leafTimes = secToMs(0.3);
	
	public static long[] glassTimes = secToMs(0.45);
	
	public static final long[] gravelTimes = secToMs(0.9, 0.45, 0.25, 0.15, 0.15, 0.1);
	
	public static long[] railsTimes = secToMs(1.05, 0.55, 0.3, 0.2, 0.15, 0.1);
	
	public static final long[] woodTimes = secToMs(3, 1.5, 0.75, 0.5, 0.4, 0.25);
	
	public static final long[] ironTimes = secToMs(15, 15, 1.15, 0.75, 0.6, 15);
	
	public static final long[] diamondTimes = secToMs(15, 15, 15, 0.75, 0.6, 15);
	
	private static final long[] indestructibleTimes = new long[] {indestructible, indestructible, indestructible, indestructible, indestructible, indestructible}; 

	
	/** Instantly breakable. */ 
	public static final BlockProps instantType = new BlockProps(noTool, 0, instantTimes);
	
	public static final BlockProps glassType = new BlockProps(noTool, 0.3f, glassTimes, 2f);
	
	public static final BlockProps gravelType = new BlockProps(woodSpade, 0.6f, gravelTimes);
	/** Stone type blocks. */
	public static final BlockProps stoneType = new BlockProps(woodPickaxe, 1.5f);

	public static final BlockProps woodType = new BlockProps(woodAxe, 2, woodTimes);
	
	public static final BlockProps brickType = new BlockProps(woodPickaxe, 2);
	
	public static final BlockProps coalType = new BlockProps(woodPickaxe, 3);
	
	public static final BlockProps ironType = new BlockProps(stonePickaxe, 3, ironTimes);
	
	public static final BlockProps diamondType = new BlockProps(ironPickaxe, 3, diamondTimes);
	
	public static final BlockProps goldBlockType = new BlockProps(woodPickaxe, 3, secToMs(15, 7.5, 3.75, 0.7, 0.55, 1.2));
	
	public static final BlockProps ironBlockType = new BlockProps(woodPickaxe, 5, secToMs(25, 12.5, 2.0, 1.25, 0.95, 2.0));
	
	public static final BlockProps diamondBlockType = new BlockProps(woodPickaxe, 5, secToMs(25, 12.5, 6.0, 1.25, 0.95, 2.0));

	public static final BlockProps hugeMushroomType = new BlockProps(woodAxe, 0.2f, secToMs(0.3, 0.15, 0.1, 0.05, 0.05, 0.05));
	
	public static final BlockProps leafType = new BlockProps(noTool, 0.2f, leafTimes);
	
	public static final BlockProps sandType = new BlockProps(woodSpade, 0.5f, secToMs(0.75, 0.4, 0.2, 0.15, 0.1, 0.1));
	
	public static final BlockProps leverType = new BlockProps(noTool, 0.5f, secToMs(0.75));
	
	public static final BlockProps sandStoneType = new BlockProps(woodPickaxe, 0.8f);
	
	public static final BlockProps pumpkinType = new BlockProps(woodAxe, 1, secToMs(1.5, 0.75, 0.4, 0.25, 0.2, 0.15));
	
	public static final BlockProps chestType = new BlockProps(woodAxe, 2.5f, secToMs(3.75, 1.9, 0.95, 0.65, 0.5, 0.35));
	
	public static final BlockProps woodDoorType = new BlockProps(woodAxe, 3.0f, secToMs(4.5, 2.25, 1.15, 0.75, 0.6, 0.4));
	
	public static final BlockProps dispenserType = new BlockProps(woodPickaxe, 3.5f);
	
	public static final BlockProps ironDoorType = new BlockProps(woodPickaxe, 5);
	
	private static final BlockProps indestructibleType = new BlockProps(noTool, -1f, indestructibleTimes);
	
	/** Returned if unknown */
	private static BlockProps defaultBlockProps = instantType;
	
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
	
	private static BlockCache blockCache = null; 
	private static PlayerLocation pLoc = null;
	
    protected static final long[] blockFlags = new long[maxBlocks];
    
    /** Flag position for stairs. */
    public static final long F_STAIRS 		 = 0x1;
    public static final long F_LIQUID 		 = 0x2;
    // TODO: maybe remove F_SOLID use (unless for setting F_GROUND on init).
    /** Minecraft isSolid result. Used for setting ground flag - Subject to change / rename.*/
    public static final long F_SOLID 		 = 0x4;
    /** Compatibility flag: regard this block as passable always. */
    public static final long F_IGN_PASSABLE	 = 0x8;
    public static final long F_WATER         = 0x10;
    public static final long F_LAVA          = 0x20;
    /** Override bounding box: 1.5 blocks high, like fences.<br>
     *  NOTE: This might have relevance for passable later.
     */
    public static final long F_HEIGHT150     = 0x40;
    /** The player can stand on these, sneaking or not. */
    public static final long F_GROUND        = 0x80; // TODO: 
    /** Override bounding box: 1 block height.<br>
     * NOTE: This should later be ignored by passable, rather.
     */
    public static final long F_HEIGHT100	= 0x100;
    /** Climbable like ladder and vine (allow to land on without taking damage). */
    public static final long F_CLIMBABLE	= 0x200;
    /** The block can change shape. This is most likely not 100% accurate... */
    public static final long F_VARIABLE		= 0x400;
//    /** The block has full bounds (0..1), inaccurate! */
//    public static final int F_FULL   		= 0x800;
    /** Block has full xz-bounds. */
    public static final long F_XZ100		= 0x800;
    
    /** This flag indicates that even though a passable workaround, everything above passable height is still ground. */
    public static final long F_GROUND_HEIGHT	= 0x1000;
    
    /** 
     * The height is assumed to decrease from 1.0 with increasing data value from 0 to 0x7, with 0x7 being the lowest.
     * (repeating till 0x15)). 0x8 means falling/full block. This is meant to model flowing water/lava. <br>
     * However the hit-box for collision checks  will be set to 0.5 height or 1.0 height only.
     */
    public static final long F_HEIGHT_8SIM_DEC		= 0x2000;
    
    /**
     * The height is assumed to increase with data value up to 0x7, repeating up to 0x15.<br>
     * However the hit-box for collision checks  will be set to 0.5 height or 1.0 height only,<br>
     * as with the 1.4.x snow levels.
     */
    public static final long F_HEIGHT_8SIM_INC		= 0x4000;
    
    
    /**
     * The height increases with data value (8 heights).<br>
     * This is for MC 1.5 snow levels.
     */
    public static final long F_HEIGHT_8_INC			= 0x8000;
    
    /** All rail types a minecart can move on. */
    public static final long F_RAILS		= 0x10000;
    
    /** ICE */
    public static final long F_ICE 			= 0x20000;
    
    /**
     * Map flag to names.
     */
    private static final Map<Long, String> flagNameMap = new LinkedHashMap<Long, String>();
    /**
     * Map flag name to flag, both names starting with F_... and the name without F_.
     */
    private static final Map<String, Long> nameFlagMap = new LinkedHashMap<String, Long>();
    
    static{
    	// Use reflection to get a flag -> name mapping and vice versa.
    	for (Field field : BlockProperties.class.getDeclaredFields()){
    		String name = field.getName();
    		if (name.startsWith("F_")){
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
     * Initialize blocks and tools properties. This can be called at any time during runtime.
     * @param mcAccess If mcAccess implements BlockPropertiesSetup, mcAccess.setupBlockProperties will be called directly after basic initialization but before the configuration is applied.
     * @param worldConfigProvider
     */
    public static void init(final MCAccess mcAccess, final WorldConfigProvider<?> worldConfigProvider) {
	    blockCache = mcAccess.getBlockCache(null);
	    pLoc = new PlayerLocation(mcAccess, null);
		try{
		    initTools(mcAccess, worldConfigProvider);
		    initBlocks(mcAccess, worldConfigProvider);
		    // Extra hand picked setups.
		    try{
		    	new VanillaBlocksFactory().setupBlockProperties(worldConfigProvider);
		    }
		    catch(Throwable t){
		    	LogUtil.logSevere("[NoCheatPlus] Could not initialize vanilla blocks: " + t.getClass().getSimpleName() + " - " + t.getMessage());
		    	LogUtil.logSevere(t);
		    }
		    // Allow mcAccess to setup block properties.
		    if (mcAccess instanceof BlockPropertiesSetup){
			    try{
			    	((BlockPropertiesSetup) mcAccess).setupBlockProperties(worldConfigProvider);
			    }
			    catch(Throwable t){
			    	LogUtil.logSevere("[NoCheatPlus] McAccess.setupBlockProperties (" + mcAccess.getClass().getSimpleName() + ") could not execute properly: " + t.getClass().getSimpleName() + " - " + t.getMessage());
			    	LogUtil.logSevere(t);
			    }
		    }
		    // TODO: Add registry for further BlockPropertiesSetup instances.
		}
		catch(Throwable t){
			LogUtil.logSevere(t);
		}
    }
	
	private static void initTools(final MCAccess mcAccess, final WorldConfigProvider<?> worldConfigProvider) {
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

    private static void initBlocks(final MCAccess mcAccess, final WorldConfigProvider<?> worldConfigProvider) {
		Arrays.fill(blocks, null);
		///////////////////////////
		// Initalize block flags
		///////////////////////////
		
		// Generic initialization.
		for (int i = 0; i <maxBlocks; i++){
			blockFlags[i] = 0;

			if (mcAccess.isBlockLiquid(i).decide()){
				// TODO: do not set F_GROUND for liquids ?
				blockFlags[i] |= F_LIQUID;
				if (mcAccess.isBlockSolid(i).decide()) blockFlags[i] |= F_SOLID;
			}
			else if (mcAccess.isBlockSolid(i).decide()){
			    blockFlags[i] |= F_SOLID | F_GROUND;
			}
			
		}
		
		// Stairs.
		for (final Material mat : new Material[] { Material.NETHER_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, 
				Material.SMOOTH_STAIRS, Material.BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.WOOD_STAIRS, 
				Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS }) {
			blockFlags[mat.getId()] |= F_STAIRS | F_HEIGHT100 | F_XZ100 | F_GROUND | F_GROUND_HEIGHT; // Set ground too, to be sure.
		}
		
		// Step
		for (final Material mat : new Material[]{
				Material.STEP, Material.WOOD_STEP,
        }){
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
        
        // 1.5 block high.
        for (final Material mat : new Material[]{
                Material.FENCE, Material.FENCE_GATE,
                Material.NETHER_FENCE,
        }){
            blockFlags[mat.getId()] |= F_HEIGHT150;
        }
        
        // Climbable
        for (final Material mat : new Material[]{
                Material.VINE, Material.LADDER,
        }){
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
        }){
            blockFlags[mat.getId()] |= F_GROUND;
        }
        
        // Full block height.
        for (final Material mat : new Material[]{
//        		Material.ENDER_PORTAL_FRAME,
        		Material.BREWING_STAND,
        		Material.PISTON_EXTENSION,
        }){
            blockFlags[mat.getId()] |= F_HEIGHT100;
        }
        
        // Full xz-bounds.
        for (final Material mat : new Material[]{
        		Material.PISTON_EXTENSION,
        }){
            blockFlags[mat.getId()] |= F_XZ100;
        }
        
	    // ICE
        blockFlags[Material.ICE.getId()] |= F_ICE;
        
		// Not ground (!).
		for (final Material mat : new Material[]{
				Material.WALL_SIGN, Material.SIGN_POST,
		}){
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
		}){
			blockFlags[mat.getId()] |= F_IGN_PASSABLE;
		}
		
		// Blocks changing depending on neighbor blocks.
		for (final Material mat : new Material[]{
				Material.FENCE, Material.FENCE_GATE, Material.COBBLE_WALL,
				Material.NETHER_FENCE,
				Material.IRON_FENCE, Material.THIN_GLASS,
				
		}){
			blockFlags[mat.getId()] |= F_VARIABLE;
		}
		// ? Extra flag for COCOA, ANVIL: depends on data value (other issue)
		
		// Flexible ground (height):
		for (final Material mat : new Material[]{
				// Strictly needed (multiple boxes otherwise).
				Material.PISTON_EXTENSION,
				Material.BREWING_STAND,
				// XZ-bounds issues.
				Material.CAKE_BLOCK
				// Already worked around with isPassableWorkaround (kept for dev-reference).
//				Material.ANVIL,
//				Material.SKULL, Material.FLOWER_POT,
//				Material.DRAGON_EGG,
//				Material.COCOA,
		}){
			blockFlags[mat.getId()] |= F_GROUND_HEIGHT;
		}
		
		////////////////////////////////
		// Set block break properties.
		////////////////////////////////
		// Instantly breakable.
		for (final Material mat : instantMat){
			blocks[mat.getId()] = instantType;
		}
		// TODO: Bed is special.
		// Leaf type
		for (Material mat : new Material[]{ 
				Material.LEAVES, Material.BED_BLOCK}){
			blocks[mat.getId()] = leafType;
		}
		// Huge mushroom type (...)
		for (Material mat : new Material[]{ 
				Material.HUGE_MUSHROOM_1, Material.HUGE_MUSHROOM_2,
				Material.VINE, Material.COCOA}){
			blocks[mat.getId()] = hugeMushroomType;
		}
		
		blocks[Material.SNOW.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(0.5, 0.1, 0.05, 0.05, 0.05, 0.05));
		blocks[Material.SNOW_BLOCK.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(1, 0.15, 0.1, 0.05, 0.05, 0.05));
		for (Material mat : new Material[]{ 
				Material.REDSTONE_LAMP_ON, Material.REDSTONE_LAMP_OFF,
				Material.GLOWSTONE, Material.GLASS,
		}){
			blocks[mat.getId()] = glassType;
		}
		blocks[102] = glassType; // glass panes
		blocks[Material.NETHERRACK.getId()] = new BlockProps(woodPickaxe, 0.4f, secToMs(2, 0.3, 0.15, 0.1, 0.1, 0.05));
		blocks[Material.LADDER.getId()] = new BlockProps(noTool, 0.4f, secToMs(0.6), 2.5f);
		blocks[Material.CACTUS.getId()] = new BlockProps(noTool, 0.4f, secToMs(0.6));
		blocks[Material.WOOD_PLATE.getId()] = new BlockProps(woodAxe, 0.5f, secToMs(0.75, 0.4, 0.2, 0.15, 0.1, 0.1));
		blocks[Material.STONE_PLATE.getId()] = new BlockProps(woodPickaxe, 0.5f, secToMs(2.5, 0.4, 0.2, 0.15, 0.1, 0.07));
		blocks[Material.SAND.getId()] = sandType;
		blocks[Material.SOUL_SAND.getId()] = sandType;
		for (Material mat: new Material[]{Material.LEVER, Material.PISTON_BASE, 
				Material.PISTON_EXTENSION, Material.PISTON_STICKY_BASE,
				Material.STONE_BUTTON, Material.PISTON_MOVING_PIECE}){
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
		}){
			blocks[mat.getId()] = gravelType;
		}
		for (Material mat : new Material[]{
				Material.RAILS, Material.POWERED_RAIL, Material.DETECTOR_RAIL,
		}){
			blocks[mat.getId()] = new BlockProps(woodPickaxe, 0.7f, railsTimes);
		}
		blocks[Material.MONSTER_EGGS.getId()] = new BlockProps(noTool, 0.75f, secToMs(1.15)); 
		blocks[Material.WOOL.getId()] = new BlockProps(noTool, 0.8f, secToMs(1.2), 3f);
		blocks[Material.SANDSTONE.getId()] = sandStoneType;
		blocks[Material.SANDSTONE_STAIRS.getId()] = sandStoneType;
		for (Material mat : new Material[]{
				Material.STONE, Material.SMOOTH_BRICK, Material.SMOOTH_STAIRS,
		}){
			blocks[mat.getId()] =  stoneType;
		}
		blocks[Material.NOTE_BLOCK.getId()] = new BlockProps(woodAxe, 0.8f, secToMs(1.2, 0.6, 0.3, 0.2, 0.15, 0.1));
		blocks[Material.WALL_SIGN.getId()] = pumpkinType;
		blocks[Material.SIGN_POST.getId()] = pumpkinType;
		blocks[Material.PUMPKIN.getId()] = pumpkinType;
		blocks[Material.JACK_O_LANTERN.getId()] = pumpkinType;
		blocks[Material.MELON_BLOCK.getId()] = new BlockProps(noTool, 1, secToMs(1.45), 3); // 1.5 but maybe event delay one tick.
		blocks[Material.BOOKSHELF.getId()] = new BlockProps(woodAxe, 1.5f, secToMs(2.25, 1.15, 0.6, 0.4, 0.3, 0.2));
		for (Material mat : new Material[]{
				Material.WOOD_STAIRS, Material.WOOD, Material.WOOD_STEP, Material.LOG,
				Material.FENCE, Material.FENCE_GATE, Material.JUKEBOX,
				Material.JUNGLE_WOOD_STAIRS, Material.SPRUCE_WOOD_STAIRS,
				Material.BIRCH_WOOD_STAIRS, 
				Material.WOOD_DOUBLE_STEP, // ?
				// double slabs ?
		}){
			blocks[mat.getId()] =  woodType;
		}
		for (Material mat : new Material[]{
				Material.COBBLESTONE_STAIRS, Material.COBBLESTONE, 
				Material.NETHER_BRICK, Material.NETHER_BRICK_STAIRS, Material.NETHER_FENCE,
				Material.CAULDRON, Material.BRICK, Material.BRICK_STAIRS,
				Material.MOSSY_COBBLESTONE, Material.BRICK, Material.BRICK_STAIRS,
				Material.STEP, Material.DOUBLE_STEP, // ?
				
		}){
			blocks[mat.getId()] =  brickType;
		}
		blocks[Material.WORKBENCH.getId()] = chestType;
		blocks[Material.CHEST.getId()] = chestType;
		blocks[Material.WOODEN_DOOR.getId()] = woodDoorType;
		blocks[Material.TRAP_DOOR.getId()] = woodDoorType;
		for (Material mat : new Material[]{
				Material.ENDER_STONE, Material.DRAGON_EGG, Material.COAL_ORE,
				
		}){
			blocks[mat.getId()] =  coalType;
		}
		for (Material mat : new Material[]{
				Material.LAPIS_ORE, Material.LAPIS_BLOCK, Material.IRON_ORE,
				
		}){
			blocks[mat.getId()] =  ironType;
		}
		for (Material mat : new Material[]{
				Material.REDSTONE_ORE, Material.GLOWING_REDSTONE_ORE,
				Material.EMERALD_ORE, Material.GOLD_ORE, Material.DIAMOND_ORE,
				
		}){
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
		}){
			blocks[mat.getId()] = ironDoorType;
		}
		blocks[Material.IRON_BLOCK.getId()] = ironBlockType;
		blocks[Material.DIAMOND_BLOCK.getId()] = diamondBlockType;
		blocks[Material.ENDER_CHEST.getId()] = new BlockProps(woodPickaxe, 22.5f);
		blocks[Material.OBSIDIAN.getId()] = new BlockProps(diamondPickaxe, 50, secToMs(250, 250, 250, 250, 9.4, 250));
		
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
		}){
			blocks[mat.getId()] = indestructibleType; 
		}
		blocks[95] = indestructibleType; // Locked chest (prevent crash with 1.7).
	}
	
	public static void dumpBlocks(boolean all) {
		List<String> missing = new LinkedList<String>();
		if (all) {
			LogUtil.logInfo("[NoCheatPlus] Dump block properties for fastbreak check:");
			LogUtil.logInfo("--- Present entries -------------------------------");
		}
		List<String> tags = new ArrayList<String>();
		for (int i = 0; i < blocks.length; i++){
			String mat;
			try{
				Material temp = Material.getMaterial(i);
				if (!temp.isBlock()) continue;
				mat = temp.toString();
			}
			catch(Exception e){
				mat = "?";
			}
			tags.clear();
			addFlagNames(blockFlags [i], tags);
			String tagsJoined = tags.isEmpty() ? "" : " / " + StringUtil.join(tags, "+");
			if (blocks[i] == null){
				if (mat.equals("?")) continue;
				missing.add("* MISSING "+i + "(" + mat + tagsJoined + ") ");
			}
			else if (all) LogUtil.logInfo(i + ": (" + mat + tagsJoined + ") " + blocks[i].toString());
		}
		if (!missing.isEmpty()){
			Bukkit.getLogger().warning("[NoCheatPlus] The block breaking data is incomplete, default to allow instant breaking:");
			LogUtil.logWarning("--- Missing entries -------------------------------");
			for (String spec : missing){
				LogUtil.logWarning(spec);
			}
		}
	}

	/**
	 * Add all flag names for existing default flags to the list.
	 * @param flags
	 * @param tags Flag names will be added to this list (not with the "F_"-prefix).
	 */
	public static void addFlagNames(final long flags, final Collection<String> tags) {
		String tag = flagNameMap.get(flags);
		if (tag != null){
			tags.add(tag);
			return;
		}
		for (final Long flag : flagNameMap.keySet()){
			if ((flags & flag) != 0) tags.add(flagNameMap.get(flag));
		}
	}
	
	/**
	 * Return a collection containing all names of the flags.
	 * @param flags
	 * @return
	 */
	public static Collection<String> getFlagNames(final Long flags) {
		final ArrayList<String> tags = new ArrayList<String>(flagNameMap.size());
		if (flags == null) return tags;
		addFlagNames(flags, tags);
		return tags;
	}
	
	/**
	 * Convenience method to parse a flag.
	 * @param input
	 * @return
	 * @throws InputMismatchException if failed to parse.
	 */
	public static long parseFlag(final String input){
		final String ucInput = input.trim().toUpperCase();
		final Long flag = nameFlagMap.get(ucInput);
		if (flag != null) return flag.longValue();
		try{
			final Long altFlag = Long.parseLong(input);
			return altFlag;
		} catch (NumberFormatException e){}
		// TODO: This very exception type?
		throw new InputMismatchException();
	}

	public static long[] secToMs(final double s1, final double s2, final double s3, final double s4, final double s5, final double s6){
		return new long[] { (long) (s1 * 1000d), (long) (s2 * 1000d), (long) (s3 * 1000d), (long) (s4 * 1000d), (long) (s5 * 1000d), (long) (s6 * 1000d) };
	}
	
	public static long[] secToMs(final double s1){
		final long v = (long) (s1 * 1000d);
		return new long[]{v, v, v, v, v, v};
	}
	
	public static ToolProps getToolProps(final ItemStack stack){
		if (stack == null) return noTool;
		else return getToolProps(stack.getTypeId());
	}
	
	public static ToolProps getToolProps(final Material mat){
		if (mat == null) return noTool;
		else return getToolProps(mat.getId());
	}
	
	public static ToolProps getToolProps(final Integer id){
		final ToolProps props = tools.get(id);
		if (props == null) return noTool;
		else return props;
	}
	
	public static BlockProps getBlockProps(final ItemStack stack){
		if (stack == null) return defaultBlockProps;
		else return getBlockProps(stack.getTypeId());
	}
	
	public static BlockProps getBlockProps(final Material mat){
		if (mat == null) return defaultBlockProps;
		else return getBlockProps(mat.getId());
	}
	
	public static BlockProps getBlockProps(final int blockId){
		if (blockId <0 || blockId >= blocks.length || blocks[blockId] == null) return defaultBlockProps;
		else return blocks[blockId];
	}
	
	/**
	 * Convenience method.
	 * @param blockId
	 * @param player
	 * @return
	 */
	public static long getBreakingDuration(final int blockId, final Player player){
		return getBreakingDuration(blockId, player.getItemInHand(), player.getInventory().getHelmet(), player, player.getLocation());
	}
	
	/**
	 * TODO: repair signature some day (rid of PlayerLocation).
	 * @param BlockId
	 * @param itemInHand May be null.
	 * @param helmet May be null.
	 * @param location The normal location of a player.
	 * @return
	 */
	public static long getBreakingDuration(final int blockId, final ItemStack itemInHand, final ItemStack helmet, final Player player, final Location location){
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
		if ((headFlags & F_WATER) == 0){
			inWater = false;
		}
		else{
			// Check real bounding box collision.
			// (Not sure which to use here.)
			final int data8 = (blockCache.getData(bx, by, bz) & 0xF) % 8;
			final double level;
			if ((data8 & 8) != 0){
				level = 1.0;
			}
			else{
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
	 * Get the normal breaking duration, including enchantments, and tool properties.
	 * @param blockId
	 * @param itemInHand
	 * @return
	 */
	public static long getBreakingDuration(final int blockId, final ItemStack itemInHand, final boolean onGround, final boolean inWater, final boolean aquaAffinity, final int haste){
		// TODO: more configurability / load from file for blocks (i.e. set for shears etc.
		if (itemInHand == null) return getBreakingDuration(blockId, getBlockProps(blockId), noTool, onGround, inWater, aquaAffinity, 0);
		else{
			int efficiency = 0;
			if (itemInHand.containsEnchantment(Enchantment.DIG_SPEED)) efficiency = itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
			return getBreakingDuration(blockId, getBlockProps(blockId), getToolProps(itemInHand.getTypeId()), onGround, inWater, aquaAffinity, efficiency, haste);
		}
	}
	
	    /**
	     * 
	     * @param blockId
	     * @param blockProps
	     * @param toolProps
	     * @param onGround
	     * @param inWater
	     * @param aquaAffinity
	     * @param efficiency
	     * @param haste Amplifier of haste potion effect (assume > 0 for effect there at all, so 1 is haste I, 2 is haste II).
	     * @return
	     */
	public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency, int haste) {
	    final long dur = getBreakingDuration(blockId, blockProps, toolProps, onGround, inWater, aquaAffinity, efficiency);
	    // TODO: haste ...
	    return haste > 0 ? (long) (Math.pow(0.8, haste) * dur): dur;
	}

	public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency) {	
		if (efficiency > 0){
			// Workaround until something better is found..
			if (blockId == Material.LEAVES.getId() || blockProps == glassType){
				/*
				 * TODO: Some might be dealt with by insta break, by now, 
				 * still getting exact durations would be nice to have, for expected breaking times and 
				 * general API use (spin off, analysis?).
				 */
				if (efficiency == 1) return 100;
				else return 0; // insta break.
			}
			else if (blockId == Material.MELON_BLOCK.getId()){
				// 450, 200 , 100 , 50 , 0
				return 450 / (long) Math.pow(2, efficiency - 1); 
			}
			else if (blockProps == chestType){
				// TODO: The no tool time might be reference anyway for some block types.
				return (long) ((double )blockProps.breakingTimes[0] / 5f / efficiency);
			}
		}
		
		long duration;
		
		boolean isValidTool = isValidTool(blockId, blockProps, toolProps, efficiency);
		
		if (isValidTool){
			// appropriate tool
			duration = blockProps.breakingTimes[toolProps.materialBase.index];
			if (efficiency > 0){
				duration = (long) (duration / blockProps.efficiencyMod);
			}
		}
		else{
			// Inappropriate tool.
			duration = blockProps.breakingTimes[0];
			// Swords are always appropriate.
			if (toolProps.toolType == ToolType.SWORD) duration = (long) ((float) duration / 1.5f);
		}
	
		// Specialties:
		if (toolProps.toolType == ToolType.SHEARS){
			// (Note: shears are not in the block props, anywhere)
			// Treat these extra (partly experimental):
			if (blockId == Material.WEB.getId()){
				duration = 400;
				isValidTool = true;
			}
			else if (blockId == Material.WOOL.getId()){
				duration = 240;
				isValidTool = true;
			}
			else if (blockId == Material.LEAVES.getId()){
				duration = 20;
				isValidTool = true;
			}
			else if (blockId == Material.VINE.getId()){
				duration = 300;
				isValidTool = true;
			}
		}
		// (sword vs web already counted)
		else if (blockId == Material.VINE.getId() && toolProps.toolType == ToolType.AXE){
			isValidTool = true;
			if (toolProps.materialBase == MaterialBase.WOOD || toolProps.materialBase == MaterialBase.STONE) duration = 100;
			else duration = 0;
		}
		
		if (isValidTool || blockProps.tool.toolType == ToolType.NONE){
		    float mult = 1f;
			if (inWater && !aquaAffinity) 	mult *= breakPenaltyInWater;
			if (!onGround) 					mult *= breakPenaltyOffGround;
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
				for (int i = 0; i < efficiency; i++){
					duration /= 1.33; // Matches well with obsidian.
				}
				// Formula from MC wiki.
				// TODO: Formula from mc wiki does not match well (too fast for obsidian).
//				duration /= (1.0 + 0.5 * efficiency);
				
				// More Workarounds:
				// TODO: Consider checking a generic workaround (based on duration, assuming some dig packets lost, proportional to duration etc.).
	            if (toolProps.materialBase == MaterialBase.WOOD){
	            	if (toolProps.toolType == ToolType.PICKAXE && (blockProps == ironDoorType || blockProps == dispenserType)){
				    	 // Special correction.
	            		// TODO: Uncomfortable: hide this in the blocks by some flags / other type of workarounds !
	            		if (blockProps == dispenserType){
	            			duration = (long) (duration / 1.5 - (efficiency - 1) * 60);
	            		}
	            		else if (blockProps == ironDoorType){
	            			duration = (long) (duration / 1.5 - (efficiency - 1) * 100);
	            		}
	            	}
	            	else if (blockId == Material.LOG.getId()) duration -= efficiency >= 4 ? 250 : 400;
				    else if (blockProps.tool.toolType == toolProps.toolType) duration -= 250;
				    else duration -= efficiency * 30;
				    
                }
				else if (toolProps.materialBase == MaterialBase.STONE){
				    if (blockId == Material.LOG.getId()) duration -= 100;
                }
			}
		}
		return Math.max(0, duration);
	}
	
	/**
	 * Check if the tool is officially appropriate for the block id, counting in efficiency enchantments.
	 * @param blockId
	 * @param blockProps
	 * @param toolProps
	 * @param efficiency
	 * @return
	 */
	public static boolean isValidTool(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final int efficiency) {
		boolean isValidTool = blockProps.tool.toolType == toolProps.toolType;
		
		if (!isValidTool && efficiency > 0){
			// Efficiency makes the tool.
			// (wood, sand, gravel, ice)
			if (blockId == Material.SNOW.getId()) return toolProps.toolType == ToolType.SPADE;
			if (blockId == Material.WOOL.getId()) return true;
			if (blockId == Material.WOODEN_DOOR.getId()) return true;
			if (blockProps.hardness <= 2 
					&& (blockProps.tool.toolType == ToolType.AXE 
					|| blockProps.tool.toolType == ToolType.SPADE
					|| (blockProps.hardness < 0.8 && (blockId != Material.NETHERRACK.getId() && blockId != Material.SNOW.getId() && blockId != Material.SNOW_BLOCK.getId() && blockId != Material.STONE_PLATE.getId())))){
				// Also roughly.
				return true;
			}
		}
		return isValidTool;
	}

	/**
	 * Access API for setting tool properties.<br>
	 * NOTE: No guarantee that this harmonizes with internals and workarounds, currently.
	 * @param itemId
	 * @param toolProps
	 */
	public static void setToolProps(int itemId, ToolProps toolProps){
		if (toolProps == null) throw new NullPointerException("ToolProps must not be null");
		toolProps.validate();
		// No range check.
		tools.put(itemId, toolProps);
	}
	
	/**
	 * Access API to set a blocks properties.
	 * NOTE: No guarantee that this harmonizes with internals and workarounds, currently.
	 * @param blockId
	 * @param blockProps
	 */
	public static void setBlockProps(int blockId, BlockProps blockProps){
		if (blockProps == null) throw new NullPointerException("BlockProps must not be null");
		blockProps.validate();
		if (blockId < 0 || blockId >= blocks.length) throw new IllegalArgumentException("The blockId is outside of supported range: " + blockId);
		blocks[blockId] = blockProps;
	}


	public static boolean isValidTool(final int blockId, final ItemStack itemInHand) {
		final BlockProps blockProps = getBlockProps(blockId);
		final ToolProps toolProps = getToolProps(itemInHand);
		final int efficiency = itemInHand == null ? 0 : itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
		return isValidTool(blockId, blockProps, toolProps, efficiency);
	}


	public static BlockProps getDefaultBlockProps() {
		return defaultBlockProps;
	}

	/**
	 * Feeding null will cause an npe - will validate. 
	 * @param blockProps
	 */
	public static void setDefaultBlockProps(BlockProps blockProps) {
		blockProps.validate();
		BlockProperties.defaultBlockProps = blockProps;
	}
	
	/**
	 * @deprecated Checks for lava and water and only stationary. Long outdated. Subject to removal.
	 * @param blockId
	 * @return
	 */
	public static boolean isInWater(final int blockId) {
		if (blockId == Material.STATIONARY_WATER.getId() || blockId == Material.STATIONARY_LAVA.getId()) return true;
		// TODO: count in water height ?
		// TODO: lava ?
		return false;
	}

	/**
	 * Simple checking method, heavy. No isIllegal check.
	 * @param player
	 * @param location
	 * @param yOnGround
	 * @return
	 */
	public static boolean isOnGround(final Player player, final Location location, final double yOnGround) {
		// Bit fat workaround, maybe put the object through from check listener ?
		blockCache.setAccess(location.getWorld());
		pLoc.setBlockCache(blockCache);
		pLoc.set(location, player, yOnGround);
		final boolean onGround = pLoc.isOnGround();
		blockCache.cleanup();
		pLoc.cleanup();
		return onGround;
	}
	
	/**
	 * Simple checking method, heavy. No isIllegal check.
	 * @param player
	 * @param location
	 * @param yOnGround
	 * @return
	 */
	public static boolean isOnGroundOrResetCond(final Player player, final Location location, final double yOnGround){
		blockCache.setAccess(location.getWorld());
		pLoc.setBlockCache(blockCache);
		pLoc.set(location, player, yOnGround);
		final boolean res = pLoc.isOnGround() || pLoc.isResetCond();
		blockCache.cleanup();
		pLoc.cleanup();
		return res;
	}

	/**
	 * @deprecated Typo in method name.
	 * @param id
	 * @return
	 */
	public static final long getBLockFlags(final int id){
		return blockFlags[id];
	}
	
	public static final long getBlockFlags(final int id){
		return blockFlags[id];
	}

	public static final void setBlockFlags(final int id, final long flags){
		blockFlags[id] = flags;
	}
	
	/**
	 * Auxiliary check for if this block is climbable and allows climbing up. Does not account for jumping off ground etc.
	 * @param cache
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static final boolean canClimbUp(final BlockCache cache, final int x, final int y, final int z){
		final int id = cache.getTypeId(x, y, z);
		if ((blockFlags[id] & F_CLIMBABLE) == 0) return false;
		if (id == Material.LADDER.getId()) return true;
		// The direct way is a problem (backwards compatibility to before 1.4.5-R1.0).
		if ((blockFlags[cache.getTypeId(x + 1, y, z)] & F_SOLID) != 0) return true;
		if ((blockFlags[cache.getTypeId(x - 1, y, z)] & F_SOLID) != 0) return true;
		if ((blockFlags[cache.getTypeId(x, y, z + 1)] & F_SOLID) != 0) return true;
		if ((blockFlags[cache.getTypeId(x, y, z - 1)] & F_SOLID) != 0) return true;
		return false;
	}
	
	public static final boolean isClimbable(final int id) {
		return (blockFlags[id] & F_CLIMBABLE) != 0;
	}

	public static final boolean isStairs(final int id) {
		return (blockFlags[id] & F_STAIRS) != 0;
	}


	public static final boolean isLiquid(final int id) {
		return (blockFlags[id] & F_LIQUID) != 0;
	}
	
	public static final boolean isIce(final int id) {
		return (blockFlags[id] & F_ICE) != 0;
	}
	
	/**
	 * Might hold true for liquids too.
	 * @param id
	 * @return
	 */
	public static final boolean isSolid(final int id){
		return (blockFlags[id] & F_SOLID) != 0;
	}
	
	/**
     * Might hold true for liquids too.
     * @param id
     * @return
     */
    public static final boolean isGround(final int id){
        return (blockFlags[id] & F_GROUND) != 0;
    }
	
	/**
	 * Just check if a position is not inside of a block that has a bounding box.<br>
	 * This is an inaccurate check, it also returns false for doors etc.
	 * @param id
	 * @return
	 */
	public static final boolean isPassable(final int id){
	    final long flags = blockFlags[id];
		if ((flags & (F_LIQUID | F_IGN_PASSABLE)) != 0) return true;
		else return (flags & F_SOLID) == 0;
	}
	
	/**
	 * All rail types a minecart can move on.
	 * @param id
	 * @return
	 */
	public static final boolean isRails(final int id){
		return (blockFlags[id] & F_RAILS) != 0; 
	}
	
	/**
	 * Test if a position can be passed through (collidesBlock + passable test, no fences yet).<br>
	 * NOTE: This is experimental.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param id
	 * @return
	 */
	public static final boolean isPassable(final BlockCache access, final double x, final double y, final double z, final int id){
		// Simple exclusion check first.
		if (isPassable(id)){
			return true;
		}
		// Check if the position is inside of a bounding box.
		final int bx = Location.locToBlock(x);
		final int by = Location.locToBlock(y);
		final int bz = Location.locToBlock(z);
		final double[] bounds = access.getBounds(bx, by, bz);
		if (bounds == null || !collidesBlock(access, x, y, z, x, y, z, bx, by, bz, id, bounds, blockFlags[id])){
			return true;
		}
		
		final double fx = x - bx;
		final double fy = y - by;
		final double fz = z - bz;
		// TODO: Check f_itchy if/once exists.
		// Check workarounds (blocks with bigger collision box but passable on some spots).
		if (!isPassableWorkaround(access, bx, by, bz, fx, fy, fz, id, 0, 0, 0, 0)){
			// Not passable.
			return false;
		}
		return true;
	}
	
	/**
	 * Checking the block below to account for fences and such. This must be called extra to isPassable(...).
	 * @param access
	 * @param x
	 * @param y
	 * @param z
	 * @param id
	 * @return
	 */
	public static final boolean isPassableH150(final BlockCache access, final double x, final double y, final double z){
		// Check for fences.
		final int by = Location.locToBlock(y) - 1;
		final double fy = y - by;
		if (fy >= 1.5){
			return true;
		}
		final int bx = Location.locToBlock(x);
		final int bz = Location.locToBlock(z);
		final int belowId = access.getTypeId(bx, by, bz);
		final long belowFlags = blockFlags[belowId]; 
		if ((belowFlags & F_HEIGHT150) == 0 || isPassable(belowId)){
			return true;
		}
		final double[] belowBounds = access.getBounds(bx, by, bz);
		if (belowBounds == null){
			return true;
		}
		if (!collidesBlock(access, x, y, z, x, y, z, bx, by, bz, belowId, belowBounds, belowFlags)){
			return true;
		}
		final double fx = x - bx;
		final double fz = z - bz;
		return isPassableWorkaround(access, bx, by, bz, fx, fy, fz, belowId, 0, 0, 0, 0);
	}
	
	/**
	 * Check if passable, including blocks with height 1.5.
	 * @param access
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static final boolean isPassableExact(final BlockCache access, final double x, final double y, final double z, final int id){
		return isPassable(access, x, y, z, id) && isPassableH150(access, x, y, z);
	}
	
	/**
	 * Check if passable, including blocks with height 1.5.
	 * @param access
	 * @param loc
	 * @return
	 */
	public static final boolean isPassableExact(final BlockCache access, final Location loc){
		return isPassableExact(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	
	/**
	 * Requires the hit-box of the block is hit (...): this checks for special blocks properties such as glass panes and similar.<br>
	 * Ray-tracing version for passable-workarounds.
	 * @param access
	 * @param bx Block-coordinates.
	 * @param by
	 * @param bz
	 * @param fx Offset from block-coordinates in [0..1].
	 * @param fy
	 * @param fz
	 * @param id Type-id of the block.
	 * @param dX Total ray distance for coordinated (see dT).
	 * @param dY
	 * @param dZ
	 * @param dT Time to cover from given position in [0..1], relating to dX, dY, dZ.
	 * @return
	 */
	public static final boolean isPassableWorkaround(final BlockCache access, final int bx, final int by, final int bz, final double fx, final double fy, final double fz, final int id, final double dX, final double dY, final double dZ, final double dT){
		// Note: Since this is only called if the bounding box collides, out-of-bounds checks should not be necessary.
		final long flags = blockFlags[id];
		if ((flags & F_STAIRS) != 0){
			if ((access.getData(bx, by, bz) & 0x4) != 0){
				if (Math.max(fy, fy + dY * dT) < 0.5) return true;
			}
			else{
				// what with >= 1?
				if (Math.min(fy, fy + dY * dT) >= 0.5) return true;
			}
		}
		else if (id == Material.SOUL_SAND.getId()){
			if (Math.min(fy, fy + dY * dT) >= 0.875) return true; // 0.125
		}
		else if (id == Material.IRON_FENCE.getId() || id == Material.THIN_GLASS.getId()){
			if (!collidesFence(fx, fz, dX, dZ, dT, 0.05)) return true;
		}
		else if (id == Material.FENCE.getId() || id == Material.NETHER_FENCE.getId()){
			if (!collidesFence(fx, fz, dX, dZ, dT, 0.425)) return true;
		}
		else if (id == Material.FENCE_GATE.getId()){
			if ((access.getData(bx, by, bz) & 0x4)!= 0) return true;
		}
		else if (id == Material.CAKE_BLOCK.getId()){
			if (Math.min(fy, fy + dY * dT) >= 0.4375) return true; // 0.0625 = 0.125 / 2
		}
		else if (id == Material.CAULDRON.getId()){
		    if (Math.min(fy, fy + dY * dT) >= 0.3125){
		        // Check for moving through walls or floor.
		    	// TODO: Maybe this is too exact...
			    return isInsideCenter(fx, fz, dX, dZ, dT, 0.125);
		    }
		}
		else if (id == Material.CACTUS.getId()){
			if (Math.min(fy, fy + dY * dT) >= 0.9375){
				return true;
			}
			return !collidesCenter(fx, fz, dX, dZ, dT, 0.0625);
		}
		else if (id == Material.PISTON_EXTENSION.getId()){
			if (Math.min(fy, fy + dY * dT) >= 0.625){
				return true;
			}
		}
		// Nothing found.
		return false;
	}
	
	/**
	 * XZ-collision check for (bounds / pseudo-ray) with fence type blocks (fences, glass panes), margin configurable.
	 * @param fx
	 * @param fz
	 * @param dX
	 * @param dZ
	 * @param dT
	 * @param d Distance to the fence middle to keep (see code of isPassableworkaround for reference).
	 * @return
	 */
	public static boolean collidesFence(final double fx, final double fz, final double dX, final double dZ, final double dT, final double d) {
		final double dFx = 0.5 - fx;
		final double dFz = 0.5 - fz;
		if (Math.abs(dFx) > 0.05 && Math.abs(dFz) > d){
			// Check moving between quadrants.
			final double dFx2 = 0.5 - (fx + dX * dT);
			final double dFz2 = 0.5 - (fz + dZ * dT);
			if (Math.abs(dFx2) > 0.05 && Math.abs(dFz2) > d){
				if (dFx * dFx2 > 0.0 && dFz * dFz2 > 0.0){
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Collision for x-z ray / bounds. Use this to check if a box is really outside.
	 * @param fx
	 * @param fz
	 * @param dX
	 * @param dZ
	 * @param dT
	 * @return False if no collision with the center bounds.
	 */
	public static final boolean collidesCenter(final double fx, final double fz, final double dX, final double dZ, final double dT, final double inset){
		final double low = inset;
		final double high = 1.0 - inset;
		final double xEnd = fx + dX * dT;
		if (xEnd < low && fx < low) return false;
		else if (xEnd >= high && fx >= high) return false;
		final double zEnd = fz + dZ * dT;
		if (zEnd < low && fz < low) return false;
		else if (zEnd >= high && fz >= high) return false;
		return true;
	}
	
	/**
	 * Collision for x-z ray / bounds. Use this to check if a box is really inside.
	 * @param fx
	 * @param fz
	 * @param dX
	 * @param dZ
	 * @param dT
	 * @return True if the box is really inside of the center bounds.
	 */
	public static final boolean isInsideCenter(final double fx, final double fz, final double dX, final double dZ, final double dT, final double inset){
		final double low = inset;
		final double high = 1.0 - inset;
		final double xEnd = fx + dX * dT;
		if (xEnd < low || fx < low) return false;
		else if (xEnd >= high || fx >= high) return false;
		final double zEnd = fz + dZ * dT;
		if (zEnd < low || fz < low) return false;
		else if (zEnd >= high || fz >= high) return false;
		return true;
	}
	
	/**
	 * Reference block height for on-ground judgment: player must be at this or greater height to stand on this block.<br>
	 * <br>
	 * TODO: Check naming convention, might change to something with max ... volatile!
	 * <br>
	 * This might return 0 or somewhat arbitrary values for some blocks that don't have full bounds (!), might return 0 for blocks with the F_GROUND_HEIGHT flag, unless they are treated individually here.
	 * @param access
	 * @param x
	 * @param y
	 * @param z
	 * @param id
	 * @param bounds
	 * @param flags
	 * @return
	 */
    public static double getGroundMinHeight(final BlockCache access, final int x, final int y, final int z, final int id, final double[] bounds, final long flags) {
    	// TODO: Check which ones are really needed !
    	if ((flags & F_HEIGHT_8SIM_INC) != 0){
    		final int data = (access.getData(x, y, z) & 0xF) % 8;
    		if (data < 3) return 0;
    		else return 0.5;
    	}
    	else if ((flags & F_HEIGHT_8_INC) != 0){
    		final int data = (access.getData(x, y, z) & 0xF) % 8;
    		return 0.125 * (double) data;
    	}
    	// Height 100 is ignored (!).
		else if ((flags & F_HEIGHT150) != 0){
    		return 1.5;
    	}
    	else if ((flags & F_STAIRS) != 0){
			if ((access.getData(x, y, z) & 0x4) != 0){
				return 1.0;
			}
			else{
				// what with >= 1?
				return 0.5;
			}
		}
		else if (id == Material.SOUL_SAND.getId()){
			return 0.875;
		}
//		else if (id == Material.CAKE_BLOCK.getId()){
//			return 0.4375;
//		}
		else if (id == Material.CAULDRON.getId()){
			// TODO: slightly over 0.
			return 0.3125;
		}
		else if (id == Material.CACTUS.getId()){
			return 0.9375;
		}
		else if (id == Material.PISTON_EXTENSION.getId()){
			return 0.625;
		}
		else if ((flags & F_GROUND_HEIGHT) != 0){
			// All blocks that are not treated individually are ground all through.
			// TODO: Experimental workaround.
			return 0;
		}
		else{
			// Nothing found.
			return bounds[4];
		}
	}

	/**
	 * Convenience method for debugging purposes. 
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final PlayerLocation loc) {
		return isPassable(loc.getBlockCache(), loc.getX(), loc.getY(), loc.getZ(), loc.getTypeId());
	}
	
	/**
	 * Convenience method for debugging purposes. 
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final Location loc) {
	    blockCache.setAccess(loc.getWorld());
		boolean res = isPassable(blockCache, loc.getX(), loc.getY(), loc.getZ(), blockCache.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
		blockCache.cleanup();
		return res;
	}
	
	/**
	 * Normal ray tracing.
	 * @param from
	 * @param to
	 * @return
	 */
	public static final boolean isPassable(final Location from, final Location to){
		blockCache.setAccess(from.getWorld());
    	PassableRayTracing rt = new PassableRayTracing();
    	rt.setBlockCache(blockCache);
    	rt.set(from.getX(), from.getY(), from.getZ(), to.getX(), to.getY(), to.getZ());
    	rt.loop();
    	final boolean collides = rt.collides();
    	blockCache.cleanup();
    	rt.cleanup();
    	return !collides;
	}
	
	/**
	 * Convenience method to allow using an already fetched or prepared IBlockAccess.
	 * @param access
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final BlockCache  access, final Location loc)
	{
		return isPassable(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	/**
	 * API access to read extra properties from files.
	 * @param config
	 */
    public static void applyConfig(final RawConfigFile config, final String pathPrefix) {
        // Ignore passable.
        for (final String input : config.getStringList(pathPrefix + ConfPaths.SUB_IGNOREPASSABLE)){
            final Integer id = RawConfigFile.parseTypeId(input);
            if (id == null || id < 0 || id >= 4096) LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + ConfPaths.SUB_IGNOREPASSABLE + "): " + input);
            else blockFlags[id] |= F_IGN_PASSABLE;
        }
        
        // Allow instant breaking.
        for (final String input : config.getStringList(pathPrefix + ConfPaths.SUB_ALLOWINSTANTBREAK)){
            final Integer id = RawConfigFile.parseTypeId(input);
            if (id == null || id < 0 || id >= 4096) LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + ConfPaths.SUB_ALLOWINSTANTBREAK + "): " + input);
            else {
            	setBlockProps(id, instantType);
            }
        }
        
        // Override block flags.
        ConfigurationSection section = config.getConfigurationSection(pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS);
        if (section != null){
        	final Map<String, Object> entries = section.getValues(false);
        	boolean hasErrors = false;
        	for (final Entry<String, Object> entry : entries.entrySet()){
        		final String key = entry.getKey();
        		final Integer id = RawConfigFile.parseTypeId(key);
                if (id == null || id < 0 || id >= 4096){
                	LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + "): " + key);
                	continue;
                }
                final Object obj = entry.getValue();
                if (!(obj instanceof String)){
                	LogUtil.logWarning("[NoCheatplus] Bad flags at " + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + " for key: " + key);
                	hasErrors = true;
                	continue;
                }
                final Collection<String> split = StringUtil.split((String) obj, ' ', ',', '/', '|', '+', ';', '\t');
                long flags = 0;
                boolean error = false;
                for (String input : split){
                	input = input.trim();
                	if (input.isEmpty()) continue;
                	else if (input.equalsIgnoreCase("default")){
                		flags |= blockFlags[id];
                		continue;
                	}
                	try{
                		flags |= parseFlag(input);
                	} catch(InputMismatchException e){
                		LogUtil.logWarning("[NoCheatplus] Bad flag at " + pathPrefix + ConfPaths.SUB_OVERRIDEFLAGS + " for key " + key + " (skip setting flags for this block): " + input);
                		error = true;
                		hasErrors = true;
                		break;
                	}
                }
                if (error){
                	continue;
                }
                blockFlags[id] = flags;
        	}
        	if (hasErrors){
        		LogUtil.logInfo("[NoCheatPlus] Overriding block-flags was not entirely successful, all available flags: \n" + StringUtil.join(flagNameMap.values(), "|"));
        	}
        }
    }
    
    /**
     * Test if the bounding box overlaps with a block of given flags (does not check the blocks bounding box).
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags Block flags (@see fr.neatmonster.nocheatplus.utilities.BlockProperties). 
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags){
        return hasAnyFlags(access, Location.locToBlock(minX), Location.locToBlock(minY), Location.locToBlock(minZ), Location.locToBlock(maxX), Location.locToBlock(maxY), Location.locToBlock(maxZ), flags);
    }

    
    /**
     * Test if the bounding box overlaps with a block of given flags (does not check the blocks bounding box).
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags Block flags (@see fr.neatmonster.nocheatplus.utilities.BlockProperties). 
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final BlockCache access,final int minX, int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final long flags){
        for (int x = minX; x <= maxX; x++){
            for (int z = minZ; z <= maxZ; z++){
                for (int y = minY; y <= maxY; y++){
                    if ((blockFlags[access.getTypeId(x, y, z)] & flags) != 0) return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Test if the box collide with any block that matches the flags somehow.
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags The flags to match.
     * @return
     */
    public static final boolean collides(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags){
    	final int iMinX = Location.locToBlock(minX);
    	final int iMaxX = Location.locToBlock(maxX);
    	// At least find fences etc. if searched for.
    	// TODO: F_HEIGHT150 could also be ground etc., more consequent might be to always use or flag it.
    	final int iMinY = Location.locToBlock(minY - ((flags & F_HEIGHT150) != 0 ? 0.5625 : 0));
    	final int iMaxY = Location.locToBlock(maxY);
    	final int iMinZ = Location.locToBlock(minZ);
    	final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++){
            for (int z = iMinZ; z <= iMaxZ; z++){
                for (int y = iMinY; y <= iMaxY; y++){
                    final int id = access.getTypeId(x, y, z);
                    final long cFlags = blockFlags[id];
                    if ((cFlags & flags) != 0){
                        // Might collide.
                    	final double[] bounds = access.getBounds(x, y, z);
                    	if (bounds != null){
                    		if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, cFlags)){
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
     * Check if a block with the given id is overlapping with the bounds, this does not check the blocks actual bounds (All bounds of the block are seen as 0...1, for exact box match use collidesBlock). 
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param id
     * @return
     */
    public static final boolean collidesId(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int id){
    	final int iMinX = Location.locToBlock(minX);
    	final int iMaxX = Location.locToBlock(maxX);
    	final int iMinY = Location.locToBlock(minY - ((blockFlags[id] & F_HEIGHT150) != 0 ? 0.5625 : 0));
    	final int iMaxY = Location.locToBlock(maxY);
    	final int iMinZ = Location.locToBlock(minZ);
    	final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++){
            for (int z = iMinZ; z <= iMaxZ; z++){
                for (int y = iMinY; y <= iMaxY; y++){
                    if (id == access.getTypeId(x, y, z)){
                    	return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if the given bounding box collides with a block of the given id, taking into account the actual bounds of the block. 
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param id
     * @return
     */
    public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int id){
    	final int iMinX = Location.locToBlock(minX);
    	final int iMaxX = Location.locToBlock(maxX);
    	final int iMinY = Location.locToBlock(minY - ((blockFlags[id] & F_HEIGHT150) != 0 ? 0.5625 : 0));
    	final int iMaxY = Location.locToBlock(maxY);
    	final int iMinZ = Location.locToBlock(minZ);
    	final int iMaxZ = Location.locToBlock(maxZ);
        for (int x = iMinX; x <= iMaxX; x++){
            for (int z = iMinZ; z <= iMaxZ; z++){
                for (int y = iMinY; y <= iMaxY; y++){
                    if (id == access.getTypeId(x, y, z)){
                    	final double[] bounds = access.getBounds(x, y, z);
                    	if (bounds != null){
                        	if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, blockFlags[id])){
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
     * Check if the bounds collide with the block that has the given type id at the given position.
     * <br> Delegates: This method signature is not used internally anymore.
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param id
     */
    public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int x, final int y, final int z, final int id){
        // TODO: use internal block data unless delegation wanted?
    	final double[] bounds = access.getBounds(x,y,z);
        if (bounds == null) return false; // TODO: policy ?
        final long flags = blockFlags[id];
        return collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags);
    }
        
   /**
    * Check if the bounds collide with the block for the given type id at the given position.
    * @param access
    * @param minX
    * @param minY
    * @param minZ
    * @param maxX
    * @param maxY
    * @param maxZ
    * @param x
    * @param y
    * @param z
    * @param id
    * @param bounds Not null: bounds of the block at x, y, z.
    * @param flags Block flags for the block at x, y, z.
    * @return
    */
   public static final boolean collidesBlock(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int x, final int y, final int z, final int id, final double[] bounds, final long flags){
	    final double bminX, bminZ, bminY;
        final double bmaxX, bmaxY, bmaxZ;
        // TODO: Consider a quick shortcut checks flags == F_NORMAL_GROUND
        if ((flags & F_STAIRS) != 0){ // TODO: make this a full block flag ?
        	// Mainly for on ground style checks, would not go too well with passable.
        	// TODO: change this to something like F_FULLBOX probably.
        	bminX = bminY = bminZ = 0D;
        	bmaxX = bmaxY = bmaxZ = 1D;
        }
        else{
        	// xz-bounds
        	if ((flags & F_XZ100) != 0){
        		bminX = bminZ = 0;
        		bmaxX = bmaxZ = 1;
        	}
        	else{
        		bminX = bounds[0]; // block.v(); // minX
            	bminZ = bounds[2]; // block.z(); // minZ
            	bmaxX = bounds[3]; //block.w(); // maxX
                bmaxZ = bounds[5]; //block.A(); // maxZ
        	}
        	// y-bounds
        	if ((flags & F_HEIGHT_8SIM_INC) != 0){
        		// TODO: remove / solve differently ?
        		bminY = 0;
        		final int data = (access.getData(x, y, z) & 0xF) % 8;
//        		bmaxY = (double) (1 +  data) / 8.0;
        		bmaxY = data < 3 ? 0 : 0.5;
        	}
        	else if ((flags & F_HEIGHT_8_INC) != 0){
        		bminY = 0;
        		final int data = (access.getData(x, y, z) & 0xF) % 8;
        		bmaxY = 0.125 * data;
        	}
        	else if ((flags & F_HEIGHT150) != 0){
        		bminY = 0;
        		bmaxY = 1.5;
        	}
            else if ((flags & F_HEIGHT100) != 0){
            	bminY = 0;
            	bmaxY = 1.0;
            }
            else if ((flags & F_HEIGHT_8SIM_DEC) != 0){
            	bminY = 0;
            	final int data = access.getData(x, y, z);
            	if ((data & 0x8) == 0){
            		// This box works for jumping over flowing water.
//            		bmaxY = (0.8 - (double) ((data & 0xF) % 8) / 9.8);
            		final int data8 = (data & 0xF) % 8;
            		if (data8 > 4){
            			// TODO: With block breaking and data8 == 7 this would be too high.
            			bmaxY = 0.5;
            		}
            		else{
            			bmaxY = 1.0; // - (double) data8 / 9.0;
            		}
            	}
            	else{
            		bmaxY = 1.0;
            	}
            } 
            else{
            	bminY = bounds[1]; // minY
            	bmaxY = bounds[4]; // maxY
            }
        }
        if (minX >= bmaxX + x || maxX < bminX + x) return false;
        else if (minY >= bmaxY + y || maxY < bminY + y) return false;
        else if (minZ >= bmaxZ + z || maxZ < bminZ + z) return false;
        else return true;
   }
   
   /**
    * Attempt to return the exact outside bounds, corrected by flags and other.
	* @deprecated Not yet for real (only used in certain checks/contexts).
    * @param access
    * @param x
    * @param y
    * @param z
    * @return If changed, a copy is returned, otherwise the original bounds returned by the BlockCache instance.
    */
   public static double[] getCorrectedBounds(final BlockCache access, final int x, final int y, final int z) {
		return getCorrectedBounds(x, y, z, access.getTypeId(x, y, z), access.getBounds(x, y, z));
	}
    
	/**
	 * Attempt to return the exact outside bounds, corrected by flags and other.
	 * @deprecated Not yet for real (only used in certain checks/contexts).
	 * @param x
	 * @param y
	 * @param z
	 * @param typeId
	 * @param bounds
	 * @return If changed, a copy is returned, otherwise the original array as given.
	 */
   public static double[] getCorrectedBounds(final int x, final int y, final int z, final int id, final double[] bounds) {
		if (bounds == null) return null;
		//final long flags = blockFlags[id];
		// TODO: Consider to change to adaptBounds and to store the adapted bounds already.
		// TODO: IMPLEMENT special bounds, at least: Step + stairs (dy=0.5 for both) + snow.
		return bounds;
	}

/**
    * Similar to collides(... , F_GROUND), but also checks the block above (against spider).<br>
    * NOTE: This does not return true if stuck, to check for that use collidesBlock for the players location.
    * @param access
    * @param minX
    * @param minY
    * @param minZ
    * @param maxX
    * @param maxY
    * @param maxZ
    * @return
    */
   public static final boolean isOnGround(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
    	return isOnGround(access, minX, minY, minZ, maxX, maxY, maxZ, 0L);
   }
    
    /**
     * Similar to collides(... , F_GROUND), but also checks the block above (against spider).<br>
     * NOTE: This does not return true if stuck, to check for that use collidesBlock for the players location.
     * @param access
     * @param minX Bounding box coordinates...
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY Meant to be the foot-level.
     * @param maxZ
     * @param ignoreFlags Blocks with these flags are not counted as ground.
     * @return
     */
    public static final boolean isOnGround(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long ignoreFlags){
    	// TODO: Consider to add extra parameter xzMargin.
    	final int maxBlockY = access.getMaxBlockY();
    	final int iMinX = Location.locToBlock(minX);
    	final int iMaxX = Location.locToBlock(maxX);
    	final int iMinY = Location.locToBlock(minY - 0.5626);
    	if (iMinY > maxBlockY) return false;
    	final int iMaxY = Math.min(Location.locToBlock(maxY), maxBlockY);
    	final int iMinZ = Location.locToBlock(minZ);
    	final int iMaxZ = Location.locToBlock(maxZ);
//    	System.out.println("*** isOnGround check size: " + ((iMaxX - iMinX + 1) * (iMaxY - iMinY + 1) * (iMaxZ - iMinZ + 1)));
        for (int x = iMinX; x <= iMaxX; x++){
            for (int z = iMinZ; z <= iMaxZ; z++){
            	
            	// TODO: Might move above block check right here.
            	// TODO: Given workaround fixes, could go for the much-simplified-top-down-loop once more.
            	
                for (int y = iMaxY; y >= iMinY; y --){
                	
                	// TODO: Remember the state of the last block below instead of checking the block above.
                	// TODO: Alternatively optimize: block-above-check: use loop up to iMaxY...
                	
                    final int id = access.getTypeId(x, y, z);
                    final long flags = blockFlags[id];
                    
                    
                    // TODO: LIQUID could be a quick return as well.
                    // (IGN_PASSABLE might still allow standing on.)
                    if ((flags & F_GROUND) == 0 || (flags & ignoreFlags) != 0){
                    	continue;
                    }
                    	
                    // Might collide.
                	final double[] bounds = access.getBounds(x, y, z);
                	if (bounds == null){
                		// TODO: Safety check, uncertain.
                		return true;
                	}
                    if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id, bounds, flags)){
                    	continue;
                    }
                    
                    // TODO: Make this one work (passable workaround).
                    // Check if the block can be passed through with the bounding box (disregard the ignore flag).
                    if (isPassableWorkaround(access, x, y, z, minX - x, minY - y, minZ - z, id, maxX - minX, maxY - minY, maxZ - minZ, 1.0)){
                    	// Spider !
                    	// Not nice but...
                    	// TODO: GROUND_HEIGHT: would have to check passable workaround again ?
                    	// TODO: height >= ?
                    	// TODO: Another concept is needed for the stand-on-passable !
                    	// TODO: Add getMinGroundHeight, getMaxGroundHeight.
                    	if ((flags & F_GROUND_HEIGHT) == 0 ||  getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - y){
                    		// Don't break, though could for some cases (?), since a block below still can be ground.
                        	continue;
                    	}
                    }
                    
                    // Don't count as ground if a block contains the foot.
                    // height >= ?
					if (getGroundMinHeight(access, x, y, z, id, bounds, flags) > maxY - y){
						// Within block, this x and z is no candidate for ground.
						if (isFullBounds(bounds)){
							break;
						}
						else{
							continue; 
						}
					}
					
					if (maxY - y < 1.0){
						// No need to check the block above (half slabs, stairs).
						return true;
					}
					
                    // Check if the block above allows this to be ground. 
                    
                	if (y >= maxBlockY){
                		// Only air above.
                		return true;
                	}
                	
                	boolean variable = (flags & F_VARIABLE) != 0;
                	
                	// TODO: Keep an eye on this one for exploits.
                	if (y != iMaxY && !variable){
                		// Ground found and the block above is passable, no need to check above.
                		return true;
                	}
                	// TODO: Else if variable : continue ?
                	// TODO: Highest block is always the foot position, even if just below 1.0, a return true would be ok?
                	
                    final int aboveId = access.getTypeId(x, y + 1, z);
                    final long aboveFlags = blockFlags[aboveId];
                    if ((aboveFlags & F_IGN_PASSABLE) != 0){
                    	// Ignore these (Note for above block check before ground property).
                    	// TODO: Should this always apply ?
                    	return true;
                    }
                    
                    if ((aboveFlags & F_GROUND) == 0 || (aboveFlags & F_LIQUID) != 0 || (aboveFlags & ignoreFlags) != 0){
                    	return true;
                    }

                    variable |= (aboveFlags & F_VARIABLE) != 0;
                    // Check if it is the same id (walls!) and similar.
                    if (!variable && id == aboveId){
                    	// Exclude stone walls "quickly", can not stand on.
                    	if (isFullBounds(bounds)){
                    		break;
                    	}
                    	else{
                    		continue;
                    	}
                    }
                
                    // Check against spider type hacks.
                	final double[] aboveBounds = access.getBounds(x, y + 1, z);
                	if (aboveBounds == null){
                		return true;
                	}
                	
                	// TODO: 1.49 might be obsolete !
                    if (!collidesBlock(access, minX, minY, minZ, maxX, Math.max(maxY, 1.49 + y), maxZ, x, y + 1, z, aboveId, aboveBounds, aboveFlags)){
                    	return true;
                    }
                    
                    // Check passable workaround without checking ignore flag.
                    if (isPassableWorkaround(access, x, y + 1, z, minX - x, minY - (y + 1), minZ - z, id, maxX - minX, maxY - minY, maxZ - minZ, 1.0)){
                    	return true;
                    }
                    
                    if (isFullBounds(aboveBounds)){
                    	// Can not be ground at this x - z position.
                    	break;
                    }
                    
                    // TODO: Is this variable workaround still necessary ? Has this not been tested above already (passable workaround!)
                    // TODO: This might be seen as a violation for many block types.
                	// TODO: More distinction necessary here.
            		if (variable){
            			// Simplistic hot fix attempt for same type + same shape.
            			// TODO: Needs passable workaround check.
                		if (isSameShape(bounds, aboveBounds)){
                			// Can not stand on (rough heuristics).
                			// TODO: Test with cactus.
                			break;
                			// continue;
                		}
                		else{
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
     * @param bounds Block bounds: minX, minY, minZ, maxX, maxY, maxZ
     * @return
     */
    public static final boolean isFullBounds(final double[] bounds) {
		for (int i = 0; i < 3; i++){
			if (bounds[i] != 0.0 || bounds[i + 3] != 1.0){
				return false;
			}
		}
		return true;
	}

	/**
     * Check if the bounds are the same. No null checks.
     * @param bounds1
     * @param bounds2
     * @return Block bounds: minX, minY, minZ, maxX, maxY, maxZ
     */
    public static final boolean isSameShape(final double[] bounds1, final double[] bounds2) {
    	// TODO: further exclude simple full shape blocks, or confine to itchy block types
		// TODO: make flags for it.
		// Allow as ground for differing shapes.
		for (int i = 0; i <  6; i++){
			if (bounds1[i] != bounds2[i]){
				// Simplistic.
				return false;
			}
		}
		return true;
	}

	/**
     * Check if a move determined by xDistance and zDistance is leading down stream.
     * @param access
     * @param x
     * @param y
     * @param z
     * @param data
     * @param dX
     * @param dZ
     * @return
     */
    public static final boolean isDownStream(final BlockCache access, final int x, final int y, final int z, final int data, 
            final double dX, final double dZ) {
        // x > 0 -> south, z > 0 -> west
        if ((data & 0x8) == 0){
            // not falling.
            if ((dX > 0)){
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) > data){
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) < data){
                    // reverse direction.
                    return true;
                }
            } else if (dX < 0){
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x - 1, y, z)) && access.getData(x - 1, y, z) > data){
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x + 1, y, z)) && access.getData(x + 1, y, z) < data){
                    // reverse direction.
                    return true;
                }
            }
            if (dZ > 0){
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x, y, z + 1)) && access.getData(x, y, z + 1) > data){
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x , y, z - 1)) && access.getData(x, y, z - 1) < data){
                    // reverse direction.
                    return true;
                }
            }
            else if (dZ < 0 ){
                if (data < 7 && BlockProperties.isLiquid(access.getTypeId(x, y, z - 1)) && access.getData(x, y, z - 1) > data){
                    return true;
                }
                else if (data > 0  && BlockProperties.isLiquid(access.getTypeId(x , y, z + 1)) && access.getData(x, y, z + 1) < data){
                    // reverse direction.
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Collect all flags of blocks touched by the bounds, this does not check versus the blocks bounding box.
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @return
     */
    public static final long collectFlagsSimple(final BlockCache access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
    	final int iMinX = Location.locToBlock(minX);
    	final int iMaxX = Location.locToBlock(maxX);
    	final int iMinY = Location.locToBlock(minY);
    	final int iMaxY = Location.locToBlock(maxY);
    	final int iMinZ = Location.locToBlock(minZ);
    	final int iMaxZ = Location.locToBlock(maxZ);
//    	System.out.println("*** collect flags check size: " + ((iMaxX - iMinX + 1) * (iMaxY - iMinY + 1) * (iMaxZ - iMinZ + 1)));
    	long flags = 0;
        for (int x = iMinX; x <= iMaxX; x++){
            for (int z = iMinZ; z <= iMaxZ; z++){
                for (int y = iMinY; y <= iMaxY; y++){
                    flags |= blockFlags[access.getTypeId(x, y, z)];
                }
            }
        }
        return flags;
    }

    /**
     * Penalty factor for block break duration if under water.
     * @return
     */
	public static float getBreakPenaltyInWater() {
		return breakPenaltyInWater;
	}

	/**
	 * Penalty factor for block break duration if under water.
	 * @param breakPenaltyInWater
	 */
	public static void setBreakPenaltyInWater(float breakPenaltyInWater) {
		BlockProperties.breakPenaltyInWater = breakPenaltyInWater;
	}

	/**
	 * Penalty factor for block break duration if not on ground.
	 * @return
	 */
	public static float getBreakPenaltyOffGround() {
		return breakPenaltyOffGround;
	}

	/**
	 * Penalty factor for block break duration if not on ground.
	 * @param breakPenaltyOffGround
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
		blockCache.cleanup();
		blockCache = null;
		// TODO: might empty mappings...
	}

	/**
	 * 
	 * @param access
	 * @param blockX Block location.
	 * @param blockY
	 * @param blockZ
	 * @param oX Origin / offset from block location.
	 * @param oY
	 * @param oZ
	 * @param dX Direction (multiplied by dT to get end point of move).
	 * @param dY
	 * @param dZ
	 * @param dT 
	 * @return
	 */
	public static final boolean isPassableRay(final BlockCache access, final int blockX, final int blockY, final int blockZ, final double oX, final double oY, final double oZ, final double dX, final double dY, final double dZ, final double dT) {
		final int id = access.getTypeId(blockX, blockY, blockZ);
		if (BlockProperties.isPassable(id)) return true;
		double[] bounds = access.getBounds(blockX, blockY, blockZ);
		if (bounds == null) return true;
		
		// Simplified check: Only collision of bounds of the full move is checked.
		final double minX, maxX;
		if (dX < 0.0){
			minX = dX * dT + oX + blockX;
			maxX = oX + blockX;
		}
		else{
			maxX = dX * dT + oX + blockX;
			minX = oX + blockX;
		}
		final double minY, maxY;
		if (dY < 0.0){
			minY = dY * dT + oY + blockY;
			maxY = oY + blockY;
		}
		else{
			maxY = dY * dT + oY + blockY;
			minY = oY + blockY;
		}
		final double minZ, maxZ;
		if (dZ < 0.0){
			minZ = dZ * dT + oZ + blockZ;
			maxZ = oZ + blockZ;
		}
		else{
			maxZ = dZ * dT + oZ + blockZ;
			minZ = oZ + blockZ;
		}
		if (!collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, blockX, blockY, blockZ, id, bounds, blockFlags[id])){
			// TODO: Might check for fence too, here.
			return true;
		}
		
		// TODO: Actual ray-collision checking?
		// TODO: Heuristic workaround for certain situations [might be better outside of this, probably a simplified version ofr the normal case]?
		
		// Check for workarounds.
		// TODO: check f_itchy once exists.
		if (BlockProperties.isPassableWorkaround(access, blockX, blockY, blockZ, oX, oY, oZ, id, dX, dY, dZ, dT)){
			return true;
		}
		// Does collide (most likely).
		// TODO: This is not entirely accurate.
		// TODO: Moving such that the full move rect overlaps, but no real collision (diagonal moves).
		// TODO: "Wrong" moves through edges of blocks (not sure, needs reproducing).
		// (Could allow start-end if passable + check first collision time or some estimate.)
		return false;
	}
	
}
