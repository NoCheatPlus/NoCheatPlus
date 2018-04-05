package fr.neatmonster.nocheatplus.actions.types.penalty;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class DefaultPenaltyList implements IPenaltyList {

    /**
     * Desperation.
     * 
     * @author asofold
     *
     * @param <RI>
     */
    private static class GenericNode<RI> {
        private final List<GenericPenalty<RI>> penalties = new LinkedList<GenericPenalty<RI>>();

        private void apply(final RI input) {
            for (final GenericPenalty<RI> penalty : penalties) {
                penalty.applyPrecisely(input);
            }
        }
    }

    private boolean isEmpty = true;
    private boolean hasGeneric = false;
    private boolean hasNonGeneric= false;
    private final Map<Class<?>, GenericNode<?>> genericPenalties = new LinkedHashMap<Class<?>, GenericNode<?>>();
    private final List<InputSpecificPenalty> inputSpecificPenalties = new LinkedList<InputSpecificPenalty>();

    @Override
    public void addInputSpecificPenalty(final InputSpecificPenalty penalty) {
        if (penalty == null) {
            // Until decided how parsing / optimized lists are done.
            return;
        }
        if (penalty instanceof GenericPenalty) {
            ((GenericPenalty<?>) penalty).addToPenaltyList(this);
        }
        else {
            inputSpecificPenalties.add(penalty);
            isEmpty = false;
            hasNonGeneric = true;
        }
    }

    @Override
    public <RI> void addGenericPenalty(final Class<RI> registeredInput, 
            final GenericPenalty<RI> penalty) {
        @SuppressWarnings("unchecked")
        GenericNode<RI> node = (GenericNode<RI>) genericPenalties.get(registeredInput);
        if (node == null) {
            node = new GenericNode<RI>();
            genericPenalties.put(registeredInput, node);
        }
        node.penalties.add(penalty);
        isEmpty = false;
        hasGeneric = true;
    }

    @Override
    public <RI, I extends RI> void applyGenericPenaltiesPrecisely(
            final Class<RI> type, final I input) {
        @SuppressWarnings("unchecked")
        final GenericNode<RI> node = (GenericNode<RI>) genericPenalties.get(type);
        if (node != null) {
            node.apply(input);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <I> void applyAllApplicableGenericPenalties(final I input) {
        final Class<?> inputClass = input.getClass();
        for (final Entry<Class<?>, GenericNode<?>> entry : genericPenalties.entrySet()) {
            if (entry.getKey().isAssignableFrom(inputClass)) {
                ((GenericNode<? super I>) entry.getValue()).apply(input);
            }
        }
    }

    @Override
    public void applyNonGenericPenalties(Object input) {
        for (final InputSpecificPenalty penalty : inputSpecificPenalties) {
            penalty.apply(input);
        }
    }

    @Override
    public boolean isEmpty() {
        return isEmpty;
    }

    @Override
    public boolean hasGenericPenalties() {
        return hasGeneric;
    }

    @Override
    public boolean hasNonGenericPenalties() {
        return hasNonGeneric;
    }

}
