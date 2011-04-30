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
	public static final int SOLID = 0;
	public static final int NONSOLID = 1;
	public static final int LADDER = 2;
	public static final int LIQUID = 3;
	public static final int UNKNOWN = 4;
	public static final int FENCE = 5;

	
	// Until I can think of a better way to determine if a block is solid or not, this is what I'll do
	public static final int types[] = new int[256];
	
	static {

		for(int i = 0; i < types.length; i++) {
			types[i] = UNKNOWN;
		}

		types[Material.AIR.getId()] = NONSOLID;
		types[Material.STONE.getId()] = SOLID;
		types[Material.GRASS.getId()] = SOLID;
		types[Material.DIRT.getId()] = SOLID;
		types[Material.COBBLESTONE.getId()] = SOLID;
		types[Material.WOOD.getId()] = SOLID;
		types[Material.SAPLING.getId()] = NONSOLID;
		types[Material.BEDROCK.getId()] = SOLID;
		types[Material.WATER.getId()] = LIQUID;
		types[Material.STATIONARY_WATER.getId()] = LIQUID;
		types[Material.LAVA.getId()] = LIQUID;
		types[Material.STATIONARY_LAVA.getId()] = LIQUID;
		types[Material.SAND.getId()] = SOLID;
		types[Material.GRAVEL.getId()] = SOLID;
		types[Material.GOLD_ORE.getId()] = SOLID;
		types[Material.IRON_ORE.getId()] = SOLID;
		types[Material.COAL_ORE.getId()] = SOLID;
		types[Material.LOG.getId()] = SOLID;
		types[Material.LEAVES.getId()] = SOLID;
		types[Material.SPONGE.getId()] = SOLID;
		types[Material.GLASS.getId()] = SOLID;
		types[Material.LAPIS_ORE.getId()] = SOLID;
		types[Material.LAPIS_BLOCK.getId()] = SOLID;
		types[Material.DISPENSER.getId()] = SOLID;
		types[Material.SANDSTONE.getId()] = SOLID;
		types[Material.NOTE_BLOCK.getId()]= SOLID;
		types[Material.WOOL.getId()]= SOLID;
		types[Material.YELLOW_FLOWER.getId()]= NONSOLID;
		types[Material.RED_ROSE.getId()]= NONSOLID;
		types[Material.BROWN_MUSHROOM.getId()]= NONSOLID;
		types[Material.RED_MUSHROOM.getId()]= NONSOLID;
		types[Material.GOLD_BLOCK.getId()]= SOLID;
		types[Material.IRON_BLOCK.getId()]= SOLID;
		types[Material.DOUBLE_STEP.getId()]= UNKNOWN;
		types[Material.STEP.getId()]= UNKNOWN;
		types[Material.BRICK.getId()]= SOLID;
		types[Material.TNT.getId()]= SOLID;
		types[Material.BOOKSHELF.getId()]= SOLID;
		types[Material.MOSSY_COBBLESTONE.getId()]  = SOLID;  	                                                                                    
		types[Material.OBSIDIAN.getId()]= SOLID;
		types[Material.TORCH.getId()]= NONSOLID;
		types[Material.FIRE.getId()]= NONSOLID;
		types[Material.MOB_SPAWNER.getId()]= SOLID;
		types[Material.WOOD_STAIRS.getId()]= UNKNOWN;
		types[Material.CHEST.getId()]= SOLID;
		types[Material.REDSTONE_WIRE.getId()]= NONSOLID;
		types[Material.DIAMOND_ORE.getId()]= SOLID;
		types[Material.DIAMOND_BLOCK.getId()]= SOLID;
		types[Material.WORKBENCH.getId()]= SOLID;
		types[Material.CROPS.getId()]= NONSOLID;
		types[Material.SOIL.getId()]= SOLID;
		types[Material.FURNACE.getId()]= SOLID;
		types[Material.BURNING_FURNACE.getId()]= SOLID;
		types[Material.SIGN_POST.getId()]= NONSOLID;
		types[Material.WOODEN_DOOR.getId()]= NONSOLID;
		types[Material.LADDER.getId()]= LADDER;
		types[Material.RAILS.getId()]= NONSOLID;
		types[Material.COBBLESTONE_STAIRS.getId()]= UNKNOWN;
		types[Material.WALL_SIGN.getId()]= NONSOLID;
		types[Material.LEVER.getId()]= NONSOLID;
		types[Material.STONE_PLATE.getId()]= UNKNOWN;
		types[Material.IRON_DOOR_BLOCK.getId()]= NONSOLID;
		types[Material.WOOD_PLATE.getId()]= NONSOLID;
		types[Material.REDSTONE_ORE.getId()]= SOLID;
		types[Material.GLOWING_REDSTONE_ORE.getId()]= SOLID;
		types[Material.REDSTONE_TORCH_OFF.getId()]= NONSOLID;
		types[Material.REDSTONE_TORCH_ON.getId()]= NONSOLID;
		types[Material.STONE_BUTTON.getId()]= NONSOLID;
		types[Material.SNOW.getId()]= UNKNOWN;
		types[Material.ICE.getId()]= UNKNOWN;
		types[Material.SNOW_BLOCK.getId()]= SOLID;
		types[Material.CACTUS.getId()]= SOLID;
		types[Material.CLAY.getId()]= SOLID;
		types[Material.SUGAR_CANE_BLOCK.getId()]= NONSOLID;
		types[Material.JUKEBOX.getId()]= SOLID;
		types[Material.FENCE.getId()]= FENCE;
		types[Material.PUMPKIN.getId()]= SOLID;
		types[Material.NETHERRACK.getId()]= SOLID;
		types[Material.SOUL_SAND.getId()]= UNKNOWN;
		types[Material.GLOWSTONE.getId()]= SOLID;
		types[Material.PORTAL.getId()]= NONSOLID;
		types[Material.JACK_O_LANTERN.getId()]= SOLID;
		types[Material.CAKE_BLOCK.getId()]= UNKNOWN;
	}
	
	public static MovingData get(final Player p) {

		final NoCheatData data = NoCheatData.getPlayerData(p);

		if(data.moving == null) {
			data.moving = new MovingData();
			data.moving.lastLocation = p.getLocation();
		}

		return data.moving;
	}

}
