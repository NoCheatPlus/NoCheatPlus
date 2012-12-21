package fr.neatmonster.nocheatplus.compat;

import java.util.ArrayList;
import java.util.List;

import fr.neatmonster.nocheatplus.compat.cb2511.MCAccessCB2511;
import fr.neatmonster.nocheatplus.compat.cb2512.MCAccessCB2512;
import fr.neatmonster.nocheatplus.compat.cb2545.MCAccessCB2545;
import fr.neatmonster.nocheatplus.utilities.LogUtil;

/**
 * Factory class to hide potentially dirty stuff.
 * @author mc_dev
 *
 */
public class MCAccessFactory {
	
	/**
	 * @throws RuntimeException if no access can be set.
	 * @return
	 */
	public MCAccess getMCAccess(){
		final List<Throwable> throwables = new ArrayList<Throwable>();
		try{
			return new MCAccessCB2511();
		}
		catch(Throwable t){
			throwables.add(t);
		};
		
		try{
			return new MCAccessCB2512();
		}
		catch(Throwable t){
			throwables.add(t);
		};
		try{
			return new MCAccessCB2545();
		}
		catch(Throwable t){
			throwables.add(t);
		};
		LogUtil.logSevere("[NoCheatPlus] Could not set up MC version specific access.");
		for (Throwable t : throwables ){
			LogUtil.logSevere(t);
		}
		throw new RuntimeException("Could not set up access to Minecraft API.");
	}
}
