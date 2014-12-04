package fr.neatmonster.nocheatplus.checks.access;

/**
 * Abstract implementation to do nothing.
 * @author mc_dev
 *
 */
public abstract class ACheckData implements ICheckData {

    public boolean debug; // TODO: Might make private.
    
    public ACheckData(ICheckConfig config) {
        setDebug(config.getDebug());
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean getDebug() {
        return debug;
    }

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
