package fr.neatmonster.nocheatplus.components.registry.store;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.components.registry.exception.AlreadyRegisteredException;
import fr.neatmonster.nocheatplus.components.registry.order.IGetRegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.IRegisterWithOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegisterWithOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder;
import fr.neatmonster.nocheatplus.components.registry.order.RegistrationOrder.AbstractRegistrationOrderSort;
import fr.neatmonster.nocheatplus.components.registry.order.SetupOrder;

/**
 * Keep sorted lists of registered (generic) items by type (support
 * RegistrationOrder, IRegisterWithOrder, RegisterWithOrder, possibly
 * other/deprecated). This is an internal registry object, not meant for direct
 * external manipulation. Registering the same instance for several class types
 * is possible. All registered items should differ by equals (!).
 * 
 * @author asofold
 *
 */
public class RegisteredItemStore {

    // TODO: Interface

    /**
     * Newly created Order object and item instance. Allows sorting by
     * RegistrationOrder (via
     * {@link RegistrationOrder#sortIGetExtendsRegistrationOrder}), as well as
     * sorting by internalId via the Comparable interface.
     * 
     * @author asofold
     *
     */
    static class ItemNode <T> implements IGetRegistrationOrder, Comparable<ItemNode<T>> {

        // TODO: implement IGetItem

        final RegistrationOrder order;
        final T item;

        /**
         * A counter used to distinguish entries of otherwise equaling order.
         * Always differs.
         */
        final int internalCount;

        <I extends T> ItemNode(RegistrationOrder order, I item, int internalCount) {
            this.order = order;
            this.item = item;
            this.internalCount = internalCount;
        }

        @Override
        public RegistrationOrder getRegistrationOrder() {
            return order;
        }

        @Override
        public int compareTo(ItemNode<T> o) {
            return Integer.compare(internalCount, o.internalCount);
        }

    }

    static final class SortItemNode<T> extends AbstractRegistrationOrderSort<ItemNode<T>> {
        @Override
        protected RegistrationOrder fetchRegistrationOrder(ItemNode<T> item) {
            return item.getRegistrationOrder();
        }
    };

    static final class ItemList <T> {
        /** I bit heavy on the tip of the blade, java. */
        private final SortItemNode<T> typedSort = new SortItemNode<T>();
        // TODO: always fetch an array and store as sorted.
        private final List<ItemNode<T>> itemNodes = new LinkedList<ItemNode<T>>();
        private List<ItemNode<T>> sortedItemNodes = null;

        /**
         * For lazy sorting.
         */
        void sort() {
            // TODO: Might create the typed sort on the fly, instead of storing it ...
            typedSort.sort(itemNodes);
            // TODO: extra sorted list (better: array once available), preserves order of registration.
            sortedItemNodes = null;
        }

        List<T> getSortedItemsCopyList() {
            if (sortedItemNodes == null) {
                sort();
            }
            final List<T> out = new LinkedList<T>();
            for (final ItemNode<T> node : sortedItemNodes) {
                out.add(node.item);
            }
            return out;
        }

        /**
         * 
         * @param order
         * @param item Not null.
         * @param internalCount
         */
        void register(final RegistrationOrder order, final T item, final int internalCount) {
            itemNodes.add(new ItemNode<T>(order, item, internalCount));
            sortedItemNodes = null;
        }

