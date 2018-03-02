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
package fr.neatmonster.nocheatplus.players;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Lock;

import org.bukkit.Bukkit;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.compat.AlmostBoolean;
import fr.neatmonster.nocheatplus.components.config.value.OverrideType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckNodeWithDebug;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree;
import fr.neatmonster.nocheatplus.components.data.checktype.IBaseCheckNode;
import fr.neatmonster.nocheatplus.config.ConfigFile;
import fr.neatmonster.nocheatplus.hooks.ExemptionContext;
import fr.neatmonster.nocheatplus.players.PlayerCheckTypeTree.PlayerCheckTypeTreeNode;
import fr.neatmonster.nocheatplus.worlds.IWorldCheckNode;
import fr.neatmonster.nocheatplus.worlds.IWorldData;

/**
 * <hr>
 * Exempting and unexempting are handled within two contexts: primary thread and
 * asynchronous. Exemption testing may account for both states (without
 * synchronization).
 * 
 * @author asofold
 *
 */
public class PlayerCheckTypeTree extends CheckTypeTree<PlayerCheckTypeTreeNode>{

    /**
     * <hr>
     * <ul>
     * <li>Nodes are not meant to be passed to the outside (!).</li>
     * <li>Locking for 'asynchronous' access has to be done externally.</li>
     * </ul>
     * 
     * @author asofold
     *
     */
    static class PlayerCheckTypeTreeNode extends CheckNodeWithDebug<PlayerCheckTypeTreeNode> implements IBaseCheckNode {

        // TODO: Compactify flags?

        /**
         * Explicitly exempted by API call (cumulative flag), excludes checking for
         * meta data and the like.
         */
        private boolean exemptedPrimaryThread = false;
        /**
         * Explicitly exempted by API call (cumulative flag), excludes checking for
         * meta data and the like.
         */
        private boolean exemptedAsynchronous = false;

        /**
         * Exemption contexts that apply here. Lazily allocate.
         */
        /*
         * TODO: Consider a ParallelList (access independently without need for
         * mergePrimaryThread, or extend DualList by appropriate methods).
         */
        private List<ExemptionContext> exemptionsPrimaryThread = null;
        private List<ExemptionContext> exemptionsAsynchronous = null;

        PlayerCheckTypeTreeNode(final CheckType checkType,
                final PlayerCheckTypeTreeNode parent,
                final CheckTypeTreeNodeFactory<PlayerCheckTypeTreeNode> factory) {
            super(checkType, parent, factory);
        }

        /**
         * Check for exemption by explicit API call. Excludes checking for meta
         * data or other API.
         * <hr>
         * No locking involved, checking flags for primary thread and
         * asynchronous access.
         * 
         * @return
         */
        boolean isExempted() {
            // Typically this should boil down to thread-local use, as it is CheckType-bound.
            return exemptedPrimaryThread || exemptedAsynchronous;
        }

        /**
         * Must call under lock.
         * 
         * @param context
         */
        void exemptAsynchronous(final ExemptionContext context) {
            exemptedAsynchronous = true;
            if (exemptionsAsynchronous == null) {
                exemptionsAsynchronous = new LinkedList<ExemptionContext>();
            }
            exemptionsAsynchronous.add(0, context);
        }

        void exemptPrimaryThread(final ExemptionContext context) {
            exemptedPrimaryThread = true;
            if (exemptionsPrimaryThread == null) {
                exemptionsPrimaryThread = new LinkedList<ExemptionContext>();
            }
            exemptionsPrimaryThread.add(0, context);
        }

        /**
         * Must call under lock.
         * 
         * @param context
         */
        void unexemptAsynchronous(final ExemptionContext context) {
            if (exemptionsAsynchronous != null) {
                exemptionsAsynchronous.remove(context);
                if (exemptionsAsynchronous.isEmpty()) {
                    // TODO: Have a counter to delay resetting?
                    exemptedAsynchronous = false;
                    exemptionsAsynchronous = null;
                }
            }
        }

        void unexemptPrimaryThread(final ExemptionContext context) {
            if (exemptionsPrimaryThread != null) {
                exemptionsPrimaryThread.remove(context);
                if (exemptionsPrimaryThread.isEmpty()) {
                    // TODO: Have a counter to delay resetting?
                    exemptedPrimaryThread = false;
                    exemptionsPrimaryThread = null;
                }
            }
        }

