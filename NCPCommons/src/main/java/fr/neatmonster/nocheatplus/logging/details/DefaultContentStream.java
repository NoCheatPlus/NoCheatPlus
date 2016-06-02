/*
 * This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
