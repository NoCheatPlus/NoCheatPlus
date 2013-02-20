package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.NoCheatPlus;
import fr.neatmonster.nocheatplus.compat.MCAccess;
import fr.neatmonster.nocheatplus.components.MCAccessHolder;
import fr.neatmonster.nocheatplus.components.NCPListener;

/**
 * This class provides naming etc for registration with ListenerManager.
 * For listeners registered by NoCheatPlus only.
 * @author mc_dev
 *
 */
public class CheckListener extends NCPListener implements MCAccessHolder{
	
	/** Check group / type which this listener is for. */
	protected final CheckType checkType;
	protected MCAccess mcAccess;
	
	public CheckListener(){
		this(null);
	}

	public CheckListener(CheckType checkType){
		this.checkType = checkType; 
		this.mcAccess = NoCheatPlus.getMCAccess();
	}
	
	@Override
	public String getComponentName() {
		final String part = super.getComponentName();
		return checkType == null ? part : part + "_" + checkType.name();
	}

	@Override
	public void setMCAccess(MCAccess mcAccess) {
		this.mcAccess = mcAccess;
	}

	@Override
	public MCAccess getMCAccess() {
		return mcAccess;
	}
	
	/**
	 * Convenience method to add checks as components to NCP.
	 * @param check
	 * @return The given Check instance, for chaining.
	 */
	protected <C extends Check>  C addCheck(C check){
		// Could also set up a map from check type to check, etc.
		NoCheatPlus.getAPI().addComponent(check);
		return check;
	}
}
