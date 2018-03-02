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
package fr.neatmonster.nocheatplus.event.mini;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

import fr.neatmonster.nocheatplus.components.registry.feature.ComponentWithName;
import fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.AbstractRegistrationOrderSort;
import fr.neatmonster.nocheatplus.logging.StaticLog;
import fr.neatmonster.nocheatplus.logging.Streams;
import fr.neatmonster.nocheatplus.utilities.StringUtil;

/**
 * Hold MiniListener instances - sort by RegistrationOrder - allow thread-safe
 * reading for actual event handling.
 * 
 * @author asofold
 *
 * @param <E>
 * @param <P>
 */
public class MiniListenerNode<E, P> {

    // TODO: Pass lock and SortedItemStore(Node), lazily (?) get.

    /**
     * This is intended to be "complete" in terms of containing all information
     * for order, extra properties like ignoreCancelled.
     * 
     * @author asofold
     * 
     * @param <E>
     */
    protected static class ListenerEntry<E> implements IGetRegistrationOrder {

        public final MiniListener<E> listener;
        public final boolean ignoreCancelled;
        private final RegistrationOrder order;

        public ListenerEntry(MiniListener<E> listener, boolean ignoreCancelled, RegistrationOrder order) {
            this.listener = listener;
            this.ignoreCancelled = ignoreCancelled;
            this.order = order == null ? new RegistrationOrder() : new RegistrationOrder(order);
        }

        @Override
        public RegistrationOrder getRegistrationOrder() {
            return order;
        }
    }

    protected static final class SortListenerEntry<E> extends AbstractRegistrationOrderSort<ListenerEntry<E>> {
        @Override
        protected RegistrationOrder fetchRegistrationOrder(ListenerEntry<E> item) {
            return item.getRegistrationOrder();
        }
    };

    protected final SortListenerEntry<E> typedSort = new SortListenerEntry<E>();

    protected final List<ListenerEntry<E>> registeredListeners = new ArrayList<ListenerEntry<E>>();

    @SuppressWarnings("unchecked")
    protected ListenerEntry<E>[] sortedListeners = new ListenerEntry[0];

    protected final Class<E> baseType;
    /**
     * Stored for the case of exceptions.
     */
    protected final P basePriority;

    public MiniListenerNode(Class<E> baseType, P basePriority) {
        this.baseType = baseType;
        this.basePriority = basePriority;
    }

    /**
     * Remove all listeners.
     */
    @SuppressWarnings("unchecked")
    public void clear() {
        registeredListeners.clear();
        sortedListeners = new ListenerEntry[0];
    }

    public void removeMiniListener(MiniListener<E> listener) {
        boolean changed = false;
        final Iterator<ListenerEntry<E>> it = registeredListeners.iterator();
        while (it.hasNext()) {
            if (it.next().listener == listener) {
                it.remove();
                changed = true;
                break; // TODO: Ensure register once or remove all or what not.
            }
        }
        if (changed) {
            generateSortedListeners();
        }
    }

    public void addMiniListener(MiniListener<E> listener, boolean ignoreCancelled, RegistrationOrder order) {
        registeredListeners.add(new ListenerEntry<E>(listener, ignoreCancelled, order));
        generateSortedListeners();
    }

    protected void generateSortedListeners() {
        // TODO: Allow postponing sorting.
        // TODO: Store an optimally ordered thing? (Now entries in registeredListeners are within order of registration.)
        if (registeredListeners.isEmpty()) {
            clear();
        }
        else  {
            final Object[] sortedOdd = typedSort.getSortedArray(registeredListeners);
            @SuppressWarnings("unchecked")
            final ListenerEntry<E>[] sortedListeners = new ListenerEntry[sortedOdd.length];
            System.arraycopy(sortedOdd, 0, sortedListeners, 0, sortedOdd.length);
            this.sortedListeners = sortedListeners;
        }
    }

    /**
     * Override to implement events that can be cancelled.
     * 
     * @param event
     */
    protected boolean isCancelled(E event) {
        return false;
    }

    public void onEvent(final E event) {
        // Go through mini listeners....
        // Note that cancelled events get in here too.
        final ListenerEntry<E>[] listeners = this.sortedListeners; // Allow concurrent update.
        for (int i = 0; i < listeners.length; i++) {
            final ListenerEntry<E> entry = listeners[i];
            if (!entry.ignoreCancelled || !isCancelled(event)) {
                try {
                    entry.listener.onEvent(event);
                }
                catch (Throwable t) {
                    // TODO: More fine grained catch.
                    logListenerException(entry, i, listeners.length, event, t);
                }
            }
        }
    }

    private void logListenerException(final ListenerEntry<E> entry, 
            final int index, final int length, 
            final E event, final Throwable t) {
        // Log long part once, to keep spam slightly down.
        // TODO: Add more info (ORDER with tags, class/wrapped class).
        final StringBuilder builder = new StringBuilder(1024);
        builder.append(" Details:");
        builder.append(" listenerType=" + entry.listener.getClass().getName());
        builder.append(" listenerOrder=" + entry.order);
        builder.append(" listenerIndex=" + index + "/" + length);
        if (entry.listener instanceof ComponentWithName) {
            builder.append(" listenerName=");
            builder.append(((ComponentWithName) entry.listener).getComponentName());
        }
        builder.append("\n exception:\n");
        builder.append(StringUtil.throwableToString(t));
        final Set<Throwable> done = new HashSet<Throwable>();
        done.add(t);
        Throwable cause = t.getCause();
        while (cause != null && !done.contains(cause)) {
            done.add(cause);
            builder.append("\n caused by:\n");
            builder.append(StringUtil.throwableToString(cause));
            cause = t.getCause();
        }
        // TODO: Add id information to compare to registry log (later).
        StaticLog.logOnce(Streams.STATUS, Level.SEVERE, 
                "Listener exception: baseType=" + baseType.getName() 
                + " basePriority=" + this.basePriority
                + " eventType=" + event.getClass().getName(),
                builder.toString());
    }

}
