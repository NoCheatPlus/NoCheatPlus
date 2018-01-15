package fr.neatmonster.nocheatplus.test;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNode;
import fr.neatmonster.nocheatplus.components.data.checktype.CheckTypeTree.CheckTypeTreeNodeFactory;
import fr.neatmonster.nocheatplus.hooks.APIUtils;


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
            if (node.getChildren().size() != APIUtils.getDirectChildren(checkType).size()) {
                fail("Wrong size of children.");
            }
        }

        if (tree.getNode(null) != null) {
            fail("tree.getNode(null) returns a non null node for CheckType: " + tree.getNode(null).getCheckType());
        }

    }

}
