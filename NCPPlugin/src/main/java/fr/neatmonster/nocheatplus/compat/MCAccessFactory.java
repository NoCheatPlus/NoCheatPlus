package fr.neatmonster.nocheatplus.compat;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.compat.bukkit.MCAccessBukkit;
import fr.neatmonster.nocheatplus.compat.cb2511.MCAccessCB2511;
import fr.neatmonster.nocheatplus.compat.cb2512.MCAccessCB2512;
import fr.neatmonster.nocheatplus.compat.cb2545.MCAccessCB2545;
import fr.neatmonster.nocheatplus.compat.cb2602.MCAccessCB2602;
import fr.neatmonster.nocheatplus.compat.cb2645.MCAccessCB2645;
import fr.neatmonster.nocheatplus.compat.cb2691.MCAccessCB2691;
import fr.neatmonster.nocheatplus.compat.cb2763.MCAccessCB2763;
import fr.neatmonster.nocheatplus.compat.cb2794.MCAccessCB2794;
import fr.neatmonster.nocheatplus.compat.cbdev.MCAccessCBDev;
import fr.neatmonster.nocheatplus.config.ConfPaths;
import fr.neatmonster.nocheatplus.config.ConfigManager;
import fr.neatmonster.nocheatplus.logging.LogUtil;

/**
 * Factory class to hide potentially dirty stuff.
 * @author mc_dev
 *
 */
public class MCAccessFactory {
	
	private final String[] updateLocs = new String[]{
		"[NoCheatPlus]  Check for updates at BukkitDev: http://dev.bukkit.org/server-mods/nocheatplus/",
		"[NoCheatPlus]  Development builds: http://ci.ecocitycraft.com/job/NoCheatPlus/",
//		"[NoCheatPlus]  Development builds: http://nocheatplus.org:8080/job/NoCheatPlus/",
	};
	
	/**
	 * Get a new MCAccess instance using the config value for ConfPaths.COMPATIBILITY_BUKKITONLY.
	 * @return MCAccess instance.
	 * @throws RuntimeException if no access can be set.
	 */
	public MCAccess getMCAccess(){
		return getMCAccess(ConfigManager.getConfigFile().getBoolean(ConfPaths.COMPATIBILITY_BUKKITONLY));
	}
	
	/**
	 * Get a new MCAccess instance.
	 * @param bukkitOnly Set to true to force using an API-only module.
	 * @return
	 * @throws RuntimeException if no access can be set.
	 */
	public MCAccess getMCAccess(final boolean bukkitOnly){
		final List<Throwable> throwables = new ArrayList<Throwable>();
		
		// Try to set up native access.
		if (!bukkitOnly){
			
			// TEST //
			// Only add as long as no stable module has been added.
			// 1.6.2
			try{
				return new MCAccessCBDev();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			// TEST END //
			
			// 1.6.1
			try{
				return new MCAccessCB2794();
			}
			catch(Throwable t){
				throwables.add(t);
			};
						
			// 1.5.2
			try{
				return new MCAccessCB2763();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.5.1 (cb beta)
			try{
				return new MCAccessCB2691();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.5
			try{
				return new MCAccessCB2645();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.4.7
			try{
				return new MCAccessCB2602();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.4.6
			try{
				return new MCAccessCB2545();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.4.5-R1.0
			try{
				return new MCAccessCB2512();
			}
			catch(Throwable t){
				throwables.add(t);
			};
			
			// 1.4.2 ... 1.4.5 (up to CB2511).
			try{
				return new MCAccessCB2511();
			}
			catch(Throwable t){
				throwables.add(t);
			};
		}
		
		// Try to set up api-only access (since 1.4.6).
		try{
			final String msg;
			if (bukkitOnly){
				msg = "[NoCheatPlus] The plugin is configured for Bukkit-API-only access.";
			}
			else{
				msg = "[NoCheatPlus] Could not set up native access for your specific Minecraft + server-mod version.";
			}
			LogUtil.logWarning(msg);
			final MCAccess mcAccess = new MCAccessBukkit();
			LogUtil.logWarning("[NoCheatPlus] API-only MCAccess: Some features will likely not function properly, performance might suffer.");
			for (String uMsg : updateLocs){
				LogUtil.logWarning(uMsg);
			}
			return mcAccess;
		}
		catch(Throwable t){
			throwables.add(t);
		};
		
		// All went wrong.
		// TODO: Fall-back solution (disable plugin, disable checks).
		LogUtil.logSevere("[NoCheatPlus] Your version of NoCheatPlus does not seem to provide support for either your Minecraft version or your specific server-mod.");
		for (String msg : updateLocs){
			LogUtil.logSevere(msg);
		}
		LogUtil.logSevere("[NoCheatPlus] Could not set up MC version specific access.");
		for (Throwable t : throwables ){
			LogUtil.logSevere(t);
		}
		throw new RuntimeException("Could not set up access to Minecraft API.");
	}
}
