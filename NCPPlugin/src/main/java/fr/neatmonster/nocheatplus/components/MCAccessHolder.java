package fr.neatmonster.nocheatplus.components;

import fr.neatmonster.nocheatplus.compat.MCAccess;

/**
 * MCAccessHolder will be updated automatically with the current MCAccess.
 * <br>How to name this...
 * @author mc_dev
 *
 */
public interface MCAccessHolder {
	/**
	 * Set access.
	 * @param mcAccess
	 */
	public void setMCAccess(MCAccess mcAccess);
	
	/**
	 * Getter.
	 * @return
	 */
	public MCAccess getMCAccess();
}
