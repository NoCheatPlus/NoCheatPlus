package fr.neatmonster.nocheatplus.logging.details;


/**
 * Contracts:
 * <li>Intended usage: register=seldom, log=often</li>
 * <li>Asynchronous access must be possible and fast, without locks (rather copy on write). </li>
 * @author dev1mc
 *
 * @param <C>
 */
public interface ContentStream <C> extends ContentLogger<C> {
    
    // TODO: Maybe also an abstract class.
    
    // Maybe not: addFilter (filter away some stuff, e.g. by regex from config).
    
    // TODO: Consider extra arguments for efficient registratioon with COWs.
    public void addNode(LogNode<C> node);
    
    // addAdapter(ContentAdapter<C, ?> adapter) ? ID etc., attach to another stream.
    
    // Removal and look up methods.
    
    /**
     * Remove all attached loggers and other.
     */
    public void clear();
    
}
