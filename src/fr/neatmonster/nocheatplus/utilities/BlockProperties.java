package fr.neatmonster.nocheatplus.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.minecraft.server.AxisAlignedBB;
import net.minecraft.server.Block;
import net.minecraft.server.IBlockAccess;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigFile;

/**
 * Poperties of blocks.
 * 
 * Likely to be added:
 * - reading properties from files.
 * - reading the default properties from a file too.
 *
 */
public class BlockProperties {
	public static enum ToolType{
		NONE,
		SWORD,
		SHEARS,
		SPADE,
		AXE,
		PICKAXE,
//		HOE,
	}
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
	/** Properties of a tool. */
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
	/** Properties of a block. */
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
	protected static Map<Integer, ToolProps> tools = new HashMap<Integer, ToolProps>(50, 0.5f);
	
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
	
	private static final PlayerLocation pLoc = new PlayerLocation();
	
    protected static final long[] blockFlags = new long[maxBlocks];
    
    /** Flag position for stairs. */
    public static final int F_STAIRS 		= 0x1;
    public static final int F_LIQUID 		= 0x2;
    // TODO: maybe remove F_SOLID use (unless for setting F_GROUND on init).
    /** Minecraft isSolid result. Used for setting ground flag - Subject to change / rename.*/
    public static final int F_SOLID 		= 0x4;
    /** Compatibility flag: regard this block as passable always. */
    public static final int F_IGN_PASSABLE 	= 0x8;
    public static final int F_WATER         = 0x10;
    public static final int F_LAVA          = 0x20;
    /** Override bounding box: 1.5 blocks high, like fences.<br>
     *  NOTE: This might have relevance for passable later.
     */
    public static final int F_HEIGHT150     = 0x40;
    /** The player can stand on these, sneaking or not. */
    public static final int F_GROUND        = 0x80; // TODO: 
    /** Override bounding box: 1 block height.<br>
     * NOTE: This should later be ignored by passable, rather.
     */
    public static final int F_HEIGHT100     = 0x100;
    
	static{
		init();
	}
	
   public static void init() {
        try{
            initTools();
            initBlocks();
        }
        catch(Throwable t){
            t.printStackTrace();
        }
    }
	
