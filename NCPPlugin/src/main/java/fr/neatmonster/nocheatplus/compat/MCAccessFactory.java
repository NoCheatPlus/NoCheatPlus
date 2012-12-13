package fr.neatmonster.nocheatplus.compat;

import fr.neatmonster.nocheatplus.compat.cb2511.MCAccessCB2511;
import fr.neatmonster.nocheatplus.compat.cb2512.MCAccessCB2512;

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
		try{
			return new MCAccessCB2511();
		}
		catch(Throwable t){};
		
		try{
			return new MCAccessCB2512();
		}
		catch(Throwable t){};
		
		throw new RuntimeException("Could not set up access to Minecraft API.");
	}
}
