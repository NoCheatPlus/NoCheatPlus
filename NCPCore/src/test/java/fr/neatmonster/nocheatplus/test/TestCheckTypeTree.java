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
package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNode;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.utilities.CheckTypeUtil;


public class TestCheckTypeTree {

    static class TestNode extends CheckTypeTreeNode<TestNode> {

        public TestNode(CheckType checkType, TestNode parent,
                CheckTypeTreeNodeFactory<TestNode> factory) {
            super(checkType, parent, factory);
        }

    }

    @Test
    public void testCreationCompleteness() {
        CheckTypeTree<TestNode> tree = new CheckTypeTree<TestNode>() {
            @Override
            protected TestNode newNode(CheckType checkType, TestNode parent,
                    CheckTypeTreeNodeFactory<TestNode> factory) {
                return new TestNode(checkType, parent, factory);
            }
        };

        for (CheckType checkType : CheckType.values()) {
            TestNode node = tree.getNode(checkType);
            CheckType rct = node.getCheckType();
            if (rct != checkType) {
                fail("Bad check type, expext " + checkType + ", got instead: " + rct);
            }
            if (rct.getParent() != null && node.getParent().getCheckType() != rct.getParent()) {
                fail("Wrong type of parent.");
            }
            if (node.getChildren().size() != CheckTypeUtil.getDirectChildren(checkType).size()) {
                fail("Wrong size of children.");
            }
        }

        if (tree.getNode(null) != null) {
            fail("tree.getNode(null) returns a non null node for CheckType: " + tree.getNode(null).getCheckType());
        }

    }

}
