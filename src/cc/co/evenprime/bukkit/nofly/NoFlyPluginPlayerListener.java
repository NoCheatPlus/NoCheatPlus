package cc.co.evenprime.bukkit.nofly;


import java.util.HashMap;
import java.util.Map;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerListener;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Handle events for all Player related events
 * @author Evenprime
 */

public class NoFlyPluginPlayerListener extends PlayerListener {
	
	/**
	 * Storage for data persistence between events
	 *
	 */
	public class NoFlyPluginData {
		
		/**
		 *  Don't rely on any of these yet, they are likely going to 
		 * change their name/functionality 
		 */
		private int phase = 0; // current jumpingPhase
		public long previousUpdate = 0; // timestamp of last event
		public int violations = 0; // number of cancelled events
		
		private NoFlyPluginData() { }
	}
	
    private final NoFlyPlugin plugin;
    
    // previously-calculated upper bound values for jumps. Minecraft is very deterministic when it comes to jumps
    // Each entry represents the maximum gain in height per move event.
    private static double jumpingPhases[] = new double[]{ 0.43D, 0.34D, 0.26D, 0.17D, 0.09D, 0.02D, 0.00D, -0.07D, -0.15D, -0.22D, -0.29D, -0.36D, -0.43D, -0.50D };
    
    // Very rough estimates
    private static double maxX = 0.5D;
    private static double maxZ = 0.5D;
    
    // Store data between Events
    private static Map<String, NoFlyPluginData> playerData = new HashMap<String, NoFlyPluginData>();
    

    public NoFlyPluginPlayerListener(NoFlyPlugin instance) {
        plugin = instance;
    }

    @Override
    public void onPlayerQuit(PlayerEvent event) {
    	playerData.remove(event.getPlayer().getName());
    }
    
