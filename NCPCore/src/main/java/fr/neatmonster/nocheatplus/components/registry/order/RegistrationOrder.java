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
package fr.neatmonster.nocheatplus.components.registry.order;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import fr.neatmonster.nocheatplus.utilities.ds.map.CoordHash;

/**
 * The order of registration. These objects can't be sorted reliably, due to
 * tags allowing virtually everything, thus specific (greedy) rules are needed.
 * 
 * @author asofold
 *
 */
public class RegistrationOrder {

    /**
     * General registration order for a type.
     * 
     * @author asofold
     *
     */
    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterWithOrder {
        /** Crude workaround for an Integer that may be null. */
        public String basePriority() default "";
        public String tag() default "";
        public String beforeTag() default "";
        public String afterTag() default "";
    }

    /**
     * Aimed at event listeners, to provide a default order.
     * 
     * @author asofold
     *
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterEventsWithOrder {
        /** Crude workaround for an Integer that may be null. */
        public String basePriority() default "";
        public String tag() default "";
        public String beforeTag() default "";
        public String afterTag() default "";
    }

    /**
     * Aimed at event handlers.
     * @author asofold
     *
     */
    @Documented
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RegisterMethodWithOrder {
        /** Crude workaround for an Integer that may be null. */
        public String basePriority() default "";
        public String tag() default "";
        public String beforeTag() default "";
        public String afterTag() default "";
    }

    /**
     * Compare on base of basePriority. Entries with null priority are sorted to
     * the front.
     */
    public static Comparator<RegistrationOrder> cmpBasePriority = new Comparator<RegistrationOrder>() {
        @Override
        public int compare(final RegistrationOrder o1, final RegistrationOrder o2) {
            final Integer p1 = o1.getBasePriority();
            final Integer p2 = o2.getBasePriority();
            if (p1 == null) {
                return p2 == null ? 0 : -1; // o1 to front.
            } else if (p2 == null) {
                return 1; // o2 to front.
            } else {
                return p1.compareTo(p2);
            }
        }
    };

    /**
     * Auxiliary class to contain the basic sorting algorithm, using an abstract
     * method to fetch the RegistrationOrder from the input type. The sorting
     * isn't necessarily stable, and tag-specific order can vary wildly
     * depending on the order of input.
     * <hr>
     * The basePriority comes first on sorting, beforeTag set means being put to
     * front of a priority level, afterTag means being sorted to the end (with
     * beforeTag still in front of the priority level, without to the end). With
     * null priority entries are sorted to the very start/end of the list,
     * unless both of beforeTag and afterTag are null, in which case they're
     * sorted to/around the level of basePriority being 0. Tag-based comparison
     * uses regular expressions via tagA.matchhes(beforeTagB|afterTagB). The
     * element sorted into an existing (sub-) list has their beforeTag/afterTag
     * checked first (greedy).<br>
     * TODO: Describe the sorting algorithm in more detail, if needed.
     * 
     * @author asofold
     *
     * @param <F>
     */
    public static abstract class AbstractRegistrationOrderSort<F> {

        // TODO: Signature with passing IFetchRegistrationOrder<F> to the sorting?
        // TODO: Back to generic static methods?

        private final Comparator<F> cmp = new Comparator<F>() {
            @Override
            public int compare(final F o1, final F o2) {
                return RegistrationOrder.cmpBasePriority.compare(
                        fetchRegistrationOrder(o1), fetchRegistrationOrder(o2));
            }
        };

        protected abstract RegistrationOrder fetchRegistrationOrder(F item);

        public void sort(final Collection<F> input) {
            final LinkedList<F> sorted = getSortedLinkedList(input);
            input.clear();
            input.addAll(sorted);
        }

        /**
         * Create a new linked list with elements of input in sorted order (from
         * a sorted array).
         * 
         * @param input
         * @return
         */
        public LinkedList<F> getSortedLinkedList(final Collection<F> input) {
            final F[] arr = getSortedArray(input);
            final LinkedList<F> out = new LinkedList<F>();
            Collections.addAll(out, arr);
            return out;
        }

        public final <O extends Collection<F>> O sort(final Collection<F> input, final O output) {
            output.addAll(getSortedLinkedList(input));
            return output;
        }

        /**
         * Core sorting method, return an array with all elements of input in
         * sorted order.
         * 
         * @param input
         * @return
         */
        public F[] getSortedArray(final Collection<F> input) {
            /*
             * (Due to the need for a ListIterator, you can't just use a given
             * type that merely extends Collection.)
             */
            // TODO: Implement using an array for output with insertionIndex (less iterators etc).
            @SuppressWarnings("unchecked")
            final F[] output = (F[]) new Object[input == null ? 0 : input.size()];
            if (input == null || input.isEmpty()) {
                return output;
            }
            else if (input.size() == 1) {
                input.toArray(output);
                return output;
            }
            // Sort into rough groups.
            final LinkedList<F> belowZeroPriority = new LinkedList<F>();
            final LinkedList<F> zeroPriority = new LinkedList<F>();
            final LinkedList<F> aboveZeroPriority = new LinkedList<F>();
            int insertionIndex = output.length - 1; // Where to start sorting in elements to output.
            for (final F item : input) {
                final RegistrationOrder order = fetchRegistrationOrder(item);
                final Integer basePriority = order.getBasePriority();
                if (basePriority == null) {
                    // Distinguish cases, note the sorting order of cmpBasePriority.
                    if (order.getBeforeTag() != null) {
                        /*
                         * These will be sorted to front, processed last within
                         * this list, resulting in optimal order.
                         */
                        belowZeroPriority.add(item);
                    }
                    else if (order.getAfterTag() != null) {
                        // These will be last in the end, thus add to tempOut directly.
                        sortInFromStart(item, output, insertionIndex);
                        insertionIndex --;
                    }
                    else {
                        /*
                         * These are somehow added to 0-priority - could end up
                         * anywhere, if 0-basePriority items have an afterTag
                         * set that matches the tag. Consequently items with
                         * null base priority are processed last.
                         */
                        zeroPriority.add(item);
                    }
                }
                else if (basePriority < 0) {
                    belowZeroPriority.add(item);
                }
                else if (basePriority > 0) {
                    aboveZeroPriority.add(item);
                }
                else {
                    // Ensure to process items with 0 basePriority set first.
                    zeroPriority.add(0, item);
                }
            }
            // Combine lists into output.
            // TODO: Perhaps could use optimized methods for sorting in later on.
            if (!aboveZeroPriority.isEmpty()) {
                addSortedSubList(aboveZeroPriority, output, insertionIndex);
                insertionIndex -= aboveZeroPriority.size();
            }
            if (!zeroPriority.isEmpty()) {
                // Still need to sort the roughly pre-grouped items, to match tags.
                for (final F item : zeroPriority) {
                    sortInFromStart(item, output, insertionIndex);
                    insertionIndex --;
                }
            }
            if (!belowZeroPriority.isEmpty()) {
                addSortedSubList(belowZeroPriority, output, insertionIndex);
                //insertionIndex -= belowZeroPriority.size();
            }
            return output;
        }

        /**
         * Sort, reverse and then add the subList to the output via
         * sortInFromStart.
         * 
         * @param subList
         * @param output
         */
        private void addSortedSubList(final List<F> subList, final F[] output, int insertionIndex) {
            Collections.sort(subList, cmp);
            Collections.reverse(subList);
            for (final F item : subList) {
                sortInFromStart(item, output, insertionIndex);
                insertionIndex --;
            }
        }

        private void sortInFromStart(F item, final F[] output, final int insertionIndex) {
            // (The place at insertionIndex should be free.)
            final RegistrationOrder order = fetchRegistrationOrder(item);
            for (int i = insertionIndex; i < output.length; i++) {
                if (i == output.length - 1 || shouldSortBefore(order, fetchRegistrationOrder(output[i + 1]))) {
                    output[i] = item;
                    return;
                }
                else  {
                    output[i] = output[i + 1];
                }
            }
            output[output.length - 1] = item;
        }

        /**
         * The heart of all the sorting. See {@link AbstractRegistrationOrderSort} for a description.
         * @param order1
         * @param order2
         * @return
         */
        public static boolean shouldSortBefore(final RegistrationOrder order1, final RegistrationOrder order2) {
            // TODO: Javadocs - where / how.
            final Integer basePriority1 = order1.getBasePriority();
            final String tag1 = order1.getTag();
            final String beforeTag1 = order1.getBeforeTag();
            final String afterTag1 = order1.getAfterTag();
            final Integer basePriority2 = order2.getBasePriority();
            final String tag2 = order2.getTag();
            final String beforeTag2 = order2.getBeforeTag();
            final String afterTag2 = order2.getAfterTag();
            // TODO: Can the tag comparison be unified (simplification)?
            // TODO: Final decision: first all tags of the first, or first 'before' each then 'after' each?
            if (basePriority1 == null) {
                if (basePriority2 == null) {
                    if (beforeTag1 == null) {
                        if (beforeTag2 == null) {
                            if (afterTag1 == null) {
                                // Safe to abort here.
                                return true;
                            }
                            else {
                                if (
                                        // order1 might be set to come after order2.
                                        tag2 != null && tag2.matches(afterTag1) 
                                        // Must proceed to check other entries with afterTag set.
                                        || afterTag2 == null
                                        || tag1 == null
                                        // The other entry is not set to come after this one.
                                        || !tag1.matches(afterTag2)) {
                                    return false;
                                }
                                else {
                                    return true;
                                }
                            }
                        }
                        else {
                            /*
                             * beforeTag2 is not null - the only way to let
                             * order1 come first, is to have tag1 match
                             * afterTag2. Otherwise 
                             */
                            return tag1 != null && afterTag2 != null && tag1.matches(afterTag2);
                        }
                    }
                    else {
                        /*
                         * beforeTag1 is not null - the only way to let
                         * order2 come first, is to have tag2 match
                         * afterTag1. Otherwise 
                         */
                        return tag2 == null || afterTag1 == null || !tag2.matches(afterTag1);
                    }
                }
                else {
                    // order2 has basePriority set, proceed depending on tags.
                    return 
                            // Pre-any-basePriority region.
                            beforeTag1 != null 
                            // To center region (simple).
                            || afterTag1 == null && basePriority2 > 0
                            // To center region (with comparison).
                            || afterTag1 == null && basePriority2 == 0
                            && (
                                    // Sort to the end of 0-basePriority, where neither beforeTag nor afterTag is set.
                                    tag1 == null && (beforeTag2 == null && afterTag2 != null)
                                    // The other is explicitly set to be later.
                                    || tag1 != null && afterTag2 != null && tag1.matches(afterTag2)
                                    // Before anything that only has the afterTag set, otherwise.
                                    || beforeTag2 == null && afterTag2 != null
                                    )
                            ;
                }
            }
            else if (basePriority2 == null) {
                // Determine region to fit in (...).
                if (beforeTag2 == null) {
                    if (afterTag2 == null) {
                        // order2 is within the 0-basePriority region.
                        if (basePriority1 < 0) {
                            return false;
                        }
                        else if (basePriority1 > 0) {
                            return true;
                        }
                        else {
                            // Both are within the same region.
                            if (tag2 != null && afterTag1 != null && tag2.matches(afterTag1)) {
                                // order1 is set to come after order2.
                                return false;
                            }
                            else if (beforeTag1 != null) {
                                // Order1 comes first.
                                return true;
                            }
                            else if (afterTag1 != null) {
                                // Order1 is sorted to the end of the region rather.
                                return false;
                            }
                            else {
                                /*
                                 * Prefer to have have set basePriority 0 first,
                                 * assuming null basePriority to be interpreted
                                 * as 'later registered', keeping the order of
                                 * registration rather.
                                 */
                                return true;
                            }
                        }
                    }
                    else {
                        // order2 comes later.
                        return true;
                    }
                }
                else {
                    // order2 comes first.
                    return false;
                }
            }
            else {
                // Both not null.
                if (basePriority1 < basePriority2) {
                    return true;
                }
                else if (basePriority1 > basePriority2) {
                    return false;
                }
                else {
                    // Same priority set, distinguish by tag settings.
                    if (beforeTag1 == null) {
                        if (beforeTag2 == null) {
                            // Only distinction could be afterTagS.
                            if (afterTag1 == null) {
                                // Either can't distinguish, or order2 should come afterwards.
                                return true;
                            }
                            else {
                                if (afterTag2 == null) {
                                    // order1 should be passed on.
                                    return false;
                                }
                                else {
                                    // Both afterTagS are not null.
                                    if (tag2 != null && tag2.matches(afterTag1)) {
                                        // Greedy (could override afterTag2).
                                        return false;
                                    }
                                    else if (tag1 != null && tag1.matches(afterTag2)) {
                                        // order2 is set to come after order1.
                                        return true;
                                    }
                                    else {
                                        // order1 may be set to come after another entry that follows.
                                        return false;
                                    }
                                }
                            }
                        }
                        else {
                            // beforeTag1 is null, beforeTag2 not.
                            if (afterTag2 != null && tag1 != null && tag1.matches(afterTag2)) {
                                // order2 is set to come after order1,
                                return true;
                            }
                            else {
                                // beforeTag set rules otherwise.
                                return false;
                            }
                        }
                    }
                    else {
                        // beforeTag1 is null.
                        if (beforeTag2 == null) {
                            if (afterTag1 != null && tag2 != null && tag2.matches(afterTag1)) {
                                // order1 is explicitly set to come after order2.
                                return false;
                            }
                            else {
                                // TODO: Very complicated - explain.
                                return afterTag2 == null && afterTag1 == null;
                            }
                        }
                        else {
                            // Both beforeTag1 and beforeTag2 are not so null.
                            if (tag2 != null && tag2.matches(beforeTag1)) {
                                // order1 is set to come before order2.
                                return true;
                            }
                            else if (tag1 != null && tag1.matches(beforeTag2)) {
                                // order2 is set to come before order1.
                                return false;
                            }
                            else if (afterTag1 == null) {
                                // Either can't distinguish, or afterTag2 is set, thus sort in before.
                                return true;
                            }
                            else {
                                // TODO: If correct, simplify (one condition returns true).
                                // afterTag1 is not null.
                                if (tag2 != null && tag2.matches(afterTag1)) {
                                    // order1 is set to come after order2.
                                    return false;
                                }
                                else if (afterTag2 != null){
                                    if (tag1 != null && tag1.matches(afterTag2)) {
                                        // order2 is set to come after order1.
                                        return true;
                                    }
                                    else {
                                        // Rather keep checking.
                                        return false;
                                    }
                                }
                                else {
                                    // afterTag1 is null, otherwise not matching, thus order1 comes behind.
                                    return false;
                                }
                            }
                        }
                    }
                }
            }
            // (No catch all.)
        }
    }

    public static class SortRegistrationOrder extends AbstractRegistrationOrderSort<RegistrationOrder> {
        @Override
        protected RegistrationOrder fetchRegistrationOrder(RegistrationOrder item) {
            return item;
        }
    }

    public static class SortIGetRegistrationOrder extends AbstractRegistrationOrderSort<IGetRegistrationOrder> {
        @Override
        protected RegistrationOrder fetchRegistrationOrder(IGetRegistrationOrder item) {
            return item.getRegistrationOrder();
        }
    }

    /** The default order (no preference, middle). */
    public static final RegistrationOrder DEFAULT_ORDER = new RegistrationOrder();

    /**
     * Sort collections of RegistrationOrder instances.
     */
    public static final SortRegistrationOrder sortRegistrationOrder = new SortRegistrationOrder();

    /**
     * Sort collections of IGetRegistrationOrder instances.
     */
    public static final SortIGetRegistrationOrder sortIGetRegistrationOrder = new SortIGetRegistrationOrder();

    /*
     * TODO: Consider a registration id / count (meant unique, increasing with
     * the next registration). When both priorities are 0, this id could be used
     * to guarantee order.
     */
    private final Integer basePriority;
    private final String tag; 
    private final String beforeTag; // TODO: Set ?
    private final String afterTag; // TODO: Set ?

    /**
     * LEGACY support, to be deprecated.
     * @param bluePrint
     */
    public RegistrationOrder(SetupOrder bluePrint) {
        this(bluePrint.priority());
    }

    /**
     * 
     * @param bluePrint
     * @throws NumberFormatException, if basePriority can't be parsed.
     */
    public RegistrationOrder(RegisterWithOrder bluePrint) {
        // TODO: InvalidOrderException via static method for parsing.
        this(bluePrint.basePriority().isEmpty() ? null : Integer.parseInt(bluePrint.basePriority()), 
                bluePrint.tag().isEmpty() ? null : bluePrint.tag(), 
                        bluePrint.beforeTag().isEmpty() ? null : bluePrint.beforeTag(), 
                                bluePrint.afterTag().isEmpty() ? null : bluePrint.afterTag());
    }

    /**
     * 
     * @param bluePrint
     * @throws NumberFormatException, if basePriority can't be parsed.
     */
    public RegistrationOrder(RegisterEventsWithOrder bluePrint) {
        // TODO: InvalidOrderException via static method for parsing.
        this(bluePrint.basePriority().isEmpty() ? null : Integer.parseInt(bluePrint.basePriority()), 
                bluePrint.tag().isEmpty() ? null : bluePrint.tag(), 
                        bluePrint.beforeTag().isEmpty() ? null : bluePrint.beforeTag(), 
                                bluePrint.afterTag().isEmpty() ? null : bluePrint.afterTag());
    }

    /**
     * 
     * @param bluePrint
     * @throws NumberFormatException, if basePriority can't be parsed.
     */
    public RegistrationOrder(RegisterMethodWithOrder bluePrint) {
        // TODO: InvalidOrderException via static method for parsing.
        this(bluePrint.basePriority().isEmpty() ? null : Integer.parseInt(bluePrint.basePriority()), 
                bluePrint.tag().isEmpty() ? null : bluePrint.tag(), 
                        bluePrint.beforeTag().isEmpty() ? null : bluePrint.beforeTag(), 
                                bluePrint.afterTag().isEmpty() ? null : bluePrint.afterTag());
    }

    public RegistrationOrder(RegistrationOrder bluePrint) {
        this(bluePrint.getBasePriority(), bluePrint.getTag(), bluePrint.getBeforeTag(), bluePrint.getAfterTag());
    }

    public RegistrationOrder() {
        this(null, null, null, null);
    }

    public RegistrationOrder(Integer basePriority) {
        this(basePriority, null, null, null);
    }

    public RegistrationOrder(Integer basePriority, String tag) {
        this(basePriority, tag, null, null);
    }

    public RegistrationOrder(String tag) {
        this(null, tag, null, null);
    }

    public RegistrationOrder(String tag, String beforeTag, String afterTag) {
        this(null, tag, beforeTag, afterTag);
    }

    /**
     * 
     * @param basePriority
     *            Basic priority for sorting (first priority). If this is not
     *            set (i.e. is null), beforeTag and afterTag allow sorting
     *            independently of the basePriority of other instances, however
     *            the sorting is greedy/special.
     * @param tag
     * @param beforeTag
     * @param afterTag
     */
    public RegistrationOrder(Integer basePriority, String tag, String beforeTag, String afterTag) {
        this.basePriority = basePriority;
        this.tag = tag;
        this.beforeTag = beforeTag;
        this.afterTag = afterTag;
    }

    public Integer getBasePriority() {
        return basePriority;
    }

    public String getTag() {
        return tag;
    }

    public String getBeforeTag() {
        return beforeTag;
    }

    public String getAfterTag() {
        return afterTag;
    }

    @Override
    public int hashCode() {
        // (Special value for null.)
        return (basePriority == null ? CoordHash.hashCode3DPrimes(-1, -1, -1) : basePriority.hashCode()) 
                ^ (tag == null ? CoordHash.hashCode3DPrimes(1, 0, 0) : tag.hashCode()) 
                ^ (beforeTag == null ? CoordHash.hashCode3DPrimes(0, 1, 0) : beforeTag.hashCode()) 
                ^ (afterTag == null ? CoordHash.hashCode3DPrimes(0, 0, 1) : afterTag.hashCode()) ;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof RegistrationOrder)) {
            return false;
        }
        final RegistrationOrder other = (RegistrationOrder) obj;
        return (basePriority == null ? other.getBasePriority() == null : basePriority.equals(other.getBasePriority()))
                && (tag == null ? other.getTag() == null : tag.equals(other.getTag()))
                && (beforeTag == null ? other.getBeforeTag() == null : beforeTag.equals(other.getBeforeTag()))
                && (afterTag == null ? other.getAfterTag() == null : afterTag.equals(other.getAfterTag()));
    }

    @Override
    public String toString() {
        return "RegistrationOrder(p=" + basePriority 
                + (tag == null ? "" : " t='" + tag + "'") 
                + (beforeTag == null ? "" : " bt='" + beforeTag + "'") 
                + (afterTag == null ? "" : "at='" + afterTag + "'")
                + ")";
    }

}