	private static void initTools() {
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

    private static void initBlocks() {
		Arrays.fill(blocks, null);
		///////////////////////////
		// Initalize block flags
		///////////////////////////
		for (int i = 0; i <maxBlocks; i++){
			blockFlags[i] = 0;
			final net.minecraft.server.Block block = net.minecraft.server.Block.byId[i];
			if (block != null){
				if (block.material != null){
					final net.minecraft.server.Material material = block.material;
					if (material.isLiquid()){
						// TODO: do not set F_GROUND for fluids ?
						blockFlags[i] |= F_LIQUID;
						if (material.isSolid()) blockFlags[i] |= F_SOLID;
					}
					else if (material.isSolid()){
					    blockFlags[i] |= F_SOLID | F_GROUND;
					}
				}
			}
		}
		// Stairs.
		for (final Material mat : new Material[] { Material.NETHER_BRICK_STAIRS, Material.COBBLESTONE_STAIRS, 
				Material.SMOOTH_STAIRS, Material.BRICK_STAIRS, Material.SANDSTONE_STAIRS, Material.WOOD_STAIRS, 
				Material.SPRUCE_WOOD_STAIRS, Material.BIRCH_WOOD_STAIRS, Material.JUNGLE_WOOD_STAIRS }) {
			blockFlags[mat.getId()] |= F_STAIRS | F_HEIGHT100 | F_GROUND; // Set ground too, to be sure.
		}
		// WATER.
		for (final Material mat : new Material[]{
				Material.STATIONARY_WATER, Material.WATER,
		}) {
			blockFlags[mat.getId()] |= F_LIQUID | F_WATER;
		}
		// LAVA.
        for (final Material mat : new Material[]{
                Material.LAVA, Material.STATIONARY_LAVA,
        }) {
            blockFlags[mat.getId()] |= F_LIQUID | F_LAVA;
        }
        // Fence types (150% height).
        for (final Material mat : new Material[]{
                Material.FENCE, Material.FENCE_GATE,
                Material.NETHER_FENCE,
        }){
            blockFlags[mat.getId()] |= F_HEIGHT150;
        }
        // Workarounds.
        for (final Material mat : new Material[]{
                Material.WATER_LILY, Material.LADDER,
                Material.DIODE_BLOCK_OFF, Material.DIODE_BLOCK_ON,
        }){
            blockFlags[mat.getId()] |= F_GROUND;
        }
        blockFlags[Material.ENDER_PORTAL_FRAME.getId()] |= F_HEIGHT100;
		// Ignore for passable.
		for (final Material mat : new Material[]{
				Material.WOOD_PLATE, Material.STONE_PLATE, 
				Material.WALL_SIGN, Material.SIGN_POST,
				Material.DIODE_BLOCK_ON, Material.DIODE_BLOCK_OFF,
				Material.LADDER,
		}){
			blockFlags[mat.getId()] |= F_IGN_PASSABLE;
		}
		
		////////////////////////////////
		// Set block props.
		////////////////////////////////
		// Instantly breakable.
		for (final Material mat : instantMat){
			blocks[mat.getId()] = instantType;
		}
		blocks[Material.SNOW.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(0.5, 0.1, 0.05, 0.05, 0.05, 0.05));
		for (Material mat : new Material[]{ 
				Material.VINE, Material.LEAVES, Material.COCOA, Material.BED_BLOCK}){
			blocks[mat.getId()] = leafType;
		}
		blocks[Material.SNOW_BLOCK.getId()] = new BlockProps(getToolProps(Material.WOOD_SPADE), 0.1f, secToMs(1, 0.15, 0.1, 0.05, 0.05, 0.05));
		blocks[Material.HUGE_MUSHROOM_1.getId()] = hugeMushroomType;
		blocks[Material.HUGE_MUSHROOM_2.getId()] = hugeMushroomType;
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
				Material.GOLD_BLOCK,
				
		}){
			blocks[mat.getId()] =  diamondType;
		}
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
		blocks[Material.IRON_BLOCK.getId()] = new BlockProps(stonePickaxe, 5, secToMs(25, 25, 1.9, 1.25, 0.95, 25));
		blocks[Material.DIAMOND_BLOCK.getId()] = new BlockProps(ironPickaxe, 5, secToMs(25, 25, 25, 1.25, 0.95, 25));
		blocks[Material.ENDER_CHEST.getId()] = new BlockProps(woodPickaxe, 22.5f);
		blocks[Material.OBSIDIAN.getId()] = new BlockProps(diamondPickaxe, 50, secToMs(250, 250, 250, 250, 9.4, 250));
		
