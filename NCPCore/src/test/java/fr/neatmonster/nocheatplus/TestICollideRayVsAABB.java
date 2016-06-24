package fr.neatmonster.nocheatplus;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.neatmonster.nocheatplus.checks.moving.location.LocUtil;
import fr.neatmonster.nocheatplus.utilities.collision.CollideRayVsAABB;
import fr.neatmonster.nocheatplus.utilities.collision.ICollideRayVsAABB;

public class TestICollideRayVsAABB {

    @Test
    public void testBlocks() {

        ICollideRayVsAABB boulder = new CollideRayVsAABB();
        boulder.setFindNearestPointIfNotCollide(true); // Prefer to have the option.

        // Simple x.
        if (!boulder.setRay(0.5, 1.75, 0.5, 1.0, 0.0, 0.0)
                .setAABB(3, 1, 0, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
        if (!boulder.setRay(0.5, 1.75, 0.5, -1.0, 0.0, 0.0)
                .setAABB(-3, 1, 0, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
        // Simple y.
        if (!boulder.setRay(0.5, 1.75, 0.5, 0.0, 1.0, 0.0)
                .setAABB(0, 3, 0, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
        if (!boulder.setRay(0.5, 1.75, 0.5, 0.0, -1.0, 0.0)
                .setAABB(0, -3, 0, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
        // Simple z.
        if (!boulder.setRay(0.5, 1.75, 0.5, 0.0, 0.0, 1.0)
                .setAABB(0, 1, 3, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
        if (!boulder.setRay(0.5, 1.75, 0.5, 0.0, 0.0, -1.0)
                .setAABB(0, 1, -3, 0.0)
                .loop()
                .collides()) {
            doFail(boulder);
        }
    }

    private void doFail(ICollideRayVsAABB boulder) {
        fail("Failed: collides: " + boulder.collides() + " , distSq: " + boulder.getClosestDistanceSquared() + " , pos: " + LocUtil.simpleFormat(boulder));
    }

}
