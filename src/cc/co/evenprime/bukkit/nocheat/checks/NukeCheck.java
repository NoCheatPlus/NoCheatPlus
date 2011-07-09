package cc.co.evenprime.bukkit.nocheat.checks;

import java.util.Locale;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.event.Event;
import org.bukkit.event.Listener;
import org.bukkit.event.Event.Priority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.util.Vector;

import cc.co.evenprime.bukkit.nocheat.ConfigurationException;
import cc.co.evenprime.bukkit.nocheat.NoCheat;
import cc.co.evenprime.bukkit.nocheat.config.NoCheatConfiguration;
import cc.co.evenprime.bukkit.nocheat.data.NukeData;
import cc.co.evenprime.bukkit.nocheat.data.PermissionData;
import cc.co.evenprime.bukkit.nocheat.listeners.NukeBlockListener;


public class NukeCheck extends Check {

	private String kickMessage;
	private String logMessage;
	
	private boolean limitReach;
	
	public NukeCheck(NoCheat plugin, NoCheatConfiguration config) {

		super(plugin, "nuke", PermissionData.PERMISSION_NUKE, config);

	}

	@Override
	protected void configure(NoCheatConfiguration config) {

		
		
		try {
			kickMessage = config.getStringValue("nuke.kickmessage");
			logMessage = config.getStringValue("nuke.logmessage").
			replace("[player]", "%1$s");
			
			limitReach = config.getBooleanValue("nuke.limitreach");
			
			setActive(config.getBooleanValue("active.nuke"));
		} catch (ConfigurationException e) {
			setActive(false);
			e.printStackTrace();
		}
	}

	public void check(BlockBreakEvent event) {

		if(skipCheck(event.getPlayer())) {
			return;
		}
		
		NukeData data = NukeData.get(event.getPlayer());
		
		Block block = event.getBlock();

		Location eyes = event.getPlayer().getEyeLocation();
		Vector direction = eyes.getDirection();

		// Because it's not very precise on very short distances, 
		// consider the length of the side of a block to be 2.0 instead of 1.0
		final double x1 = ((double)block.getX()) - eyes.getX() - 0.5;
		final double y1 = ((double)block.getY()) - eyes.getY() - 0.5;
		final double z1 = ((double)block.getZ()) - eyes.getZ() - 0.5;

		final double x2 = x1 + 2;
		final double y2 = y1 + 2;
		final double z2 = z1 + 2;

		double factor = new Vector(x1 + 1, y1 + 1, z1 + 1).length();
		
		boolean tooFarAway = limitReach && factor > 4.85D;
		
		if(!tooFarAway && factor * direction.getX() >= x1 && factor * direction.getY() >= y1 && factor * direction.getZ() >= z1 &&
		   factor * direction.getX() <= x2 && factor * direction.getY() <= y2 && factor * direction.getZ() <= z2) {
			if(data.counter > 0) {
				data.counter--;
			}
		}
		else {
			data.counter++;
			event.setCancelled(true);
			
			if(data.counter > 10) {
				
				String log = String.format(Locale.US, logMessage, event.getPlayer().getName());
				
				plugin.log(Level.SEVERE, log);
				
				event.getPlayer().kickPlayer(kickMessage);
				data.counter = 0; // Reset to prevent problems on next login
			}
		}

		
	}


	@Override
	protected void registerListeners() {
		PluginManager pm = Bukkit.getServer().getPluginManager();

		Listener blockListener = new NukeBlockListener(this);

		// Register listeners for moving check
		pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Lowest, plugin);
		pm.registerEvent(Event.Type.BLOCK_DAMAGE, blockListener, Priority.Monitor, plugin);


	}

}
