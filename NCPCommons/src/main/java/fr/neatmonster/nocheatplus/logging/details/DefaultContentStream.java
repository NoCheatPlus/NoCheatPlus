package fr.neatmonster.nocheatplus.logging.details;

import java.util.ArrayList;
import java.util.logging.Level;

/**
 * Locking is done with the internal node list, log does not block (extra to whatever loggers would do).
 * @author dev1mc
 *
 * @param <C>
 */
public class DefaultContentStream<C> implements ContentStream<C> {
    
    private ArrayList<LogNode<C>> nodes = new ArrayList<LogNode<C>>();
    
    private final LogNodeDispatcher dispatcher;
    
    public DefaultContentStream(LogNodeDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Override
    public void log(final Level level, final C content) {
        final ArrayList<LogNode<C>> nodes = this.nodes;
        for (int i = 0; i < nodes.size(); i++) {
            dispatcher.dispatch(nodes.get(i), level, content);
        }
    }

    @Override
    public void addNode(LogNode<C> node) {
        // TODO: Consider throwing things at callers, in case of duplicate logger entries?
        synchronized (nodes) {
            if (this.nodes.contains(node)) {
                // TODO: Consider throwing something.
                return;
            }
            ArrayList<LogNode<C>> nodes = new ArrayList<LogNode<C>>(this.nodes);
            nodes.add(node);
            this.nodes = nodes;
        }
    }

    @Override
    public void clear() {
        synchronized (nodes) {
            nodes.clear();
        }
    }
    
}
