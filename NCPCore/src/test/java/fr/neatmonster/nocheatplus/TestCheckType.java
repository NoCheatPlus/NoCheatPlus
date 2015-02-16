package fr.neatmonster.nocheatplus;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.checks.CheckType;
import fr.neatmonster.nocheatplus.hooks.APIUtils;

public class TestCheckType {

    @Test
    public void testIsParent() {
        for (CheckType parent : CheckType.values()) {
            if (parent != CheckType.ALL && !APIUtils.isParent(CheckType.ALL, parent)) {
                fail("Expect ALL to be parent of " + parent + " .");
            }
            // Rough simplified check by naming.
            String parentName = parent.getName();
            for (final CheckType child : CheckType.values()) {
                if (child == parent) {
                    if (APIUtils.isParent(parent, child)) {
                        fail("Check can't be parent of itself: " + parent);
                    }
                    // Ignore otherwise.
                    continue;
                }
                String childName = child.getName();
                if (childName.startsWith(parentName) && childName.charAt(parentName.length()) == '.' && !APIUtils.isParent(parent, child)) {
                    fail("Expect " + parentName + " to be parent of " + childName + ".");
                }
            }
        }
    }

    @Test
    public void testNeedsSynchronization() {
        for (CheckType parent : new CheckType[]{CheckType.CHAT, CheckType.NET}) {
            for (CheckType type : CheckType.values()) {
                if ((parent == type || APIUtils.isParent(parent, type)) && !APIUtils.needsSynchronization(type)) {
                    fail("Expect " + type + " to need synchronization, as it is child of " + parent);
                }
            }
        }
    }

}