        /**
         * Must call under lock.
         * 
         * @param context
         */
        void unexemptAllAsynchronous(final ExemptionContext context) {
            if (exemptionsAsynchronous != null) {
                exemptionsAsynchronous.removeAll(Collections.singleton(context));
                if (exemptionsAsynchronous.isEmpty()) {
                    // TODO: Have a counter to delay resetting?
                    exemptedAsynchronous = false;
                    exemptionsAsynchronous = null;
                }
            }
        }

        void unexemptAllPrimaryThread(final ExemptionContext context) {
            if (exemptionsPrimaryThread != null) {
                exemptionsPrimaryThread.removeAll(Collections.singleton(context));
                if (exemptionsPrimaryThread.isEmpty()) {
                    // TODO: Have a counter to delay resetting?
                    exemptedPrimaryThread = false;
                    exemptionsPrimaryThread = null;
                }
            }
        }

        /**
         * Must call under lock.
         * 
         * @param context
         * @return
         */
        boolean isExemptedAsynchronous(ExemptionContext context) {
            return  exemptionsAsynchronous != null && exemptionsAsynchronous.contains(context);
        }

        boolean isExemptedPrimaryThread(final ExemptionContext context) {
            return exemptionsPrimaryThread != null && exemptionsPrimaryThread.contains(context);
        }

        /**
         * Primary thread only, must call under lock.
         */
        void clearAllExemptions() {
            exemptedAsynchronous = false;
            exemptedPrimaryThread = false;
            exemptionsAsynchronous = null;
            exemptionsPrimaryThread = null;
        }

        /**
         * Set configDebug recursively and then update.
         * 
         * @param worldData
         */
        void updateDebug(final IWorldData worldData) {
            setDebugNoUpdate(worldData);
            updateDebug(worldData.getRawConfiguration());
        }

        /**
         * Recursively set configDebug, no update.
         * 
         * @param worldData
         */
        private void setDebugNoUpdate(final IWorldData worldData) {
            final IWorldCheckNode worldNode = worldData.getCheckNode(getCheckType());
            // Just adjust recursively.
            // TODO: Simplicity of interface: hard set to the resulting value.
            configDebug.setValue(worldNode.isDebugActive(), worldNode.getOverrideTypeDebug());
            for (final PlayerCheckTypeTreeNode node : getChildren()) {
                node.updateDebug(worldData);
            }
        }

        /**
         * Update to given configuration.
         * 
         * @param rawConfiguration
         */
        @SuppressWarnings("unchecked")
        void updateDebug(ConfigFile rawConfiguration) {
            update(rawConfiguration, true, accessDebug);
        }

        @SuppressWarnings("unchecked")
        void updateDebug() {
            update(true, accessDebug);
        }

        /**
         * Hard-reset the debug properties to the underlying IWorldData instance.
         * 
         * @param worldData
         */
        void resetDebug(final IWorldData worldData) {
            resetDebugNoUpdate(worldData);
            updateDebug(worldData.getRawConfiguration());
        }

        private void resetDebugNoUpdate(IWorldData worldData) {
            final IWorldCheckNode worldNode = worldData.getCheckNode(getCheckType());
            // Just adjust recursively.
            // TODO: Simplicity of interface: hard set to the resulting value.
            /*
             * TODO: Itchy/modeling: A permanent override for a player gets
             * reset by a permanent override for a world.
             */
            configDebug.resetValue(worldNode.isDebugActive(), worldNode.getOverrideTypeDebug());
            for (final PlayerCheckTypeTreeNode node : getChildren()) {
                node.resetDebugNoUpdate(worldData);
            }
        }

        @SuppressWarnings("unchecked")
        void overrideDebug(
                final CheckType checkType, final AlmostBoolean active, 
                final OverrideType overrideType, final boolean overrideChildren) {
            override(active, overrideType, overrideChildren, accessDebug);
        }

    }

    ////////////////
    // Instance
    ////////////////

    private final Lock lock;

