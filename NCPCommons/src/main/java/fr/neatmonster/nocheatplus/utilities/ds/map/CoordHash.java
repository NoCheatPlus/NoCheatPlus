package fr.neatmonster.nocheatplus.utilities.ds.map;

public class CoordHash {

    private static final int p1 = 73856093;
    private static final int p2 = 19349663;
    private static final int p3 = 83492791;

    /**
     * Standard int-based hash code for a 3D-space, using multiplication with
     * primes and XOR results.
     * 
     * @param x
     * @param y
     * @param z
     * @return
     */
    // TODO: Link paper, or find a better one :p.
    public static final int hashCode3DPrimes(final int x, final int y, final int z) {
        return p1 * x ^ p2 * y ^ p3 * z;
    }

}