    @Override
    public void onPlayerMove(PlayerMoveEvent event) {

    	// If someone cancelled the event already, ignore it
    	// If the player is inside a vehicle, ignore it for now
    	if(event.isCancelled() || event.getPlayer().isInsideVehicle()) {
    		return;
    	}
    	
    	
    	// Get the two locations of the event
		Location from = event.getFrom();
		Location to = event.getTo();
		
		// Get the player-specific data
		NoFlyPluginData data = null;
		
		if((data = playerData.get(event.getPlayer().getName())) == null ) {
			// If we have no data for the player, create some
			data = new NoFlyPluginData();
			playerData.put(event.getPlayer().getName(), data);
		}
		
		// Measure the time since the last move update by the player
		// Not used currently, but probably will be used in future
		long time = System.currentTimeMillis();
    	//System.out.print((time - data.previousUpdate) + ",");
    	data.previousUpdate = time;
    	
		// First check the distance the player has moved horizontally
    	// TODO: Make this check much more precise
		if(!event.isCancelled()) {
			double xDistance = Math.abs(from.getX() - to.getX());
			double zDistance = Math.abs(from.getZ() - to.getZ());
			
			if(xDistance > maxX || zDistance > maxZ) {
				event.setCancelled(true);
			}
		}
		
    	// If we didn't already cancel the event, check the vertical movement
    	if(!event.isCancelled()) {

    		// pre-calculate boundary values that are needed multiple times in the following checks
    		// the array each contains [lowerX, higherX, Y, lowerZ, higherZ]
    		int fromValues[] = {(int)Math.floor(from.getX() - 0.299999999D), (int)Math.floor(from.getX() + 0.299999999D), from.getBlockY(), (int)Math.floor(from.getZ() - 0.299999999D),(int)Math.floor(from.getZ() + 0.299999999D) };
    		int toValues[] = {(int)Math.floor(to.getX() - 0.299999999D), (int)Math.floor(to.getX() + 0.299999999D), to.getBlockY(), (int)Math.floor(to.getZ() - 0.299999999D),(int)Math.floor(to.getZ() + 0.299999999D) };

    		// compare locations to the world to guess if the player is standing on the ground, a half-block or next to a ladder
    		boolean onGroundFrom = playerIsOnGround(from.getWorld(), fromValues);
    		boolean onGroundTo = playerIsOnGround(from.getWorld(), toValues);
    			
    		// Both locations seem to be on solid ground or at a ladder
    		if(onGroundFrom && onGroundTo)
    		{
    			// reset jumping
    			data.phase = 0;
    			
    			// Check if the player isn't 'walking' up unrealistically far in one step
    			if(!(to.getY() - from.getY() <= 0.50001D)) {
        			event.setCancelled(true);
        		}
    		}
    		// player is starting to jump (or starting to fall down somewhere)
    		else if(onGroundFrom && !onGroundTo)
    		{	
    			// reset jumping
    			data.phase = 0;
    			
    			// Check if player isn't jumping too high
    			if(!(to.getY() - from.getY() < jumpingPhases[data.phase])) {
        			event.setCancelled(true);
        		}
    			else if(to.getY() <= from.getY()) {
    				// Very special case if running over a cliff and then immediately jumping. 
    				// Some sort of "air jump", MC allows it, so we have to do so too.
    			}
    			else data.phase++; // Setup next phase of the jump
    		}
    		// player is probably landing somewhere
    		else if(!onGroundFrom && onGroundTo)
    		{
    			// Check if player isn't landing to high (sounds weird, but has its use)
    			if(!(to.getY() - from.getY() < jumpingPhases[data.phase])) {
        			event.setCancelled(true);
        		}
    			else {
    				data.phase = 0; // He is on ground now, so reset the jump
    			}
    		}
    		// Player is moving through air (during jumping, falling)
    		else {
    			if(!(to.getY() - from.getY() < jumpingPhases[data.phase])) {
        			event.setCancelled(true);
        		}
				else data.phase++; // Enter next phase of the flight
    		}
    		
    		// do a security check on the jumping phase, such that we don't get 
    		// OutOfArrayBoundsExceptions at long air times (falling off high places)
    		if(!(data.phase < jumpingPhases.length)) {
    			data.phase = jumpingPhases.length - 1;
    		}
    	}
    	
    	/**
    	 * Teleport the player back to the last valid position
    	 */
    	if(event.isCancelled()) {
    		// Keep count of violations
    		data.violations++;
    		// Log the violation
    		NoFlyPlugin.log.info("NoFlyPlugin: At " + data.previousUpdate + " player "+event.getPlayer().getDisplayName()+" triggered. Total Violations: "+data.violations);
    		
    		//event.getPlayer().sendMessage("NoFlyPlugin violation "+data.violations);
    		
    		// Reset the player to his old location. This prevents him from getting stuck somewhere and/or getting
    		// out of sync with the server
    		event.getPlayer().teleportTo(event.getFrom());
    		
    		// To prevent players from getting stuck in an infinite loop
    		if(data.phase > 7) data.phase = 7; 
    	}
    }
    
    /**
     * Check the four edges of the player's approximated Bounding Box for blocks or ladders, 
     * at his own height (values[2]) and below his feet (values[2]-1).
     * If there is one, the player is considered as standing on it.
     * 
     * Not perfect at all and will produce some false negatives. Probably will be refined 
     * later.
     * 
     * @param w	The world the coordinates belong to
     * @param values The coordinates [lowerX, higherX, Y, lowerZ, higherZ]
     * @return
     */
    private boolean playerIsOnGround(World w, int values[]) {
 
    	if((w.getBlockAt(values[0], values[2]-1, values[3]).getType() != Material.AIR || 
    	   w.getBlockAt(values[0], values[2]-1, values[4]).getType() != Material.AIR ||
    	   w.getBlockAt(values[0], values[2], values[3]).getType() != Material.AIR || 
    	   w.getBlockAt(values[0], values[2], values[4]).getType() != Material.AIR) || 
    	   (values[0] != values[1] && // May save some time by skipping half of the tests
    	   (w.getBlockAt(values[1], values[2]-1, values[3]).getType() != Material.AIR ||
    	   w.getBlockAt(values[1], values[2]-1, values[4]).getType() != Material.AIR ||
    	   w.getBlockAt(values[1], values[2], values[3]).getType() != Material.AIR ||
    	   w.getBlockAt(values[1], values[2], values[4]).getType() != Material.AIR)))
    		return true;
    	else
    		return false;
    }
}