    public PlayerCheckTypeTree(final Lock lock) {
        super();
        this.lock = lock;
    }

    @Override
    protected PlayerCheckTypeTreeNode newNode(CheckType checkType,
            PlayerCheckTypeTreeNode parent,
            CheckTypeTreeNodeFactory<PlayerCheckTypeTreeNode> factory) {
        return new PlayerCheckTypeTreeNode(checkType, parent, factory);
    }

    /**
     * <hr>
     * Thread-safe read, not synchronized.
     * 
     * @param checkType
     * @return
     */
    public boolean isExempted(final CheckType checkType) {
        return getNode(checkType).isExempted(); // Fast read.
    }

    public void exempt(final CheckType checkType, final ExemptionContext context) {
        final PlayerCheckTypeTreeNode node = getNode(checkType);
        if (node == null) {
            throw new IllegalArgumentException("Invalid check type.");
        }
        if (Bukkit.isPrimaryThread()) {
            exemptPrimaryThread(node, context);
        }
        else {
            exemptAsynchronous(node, context);
        }
    }

    private void exemptPrimaryThread(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.exemptPrimaryThread(context);
                return true;
            }
        });
    }

    private void exemptAsynchronous(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        lock.lock();
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.exemptAsynchronous(context);
                return true;
            }
        });
        lock.unlock();
    }

    public void unexempt(final CheckType checkType, final ExemptionContext context) {
        final PlayerCheckTypeTreeNode node = getNode(checkType);
        if (node == null) {
            throw new IllegalArgumentException("Invalid check type.");
        }
        if (Bukkit.isPrimaryThread()) {
            unexemptPrimaryThread(node, context);
        }
        else {
            unexemptAsynchronous(node, context);
        }
    }

    private void unexemptPrimaryThread(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.unexemptPrimaryThread(context);
                return true;
            }
        });
    }

    private void unexemptAsynchronous(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        lock.lock();
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.unexemptAsynchronous(context);
                return true;
            }
        });
        lock.unlock();
    }

    public void unexemptAll(final CheckType checkType, final ExemptionContext context) {
        final PlayerCheckTypeTreeNode node = getNode(checkType);
        if (node == null) {
            throw new IllegalArgumentException("Invalid check type.");
        }
        if (Bukkit.isPrimaryThread()) {
            unexemptAllPrimaryThread(node, context);
        }
        else {
            unexemptAllAsynchronous(node, context);
        }
    }

    private void unexemptAllPrimaryThread(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.unexemptAllPrimaryThread(context);
                return true;
            }
        });
    }

    private void unexemptAllAsynchronous(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        lock.lock();
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.unexemptAllAsynchronous(context);
                return true;
            }
        });
        lock.unlock();
    }

    public boolean isExempted(final CheckType checkType, final ExemptionContext context) {
        final PlayerCheckTypeTreeNode node = getNode(checkType);
        if (node == null) {
            throw new IllegalArgumentException("Invalid check type.");
        }
        if (Bukkit.isPrimaryThread()) {
            return isExemptedPrimaryThread(node, context);
        }
        else {
            return isExemptedAsynchronous(node, context);
        }
    }

    private boolean isExemptedPrimaryThread(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        return node.isExemptedPrimaryThread(context);
    }

    private boolean isExemptedAsynchronous(final PlayerCheckTypeTreeNode node, final ExemptionContext context) {
        final boolean res;
        lock.lock();
        res = node.isExemptedPrimaryThread(context);
        lock.unlock();
        return res;
    }

    /**
     * Call from the primary thread only.
     * 
     */
    public void clearAllExemptions() {
        clearAllExemptions(CheckType.ALL);
    }

    /**
     * Call from the primary thread only.
     * 
     * @param checkType
     */
    public void clearAllExemptions(final CheckType checkType) {
        final PlayerCheckTypeTreeNode node = getNode(checkType);
        if (node == null) {
            throw new IllegalArgumentException("Invalid check type.");
        }
        lock.lock();
        visitWithDescendants(node, new Visitor<PlayerCheckTypeTreeNode>() {
            @Override
            public boolean visit(final PlayerCheckTypeTreeNode node) {
                node.clearAllExemptions();
                return true;
            }
        });
        lock.unlock();
    }


}
