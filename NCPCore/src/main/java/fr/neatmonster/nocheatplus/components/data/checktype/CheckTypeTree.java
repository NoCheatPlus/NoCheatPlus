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

    public static interface Visitor<N extends CheckTypeTreeNode<N>> {
        /*
         * TODO: Not so sure this really is far reaching, due to performance
         * questions: With concurrent access, a stored instance doesn't do, so
         * we'll keep checking for the primary thread and either use the stored
         * one or create new ones. Thinkable direction could be to use stored
         * instances within thread-local data objects (for NET data), but still
         * odd calls to teleportation can wreck this. Could distinguish visit
         * under lock (write) and visit without lock (read) with COW inside.
         */
        /**
         * Called for visiting a node.
         * @param node
         * @return Return true in order to continue visiting further nodes, false to abort.
         */
        public boolean visit(N node);
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

    public boolean visitWithDescendants(final CheckType checkType, final Visitor<N> visitor) {
        return visitWithDescendants(getNode(checkType), visitor);
    }

    public boolean visitWithDescendants(final N node, final Visitor<N> visitor) {
        if (!visitor.visit(node)) {
            return false;
        }
        return visitDescendants(node, visitor);
    }

    public boolean visitDescendants(final CheckType checkType, final Visitor<N> visitor) {
        return visitDescendants(getNode(checkType), visitor);
    }

    public boolean visitDescendants(final N parentNode, final Visitor<N> visitor) {
        for (final N childNode : parentNode.getChildren()) {
            if (!visitWithDescendants(childNode, visitor)) {
                return false;
            }
        }
        return true; // Not aborted.
    }

    public boolean visitWithAncestors(final CheckType checkType, final Visitor<N> visitor) {
        return visitWithAncestors(getNode(checkType), visitor);
    }

    public boolean visitWithAncestors(final N node, final Visitor<N> visitor) {
        if (!visitor.visit(node)) {
            return false;
        }
        return visitAncestors(node, visitor);
    }

    public boolean visitAncestors(final CheckType checkType, final Visitor<N> visitor) {
        return visitAncestors(getNode(checkType), visitor);
    }

    public boolean visitAncestors(final N node, final Visitor<N> visitor) {
        final N parent = node.getParent();
        if (parent != null) {
            return visitWithAncestors(parent, visitor);
        }
        return true; // Not aborted.
    }

}