        /**
         * 
         * @param item
         * @return True if the list contained the item.
         */
        boolean unregister(final T item) {
            // (Sorting order should not change, if it were removed from all lists alike (!))
            final Iterator<ItemNode<T>> it = itemNodes.iterator();
            while (it.hasNext()) {
                // TODO: equals or ==
                if (it.next().item.equals(item)) {
                    sortedItemNodes = null;
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        /**
         * At your own risk :).
         * @param item
         * @return
         */
        @SuppressWarnings("unchecked")
        boolean unregisterObject(final Object item) {
            return unregister((T) item);
        }

        boolean isRegisteredObject(final Object item) {
            for (ItemNode<?> node : itemNodes) {
                if (item.equals(node.item)) {
                    return true;
                }
            }
            return false;
        }

    }

    // TODO: Pre-register allowed types ?
    // TODO: Thread safety on fetch / version with (abstract class with abstract methods to access store.)?

    /** Registered items in self-sorting ItemListS by class. */
    private final Map<Class<?>, ItemList<?>> itemListMap = new HashMap<Class<?>, ItemList<?>>();
    /**
     * Efficiently keep track of already registered items (registration of the
     * same instance for multiple types is possible).
     */
    private final Map<Object, Set<Class<?>>> items = new HashMap<Object, Set<Class<?>>>();

    private int internalCount = 0; // TODO: Might support a counter object, passed from extern.

    /**
     * Convenience method to register without explicitly passing a
     * RegistrationOrder instance. See
     * {@link #register(Class, Object, RegistrationOrder)}.
     * 
     * @param type
     *            The type to register the item for.
     * @param item
     *            The item to register for the type.
     * @throws NullPointerException
     *             If either of item or type is null.
     * @throws AlreadyRegisteredException
     *             If the item is already registered for that type.
     */
    public <T, I extends T> void register(Class<T> type, I item) {
        register(type, item, null);
    }

    /**
     * Register an item for the given type. If no order can be found, an attempt
     * is made to fetch order from implemented interfaces or annotations, as a
     * fall-back the indifferent default priority is used.
     * 
     * @param type
     *            The type to register the item for.
     * @param item
     *            The item to register for the type.
     * @param order
     *            If null, it will be attempted to fetch the order by
     *            interfaces/annotation. If none is found the default
     *            indifferent order will be used.
     * @throws NullPointerException
     *             If either of item or type is null.
     * @throws AlreadyRegisteredException
     *             if the item is already registered for that type.
     */
    public <T, I extends T> void register(Class<T> type, I item, RegistrationOrder order) {
        if (item == null) {
            throw new NullPointerException("Item must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("Type must not be null.");
        }
        // Check if already registered -> AlreadyRegisteredException
        Set<Class<?>> registeredFor = items.get(item);
        if (registeredFor != null && registeredFor.contains(type)) {
            throw new AlreadyRegisteredException("Already registered for type: " + type.getName());
        }
        // Ensure to have a RegistrationOrder instance, copy external ones.
        // (No merging of information is done here.)
        // TODO: Try/Catch ?
        // TODO: (Perhaps not ListenerOrder...)
        // Check the most specific interface.
        if (order == null && item instanceof IRegisterWithOrder) {
            order = ((IRegisterWithOrder) item).getRegistrationOrder(type);
        }
        if (order == null && item instanceof IGetRegistrationOrder) {
            // Better support this to avoid confusion (?).
            order = ((IGetRegistrationOrder) item).getRegistrationOrder();
        }
        // 
        if (order == null) {
            // Check Annotations.
            RegisterWithOrder annoOrder = item.getClass().getAnnotation(RegisterWithOrder.class);
            SetupOrder setupOrder = item.getClass().getAnnotation(SetupOrder.class); // To be deprecated.
            if (annoOrder != null) {
                order = new RegistrationOrder(annoOrder);
            }
            else if (setupOrder != null) {  // To be deprecated.
                order = new RegistrationOrder(setupOrder);
            }
            else {
                // Default order.
                order = RegistrationOrder.DEFAULT_ORDER;
            }
        }
        else {
            // Copy what has been found outside, huh.
            order = new RegistrationOrder(order);
        }

        @SuppressWarnings("unchecked")
        ItemList<T> itemList = (ItemList<T>) itemListMap.get(type);
        if (itemList == null) {
            itemList = new ItemList<T>();
            itemListMap.put(type, itemList);
        }
        itemList.register(order, item, ++internalCount);
        if (registeredFor == null) {
            registeredFor = new HashSet<Class<?>>();
            items.put(item, registeredFor);
        }
        registeredFor.add(type);
    }

    /**
     * Unregister the item from the given type. It can still stay registered for
     * other types.
     * 
     * @param type
     * @param item
     * @return True, if the item has been removed for this type.
     * @throws NullPointerException
     *             If either of item or type is null.
     */
    public <T, I extends T> boolean unregister(final Class<T> type, final I item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("Type must not be null.");
        }
        int count = 0;
        final Set<Class<?>> registeredFor = items.get(item);
        if (registeredFor != null) {
            if (registeredFor.remove(type)) {
                count ++;
                if (registeredFor.isEmpty()) {
                    items.remove(item);
                }
            }
        }
        @SuppressWarnings("unchecked")
        final ItemList<T> itemList = (ItemList<T>) itemListMap.get(type);
        if (itemList != null) {
            if (itemList.unregister(item)) {
                count ++;
                if (itemList.itemNodes.isEmpty()) {
                    itemListMap.remove(type);
                }
            }
        }
        // TODO: If count is 1, throw some IllegalRegistryState (extends FatalRegistryException)?
        /*
         * TODO: ItemNotRegisteredException ? Not certain this is intended -
         * could make that configurable (constructor or otherwise).
         */
        return count > 0; // Was contained (consistently or not).
    }

    /**
     * Unregister item from all types it is registered for.
     * 
     * @param type
     * @param item
     * @return True, if the item has been removed for this type.
     * @throws NullPointerException
     *             If either of item or type is null.
     */
    public boolean unregister(final Object item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null.");
        }
        /*
         * TODO: ItemNotRegisteredException ? Not certain this is intended -
         * could make that configurable (constructor or otherwise).
         */
        final Set<Class<?>> registeredFor = items.remove(item);
        if (registeredFor == null) {
            return false;
        }
        // TODO: Track if consistent?
        for (Class<?> type : registeredFor) {
            final ItemList<?> itemList = itemListMap.get(type);
            if (itemList != null) {
                if (itemList.unregisterObject(item)) {
                    if (itemList.itemNodes.isEmpty()) {
                        itemListMap.remove(type);
                    }
                }
            }
            // TODO: What if null or not contained - throw / notice somehow?
        }
        return true; // Was contained (consistently or not).
    }

    /**
     * Unregister all items just from this type.
     * 
     * @param type
     * @return
     * @throws NullPointerException
     *             If type is null.
     */
    public boolean unregister(final Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Type must not be null.");
        }
        final ItemList<?> itemList = itemListMap.remove(type);
        if (itemList == null) {
            return false;
        }
        for (ItemNode<?> node : itemList.itemNodes) {
            final Object item = node.item;
            final Set<Class<?>> registeredFor = items.get(item);
            if (registeredFor != null) {
                registeredFor.remove(type);
                if (registeredFor.isEmpty()) {
                    items.remove(item);
                }
            }
        }
        return true;
    }

