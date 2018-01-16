package fr.neatmonster.nocheatplus.components.data.checktype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNode;
import fr.neatmonster.nocheatplus.utilities.CheckTypeUtil;

/**
 * Swift sketch of a tree structure, meant to span all check types, holding
 * specific data.
 * 
 * @author asofold
 *
 */
public abstract class CheckTypeTree<N extends CheckTypeTreeNode<N>> {

    public static interface CheckTypeTreeNodeFactory<N extends CheckTypeTreeNode<N>> {
        N newNode(CheckType checkType, N parent);
    }

    public static class CheckTypeTreeNode<N extends CheckTypeTreeNode<N>> {

        private final CheckType checkType;
        private final N parent;
        private final List<N> children;

        /**
         * Following the principle of 'creation by explosion', all children are
         * created in here directly.
         * 
         * @param checkType
         */
        @SuppressWarnings("unchecked")
        public CheckTypeTreeNode(final CheckType checkType, final N parent,
                final CheckTypeTreeNodeFactory<N> factory) {
            this.checkType = checkType;
            this.parent = parent;
            final Set<CheckType> childrenTypes = CheckTypeUtil.getDirectChildren(checkType);
            final List<N> children = new ArrayList<N>(childrenTypes.size());
            for (CheckType childType : childrenTypes) {
                children.add(factory.newNode(childType, (N) this));
            }
            this.children = Collections.unmodifiableList(children);
        }

        public CheckType getCheckType() {
            return checkType;
        }

        public N getParent() {
            return parent;
        }

        /**
         * An unmodifiable list.
         * 
         * @return
         */
        public List<N> getChildren() {
            return children;
        }
    }

    private final N rootNode;

    private final Map<CheckType, N> nodeMap = new LinkedHashMap<CheckType, N>();

    public CheckTypeTree() {
        // Protective glasses on..
        class DefaultFactory implements CheckTypeTreeNodeFactory<N> {
            @Override
            public N newNode(CheckType checkType, N parent) {
                return CheckTypeTree.this.newNode(checkType, parent, this);
            }
        };
        // Create explosion.
        rootNode = newNode(CheckType.ALL, null, new DefaultFactory());
        // Create mapping for explosion.
        final List<N> allNodes = new LinkedList<N>();
        collectNodes(rootNode, allNodes);
        for (final N node : allNodes) {
            nodeMap.put(node.getCheckType(), node);
        }
    }

    private void collectNodes(final N node, final List<N> bucket) {
        bucket.add(node);
        for (final N child : node.getChildren()) {
            collectNodes(child, bucket);
        }
    }

    /**
     * Internal Factory.
     * @return
     */
    protected abstract N newNode(CheckType checkType, N parent, CheckTypeTreeNodeFactory<N> factory);

    public N getNode(CheckType checkType) {
        return nodeMap.get(checkType);
    }

}
