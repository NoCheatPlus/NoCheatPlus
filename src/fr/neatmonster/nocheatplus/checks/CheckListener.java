package fr.neatmonster.nocheatplus.checks;

import fr.neatmonster.nocheatplus.components.NCPListener;

/**
 * This class provides naming etc for registration with ListenerManager.
 * For listeners registered by NoCheatPlus only.
 * @author mc_dev
 *
 */
public class CheckListener extends NCPListener{
	
	/** Check group / type which this listener is for. */
	protected final CheckType checkType;
	
	public CheckListener(){
		this(null);
	}

	public CheckListener(CheckType checkType){
		this.checkType = checkType; 
	}
	
	@Override
	public String getComponentName() {
		final String part = super.getComponentName();
		return checkType == null ? part : part + "_" + checkType.name();
	}
}
