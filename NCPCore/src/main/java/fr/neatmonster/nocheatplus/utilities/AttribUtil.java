package fr.neatmonster.nocheatplus.utilities;

import java.util.UUID;

public class AttribUtil {
    public static final UUID ID_SPRINT_BOOST = IdUtil.UUIDFromString("662A6B8D-DA3E-4C1C-8813-96EA6097278D");

    /**
     * Get a multiplier for an AttributeModifier.
     * @param operator Exclusively allows operator 2. Otherwise will throw an IllegalArgumentException.
     * @param value
     * @throws IllegalArgumentException if the modifier is not 2.
     * @return
     */
    public static double getMultiplier(int operator, double value) {
        switch(operator) {
            case 2:
                return 1.0 + value;
            default:
                throw new IllegalArgumentException("Unsupported operator: " + operator);
        }
    }
}
