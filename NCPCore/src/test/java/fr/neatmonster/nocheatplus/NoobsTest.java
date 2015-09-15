package fr.neatmonster.nocheatplus;

import static org.junit.Assert.fail;

import org.junit.Test;

public class NoobsTest {

    @Test
    public void testSmallDoubles() {
        double x;
        x = Double.MIN_VALUE;
        if (x <= 0.0 || !(x > 0.0)) {
            fail("noob");
        }
        x = -Double.MIN_VALUE;
        if (x >= 0.0 || !(x < 0.0)) {
            fail("noob");
        }
    }

}
