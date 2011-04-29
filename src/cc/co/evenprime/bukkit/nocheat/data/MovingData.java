package cc.co.evenprime.bukkit.nocheat.data;

import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import cc.co.evenprime.bukkit.nocheat.NoCheatData;

public class MovingData {
	public int jumpPhase = 0;
	public int violationsInARow[] =  { 0, 0, 0 }; 
	public double horizFreedom = 0.0D;
	public int horizFreedomCounter = 0;
	public double vertFreedom = 0.0D;
	public int vertFreedomCounter = 0;
	public Location setBackPoint = null;
	public Runnable summaryTask = null;
	public Level highestLogLevel = null;
	public double maxYVelocity = 0.0D;
	public int sneakingFreedomCounter = 10;
	public double sneakingLastDistance = 0.0D;

	public boolean worldChanged = false;
	public boolean respawned = false;

	// WORKAROUND for changed PLAYER_MOVE logic
	public Location teleportTo = null;
	public Location lastLocation = null;

	public Location teleportInitializedByMe = null;

	// Block types that may be treated specially
	public enum BlockType {
		SOLID, NONSOLID, LADDER, LIQUID, UNKNOWN, FENCE;
	}

	// Until I can think of a better way to determine if a block is solid or not, this is what I'll do
	public static BlockType types[] = new BlockType[256];
	static {

		for(int i = 0; i < types.length; i++) {
			types[i] = BlockType.UNKNOWN;
		}

		types[Material.AIR.getId()] = BlockType.NONSOLID;
		types[Material.STONE.getId()] = BlockType.SOLID;
		types[Material.GRASS.getId()] = BlockType.SOLID;
		types[Material.DIRT.getId()] = BlockType.SOLID;
		types[Material.COBBLESTONE.getId()] = BlockType.SOLID;
		types[Material.WOOD.getId()] = BlockType.SOLID;
		types[Material.SAPLING.getId()] = BlockType.NONSOLID;
		types[Material.BEDROCK.getId()] = BlockType.SOLID;
		types[Material.WATER.getId()] = BlockType.LIQUID;
		types[Material.STATIONARY_WATER.getId()] = BlockType.LIQUID;
		types[Material.LAVA.getId()] = BlockType.LIQUID;
		types[Material.STATIONARY_LAVA.getId()] = BlockType.LIQUID;
		types[Material.SAND.getId()] = BlockType.SOLID;
		types[Material.GRAVEL.getId()] = BlockType.SOLID;
		types[Material.GOLD_ORE.getId()] = BlockType.SOLID;
		types[Material.IRON_ORE.getId()] = BlockType.SOLID;
		types[Material.COAL_ORE.getId()] = BlockType.SOLID;
		types[Material.LOG.getId()] = BlockType.SOLID;
		types[Material.LEAVES.getId()] = BlockType.SOLID;
		types[Material.SPONGE.getId()] = BlockType.SOLID;
		types[Material.GLASS.getId()] = BlockType.SOLID;
		types[Material.LAPIS_ORE.getId()] = BlockType.SOLID;
		types[Material.LAPIS_BLOCK.getId()] = BlockType.SOLID;
		types[Material.DISPENSER.getId()] = BlockType.SOLID;
		types[Material.SANDSTONE.getId()] = BlockType.SOLID;
		types[Material.NOTE_BLOCK.getId()]= BlockType.SOLID;
		types[Material.WOOL.getId()]= BlockType.SOLID;
		types[Material.YELLOW_FLOWER.getId()]= BlockType.NONSOLID;
		types[Material.RED_ROSE.getId()]= BlockType.NONSOLID;
		types[Material.BROWN_MUSHROOM.getId()]= BlockType.NONSOLID;
		types[Material.RED_MUSHROOM.getId()]= BlockType.NONSOLID;
		types[Material.GOLD_BLOCK.getId()]= BlockType.SOLID;
		types[Material.IRON_BLOCK.getId()]= BlockType.SOLID;
		types[Material.DOUBLE_STEP.getId()]= BlockType.UNKNOWN;
		types[Material.STEP.getId()]= BlockType.UNKNOWN;
		types[Material.BRICK.getId()]= BlockType.SOLID;
		types[Material.TNT.getId()]= BlockType.SOLID;
		types[Material.BOOKSHELF.getId()]= BlockType.SOLID;
		types[Material.MOSSY_COBBLESTONE.getId()]  = BlockType.SOLID;  	                                                                                    
		types[Material.OBSIDIAN.getId()]= BlockType.SOLID;
		types[Material.TORCH.getId()]= BlockType.NONSOLID;
		types[Material.FIRE.getId()]= BlockType.NONSOLID;
		types[Material.MOB_SPAWNER.getId()]= BlockType.SOLID;
		types[Material.WOOD_STAIRS.getId()]= BlockType.UNKNOWN;
		types[Material.CHEST.getId()]= BlockType.SOLID;
		types[Material.REDSTONE_WIRE.getId()]= BlockType.NONSOLID;
		types[Material.DIAMOND_ORE.getId()]= BlockType.SOLID;
		types[Material.DIAMOND_BLOCK.getId()]= BlockType.SOLID;
		types[Material.WORKBENCH.getId()]= BlockType.SOLID;
		types[Material.CROPS.getId()]= BlockType.NONSOLID;
		types[Material.SOIL.getId()]= BlockType.SOLID;
		types[Material.FURNACE.getId()]= BlockType.SOLID;
		types[Material.BURNING_FURNACE.getId()]= BlockType.SOLID;
		types[Material.SIGN_POST.getId()]= BlockType.NONSOLID;
		types[Material.WOODEN_DOOR.getId()]= BlockType.NONSOLID;
		types[Material.LADDER.getId()]= BlockType.LADDER;
		types[Material.RAILS.getId()]= BlockType.NONSOLID;
		types[Material.COBBLESTONE_STAIRS.getId()]= BlockType.UNKNOWN;
		types[Material.WALL_SIGN.getId()]= BlockType.NONSOLID;
		types[Material.LEVER.getId()]= BlockType.NONSOLID;
		types[Material.STONE_PLATE.getId()]= BlockType.UNKNOWN;
		types[Material.IRON_DOOR_BLOCK.getId()]= BlockType.NONSOLID;
		types[Material.WOOD_PLATE.getId()]= BlockType.NONSOLID;
		types[Material.REDSTONE_ORE.getId()]= BlockType.SOLID;
		types[Material.GLOWING_REDSTONE_ORE.getId()]= BlockType.SOLID;
		types[Material.REDSTONE_TORCH_OFF.getId()]= BlockType.NONSOLID;
		types[Material.REDSTONE_TORCH_ON.getId()]= BlockType.NONSOLID;
		types[Material.STONE_BUTTON.getId()]= BlockType.NONSOLID;
		types[Material.SNOW.getId()]= BlockType.UNKNOWN;
		types[Material.ICE.getId()]= BlockType.UNKNOWN;
		types[Material.SNOW_BLOCK.getId()]= BlockType.SOLID;
		types[Material.CACTUS.getId()]= BlockType.SOLID;
		types[Material.CLAY.getId()]= BlockType.SOLID;
		types[Material.SUGAR_CANE_BLOCK.getId()]= BlockType.NONSOLID;
		types[Material.JUKEBOX.getId()]= BlockType.SOLID;
		types[Material.FENCE.getId()]= BlockType.FENCE;
		types[Material.PUMPKIN.getId()]= BlockType.SOLID;
		types[Material.NETHERRACK.getId()]= BlockType.SOLID;
		types[Material.SOUL_SAND.getId()]= BlockType.UNKNOWN;
		types[Material.GLOWSTONE.getId()]= BlockType.SOLID;
		types[Material.PORTAL.getId()]= BlockType.NONSOLID;
		types[Material.JACK_O_LANTERN.getId()]= BlockType.SOLID;
		types[Material.CAKE_BLOCK.getId()]= BlockType.UNKNOWN;
	}
	
	public static MovingData get(Player p) {

		NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.moving == null) {
			data.moving = new MovingData();
		}

		return data.moving;
	}

}
