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
import org.bukkit.event.block.BlockDamageEvent;
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
	
	public NukeCheck(NoCheat plugin, NoCheatConfiguration config) {

		super(plugin, "nuke", PermissionData.PERMISSION_NUKE, config);

	}

	@Override
	protected void configure(NoCheatConfiguration config) {

		
		
		try {
			kickMessage = config.getStringValue("nuke.kickmessage");
			logMessage = config.getStringValue("nuke.logmessage").
			replace("[player]", "%1$s");
			
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

		double x1 = (double)block.getX() - eyes.getX();
		double y1 = (double)block.getY() - eyes.getY();
		double z1 = (double)block.getZ() - eyes.getZ();

		double x2 = x1 + 1;
		double y2 = y1 + 1;
		double z2 = z1 + 1;

		double factor = 1 + direction.distance(new Vector(x1 + 0.5, y1 + 0.5, z1 + 0.5));
		double errorMargin = 1.2 / factor;
		

		if(factor * direction.getX() >= x1 - errorMargin && factor * direction.getY() >= y1 - errorMargin && factor * direction.getZ() >= z1 - errorMargin &&
		   factor * direction.getX() <= x2 + errorMargin && factor * direction.getY() <= y2 + errorMargin && factor * direction.getZ() <= z2 + errorMargin) {
			if(data.counter > 0) {
				data.counter--;
			}
		}
		else {
			data.counter++;
			event.setCancelled(true);
			
			if(data.counter > 20) {
				
				String log = String.format(Locale.US, logMessage, event.getPlayer().getName());
				
				plugin.log(Level.SEVERE, log);
				
				event.getPlayer().kickPlayer(kickMessage);
				data.counter = 0; // Reset to prevent problems on next login
			}
		}

		
	}

	public void check(BlockDamageEvent event) {

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