		// More 1.4 (not insta).
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
				Material.LOCKED_CHEST, 
		}){
			blocks[mat.getId()] = indestructibleType; 
		}
	}
	
	public static void dumpBlocks(boolean all) {
		List<String> missing = new LinkedList<String>();
		if (all) {
			LogUtil.logInfo("[NoCheatPlus] Dump block properties for fastbreak check:");
			LogUtil.logInfo("--- Present entries -------------------------------");
		}
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
			if (blocks[i] == null){
				if (mat.equals("?")) continue;
				missing.add("* MISSING "+i + "(" + mat +") ");
			}
			else if (all) LogUtil.logInfo(i + ": (" + mat + ") " + blocks[i].toString());
		}
		if (!missing.isEmpty()){
			Bukkit.getLogger().warning("[NoCheatPlus] The block breaking data is incomplete, default to allow instant breaking:");
			LogUtil.logWarning("--- Missing entries -------------------------------");
			for (String spec : missing){
				LogUtil.logWarning(spec);
			}
		}
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
		final int x = location.getBlockX();
		final int y = location.getBlockY();
		final int z = location.getBlockZ();
		final World world = location.getWorld();
		final boolean onGround = isOnGround(player, location) || world.getBlockTypeIdAt(x, y, z) == Material.WATER_LILY.getId();
		final boolean inWater = isInWater(world.getBlockTypeIdAt(x, y + 1, z));
		final int haste = player.hasPotionEffect(PotionEffectType.FAST_DIGGING) ? 1 : 0;
		return getBreakingDuration(blockId, itemInHand, onGround, inWater, helmet != null && helmet.containsEnchantment(Enchantment.WATER_WORKER), haste);
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
	     * @param haste Amplifier of haste potion effect (assume >0 for effect there at all).
	     * @return
	     */
	public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency, int haste) {
	    final long dur = getBreakingDuration(blockId, blockProps, toolProps, onGround, inWater, aquaAffinity, efficiency);
	    return haste > 0 ? ((long) (dur * 0.66)) : dur;
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
			// Treat these extra (party experimental):
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
		
		if (isValidTool || blockProps.tool.toolType == ToolType.NONE){
		    long mult = 1;
			if (inWater && !aquaAffinity) 	mult *= 5;
			if (!onGround) 					mult *= 5;
			duration *= mult;
			
			// Efficiency level.
			if (efficiency > 0) {
				// Workarounds ...
				if (blockId == Material.WOODEN_DOOR.getId() && toolProps.toolType != ToolType.AXE) {
					// Heck [Cleanup pending]...
					switch (efficiency) {
					case 1:
						return 1500 * mult;
					case 2:
						return 750 * mult;
					case 3:
						return 450 * mult;
					case 4:
						return 250 * mult;
					case 5:
						return 150 * mult;
					}
				}
				// This seems roughly correct.
				for (int i = 0; i < efficiency; i++){
					duration /= 1.33; // Matches well with obsidian.
				}
				// More Workarounds:
	            if (toolProps.materialBase == MaterialBase.WOOD){
				    if (blockId == Material.LOG.getId()) duration -= efficiency >= 4 ? 250 : 400;
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
	
	public static boolean isInWater(final int blockId) {
		if (blockId == Material.STATIONARY_WATER.getId() || blockId == Material.STATIONARY_LAVA.getId()) return true;
		// TODO: count in water height ?
		// TODO: lava ?
		return false;
	}

	/**
	 *  Heavy but ...
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @return
	 */
	public static boolean isOnGround(Player player, Location location) {
//		return blockId != 0 && net.minecraft.server.Block.byId[blockId].//.c();// d();
		// Bit fat workaround, maybe put the object through from check listener ?
		pLoc.set(location, player, 0.3);
		if (pLoc.isIllegal()) {
			pLoc.cleanup();
			CheckUtils.onIllegalMove(player);
			return false;
		}
		final boolean onGround = pLoc.isOnGround();
		pLoc.cleanup();
		return onGround;
	}

	/**
	 * Hiding the API access here.<br>
	 * TODO: Find description of this and use block properties from here, as well as a speaking method name.
	 * @param id
	 * @return
	 */
	public static final boolean i(final int id) {
		return Block.i(id);
	}
	
	public static final long getBLockFlags(final int id){
		return blockFlags[id];
	}

	public static final void setBlockFlags(final int id, final long flags){
		blockFlags[id] = flags;
	}

	public static final boolean isStairs(final int id) {
		return (blockFlags[id] & F_STAIRS) != 0;
	}


	public static final boolean isLiquid(final int id) {
		return (blockFlags[id] & F_LIQUID) != 0;
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
	 * Test if a position can be passed through.<br>
	 * NOTE: This is experimental.
	 * @param world
	 * @param x
	 * @param y
	 * @param z
	 * @param id
	 * @return
	 */
	public static final boolean isPassable(final IBlockAccess blockAccess, final double x, final double y, final double z, final int id){
		// Simple exclusion check first.
		if (isPassable(id)) return true;
		// Check if the position is inside of a bounding box.
		final int bx = Location.locToBlock(x);
		final int by = Location.locToBlock(y);
		final int bz = Location.locToBlock(z);
		final net.minecraft.server.Block block = net.minecraft.server.Block.byId[id];
		if (block == null) return true;
		block.updateShape(blockAccess, bx, by, bz);
		final double fx = x - bx;
		final double fy = y - by;
		final double fz = z - bz;
//		if (fx < block.minX || fx >= block.maxX || fy < block.minY || fy >= block.maxY || fz < block.minZ || fz >= block.maxZ) return true;
		if (fx < block.v() || fx >= block.w() || fy < block.x() || fy >= block.y() || fz < block.z() || fz >= block.A()) return true;
		else{
			// Workarounds (might get generalized some time).
			if (isStairs(id)){
				if ((blockAccess.getData(bx, by, bz) & 0x4) != 0){
					if (fy < 0.5) return true;
				}
				else if (fy >= 0.5) return true; 
			}
			else if (id == Material.SOUL_SAND.getId() && fy >= 0.875) return true; // 0.125
			else if (id == Material.IRON_FENCE.getId() || id == Material.THIN_GLASS.getId()){
			        if (Math.abs(0.5 - fx) > 0.05 && Math.abs(0.5 - fz) > 0.05) return true;
			}
			else if (id == Material.FENCE_GATE.getId() && (blockAccess.getData(bx, by, bz) & 0x4)!= 0) return true;
			else if (id == Material.CAKE_BLOCK.getId() && fy >= 0.4375) return true; // 0.0625 = 0.125 / 2
			else if (id == Material.CAULDRON.getId()){
			    if (Math.abs(0.5 - fx) < 0.1 && Math.abs(0.5 - fz) < 0.1 && fy > 0.1) return true;
			}
			else if (id == Material.CACTUS.getId() && fy >= 0.9375) return true;
			// Nothing found.
			return false;
		}
	}

	/**
	 * Convenience method for debugging purposes. 
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final PlayerLocation loc) {
		return isPassable(loc.getBlockAccess(), loc.getX(), loc.getY(), loc.getZ(), loc.getTypeId());
	}
	
	/**
	 * Convenience method for debugging purposes. 
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final Location loc) {
	    final IBlockAccess access = ((org.bukkit.craftbukkit.CraftWorld) loc.getWorld()).getHandle();
		return isPassable(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}
	
	/**
	 * Convenience method to allow using an already fetched or prepared IBlockAccess.
	 * @param access
	 * @param loc
	 * @return
	 */
	public static final boolean isPassable(final IBlockAccess  access, final Location loc)
	{
		return isPassable(access, loc.getX(), loc.getY(), loc.getZ(), access.getTypeId(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
	}

	/**
	 * API access to read extra properties from files.
	 * @param config
	 */
    public static void applyConfig(final ConfigFile config, final String pathPrefix) {
        // Ignore passable.
        for (final String input : config.getStringList(pathPrefix + ConfPaths.SUB_IGNOREPASSABLE)){
            final Integer id = ConfigFile.parseTypeId(input);
            if (id == null || id < 0 || id >= 4096) LogUtil.logWarning("[NoCheatplus] Bad block id (" + pathPrefix + ConfPaths.SUB_IGNOREPASSABLE + "): " + input);
            else blockFlags[id] |= F_IGN_PASSABLE;
        }
    }
    
    /**
     * 
     * @param box
     * @param flags Block flags (@see fr.neatmonster.nocheatplus.utilities.BlockProperties). 
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final IBlockAccess access, final AxisAlignedBB box, final long flags){
        return hasAnyFlags(access, box.a, box.b, box.c, box.d, box.e, box.f, flags);
    }
    
    /**
     * 
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags Block flags (@see fr.neatmonster.nocheatplus.utilities.BlockProperties). 
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final IBlockAccess access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags){
        return hasAnyFlags(access, Location.locToBlock(minX), Location.locToBlock(minY), Location.locToBlock(minZ), Location.locToBlock(maxX), Location.locToBlock(maxY), Location.locToBlock(maxZ), flags);
    }

    
    /**
     * 
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags Block flags (@see fr.neatmonster.nocheatplus.utilities.BlockProperties). 
     * @return If any block has the flags.
     */
    public static final boolean hasAnyFlags(final IBlockAccess access,final int minX, int minY, final int minZ, final int maxX, final int maxY, final int maxZ, final long flags){
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
     * @param box
     * @param flags
     * @return
     */
    public static final boolean collides(final IBlockAccess access, final AxisAlignedBB box, final long flags){
        return collides(access, box.a, box.b, box.c, box.d, box.e, box.f, flags);
    }
    
    /**
     * Test if the box collide with any block that matches the flags somehow.
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param flags
     * @return
     */
    public static final boolean collides(final IBlockAccess access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final long flags){
        for (int x = Location.locToBlock(minX); x <= Location.locToBlock(maxX); x++){
            for (int z = Location.locToBlock(minZ); z <= Location.locToBlock(maxZ); z++){
                for (int y = Location.locToBlock(minY); y <= Location.locToBlock(maxY); y++){
                    final int id = access.getTypeId(x, y, z);
                    if ((blockFlags[id] & flags) != 0){
                        // Might collide.
                        if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id)) return true;
                    }
                }
            }
        }
        return false;
    }
    
    /**
     * Check if the bounds collide with the block for the given type id.
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @param id
     */
    public static final boolean collidesBlock(final IBlockAccess access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ, final int x, final int y, final int z, final int id){
        // TODO: use internal block data unless delegation wanted?
        final Block block = Block.byId[id];
        block.updateShape(access, x, y, z);
        final double bmaxY;
//        if ((blockFlags[id] & F_HEIGHT150) != 0) block.maxY = 1.5;
        if ((blockFlags[id] & F_HEIGHT150) != 0) bmaxY = 1.5;
        else if ((blockFlags[id] & F_HEIGHT100) != 0)  bmaxY = 1.0;
        else bmaxY = block.y(); // maxY
//        if (minX > block.maxX + x || maxX < block.minX + x) return false;
//        else if (minY > block.maxY + y || maxY < block.minY + y) return false;
//        else if (minZ > block.maxZ + z || maxZ < block.minZ + z) return false;
        if (minX > block.w() + x || maxX < block.v() + x) return false;
        else if (minY > bmaxY + y || maxY < block.x() + y) return false;
        else if (minZ > block.A() + z || maxZ < block.z() + z) return false;

        else return true;
    }
    
    /**
     * Similar to collides(... , F_SOLID), but also checks the block above.
     * @param access
     * @param minX
     * @param minY
     * @param minZ
     * @param maxX
     * @param maxY
     * @param maxZ
     * @return
     */
    public static final boolean isOnGround(final IBlockAccess access, final double minX, double minY, final double minZ, final double maxX, final double maxY, final double maxZ){
        for (int x = Location.locToBlock(minX); x <= Location.locToBlock(maxX); x++){
            for (int z = Location.locToBlock(minZ); z <= Location.locToBlock(maxZ); z++){
                for (int y = Location.locToBlock(minY - 0.5626); y <= Location.locToBlock(maxY); y++){
                    final int id = access.getTypeId(x, y, z);
                    if ((blockFlags[id] & F_GROUND) != 0){
                        // Might collide.
                        if (collidesBlock(access, minX, minY, minZ, maxX, maxY, maxZ, x, y, z, id)){
                            final int aboveId = access.getTypeId(x, y + 1, z);
                            final long flags = blockFlags[aboveId];
                            if ((flags & (F_IGN_PASSABLE)) != 0); // Ignore these.
                            else if ((flags & (F_GROUND)) != 0){
                                // Check against spider type hacks.
                                if (collidesBlock(access, minX, minY, minZ, maxX, Math.max(maxY, 1.49 + y), maxZ, x, y + 1, z, aboveId)){
                                    // TODO: This might be seen as a violation for many block types.
                                    continue;
                                }
                            }
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
    public static final boolean isDownStream(final IBlockAccess access, final int x, final int y, final int z, final int data, 
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
}
