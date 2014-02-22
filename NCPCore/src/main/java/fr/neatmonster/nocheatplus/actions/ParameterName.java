package fr.neatmonster.nocheatplus.actions;

/**
 * Some wildcards that are used in commands and log messages.
 */
public enum ParameterName {
    BLOCK_ID("blockid"),
    CHECK("check"),
    TAGS("tags"),
    DISTANCE("distance"),
    FALL_DISTANCE("falldistance"),
    FOOD("food"),
    IP("ip"),
    LIMIT("limit"),
    LOCATION_FROM("locationfrom"),
    LOCATION_TO("locationto"),
    PACKETS("packets"),
    PLAYER("player"),
    REACH_DISTANCE("reachdistance"),
    VIOLATIONS("violations"),
    WORLD("world");

    /**
     * Gets the parameter associated to the text.
     * 
     * @param text
     *            the text
     * @return the parameter name
     */
    public static final ParameterName get(final String text) {
        for (final ParameterName parameterName : ParameterName.values())
            if (parameterName.text.equals(text))
                return parameterName;

        return null;
    }

    /** The text. */
    private final String text;

    /**
     * Instantiates a new parameter name.
     * 
     * @param text
     *            the text
     */
    private ParameterName(final String text) {
        this.text = text;
    }
}