    /**
     * Test if an item is registered for a given type.
     * 
     * @param type
     * @param item
     * @return
     * @throws NullPointerException
     *             If either of item or type is null.
     */
    public <T, I extends T> boolean isRegistered(Class<T> type, I item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null.");
        }
        if (type == null) {
            throw new NullPointerException("Type must not be null.");
        }
        if (items.containsKey(item)) {
            final ItemList<?> itemList = itemListMap.get(type);
            return itemList != null && itemList.isRegisteredObject(item);
        }
        else {
            return false;
        }
    }

    /**
     * Test if anything is registered for this type.
     * 
     * @param type
     * @return
     * @throws NullPointerException
     *             If type is null.
     */
    public boolean isRegistered(Class<?> type) {
        if (type == null) {
            throw new NullPointerException("Type must not be null.");
        }
        return itemListMap.containsKey(type);
    }

    /**
     * Test if an item is registered at all.
     * 
     * @param item
     * @return
     * @throws NullPointerException
     *             If item is null.
     */
    public boolean isRegistered(Object item) {
        if (item == null) {
            throw new NullPointerException("Item must not be null.");
        }
        return items.containsKey(item);
    }

    /**
     * Ensure all contained lists are sorted and optimized.
     */
    public void sortAll() {
        for (final ItemList<?> itemList : itemListMap.values()) {
            if (itemList.sortedItemNodes == null) {
                itemList.sort();
            }
        }
    }

    /**
     * Get a new list with all registered items (ordered).
     * 
     * @param type
     *            The type items are supposed to be registered for.
     * @return A new list of the registered items. Note that this list does not
     *         contain extra information like Order, in case that had been given
     *         at the time of registration.
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getSortedItemsCopyList(Class<T> type) {
        final ItemList<T> itemList = (ItemList<T>) itemListMap.get(type);
        return itemList == null ? new LinkedList<T>() : itemList.getSortedItemsCopyList();
    }

    /**
     * Remove everything.
     */
    public void clear() {
        items.clear();
        itemListMap.clear();
    }

}
