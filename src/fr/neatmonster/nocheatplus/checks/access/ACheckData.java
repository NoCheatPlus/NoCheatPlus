package fr.neatmonster.nocheatplus.checks.access;


/**
 * Abstract implementation to do nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckData implements ICheckData {

	@Override
	public boolean hasCachedPermissionEntry(String permission) {
		return false;
	}

	@Override
	public boolean hasCachedPermission(String permission) {
		return false;
	}

	@Override
	public void setCachedPermission(String permission, boolean value) {
	}
	
}
