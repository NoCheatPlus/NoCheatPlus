package fr.neatmonster.nocheatplus.utilities;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Poperties of blocks.
 * 
 * Likely to be added:
 * - reading properties from files.
 * - reading the default properties from a file too.
 *
 */
public class BlockUtils {
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
	}
	/** Properties of a block. */
	public static class BlockProps{
		public final ToolProps tool;
		public final long[] breakingTimes;
		public final float hardness;
		public BlockProps(ToolProps tool, float hardness){
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
		}
		public BlockProps(ToolProps tool, float hardness, long[] breakingTimes){
			this.tool = tool;
			this.breakingTimes = breakingTimes;
			this.hardness = hardness;
		}
		public String toString(){
			return "BlockProps(" + hardness + " / " + tool.toString() + " / " + Arrays.toString(breakingTimes) + ")";
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
	
	public static final BlockProps glassType = new BlockProps(noTool, 0.3f, glassTimes);
	
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
	public static final BlockProps defaultBlockProps = stoneType;
	
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
	};
	
	static{
		try{
			initTools();
			initBlocks();
		}
		catch(Throwable t){
			t.printStackTrace();
		}
	}
	
	private static void initTools() {
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
		for (int i = 0; i <blocks.length; i++){
			blocks[i] = null;
		}
		// 
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
		blocks[Material.LADDER.getId()] = new BlockProps(noTool, 0.4f, secToMs(0.6));
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
		blocks[Material.WOOL.getId()] = new BlockProps(noTool, 0.8f, secToMs(1.2));
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
		blocks[Material.MELON_BLOCK.getId()] = new BlockProps(noTool, 1, secToMs(1.5));
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
		blocks[Material.ENDER_CHEST.getId()] = new BlockProps(woodPickaxe, 22.5f, secToMs(112.5));
		blocks[Material.OBSIDIAN.getId()] = new BlockProps(diamondPickaxe, 50, secToMs(250, 250, 250, 250, 9.4, 250));

		for (Material mat : new Material[]{
				Material.AIR, Material.ENDER_PORTAL, Material.ENDER_PORTAL_FRAME,
				Material.PORTAL, Material.LAVA, Material.WATER, Material.BEDROCK,
				Material.STATIONARY_LAVA, Material.STATIONARY_WATER,
				Material.LOCKED_CHEST, 
		}){
			blocks[mat.getId()] = indestructibleType; 
		}
		
//		dumpBlocks(true); // Do at startup maybe.
	}
	
	public static void dumpBlocks(boolean all) {
		List<String> missing = new LinkedList<String>();
		if (all) {
			System.out.println("[NoCheatPlus] Dump block properties for fastbreak check:");
			System.out.println("--- Present entries -------------------------------");
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
			else if (all) System.out.println(i + ": (" + mat + ") " + blocks[i].toString());
		}
		if (!missing.isEmpty()){
			Bukkit.getLogger().warning("[NoCheatPlus] The block breaking data is incomplete, interpret some as stone :");
			System.out.println("--- Missing entries -------------------------------");
			for (String spec : missing){
				System.out.println(spec);
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
	
	public static ToolProps getToolProps(final Material mat){
		if (mat == null) return noTool;
		else return getToolProps(mat.getId());
	}
	
	public static ToolProps getToolProps(final Integer id){
		final ToolProps props = tools.get(id);
		if (props == null) return noTool;
		else return props;
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
		return getBreakingDuration(blockId, itemInHand, onGround, inWater, helmet != null && helmet.containsEnchantment(Enchantment.WATER_WORKER));
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
		final PlayerLocation loc = new PlayerLocation(); 
		// Bit fat workaround, maybe put the object through from check listener ?
		loc.set(location, player);
		return loc.isOnGround();
	}


	/**
	 * Get the normal breaking duration, including enchantments, and tool properties.
	 * @param blockId
	 * @param itemInHand
	 * @return
	 */
	public static long getBreakingDuration(final int blockId, final ItemStack itemInHand, final boolean onGround, final boolean inWater, final boolean aquaAffinity){
		// TODO: more configurability / load from file for blocks (i.e. set for shears etc.
		if (itemInHand == null) return getBreakingDuration(blockId, getBlockProps(blockId), noTool, onGround, inWater, aquaAffinity, 0);
		else{
			int efficiency = 0;
			if (itemInHand.containsEnchantment(Enchantment.DIG_SPEED)) efficiency = itemInHand.getEnchantmentLevel(Enchantment.DIG_SPEED);
			return getBreakingDuration(blockId, getBlockProps(blockId), getToolProps(itemInHand.getTypeId()), onGround, inWater, aquaAffinity, efficiency);
		}
	}

	public static long getBreakingDuration(final int blockId, final BlockProps blockProps, final ToolProps toolProps, final  boolean onGround, final boolean inWater, boolean aquaAffinity, int efficiency) {
		
		if (efficiency > 0 && blockId == Material.LEAVES.getId()){
			// Workaround until something better is found..
			if (efficiency == 1) return 100;
			else return 0; // insta break.
		}
		
		long duration;
		
		boolean isValidTool = isValidTool(blockId, blockProps, toolProps, efficiency);
		
		if (isValidTool){
			// appropriate tool
			duration = blockProps.breakingTimes[toolProps.materialBase.index];
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
			if (inWater && ! aquaAffinity) 
				duration *= 5;
			if (!onGround) 
				duration *= 5;
			// Efficiency level.
			if (efficiency > 0){
				// This seems roughly correct.
				for (int i = 0; i < efficiency; i++){
					duration /= 1.33; // Matches well with obsidian.
				}	
			}
		}
		return duration;
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
			if (blockProps.hardness <= 2 && (blockProps.tool.toolType == ToolType.AXE || blockProps.tool.toolType == ToolType.SPADE
					|| (blockProps.hardness < 0.8 
							&& (blockId != Material.NETHERRACK.getId() && blockId != Material.SNOW.getId() && blockId != Material.SNOW_BLOCK.getId() && blockId != Material.STONE_PLATE.getId())))){
				// Also roughly.
				isValidTool = true;
			}	
		}
		return isValidTool;
	}


	public static void read(){
		
	}


}
